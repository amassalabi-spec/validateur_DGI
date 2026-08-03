package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import com.audit.dgi.validateur_dgi.dto.VatSummaryDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MathematicalCoherenceRule implements DgiRule {

    private static final String RULE_CODE = "DGI-MATH-001";
    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");

    @Override
    public RuleResult validate(InvoiceDTO invoice) {
        if (invoice == null) {
            return new RuleResult(RULE_CODE, "invoice", false, AuditSeverity.ERROR,
                    "La facture est obligatoire.", null, "Renseignée");
        }

        BigDecimal sumLineHt = sumItems(invoice, ItemAmountSelector.HT);
        BigDecimal sumLineTva = sumItems(invoice, ItemAmountSelector.TVA);
        BigDecimal sumVatSummaryTva = sumVatSummaries(invoice);

        BigDecimal totalHt = scale2(invoice.getTotalHt());
        BigDecimal totalTva = scale2(invoice.getTotalTva());
        BigDecimal totalTtc = scale2(invoice.getTotalTtc());
        BigDecimal expectedTtc = totalHt.add(totalTva).setScale(2, RoundingMode.HALF_UP);

        boolean htValid = withinTolerance(sumLineHt, totalHt);
        boolean tvaValid = withinTolerance(sumLineTva, totalTva) && withinTolerance(sumVatSummaryTva, totalTva);
        boolean ttcValid = withinTolerance(expectedTtc, totalTtc);

        if (htValid && tvaValid && ttcValid) {
            return new RuleResult(RULE_CODE, "totals", true, AuditSeverity.INFO,
                    "Les totaux HT/TVA/TTC sont cohérents.", totalTtc.toPlainString(), expectedTtc.toPlainString());
        }

        return new RuleResult(RULE_CODE, "totals", false, AuditSeverity.ERROR,
                "Incohérence mathématique détectée sur les totaux de facture.",
                "HT=" + totalHt.toPlainString() + ", TVA=" + totalTva.toPlainString() + ", TTC=" + totalTtc.toPlainString(),
                "Sommes lignes HT=" + sumLineHt.toPlainString() + ", Sommes lignes TVA=" + sumLineTva.toPlainString() + ", TTC attendu=" + expectedTtc.toPlainString());
    }

    private BigDecimal sumItems(InvoiceDTO invoice, ItemAmountSelector selector) {
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return invoice.getItems().stream()
                .map(item -> selector == ItemAmountSelector.HT ? item.getTotalLineHt() : item.getTotalLineTva())
                .map(this::scale2)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumVatSummaries(InvoiceDTO invoice) {
        if (invoice.getVatSummaries() == null || invoice.getVatSummaries().isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return invoice.getVatSummaries().stream()
                .map(VatSummaryDTO::getVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean withinTolerance(BigDecimal expected, BigDecimal actual) {
        return expected.subtract(actual).abs().compareTo(TOLERANCE) <= 0;
    }

    private BigDecimal scale2(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }


    private enum ItemAmountSelector {
        HT, TVA
    }
}

