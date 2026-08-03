package com.audit.dgi.validateur_dgi.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientIceValidationRuleTest {

    private final ClientIceValidationRule rule = new ClientIceValidationRule();

    @Test
    void validClientIcePasses() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.compliant());
        assertTrue(result.isValid());
    }

    @Test
    void missingClientFails() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.empty());
        assertFalse(result.isValid());
    }

    @Test
    void invalidClientIceFails() {
        var invoice = TestInvoices.compliant();
        invoice.getClient().setIce("12345");
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }
}
