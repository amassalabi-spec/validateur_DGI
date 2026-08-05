package com.audit.dgi.validateur_dgi.service.generator;

import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.TemplateStyle;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    private final SpringTemplateEngine templateEngine;

    public PdfGeneratorService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateInvoicePdf(Invoice invoice, TemplateStyle style) throws Exception {
        return generateInvoicePdf(invoice, style, null);
    }

    public byte[] generateInvoicePdf(Invoice invoice, TemplateStyle style, Company company) throws Exception {
        Context ctx = new Context();
        ctx.setVariable("invoice", invoice);
        ctx.setVariable("style", style == null ? TemplateStyle.MODERN : style);
        ctx.setVariable("settings", company);
        ctx.setVariable("accent", company != null && company.getTemplateAccent() != null ? company.getTemplateAccent() : "#4f46e5");
        ctx.setVariable("mentions", company != null && company.getTemplateMentions() != null
                ? company.getTemplateMentions()
                : "Facture émise conformément à l'article 145 du Code Général des Impôts.");
        ctx.setVariable("showStampDuty", company == null || company.getTemplateShowStampDuty() == null || company.getTemplateShowStampDuty());
        ctx.setVariable("language", company != null && company.getTemplateLanguage() != null ? company.getTemplateLanguage() : "fr");

        String html = templateEngine.process("pdf/modern", ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }
}

