package com.audit.dgi.validateur_dgi.service.parser;

import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.domain.TemplateStyle;
import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Extraction de facture via l'API REST Generative Language de Gemini (google.generative).
 * On appelle directement le endpoint REST avec la cle API ({@code x-goog-api-key}); le client
 * VertexAI de Spring AI est volontairement ignore (il exigerait des credentials GCP).
 * Sans cle API, on bascule en toute transparence sur {@link FallbackParsingService}.
 */
@Service
public class SpringAiParsingService implements InvoiceParsingService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiParsingService.class);

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /** Budget d'entree (tokens): on n'envoie jamais plus de X caracteres de texte brut. */
    private static final int MAX_INPUT_CHARS = 8000;
    /** Fraction du budget reservee au debut du texte (le reste pour la fin, ou sont les totaux). */
    private static final double HEAD_RATIO = 0.7;

    private final FallbackParsingService fallbackParsingService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key:}")
    private String apiKey;
    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String model;
    @Value("${app.gemini.temperature:0.1}")
    private double temperature;
    @Value("${app.gemini.max-output-tokens:2000}")
    private int maxOutputTokens;

    public SpringAiParsingService(RestClient.Builder builder,
                                  ObjectMapper objectMapper,
                                  FallbackParsingService fallbackParsingService) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
        this.fallbackParsingService = fallbackParsingService;
    }

    @Override
    public InvoiceData parse(String rawText) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("GEMINI_API_KEY absente, bascule sur le parser fallback");
            return fallbackParsingService.parse(rawText);
        }
        if (rawText == null || rawText.isBlank()) {
            return fallbackParsingService.parse(rawText);
        }
        try {
            String promptText = buildPrompt(rawText);
            String content = generateContent(promptText);
            InvoiceData parsed = objectMapper.readValue(sanitizeJson(content), InvoiceData.class);
            if (parsed != null) {
                log.debug("Extraction Gemini reussie");
                return mergeWithFallback(parsed, fallbackParsingService.parse(rawText));
            }
        } catch (Exception ex) {
            log.warn("Extraction Gemini echouee ({}), bascule sur le parser fallback", ex.getMessage());
        }
        return fallbackParsingService.parse(rawText);
    }

    private String generateContent(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> contents = Map.of("parts", List.of(part), "role", "user");
        Map<String, Object> generationConfig = Map.of(
                "temperature", temperature,
                "maxOutputTokens", maxOutputTokens,
                "responseMimeType", "application/json",
                "thinkingConfig", Map.of("thinkingBudget", 0));
        Map<String, Object> payload = Map.of("contents", List.of(contents), "generationConfig", generationConfig);

        JsonNode resp = restClient.post()
                .uri(GEMINI_URL, model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        if (resp == null) {
            throw new IllegalStateException("reponse Gemini vide");
        }
        JsonNode candidates = resp.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new IllegalStateException("aucun candidat dans la reponse Gemini");
        }
        JsonNode text = candidates.path(0).path("content").path("parts").path(0).path("text");
        String content = text.isMissingNode() ? null : text.asText();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("reponse Gemini sans texte");
        }
        return content;
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
                - vatSummaries: { vatRate, baseHt, vatAmount }.
                - paymentMethod: une valeur exacte parmi: %s.
                - chosenTemplate: une valeur exacte parmi: %s.
                - status: %s.
                - Montants en nombres JSON (jamais de chaînes). issueDate au format ISO yyyy-MM-dd.
                - Donnee absente -> chaîne vide ou valeur nulle coherente. AUCUN texte hors JSON.

                Texte brut:
                %s
                """.formatted(
                java.util.Arrays.stream(PaymentMethod.values()).map(Enum::name).toList(),
                java.util.Arrays.stream(TemplateStyle.values()).map(Enum::name).toList(),
                InvoiceStatus.PENDING_AUDIT,
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