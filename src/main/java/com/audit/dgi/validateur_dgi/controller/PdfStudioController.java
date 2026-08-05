package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/studio")
public class PdfStudioController {

    public static final List<Map<String, String>> ACCENTS = List.of(
            Map.of("nom", "Indigo", "hex", "#4f46e5"),
            Map.of("nom", "Ardoise", "hex", "#0f172a"),
            Map.of("nom", "Émeraude", "hex", "#059669"),
            Map.of("nom", "Terracotta", "hex", "#b45309"));

    private final CompanyRepository companyRepository;
    private final SessionUserService sessionUserService;

    public PdfStudioController(CompanyRepository companyRepository, SessionUserService sessionUserService) {
        this.companyRepository = companyRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public String studio(Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        model.addAttribute("company", company);
        model.addAttribute("accents", ACCENTS);
        model.addAttribute("accent", company.getTemplateAccent() != null ? company.getTemplateAccent() : "#4f46e5");
        model.addAttribute("langue", company.getTemplateLanguage() != null ? company.getTemplateLanguage() : "fr");
        model.addAttribute("mentions", company.getTemplateMentions() != null
                ? company.getTemplateMentions()
                : "Facture émise conformément à l'article 145 du Code Général des Impôts. Pénalité de retard : 3 % par mois entamé.");
        model.addAttribute("afficherTimbre", company.getTemplateShowStampDuty() == null || company.getTemplateShowStampDuty());
        return "studio";
    }

    @PostMapping
    public String save(@RequestParam String raisonSociale,
                       @RequestParam(defaultValue = "#4f46e5") String accent,
                       @RequestParam(defaultValue = "fr") String langue,
                       @RequestParam(required = false) String mentions,
                       @RequestParam(defaultValue = "false") String afficherTimbre,
                       RedirectAttributes redirectAttributes) {
        Long companyId = sessionUserService.currentCompanyId();
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        company.setName(raisonSociale);
        company.setTemplateAccent(accent);
        company.setTemplateLanguage(langue);
        company.setTemplateMentions(mentions);
        company.setTemplateShowStampDuty("true".equalsIgnoreCase(afficherTimbre));
        companyRepository.save(company);
        redirectAttributes.addFlashAttribute("saved", true);
        return "redirect:/studio";
    }
}
