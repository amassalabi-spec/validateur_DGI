package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard du Super Admin de la plateforme : vue globale de tous les clients (entreprises).
 * Accessible uniquement avec l'autorité ROLE_SUPER_ADMIN (email déclaré dans app.super.admin.emails).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminDashboardController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    public AdminDashboardController(CompanyRepository companyRepository,
                                    UserRepository userRepository,
                                    InvoiceRepository invoiceRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public record CompanyOverview(Company company,
                                  long userCount,
                                  long invoiceCount,
                                  BigDecimal totalHt,
                                  BigDecimal totalTtc,
                                  long compliantCount,
                                  BigDecimal complianceRate) {
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String admin(Model model) {
        List<Company> companies = companyRepository.findAll();
        List<AppUser> users = userRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();

        Map<Long, Long> userCounts = users.stream()
                .collect(Collectors.groupingBy(AppUser::getCompanyId, Collectors.counting()));

        Map<Long, List<Invoice>> invoicesByCompany = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getCompanyId));

        List<CompanyOverview> rows = companies.stream()
                .map(c -> buildOverview(c, userCounts, invoicesByCompany.getOrDefault(c.getId(), List.of())))
                .sorted(Comparator.comparing(o -> o.company().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        BigDecimal totalCaTtc = invoices.stream()
                .map(Invoice::getTotalTtc)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCaHt = invoices.stream()
                .map(Invoice::getTotalHt)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        long totalInvoices = invoices.size();
        long compliantInvoices = invoices.stream().filter(AdminDashboardController::isCompliant).count();
        BigDecimal globalRate = totalInvoices == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(compliantInvoices * 100.0 / totalInvoices).setScale(1, RoundingMode.HALF_UP);

        model.addAttribute("rows", rows);
        model.addAttribute("totalCompanies", companies.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("totalCaHt", totalCaHt);
        model.addAttribute("totalCaTtc", totalCaTtc);
        model.addAttribute("globalComplianceRate", globalRate);
        model.addAttribute("active", "admin");
        return "admin";
    }

    private CompanyOverview buildOverview(Company company, Map<Long, Long> userCounts, List<Invoice> invoices) {
        long invoiceCount = invoices.size();
        BigDecimal totalHt = sum(invoices, Invoice::getTotalHt);
        BigDecimal totalTtc = sum(invoices, Invoice::getTotalTtc);
        long compliant = invoices.stream().filter(AdminDashboardController::isCompliant).count();
        BigDecimal rate = invoiceCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(compliant * 100.0 / invoiceCount).setScale(1, RoundingMode.HALF_UP);
        return new CompanyOverview(
                company,
                userCounts.getOrDefault(company.getId(), 0L),
                invoiceCount,
                totalHt,
                totalTtc,
                compliant,
                rate);
    }

    private static BigDecimal sum(List<Invoice> invoices, java.util.function.Function<Invoice, BigDecimal> mapper) {
        return invoices.stream()
                .map(mapper)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isCompliant(Invoice invoice) {
        return Boolean.TRUE.equals(invoice.getCompliant()) || invoice.getStatus() == InvoiceStatus.COMPLIANT;
    }
}
