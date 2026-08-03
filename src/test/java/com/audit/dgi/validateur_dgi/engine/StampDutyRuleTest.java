package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StampDutyRuleTest {

    private final StampDutyRule rule = new StampDutyRule();

    @Test
    void transferPaymentNotSubjectToStampDuty() {
        DgiRule.RuleResult result = rule.validate(TestInvoices.compliant());
        assertTrue(result.isValid());
    }

    @Test
    void cashPaymentWithCorrectStampPasses() {
        var invoice = TestInvoices.compliant();
        invoice.setPaymentMethod(PaymentMethod.CASH);
        invoice.setStampDuty(new BigDecimal("1.00"));
        invoice.setTotalTtc(new BigDecimal("121.00"));
        DgiRule.RuleResult result = rule.validate(invoice);
        assertTrue(result.isValid());
    }

    @Test
    void cashPaymentWithWrongStampFails() {
        var invoice = TestInvoices.compliant();
        invoice.setPaymentMethod(PaymentMethod.CASH);
        invoice.setStampDuty(BigDecimal.ZERO);
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }

    @Test
    void nullTtcFails() {
        var invoice = TestInvoices.compliant();
        invoice.setTotalTtc(null);
        DgiRule.RuleResult result = rule.validate(invoice);
        assertFalse(result.isValid());
    }
}
