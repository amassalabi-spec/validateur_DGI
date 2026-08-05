package com.audit.dgi.validateur_dgi.service.parser;

import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.domain.TemplateStyle;
import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class SpringAiParsingService implements InvoiceParsingService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiParsingService.class);

    /** Budget d'entrée (tokens): on n'envoie jamais plus de X caracteres de texte brut. */
    private static final int MAX_INPUT_CHARS = 8000;
    /** Fraction du budget reservee au debut du texte (le reste pour la fin, ou sont les totaux). */
    private static final double HEAD_RATIO = 0.7;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final FallbackParsingService fallbackParsingService;
    private final BeanOutputConverter<InvoiceData> outputConverter = new BeanOutputConverter<>(InvoiceData.class);

    public SpringAiParsingService(ObjectProvider<ChatModel> chatModelProvider,
                                  FallbackParsingService fallbackParsingService) {
        this.chatModelProvider = chatModelProvider;
        this.fallbackParsingService = fallbackParsingService;
    }

    @Override
    public InvoiceData parse(String rawText) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null || rawText == null || rawText.isBlank()) {
            log.debug("Spring AI indisponible, bascule sur le parser fallback");
            return fallbackParsingService.parse(rawText);
        }

        try {
            String promptText = buildPrompt(rawText);
            ChatResponse response = chatModel.call(new Prompt(new UserMessage(promptText)));
            String content = response.getResult().getOutput().getText();
            InvoiceData parsed = outputConverter.convert(sanitizeJson(content));
            if (parsed != null) {
                log.debug("Extraction Spring AI réussie");
                return mergeWithFallback(parsed, fallbackParsingService.parse(rawText));
            }
        } catch (Exception ex) {
            log.warn("Échec de l'extraction Spring AI ({}), bascule sur le parser fallback", ex.getMessage());
        }
        return fallbackParsingService.parse(rawText);
    }

    private String sanitizeJson(String content) {
        if (content == null) return "";
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private InvoiceData mergeWithFallback(InvoiceData ai, InvoiceData fallback) {
        if (fallback == null) return ai;
        if (ai.getInvoiceNumber() == null || ai.getInvoiceNumber().isBlank()) {
            ai.setInvoiceNumber(fallback.getInvoiceNumber());
        }
        if (ai.getIssueDate() == null) ai.setIssueDate(fallback.getIssueDate());
        if (ai.getPaymentMethod() == null) ai.setPaymentMethod(fallback.getPaymentMethod());
        if (ai.getStatus() == null) ai.setStatus(InvoiceStatus.PENDING_AUDIT);
        if (ai.getCompliant() == null) ai.setCompliant(fallback.getCompliant());
        if (ai.getChosenTemplate() == null) ai.setChosenTemplate(fallback.getChosenTemplate());
        if (ai.getStampDuty() == null) ai.setStampDuty(fallback.getStampDuty());

        if (ai.getIssuer() == null) {
            ai.setIssuer(fallback.getIssuer());
        } else if (fallback.getIssuer() != null) {
            IssuerDTO issuer = ai.getIssuer();
            if (isBlank(issuer.getName())) issuer.setName(fallback.getIssuer().getName());
            if (isBlank(issuer.getAddress())) issuer.setAddress(fallback.getIssuer().getAddress());
            if (isBlank(issuer.getIce())) issuer.setIce(fallback.getIssuer().getIce());
            if (isBlank(issuer.getIfNumber())) issuer.setIfNumber(fallback.getIssuer().getIfNumber());
            if (isBlank(issuer.getPatente())) issuer.setPatente(fallback.getIssuer().getPatente());
            if (isBlank(issuer.getRc())) issuer.setRc(fallback.getIssuer().getRc());
            if (isBlank(issuer.getCnss())) issuer.setCnss(fallback.getIssuer().getCnss());
        }

        if (ai.getClient() == null) {
            ai.setClient(fallback.getClient());
        } else if (fallback.getClient() != null) {
            ClientDTO client = ai.getClient();
            if (isBlank(client.getName())) client.setName(fallback.getClient().getName());
            if (isBlank(client.getAddress())) client.setAddress(fallback.getClient().getAddress());
            if (isBlank(client.getIce())) client.setIce(fallback.getClient().getIce());
        }

        if (ai.getItems() == null || ai.getItems().isEmpty()) {
            ai.setItems(fallback.getItems());
        }
        if (ai.getVatSummaries() == null || ai.getVatSummaries().isEmpty()) {
            ai.setVatSummaries(fallback.getVatSummaries());
        }
        return ai;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String buildPrompt(String rawText) {
        String input = truncateText(rawText);
        return """
                Tu es un moteur d'extraction fiscale DGI Maroc.
                Analyse le texte brut de facture et retourne UNIQUEMENT un objet JSON valide conforme au schema.
                Règles:
                - Inclus au minimum: invoiceNumber, issueDate, paymentMethod, issuer, client, totalHt, totalTva, stampDuty, totalTtc, items, vatSummaries.
                - issuer: { name, address, ice (15 chiffres), ifNumber (6 a 8 chiffres), patente, rc, cnss }.
                - client: { name, address, ice (15 chiffres) }.
                - items: { lineNumber, description, quantity, unitPriceHt, discountAmount, vatRate, totalLineHt, totalLineTva, totalLineTtc, cgiExemptionClause }.
                - vatSummaries: { vatRate, taxableBase, vatAmount }.
                - paymentMethod: une valeur exacte parmi: %s.
                - chosenTemplate: une valeur exacte parmi: %s.
                - status: %s.
                - Montants en nombres JSON (jamais de chaînes). issueDate au format ISO yyyy-MM-dd.
                - Donnee absente -> chaîne vide ou valeur nulle coherente. AUCUN texte hors JSON.

                Schema attendu:
                %s

                Texte brut:
                %s
                """.formatted(
                java.util.Arrays.stream(PaymentMethod.values()).map(Enum::name).toList(),
                java.util.Arrays.stream(TemplateStyle.values()).map(Enum::name).toList(),
                InvoiceStatus.PENDING_AUDIT,
                outputConverter.getFormat(),
                input);
    }

    private String truncateText(String rawText) {
        if (rawText == null || rawText.length() <= MAX_INPUT_CHARS) {
            return rawText;
        }
        int head = (int) (MAX_INPUT_CHARS * HEAD_RATIO);
        int tail = MAX_INPUT_CHARS - head;
        return rawText.substring(0, head) + "\n[...] (texte tronqué pour limiter les tokens) [...]\n"
                + rawText.substring(rawText.length() - tail);
    }
}
