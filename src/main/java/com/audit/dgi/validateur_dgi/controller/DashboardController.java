package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final InvoiceRepository invoiceRepository;
    private final SessionUserService sessionUserService;

    public DashboardController(InvoiceRepository invoiceRepository, SessionUserService sessionUserService) {
        this.invoiceRepository = invoiceRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        List<Invoice> invoices = invoiceRepository.findAllByCompanyId(companyId);

        BigDecimal totalHt = invoices.stream().map(Invoice::getTotalHt).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTtc = invoices.stream().map(Invoice::getTotalTtc).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal stampDuty = invoices.stream().map(Invoice::getStampDuty).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat20 = invoices.stream()
                .flatMap(invoice -> invoice.getVatSummaries().stream())
                .filter(v -> v.getVatRate() != null && v.getVatRate().compareTo(new BigDecimal("20")) == 0)
                .map(v -> v.getVatAmount() == null ? BigDecimal.ZERO : v.getVatAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat10 = invoices.stream()
                .flatMap(invoice -> invoice.getVatSummaries().stream())
                .filter(v -> v.getVatRate() != null && v.getVatRate().compareTo(new BigDecimal("10")) == 0)
                .map(v -> v.getVatAmount() == null ? BigDecimal.ZERO : v.getVatAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long compliantCount = invoices.stream().filter(i -> Boolean.TRUE.equals(i.getCompliant()) || i.getStatus() == InvoiceStatus.COMPLIANT).count();
        long nonCompliantCount = invoices.stream().filter(i -> Boolean.FALSE.equals(i.getCompliant()) || i.getStatus() == InvoiceStatus.NON_COMPLIANT).count();
        long totalCount = invoices.size();
        BigDecimal complianceRate = totalCount == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(compliantCount * 100.0 / totalCount).setScale(1, RoundingMode.HALF_UP);

        model.addAttribute("totalHt", totalHt.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("totalTtc", totalTtc.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("vat20", vat20.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("vat10", vat10.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("stampDuty", stampDuty.setScale(2, RoundingMode.HALF_UP));
        model.addAttribute("complianceRate", complianceRate);
        model.addAttribute("invoiceCount", totalCount);
        model.addAttribute("compliantCount", compliantCount);
        model.addAttribute("nonCompliantCount", nonCompliantCount);
        model.addAttribute("recentInvoices", invoices.stream().sorted(Comparator.comparing(Invoice::getCreatedAt).reversed()).limit(8).toList());
        return "dashboard";
    }
}

