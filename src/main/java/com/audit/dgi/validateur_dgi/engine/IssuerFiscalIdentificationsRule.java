package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

@Component
public class IssuerFiscalIdentificationsRule implements DgiRule {

    private static final String RULE_CODE = "DGI-ISSUER-001";

    @Override
    public RuleResult validate(InvoiceDTO invoice) {
        if (invoice == null || invoice.getIssuer() == null) {
            return new RuleResult(RULE_CODE, "issuer", false, AuditSeverity.ERROR,
                    "L'émetteur est obligatoire.", null, "Renseigné");
        }

        var issuer = invoice.getIssuer();
        if (isBlank(issuer.getAddress())) {
            return new RuleResult(RULE_CODE, "issuer.address", false, AuditSeverity.ERROR,
                    "L'adresse de l'émetteur est obligatoire.", null, "Adresse renseignée");
        }
        if (isBlank(issuer.getIce()) || !issuer.getIce().matches("^\\d{15}$")) {
            return new RuleResult(RULE_CODE, "issuer.ice", false, AuditSeverity.ERROR,
                    "L'ICE de l'émetteur doit contenir exactement 15 chiffres.", issuer.getIce(), "^\\d{15}$");
        }
        if (isBlank(issuer.getIfNumber()) || !issuer.getIfNumber().matches("^\\d{6,8}$")) {
            return new RuleResult(RULE_CODE, "issuer.ifNumber", false, AuditSeverity.ERROR,
                    "L'IF de l'émetteur doit contenir entre 6 et 8 chiffres.", issuer.getIfNumber(), "^\\d{6,8}$");
        }
        if (isBlank(issuer.getRc())) {
            return new RuleResult(RULE_CODE, "issuer.rc", false, AuditSeverity.ERROR,
                    "Le RC de l'émetteur est obligatoire.", null, "RC renseigné");
        }
        if (isBlank(issuer.getPatente())) {
            return new RuleResult(RULE_CODE, "issuer.patente", false, AuditSeverity.ERROR,
                    "La patente de l'émetteur est obligatoire.", null, "Patente renseignée");
        }

        return new RuleResult(RULE_CODE, "issuer", true, AuditSeverity.INFO,
                "Les identifiants fiscaux de l'émetteur sont conformes.", "OK", "OK");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

