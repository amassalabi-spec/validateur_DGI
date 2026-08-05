package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final CompanyRepository companyRepository;
    private final SessionUserService sessionUserService;

    public ProfileController(CompanyRepository companyRepository, SessionUserService sessionUserService) {
        this.companyRepository = companyRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public String profile(Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        model.addAttribute("company", company);
        return "profile";
    }

    @PostMapping
    public String save(@ModelAttribute("company") Company form, RedirectAttributes redirectAttributes) {
        Long companyId = sessionUserService.currentCompanyId();
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        company.setName(form.getName());
        company.setIce(form.getIce());
        company.setIfNumber(form.getIfNumber());
        company.setPatente(form.getPatente());
        company.setRc(form.getRc());
        company.setCnss(form.getCnss());
        company.setLogoUrl(form.getLogoUrl());
        company.setTvaRegime(form.getTvaRegime());
        companyRepository.save(company);
        redirectAttributes.addFlashAttribute("saved", true);
        return "redirect:/profile";
    }
}

