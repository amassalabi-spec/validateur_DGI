package com.audit.dgi.validateur_dgi.engine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VatExemptionClauseRuleTest {

    private final VatExemptionClauseRule rule = new VatExemptionClauseRule();

    @Test
    void taxedItemPasses() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.compliant());
        assertTrue(result.isValid());
    }

    @Test
    void exemptItemWithoutClauseFails() {
        var invoice = TestInvoices.compliant();
        invoice.getItems().get(0).setVatRate(BigDecimal.ZERO);
        invoice.getItems().get(0).setCgiExemptionClause(null);
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }

    @Test
    void exemptItemWithClausePasses() {
        var invoice = TestInvoices.compliant();
        invoice.getItems().get(0).setVatRate(BigDecimal.ZERO);
        invoice.getItems().get(0).setCgiExemptionClause("Article 92 du CGI");
        DgiRule.RuleResult result = rule.validate(invoice);
        assertTrue(result.isValid());
    }
}
