package com.audit.dgi.validateur_dgi.service.parser;

import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import com.audit.dgi.validateur_dgi.dto.VatSummaryDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic fallback parser using regular expressions.
 * Handles common Moroccan invoice formats ("TOTAL HORS TAXES (HT)", "TOTAL TVA (xx%)",
 * "TOTAL À PAYER (TTC)", "Raison Sociale", "ICE Client", "VENTILATION DE LA TVA", ...).
 */
@Service
public class FallbackParsingService implements InvoiceParsingService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private static final Pattern ICE_PATTERN = Pattern.compile("\\b(\\d{15})\\b");
    private static final Pattern TOTAL_HT_PATTERN = Pattern.compile(
            "(?i)total\\s*(?:ht|hors\\s*taxes?|h\\.t\\.|hors\\s*taxe)\\s*(?:\\([^)]*\\))?\\s*[:]?\\s*([0-9][0-9 .,]*)");
    private static final Pattern TOTAL_TVA_PATTERN = Pattern.compile(
            "(?i)(?:total\\s*tva|montant\\s*(?:total\\s*)?tva)[^0-9]{0,25}([0-9][0-9 .,]*)");
    private static final Pattern TOTAL_TTC_PATTERN = Pattern.compile(
            "(?i)total\\s*(?:ttc|a\\s*payer|net\\s*a\\s*payer)\\s*(?:\\([^)]*\\))?\\s*[:]?\\s*([0-9][0-9 .,]*)");
    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile(
            "(?i)(?:facture|invoice|factura)[^0-9A-Za-z]{0,15}([A-Z0-9][A-Za-z0-9\\-/. ]{2,40})");
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?i)(?:date\\s*(?:d['e]\\s*)?(?:emission|facture|de\\s*la\\s*facture)?|période\\s*d['e]\\s*échéance)"
                    + "[^0-9]{0,10}(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})");
    private static final Pattern DUE_DATE_PATTERN = Pattern.compile(
            "(?i)(?:échéance|echeance|période)[^0-9]{0,15}(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})");
    private static final Pattern VAT_BASE_PATTERN = Pattern.compile(
            "(?i)base\\s*(?:imposable\\s*)?tva\\s*(\\d{1,3}(?:[.,]\\d+)?%?)\\s*[:]?\\s*([0-9][0-9 .,]*)");
    private static final Pattern VAT_AMOUNT_PATTERN = Pattern.compile(
            "(?i)montant\\s*(?:total\\s*)?tva\\s*(\\d{1,3}(?:[.,]\\d+)?%?)\\s*[:]?\\s*([0-9][0-9 .,]*)");
    private static final Pattern PAYMENT_PATTERN = Pattern.compile(
            "(?i)mode\\s*de\\s*règlement\\s*[:]?\\s*([^\\n]+)");
    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "^\\s*(\\d+)[.)]?\\s+(.+?)\\s{2,}([0-9][0-9 .,]*)\\s{2,}(\\d{1,3}(?:[.,]\\d+)?%)\\s{2,}([0-9][0-9 .,]*)");
    private static final Pattern ISSUER_KEY_LINE_PATTERN = Pattern.compile(
            "(?i)ice\\s*[:]?\\s*[^|\\n]*");

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
    };

    @Override
    public InvoiceData parse(String rawText) {
        if (rawText == null) return null;
        InvoiceData invoice = new InvoiceData();
        invoice.setOriginalFileName("extracted");
        invoice.setFileType("parsed_text");
        invoice.setInvoiceNumber(extractInvoiceNumber(rawText));
        invoice.setIssueDate(extractDate(DATE_PATTERN, rawText));
        invoice.setDueDate(extractDate(DUE_DATE_PATTERN, rawText));

        IssuerDTO issuer = extractIssuer(rawText);
        invoice.setIssuer(issuer);

        ClientDTO client = extractClient(rawText);
        invoice.setClient(client);

        invoice.setTotalHt(extractMoney(TOTAL_HT_PATTERN, rawText));
        invoice.setTotalTva(extractMoney(TOTAL_TVA_PATTERN, rawText));
        invoice.setTotalTtc(extractMoney(TOTAL_TTC_PATTERN, rawText));
        invoice.setStampDuty(ZERO);
        invoice.setPaymentMethod(extractPaymentMethod(rawText));
        invoice.setStatus(null);
        invoice.setCompliant(false);
        invoice.setChosenTemplate(null);

        List<InvoiceItemDTO> items = parseItems(rawText);
        invoice.setItems(items);
        if (items.isEmpty()) {
            invoice.setVatSummaries(new ArrayList<>());
        } else {
            invoice.setVatSummaries(parseVatSummaries(rawText));
        }

        // TTC dérivé si absent (HT + TVA)
        if (isZero(invoice.getTotalTtc()) && !isZero(invoice.getTotalHt())) {
            invoice.setTotalTtc(invoice.getTotalHt().add(invoice.getTotalTva()));
        }
        return invoice;
    }

    private String extractInvoiceNumber(String raw) {
        Matcher m = INVOICE_NUMBER_PATTERN.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    private LocalDate extractDate(Pattern p, String raw) {
        Matcher m = p.matcher(raw);
        if (m.find()) {
            LocalDate d = parseDate(m.group(1));
            if (d != null) return d;
        }
        return LocalDate.now();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        String s = value.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (Exception ignored) {
                // try next
            }
        }
        return null;
    }

    private IssuerDTO extractIssuer(String raw) {
        IssuerDTO issuer = new IssuerDTO();
        issuer.setName("");
        issuer.setAddress("");
        issuer.setIce("");
        issuer.setIfNumber("");
        issuer.setPatente("");
        issuer.setRc("");
        issuer.setCnss("");

        String[] lines = raw.split("\\r?\\n");
        StringBuilder block = new StringBuilder();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (isInvoiceHeader(t) || t.contains("FACTURE") || t.matches("={2,}") || t.matches("-{2,}")) {
                break;
            }
            if (block.length() > 0) block.append("\n");
            block.append(t);
        }
        String blockText = block.toString();
        if (blockText.isBlank()) blockText = raw;

        String[] blockLines = blockText.split("\\r?\\n");
        for (String line : blockLines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (issuer.getName().isBlank()) {
                issuer.setName(t);
                continue;
            }
            if (issuer.getAddress().isBlank() && looksLikeAddress(t)) {
                issuer.setAddress(t);
                continue;
            }
            if (t.contains("ICE")) {
                fillIssuerKeys(issuer, t);
            }
        }

        // fallback: recherche des clés dans tout le texte
        if (issuer.getIce().isBlank()) issuer.setIce(extractFirstMatch(ICE_PATTERN, raw));
        return issuer;
    }

    private void fillIssuerKeys(IssuerDTO issuer, String line) {
        Matcher m = Pattern.compile("ICE\\s*[:]?\\s*(\\d{15})").matcher(line);
        if (m.find()) issuer.setIce(m.group(1));
        m = Pattern.compile("\\bIF\\s*[:]?\\s*(\\d{6,8})").matcher(line);
        if (m.find()) issuer.setIfNumber(m.group(1));
        m = Pattern.compile("\\bRC\\s*[:]?\\s*([^|\\n]+)").matcher(line);
        if (m.find()) issuer.setRc(m.group(1).trim());
        m = Pattern.compile("\\bPatente\\s*[:]?\\s*([^|\\n]+)").matcher(line);
        if (m.find()) issuer.setPatente(m.group(1).trim());
        m = Pattern.compile("\\bCNSS\\s*[:]?\\s*([^|\\n]+)").matcher(line);
        if (m.find()) issuer.setCnss(m.group(1).trim());
    }

    private ClientDTO extractClient(String raw) {
        ClientDTO client = new ClientDTO();
        client.setName("");
        client.setAddress("");
        client.setIce("");

        Matcher m = Pattern.compile("(?i)raison\\s*sociale\\s*[:]?\\s*([^\\n]+)").matcher(raw);
        if (m.find()) client.setName(m.group(1).trim());
        m = Pattern.compile("(?i)^\\s*adresse\\s*[:]?\\s*([^\\n]+)").matcher(raw);
        if (m.find()) client.setAddress(m.group(1).trim());
        m = Pattern.compile("(?i)ice\\s*client\\s*[:]?\\s*(\\d{15})").matcher(raw);
        if (m.find()) client.setIce(m.group(1));
        if (client.getIce().isBlank()) {
            List<String> ices = new ArrayList<>();
            Matcher all = ICE_PATTERN.matcher(raw);
            while (all.find()) ices.add(all.group(1));
            if (ices.size() >= 2) client.setIce(ices.get(1));
        }
        if (client.getName().isBlank()) client.setName("Client");
        return client;
    }

    private PaymentMethod extractPaymentMethod(String raw) {
        Matcher m = PAYMENT_PATTERN.matcher(raw);
        String mode = m.find() ? m.group(1).toLowerCase(Locale.ROOT) : "";
        if (mode.contains("espèce") || mode.contains("cash") || mode.contains("comptant")) return PaymentMethod.CASH;
        if (mode.contains("virement") || mode.contains("transfert") || mode.contains("prélèvement")) return PaymentMethod.TRANSFER;
        if (mode.contains("chèque") || mode.contains("cheque")) return PaymentMethod.CHEQUE;
        if (mode.contains("effet") || mode.contains("traite") || mode.contains("lettre de change")) return PaymentMethod.BILL_OF_EXCHANGE;
        return PaymentMethod.OTHER;
    }

    private List<InvoiceItemDTO> parseItems(String raw) {
        List<InvoiceItemDTO> items = new ArrayList<>();
        String[] lines = raw.split("\\r?\\n");
        int lineNo = 1;
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty() || t.matches("[=\\-]{2,}")) continue;
            if (t.contains("Désignation") || t.contains("TOTAL") || t.contains("VENTILATION")
                    || t.contains("RECAPITULATIF") || t.contains("TVA") || t.contains("TTC")
                    || t.contains("Base Imposable") || t.contains("RÉFÉRENCE CLIENT")
                    || t.contains("Mode de Règlement")) {
                continue;
            }
            Matcher m = ITEM_PATTERN.matcher(t);
            if (m.find()) {
                BigDecimal base = parseMoney(m.group(3));
                BigDecimal rate = parseRate(m.group(4));
                BigDecimal tva = parseMoney(m.group(5));
                items.add(InvoiceItemDTO.builder()
                        .lineNumber(lineNo++)
                        .description(m.group(2).trim())
                        .quantity(BigDecimal.ONE)
                        .unitPriceHt(base)
                        .discountAmount(ZERO)
                        .vatRate(rate)
                        .totalLineHt(base)
                        .totalLineTva(tva)
                        .totalLineTtc(base.add(tva))
                        .build());
            }
        }
        return items;
    }

    private List<VatSummaryDTO> parseVatSummaries(String raw) {
        List<VatSummaryDTO> summaries = new ArrayList<>();
        Matcher base = VAT_BASE_PATTERN.matcher(raw);
        Matcher amount = VAT_AMOUNT_PATTERN.matcher(raw);
        BigDecimal baseValue = ZERO;
        BigDecimal amountValue = ZERO;
        BigDecimal rate = ZERO;
        if (base.find()) {
            rate = parseRate(base.group(1));
            baseValue = parseMoney(base.group(2));
        }
        if (amount.find()) {
            BigDecimal amountRate = parseRate(amount.group(1));
            if (rate.equals(ZERO)) rate = amountRate;
            amountValue = parseMoney(amount.group(2));
        }
        if (!baseValue.equals(ZERO) || !amountValue.equals(ZERO)) {
            summaries.add(VatSummaryDTO.builder()
                    .vatRate(rate)
                    .baseHt(baseValue)
                    .vatAmount(amountValue)
                    .build());
        }
        return summaries;
    }

    private String extractFirstMatch(Pattern p, String raw) {
        Matcher m = p.matcher(raw);
        if (m.find()) return m.group(1);
        return "";
    }

    private BigDecimal extractMoney(Pattern p, String raw) {
        Matcher m = p.matcher(raw);
        if (m.find()) return parseMoney(m.group(1));
        return ZERO;
    }

    private BigDecimal parseMoney(String raw) {
        if (raw == null || raw.isBlank()) return ZERO;
        String s = raw.replaceAll("[^0-9.,\\-]", "");
        if (s.isEmpty()) return ZERO;
        try {
            boolean hasComma = s.contains(",");
            boolean hasDot = s.contains(".");
            if (hasComma && !hasDot) {
                s = s.replace(",", ".");
            } else if (hasComma && hasDot) {
                if (s.lastIndexOf(',') > s.lastIndexOf('.')) {
                    s = s.replace(".", "").replace(",", ".");
                } else {
                    s = s.replace(",", "");
                }
            }
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return ZERO;
        }
    }

    private BigDecimal parseRate(String raw) {
        if (raw == null || raw.isBlank()) return ZERO;
        String s = raw.replaceAll("[^0-9.,]", "");
        if (s.isEmpty()) return ZERO;
        try {
            BigDecimal v = new BigDecimal(s.replace(",", "."));
            return v.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return ZERO;
        }
    }

    private boolean isInvoiceHeader(String line) {
        return line.matches("(?i)^(facture|factura|invoice|doctype)\\b.*");
    }

    private boolean looksLikeAddress(String line) {
        String l = line.toLowerCase(Locale.ROOT);
        return l.startsWith("siège") || l.startsWith("siege") || l.startsWith("adresse")
                || l.startsWith("adress") || l.contains("avenue") || l.contains("boulevard")
                || l.contains("bd ") || l.contains("rue ") || l.contains("hay ")
                || l.contains("km ") || l.contains("quartier") || l.contains("n°") || l.contains("n° ") || l.contains("no ");
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(ZERO) == 0;
    }
}
