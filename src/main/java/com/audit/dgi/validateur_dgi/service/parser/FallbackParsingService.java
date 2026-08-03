package com.audit.dgi.validateur_dgi.service.parser;

import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import com.audit.dgi.validateur_dgi.dto.VatSummaryDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic fallback parser using regular expressions.
 * Useful when an AI model is not available; aims to extract common fields.
 */
@Service
public class FallbackParsingService implements InvoiceParsingService {

    private static final Pattern ICE_PATTERN = Pattern.compile("\\b(\\d{15})\\b");
    private static final Pattern IF_PATTERN = Pattern.compile("\\b(\\d{6,8})\\b");
    private static final Pattern TOTAL_HT_PATTERN = Pattern.compile("(?i)total\\s*ht[:]?\\s*([0-9.,]+)");
    private static final Pattern TOTAL_TVA_PATTERN = Pattern.compile("(?i)total\\s*tva[:]?\\s*([0-9.,]+)");
    private static final Pattern TOTAL_TTC_PATTERN = Pattern.compile("(?i)total\\s*ttc[:]?\\s*([0-9.,]+)");

    @Override
    public InvoiceData parse(String rawText) {
        if (rawText == null) return null;
        InvoiceData invoice = new InvoiceData();
        invoice.setOriginalFileName("extracted");
        invoice.setFileType("parsed_text");
        invoice.setInvoiceNumber(extractInvoiceNumber(rawText));
        invoice.setIssueDate(LocalDate.now());
        // issuer
        IssuerDTO issuer = new IssuerDTO();
        issuer.setName(extractFirstLine(rawText));
        issuer.setAddress(extractAddress(rawText));
        issuer.setIce(extractFirstMatch(ICE_PATTERN, rawText));
        issuer.setIfNumber(extractFirstMatch(IF_PATTERN, rawText));
        invoice.setIssuer(issuer);
        // client
        ClientDTO client = new ClientDTO();
        client.setName("Client");
        client.setAddress("");
        client.setIce(extractClientIce(rawText));
        invoice.setClient(client);
        // monetary
        invoice.setTotalHt(extractMoney(TOTAL_HT_PATTERN, rawText));
        invoice.setTotalTva(extractMoney(TOTAL_TVA_PATTERN, rawText));
        invoice.setTotalTtc(extractMoney(TOTAL_TTC_PATTERN, rawText));
        invoice.setStampDuty(BigDecimal.ZERO);
        invoice.setStatus(null);
        invoice.setCompliant(false);
        invoice.setChosenTemplate(null);
        // items: very simple split by lines containing number + currency
        invoice.setItems(parseItems(rawText));
        // vat summary empty
        invoice.setVatSummaries(new ArrayList<>());
        return invoice;
    }

    private String extractInvoiceNumber(String raw) {
        // naive: look for 'Facture N' or 'Invoice No'
        Pattern p = Pattern.compile("(?i)(?:facture|invoice)[^0-9A-Za-z\\n\\r]{0,10}([A-Za-z0-9-/.]+)");
        Matcher m = p.matcher(raw);
        if (m.find()) return m.group(1);
        return "";
    }

    private String extractFirstLine(String raw) {
        String[] lines = raw.split("\\r?\\n");
        for (String l : lines) {
            if (!l.isBlank()) return l.trim();
        }
        return "";
    }

    private String extractAddress(String raw) {
        // take first block after first line
        String[] parts = raw.split("\\r?\\n\\r?\\n");
        if (parts.length > 1) return parts[1].trim();
        return "";
    }

    private String extractFirstMatch(Pattern p, String raw) {
        Matcher m = p.matcher(raw);
        if (m.find()) return m.group(1);
        return "";
    }

    private String extractClientIce(String raw) {
        // take second occurrence of ICE if present
        Matcher m = ICE_PATTERN.matcher(raw);
        if (m.find()) {
            if (m.find()) return m.group(1);
            return "";
        }
        return "";
    }

    private BigDecimal extractMoney(Pattern p, String raw) {
        Matcher m = p.matcher(raw);
        if (m.find()) {
            String s = m.group(1).replaceAll("[, ]", "");
            try {
                return new BigDecimal(s).setScale(2, java.math.RoundingMode.HALF_UP);
            } catch (Exception ex) {
                return BigDecimal.ZERO.setScale(2);
            }
        }
        return BigDecimal.ZERO.setScale(2);
    }

    private List<InvoiceItemDTO> parseItems(String raw) {
        List<InvoiceItemDTO> items = new ArrayList<>();
        String[] lines = raw.split("\\r?\\n");
        int line = 1;
        for (String l : lines) {
            if (l.matches(".*\\d+[,\\.]?\\d*\\s*$")) {
                InvoiceItemDTO it = InvoiceItemDTO.builder()
                        .lineNumber(line++)
                        .description(l.trim())
                        .quantity(BigDecimal.ONE)
                        .unitPriceHt(BigDecimal.ZERO.setScale(2))
                        .discountAmount(BigDecimal.ZERO.setScale(2))
                        .vatRate(BigDecimal.ZERO.setScale(2))
                        .totalLineHt(BigDecimal.ZERO.setScale(2))
                        .totalLineTva(BigDecimal.ZERO.setScale(2))
                        .totalLineTtc(BigDecimal.ZERO.setScale(2))
                        .build();
                items.add(it);
            }
        }
        return items;
    }
}

