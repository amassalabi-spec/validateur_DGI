package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DgiAuditEngine {

    private final List<DgiRule> rules;

    public DgiAuditEngine(List<DgiRule> rules) {
        this.rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public AuditReport executeAudit(InvoiceDTO invoice) {
        List<DgiRule.RuleResult> results = new ArrayList<>();
        boolean hasError = false;

        for (DgiRule rule : rules) {
            DgiRule.RuleResult result = rule.validate(invoice);
            results.add(result);
            if (!result.isValid() && result.severity() == AuditSeverity.ERROR) {
                hasError = true;
            }
        }

        boolean compliant = !hasError;
        if (invoice != null) {
            invoice.setCompliant(compliant);
            invoice.setStatus(compliant ? InvoiceStatus.COMPLIANT : InvoiceStatus.NON_COMPLIANT);
        }

        return new AuditReport(results, compliant);
    }
}

