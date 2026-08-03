package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import org.springframework.stereotype.Component;

@Component
public class VatExemptionClauseRule implements DgiRule {

    private static final String RULE_CODE = "DGI-VAT-001";

    @Override
    public RuleResult validate(InvoiceDTO invoice) {
        if (invoice == null || invoice.getItems() == null || invoice.getItems().isEmpty()) {
            return new RuleResult(RULE_CODE, "items", true, AuditSeverity.INFO,
                    "Aucune ligne à contrôler pour l'exonération TVA.", "0", "0");
        }

        for (InvoiceItemDTO item : invoice.getItems()) {
            if (item.getVatRate() != null && item.getVatRate().compareTo(new java.math.BigDecimal("0.00")) == 0) {
                if (item.getCgiExemptionClause() == null || item.getCgiExemptionClause().isBlank()) {
                    return new RuleResult(RULE_CODE, "items[" + item.getLineNumber() + "].cgiExemptionClause", false, AuditSeverity.ERROR,
                            "La clause d'exonération CGI est obligatoire pour une ligne à TVA 0%.", null, "Ex: Article 92 du CGI");
                }
            }
        }

        return new RuleResult(RULE_CODE, "vatRate", true, AuditSeverity.INFO,
                "Les clauses d'exonération TVA sont conformes.", "OK", "OK");
    }
}

