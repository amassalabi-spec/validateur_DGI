package com.audit.dgi.validateur_dgi.service;

import com.audit.dgi.validateur_dgi.domain.*;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import com.audit.dgi.validateur_dgi.engine.AuditReport;
import com.audit.dgi.validateur_dgi.engine.DgiAuditEngine;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.audit.dgi.validateur_dgi.service.parser.PoiExtractionService;
import com.audit.dgi.validateur_dgi.service.parser.SpringAiParsingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final PoiExtractionService extractionService;
    private final SpringAiParsingService parsingService;
    private final DgiAuditEngine auditEngine;
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(PoiExtractionService extractionService, SpringAiParsingService parsingService, DgiAuditEngine auditEngine, InvoiceRepository invoiceRepository) {
        this.extractionService = extractionService;
        this.parsingService = parsingService;
        this.auditEngine = auditEngine;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceUploadResult upload(MultipartFile file, Long companyId) throws Exception {
        String raw = extractionService.extractText(file);
        InvoiceData dto = parsingService.parse(raw);
        AuditReport report = auditEngine.executeAudit(dto);

        Invoice entity = toEntity(dto);
        if (companyId == null) throw new IllegalStateException("No company in session");
        entity.setCompanyId(companyId);
        attachAudits(entity, report);
        // persist items and vat summaries
        entity = invoiceRepository.save(entity);

        return new InvoiceUploadResult(dto, report, entity.getId());
    }

    @Transactional
    public Invoice saveInvoice(InvoiceData dto, Long companyId) {
        return saveInvoice(dto, companyId, null);
    }

    @Transactional
    public Invoice saveInvoice(InvoiceData dto, Long companyId, AuditReport report) {
        if (companyId == null) throw new IllegalStateException("No company in session");
        Invoice entity = toEntity(dto);
        entity.setCompanyId(companyId);
        if (report != null) {
            entity.setStatus(report.hasErrors() ? InvoiceStatus.NON_COMPLIANT : InvoiceStatus.COMPLIANT);
            entity.setCompliant(!report.hasErrors());
        }
        attachAudits(entity, report);
        return invoiceRepository.save(entity);
    }

    private void attachAudits(Invoice invoice, AuditReport report) {
        if (report == null || report.getResults() == null || report.getResults().isEmpty()) {
            return;
        }
        List<ComplianceAudit> audits = report.getResults().stream()
                .map(result -> ComplianceAudit.builder()
                        .invoice(invoice)
                        .ruleCode(result.ruleCode())
                        .fieldName(result.fieldName())
                        .severity(result.severity())
                        .message(result.message())
                        .actualValue(result.actualValue())
                        .expectedValue(result.expectedValue())
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        invoice.setComplianceAudits(new ArrayList<>(audits));
    }

    private Invoice toEntity(InvoiceData dto) {
        Invoice inv = Invoice.builder()
                .originalFileName(dto.getOriginalFileName() == null ? "saisie-manuelle" : dto.getOriginalFileName())
                .fileType(dto.getFileType() == null ? "manual" : dto.getFileType())
                .invoiceNumber(dto.getInvoiceNumber())
                .issueDate(dto.getIssueDate() == null ? LocalDate.now() : dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .paymentMethod(dto.getPaymentMethod() == null ? PaymentMethod.CASH : dto.getPaymentMethod())
                .issuerName(blank(dto.getIssuer() == null ? null : dto.getIssuer().getName()))
                .issuerAddress(blank(dto.getIssuer() == null ? null : dto.getIssuer().getAddress()))
                .issuerIce(blank(dto.getIssuer() == null ? null : dto.getIssuer().getIce()))
                .issuerIf(blank(dto.getIssuer() == null ? null : dto.getIssuer().getIfNumber()))
                .issuerPatente(blank(dto.getIssuer() == null ? null : dto.getIssuer().getPatente()))
                .issuerRc(blank(dto.getIssuer() == null ? null : dto.getIssuer().getRc()))
                .issuerCnss(blank(dto.getIssuer() == null ? null : dto.getIssuer().getCnss()))
                .clientName(blank(dto.getClient() == null ? null : dto.getClient().getName()))
                .clientAddress(blank(dto.getClient() == null ? null : dto.getClient().getAddress()))
                .clientIce(blank(dto.getClient() == null ? null : dto.getClient().getIce()))
                .totalHt(safe(dto.getTotalHt()))
                .totalTva(safe(dto.getTotalTva()))
                .stampDuty(safe(dto.getStampDuty()))
                .totalTtc(safe(dto.getTotalTtc()))
                .status(dto.getStatus() == null ? InvoiceStatus.PENDING_AUDIT : dto.getStatus())
                .compliant(dto.getCompliant() == null ? Boolean.FALSE : dto.getCompliant())
                .chosenTemplate(dto.getChosenTemplate() == null ? TemplateStyle.MODERN : dto.getChosenTemplate())
                .build();

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            inv.setItems(new ArrayList<>());
        } else {
            inv.setItems(dto.getItems().stream().map(this::toEntityItem).filter(java.util.Objects::nonNull).collect(Collectors.toList()));
            inv.getItems().forEach(it -> it.setInvoice(inv));
        }
        if (dto.getVatSummaries() != null) {
            inv.setVatSummaries(dto.getVatSummaries().stream().map(v -> InvoiceVatSummary.builder()
                    .vatRate(v.getVatRate())
                    .baseHt(v.getBaseHt())
                    .vatAmount(v.getVatAmount())
                    .invoice(inv)
                    .build()).collect(Collectors.toList()));
        }
        return inv;
    }

    private InvoiceItem toEntityItem(InvoiceItemDTO dto) {
        if (dto == null) return null;
        return InvoiceItem.builder()
                .lineNumber(dto.getLineNumber() == null ? 1 : dto.getLineNumber())
                .description(blank(dto.getDescription()))
                .quantity(dto.getQuantity() == null ? BigDecimal.ONE : dto.getQuantity())
                .unitPriceHt(safe(dto.getUnitPriceHt()))
                .discountAmount(safe(dto.getDiscountAmount()))
                .vatRate(dto.getVatRate() == null ? BigDecimal.ZERO : dto.getVatRate())
                .totalLineHt(safe(dto.getTotalLineHt()))
                .totalLineTva(safe(dto.getTotalLineTva()))
                .totalLineTtc(safe(dto.getTotalLineTtc()))
                .cgiExemptionClause(dto.getCgiExemptionClause())
                .build();
    }

    private BigDecimal safe(BigDecimal b) {
        return b == null ? BigDecimal.ZERO.setScale(2) : b.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String blank(String s) {
        return s == null ? "" : s;
    }

    public record InvoiceUploadResult(InvoiceData invoiceDTO, AuditReport report, Long invoiceId) {}
}

