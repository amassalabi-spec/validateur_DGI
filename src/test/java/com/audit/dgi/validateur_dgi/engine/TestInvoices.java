package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.domain.TemplateStyle;
import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.dto.VatSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class TestInvoices {

    private TestInvoices() {
    }

    static InvoiceDTO compliant() {
        IssuerDTO issuer = IssuerDTO.builder()
                .name("Emetteur SARL")
                .address("12 Rue des Far, Casablanca")
                .ice("001234567890123")
                .ifNumber("1234567")
                .patente("12345678")
                .rc("123456")
                .cnss("7654321")
                .build();

        ClientDTO client = ClientDTO.builder()
                .name("Client SA")
                .address("Av Hassan II, Rabat")
                .ice("002345678901234")
                .build();

        InvoiceItemDTO item = InvoiceItemDTO.builder()
                .lineNumber(1)
                .description("Prestation de service")
                .quantity(new BigDecimal("1"))
                .unitPriceHt(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO.setScale(2))
                .vatRate(new BigDecimal("20.00"))
                .totalLineHt(new BigDecimal("100.00"))
                .totalLineTva(new BigDecimal("20.00"))
                .totalLineTtc(new BigDecimal("120.00"))
                .build();

        VatSummaryDTO vat = VatSummaryDTO.builder()
                .vatRate(new BigDecimal("20.00"))
                .baseHt(new BigDecimal("100.00"))
                .vatAmount(new BigDecimal("20.00"))
                .build();

        return InvoiceDTO.builder()
                .originalFileName("facture.pdf")
                .fileType("pdf")
                .invoiceNumber("FAC-2026-0001")
                .issueDate(LocalDate.of(2026, 1, 15))
                .paymentMethod(PaymentMethod.TRANSFER)
                .issuer(issuer)
                .client(client)
                .totalHt(new BigDecimal("100.00"))
                .totalTva(new BigDecimal("20.00"))
                .stampDuty(BigDecimal.ZERO.setScale(2))
                .totalTtc(new BigDecimal("120.00"))
                .status(null)
                .compliant(Boolean.FALSE)
                .chosenTemplate(TemplateStyle.MODERN)
                .items(List.of(item))
                .vatSummaries(List.of(vat))
                .build();
    }

    static InvoiceDTO empty() {
        return InvoiceDTO.builder().build();
    }
}
