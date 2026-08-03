package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DgiAuditEngineTest {

    private final DgiAuditEngine engine = new DgiAuditEngine(List.of(
            new IssuerFiscalIdentificationsRule(),
            new ClientIceValidationRule(),
            new MathematicalCoherenceRule(),
            new StampDutyRule(),
            new VatExemptionClauseRule()
    ));

    @Test
    void compliantInvoiceIsMarkedCompliant() {
        var invoice = TestInvoices.compliant();
        AuditReport report = engine.executeAudit(invoice);
        assertTrue(report.isCompliant());
        assertFalse(report.hasErrors());
        assertEquals(InvoiceStatus.COMPLIANT, invoice.getStatus());
    }

    @Test
    void nonCompliantInvoiceIsMarkedNonCompliant() {
        var invoice = TestInvoices.compliant();
        invoice.getIssuer().setIce("123");
        AuditReport report = engine.executeAudit(invoice);
        assertFalse(report.isCompliant());
        assertTrue(report.hasErrors());
        assertEquals(InvoiceStatus.NON_COMPLIANT, invoice.getStatus());
    }

    @Test
    void allRulesAreExecuted() {
        AuditReport report = engine.executeAudit(TestInvoices.compliant());
        assertEquals(5, report.getResults().size());
    }
}
