package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.AppUser;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.UserRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final SessionUserService sessionUserService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public GlobalModelAttributes(SessionUserService sessionUserService,
                                 UserRepository userRepository,
                                 CompanyRepository companyRepository) {
        this.sessionUserService = sessionUserService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @ModelAttribute
    public void populateShell(Model model) {
        try {
            AppUser user = sessionUserService.currentUser();
            model.addAttribute("currentUserEmail", user.getEmail());
            model.addAttribute("currentUserRole", user.getRole());
            model.addAttribute("currentUserFullName", user.getFullName());
            model.addAttribute("currentUserIsSuperAdmin", Boolean.TRUE.equals(user.isSuperAdmin()));
            Company company = companyRepository.findById(user.getCompanyId()).orElse(null);
            model.addAttribute("companyName", company == null ? null : company.getName());
        } catch (RuntimeException ex) {
            model.addAttribute("currentUserEmail", null);
            model.addAttribute("currentUserRole", null);
            model.addAttribute("currentUserFullName", null);
            model.addAttribute("currentUserIsSuperAdmin", false);
            model.addAttribute("companyName", null);
        }
    }
}
