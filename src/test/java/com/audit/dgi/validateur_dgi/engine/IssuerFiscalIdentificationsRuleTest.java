package com.audit.dgi.validateur_dgi.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssuerFiscalIdentificationsRuleTest {

    private final IssuerFiscalIdentificationsRule rule = new IssuerFiscalIdentificationsRule();

    @Test
    void validIssuerPasses() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.compliant());
        assertTrue(result.isValid());
    }

    @Test
    void missingIssuerFails() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.empty());
        assertFalse(result.isValid());
    }

    @Test
    void invalidIceLengthFails() {
        var invoice = TestInvoices.compliant();
        invoice.getIssuer().setIce("12345678901234");
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }

    @Test
    void missingPatenteFails() {
        var invoice = TestInvoices.compliant();
        invoice.getIssuer().setPatente(null);
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }
}
