package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.repository.ComplianceAuditRepository;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final String[] MONTHS = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};

    private final InvoiceRepository invoiceRepository;
    private final ComplianceAuditRepository complianceAuditRepository;
    private final SessionUserService sessionUserService;

    public DashboardController(InvoiceRepository invoiceRepository,
                               ComplianceAuditRepository complianceAuditRepository,
                               SessionUserService sessionUserService) {
        this.invoiceRepository = invoiceRepository;
        this.complianceAuditRepository = complianceAuditRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String dashboard(@RequestParam(defaultValue = "2026") int exercice, Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        List<Invoice> invoices = invoiceRepository.findAllByCompanyId(companyId).stream()
                .filter(i -> i.getIssueDate() != null && i.getIssueDate().getYear() == exercice)
                .toList();

        BigDecimal totalHt = invoices.stream().map(Invoice::getTotalHt).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTtc = invoices.stream().map(Invoice::getTotalTtc).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal stampDuty = invoices.stream().map(Invoice::getStampDuty).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat20 = vatForRate(invoices, new BigDecimal("20"));
        BigDecimal vat10 = vatForRate(invoices, new BigDecimal("10"));
        BigDecimal vatOther = invoices.stream()
                .flatMap(invoice -> invoice.getVatSummaries().stream())
                .filter(v -> v.getVatRate() != null
                        && v.getVatRate().compareTo(new BigDecimal("20")) != 0
                        && v.getVatRate().compareTo(new BigDecimal("10")) != 0)
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

        model.addAttribute("exercice", exercice);
        model.addAttribute("years", List.of(2026, 2025, 2024));
        model.addAttribute("months", List.of(MONTHS));
        model.addAttribute("monthlyTva20", monthlyVat(invoices, "20"));
        model.addAttribute("monthlyTva10", monthlyVat(invoices, "10"));
        model.addAttribute("monthlyVatOther", monthlyVatOther(invoices));
        model.addAttribute("monthlyTimbre", monthlyTimbre(invoices));
        model.addAttribute("monthlyComplianceRate", monthlyCompliance(invoices));
        model.addAttribute("paymentBreakdown", paymentBreakdown(invoices));
        model.addAttribute("topErrors", topErrors(companyId));
        return "dashboard";
    }

    private BigDecimal vatForRate(List<Invoice> invoices, BigDecimal rate) {
        return invoices.stream()
                .flatMap(invoice -> invoice.getVatSummaries().stream())
                .filter(v -> v.getVatRate() != null && v.getVatRate().compareTo(rate) == 0)
                .map(v -> v.getVatAmount() == null ? BigDecimal.ZERO : v.getVatAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<BigDecimal> monthlyVat(List<Invoice> invoices, String rate) {
        List<BigDecimal> out = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int month = m;
            BigDecimal sum = invoices.stream()
                    .filter(i -> i.getIssueDate() != null && i.getIssueDate().getMonthValue() == month)
                    .flatMap(i -> i.getVatSummaries().stream())
                    .filter(v -> v.getVatRate() != null && v.getVatRate().stripTrailingZeros().equals(new BigDecimal(rate).stripTrailingZeros()))
                    .map(v -> v.getVatAmount() == null ? BigDecimal.ZERO : v.getVatAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            out.add(sum.setScale(0, RoundingMode.HALF_UP));
        }
        return out;
    }

    private List<BigDecimal> monthlyVatOther(List<Invoice> invoices) {
        List<BigDecimal> out = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int month = m;
            BigDecimal sum = invoices.stream()
                    .filter(i -> i.getIssueDate() != null && i.getIssueDate().getMonthValue() == month)
                    .flatMap(i -> i.getVatSummaries().stream())
                    .filter(v -> v.getVatRate() != null
                            && v.getVatRate().compareTo(new BigDecimal("20")) != 0
                            && v.getVatRate().compareTo(new BigDecimal("10")) != 0)
                    .map(v -> v.getVatAmount() == null ? BigDecimal.ZERO : v.getVatAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            out.add(sum.setScale(0, RoundingMode.HALF_UP));
        }
        return out;
    }

    private List<BigDecimal> monthlyTimbre(List<Invoice> invoices) {
        List<BigDecimal> out = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int month = m;
            BigDecimal sum = invoices.stream()
                    .filter(i -> i.getIssueDate() != null && i.getIssueDate().getMonthValue() == month)
                    .map(Invoice::getStampDuty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            out.add(sum.setScale(0, RoundingMode.HALF_UP));
        }
        return out;
    }

    private List<BigDecimal> monthlyCompliance(List<Invoice> invoices) {
        List<BigDecimal> out = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int month = m;
            long total = invoices.stream().filter(i -> i.getIssueDate() != null && i.getIssueDate().getMonthValue() == month).count();
            long ok = invoices.stream()
                    .filter(i -> i.getIssueDate() != null && i.getIssueDate().getMonthValue() == month)
                    .filter(i -> Boolean.TRUE.equals(i.getCompliant()) || i.getStatus() == InvoiceStatus.COMPLIANT)
                    .count();
            double rate = total == 0 ? 0 : ok * 100.0 / total;
            out.add(BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP));
        }
        return out;
    }

    private Map<String, BigDecimal> paymentBreakdown(List<Invoice> invoices) {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            BigDecimal sum = invoices.stream()
                    .filter(i -> i.getPaymentMethod() == method)
                    .map(Invoice::getTotalTtc)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            out.put(method.name(), sum.setScale(0, RoundingMode.HALF_UP));
        }
        return out;
    }

    private List<Map<String, Object>> topErrors(Long companyId) {
        List<Object[]> rows = complianceAuditRepository.findTopErrorsByCompany(companyId,
                org.springframework.data.domain.PageRequest.of(0, 5));
        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ruleCode", r[0]);
            m.put("count", r[1]);
            return m;
        }).toList();
    }
}
