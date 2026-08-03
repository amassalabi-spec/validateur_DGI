package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class StampDutyRule implements DgiRule {

    private static final String RULE_CODE = "DGI-STAMP-001";
    private static final BigDecimal RATE = new BigDecimal("0.0025");
    private static final BigDecimal MINIMUM_LEGAL = new BigDecimal("1.00");

    @Override
    public RuleResult validate(InvoiceDTO invoice) {
        if (invoice == null || invoice.getTotalTtc() == null) {
            return new RuleResult(RULE_CODE, "stampDuty", false, AuditSeverity.ERROR,
                    "La facture ou le TTC est manquant.", null, "TTC renseigné");
        }

        if (invoice.getPaymentMethod() != PaymentMethod.CASH) {
            return new RuleResult(RULE_CODE, "paymentMethod", true, AuditSeverity.INFO,
                    "Le droit de timbre n'est pas applicable hors paiement en espèces.", invoice.getPaymentMethod() == null ? null : invoice.getPaymentMethod().name(), PaymentMethod.CASH.name());
        }

        BigDecimal expectedStamp = invoice.getTotalTtc().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
        if (expectedStamp.compareTo(MINIMUM_LEGAL) < 0) {
            expectedStamp = MINIMUM_LEGAL.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal actualStamp = invoice.getStampDuty() == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : invoice.getStampDuty().setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedFinalTtc = invoice.getTotalHt().add(invoice.getTotalTva()).add(expectedStamp).setScale(2, RoundingMode.HALF_UP);

        if (actualStamp.compareTo(expectedStamp) == 0) {
            return new RuleResult(RULE_CODE, "stampDuty", true, AuditSeverity.INFO,
                    "Le droit de timbre est conforme.", actualStamp.toPlainString(), expectedStamp.toPlainString());
        }

        return new RuleResult(RULE_CODE, "stampDuty", false, AuditSeverity.ERROR,
                "Le droit de timbre est incorrect pour un paiement en espèces.",
                actualStamp.toPlainString(), expectedStamp.toPlainString() + "; TTC final attendu=" + expectedFinalTtc.toPlainString());
    }
}

