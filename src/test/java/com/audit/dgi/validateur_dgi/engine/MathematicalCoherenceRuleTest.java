package com.audit.dgi.validateur_dgi.engine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathematicalCoherenceRuleTest {

    private final MathematicalCoherenceRule rule = new MathematicalCoherenceRule();

    @Test
    void coherentTotalsPass() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.compliant());
        assertTrue(result.isValid());
    }

    @Test
    void incoherentTtcFails() {
        var invoice = TestInvoices.compliant();
        invoice.setTotalTtc(new BigDecimal("130.00"));
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }

    @Test
    void nullInvoiceFails() {
        DgiRule.RuleResult result = rule.validate(null);
        assertFalse(result.isValid());
    }
}
