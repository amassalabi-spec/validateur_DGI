package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;

public interface DgiRule {

    RuleResult validate(InvoiceDTO invoice);

    record RuleResult(
            String ruleCode,
            String fieldName,
            boolean isValid,
            AuditSeverity severity,
            String message,
            String actualValue,
            String expectedValue
    ) {
    }
}

