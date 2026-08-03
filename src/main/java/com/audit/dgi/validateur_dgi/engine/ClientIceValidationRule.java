package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientIceValidationRule implements DgiRule {

    private static final String RULE_CODE = "DGI-CLIENT-001";

    @Override
    public RuleResult validate(InvoiceDTO invoice) {
        if (invoice == null || invoice.getClient() == null) {
            return new RuleResult(RULE_CODE, "client", false, AuditSeverity.ERROR,
                    "Le client est obligatoire en B2B.", null, "Renseigné");
        }

        String ice = invoice.getClient().getIce();
        if (ice == null || !ice.matches("^\\d{15}$")) {
            return new RuleResult(RULE_CODE, "client.ice", false, AuditSeverity.ERROR,
                    "L'ICE client est obligatoire et doit contenir exactement 15 chiffres.", ice, "^\\d{15}$");
        }

        return new RuleResult(RULE_CODE, "client.ice", true, AuditSeverity.INFO,
                "L'ICE client est conforme.", ice, "^\\d{15}$");
    }
}

