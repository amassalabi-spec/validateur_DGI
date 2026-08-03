package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.repository.ComplianceAuditRepository;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.security.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Transactional(readOnly = true)
public class AnalyticsController {

    private final InvoiceRepository invoiceRepository;
    private final ComplianceAuditRepository auditRepository;

    public AnalyticsController(InvoiceRepository invoiceRepository, ComplianceAuditRepository auditRepository) {
        this.invoiceRepository = invoiceRepository;
        this.auditRepository = auditRepository;
    }

    @GetMapping("/company")
    public ResponseEntity<?> companyKpi() {
        Long companyId = TenantContext.getCurrentTenant();
        if (companyId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Aucun tenant détecté dans le token"));
        }

        List<Invoice> invoices = invoiceRepository.findAllByCompanyId(companyId);

        BigDecimal caHt = BigDecimal.ZERO;
        BigDecimal caTtc = BigDecimal.ZERO;
        BigDecimal stampDuty = BigDecimal.ZERO;
        Map<String, BigDecimal> tvaCollected = new LinkedHashMap<>();

        for (Invoice invoice : invoices) {
            caHt = caHt.add(safe(invoice.getTotalHt()));
            caTtc = caTtc.add(safe(invoice.getTotalTtc()));
            stampDuty = stampDuty.add(safe(invoice.getStampDuty()));
            for (var vat : invoice.getVatSummaries()) {
                if (vat.getVatRate() == null) continue;
                String rateKey = vat.getVatRate().stripTrailingZeros().toPlainString() + "%";
                tvaCollected.merge(rateKey, safe(vat.getVatAmount()), BigDecimal::add);
            }
        }

        long compliantCount = invoices.stream()
                .filter(i -> Boolean.TRUE.equals(i.getCompliant()) || i.getStatus() == InvoiceStatus.COMPLIANT)
                .count();
        long totalCount = invoices.size();
        double complianceRate = totalCount == 0 ? 0.0 : Math.round(compliantCount * 10000.0 / totalCount) / 100.0;

        Map<String, BigDecimal> tvaScaled = new LinkedHashMap<>();
        tvaCollected.forEach((k, v) -> tvaScaled.put(k, v.setScale(2, RoundingMode.HALF_UP)));

        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "tvaCollected", tvaScaled,
                "caHt", caHt.setScale(2, RoundingMode.HALF_UP),
                "caTtc", caTtc.setScale(2, RoundingMode.HALF_UP),
                "stampDuty", stampDuty.setScale(2, RoundingMode.HALF_UP),
                "invoiceCount", totalCount,
                "compliantCount", compliantCount,
                "nonCompliantCount", totalCount - compliantCount,
                "complianceRate", complianceRate
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminKpi() {
        long totalInvoices = invoiceRepository.count();
        long totalCompanies = invoiceRepository.findAll().stream()
                .map(Invoice::getCompanyId).distinct().count();
        Map<String, Long> topErrors = toErrorMap(auditRepository.findTopErrors(PageRequest.of(0, 5)));
        return ResponseEntity.ok(Map.of(
                "totalInvoices", totalInvoices,
                "totalCompanies", totalCompanies,
                "topErrors", topErrors
        ));
    }

    private Map<String, Long> toErrorMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (rows == null) return map;
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return map;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }
}
