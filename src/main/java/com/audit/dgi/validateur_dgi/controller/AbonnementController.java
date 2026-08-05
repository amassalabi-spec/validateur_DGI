package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/abonnement")
public class AbonnementController {

    private static final int MONTHLY_LIMIT = 500;
    private static final int SEAT_LIMIT = 10;

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final SessionUserService sessionUserService;

    public AbonnementController(InvoiceRepository invoiceRepository,
                                UserRepository userRepository,
                                SessionUserService sessionUserService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public String abonnement(Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        YearMonth current = YearMonth.now();
        long auditedThisMonth = invoiceRepository.findAllByCompanyId(companyId).stream()
                .filter(i -> i.getCreatedAt() != null
                        && YearMonth.from(i.getCreatedAt().toLocalDate()).equals(current))
                .count();
        int seatsUsed = userRepository.findByCompanyIdOrderByIdAsc(companyId).size();

        model.addAttribute("planLabel", "Cabinet");
        model.addAttribute("planPrice", "1 490 MAD");
        model.addAttribute("renewalDate", LocalDate.now().plusMonths(1).withDayOfMonth(1));
        model.addAttribute("auditedThisMonth", auditedThisMonth);
        model.addAttribute("monthlyLimit", MONTHLY_LIMIT);
        model.addAttribute("seatsUsed", seatsUsed);
        model.addAttribute("seatLimit", SEAT_LIMIT);
        int usagePct = (int) Math.min(100, auditedThisMonth * 100.0 / MONTHLY_LIMIT);
        model.addAttribute("usagePct", usagePct);
        model.addAttribute("lastInvoices", List.of(
                new String[]{"Août 2026", "1 490,00 MAD"},
                new String[]{"Juillet 2026", "1 490,00 MAD"},
                new String[]{"Juin 2026", "1 490,00 MAD"}));
        return "abonnement";
    }
}
