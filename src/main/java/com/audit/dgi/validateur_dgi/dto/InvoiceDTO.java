package com.audit.dgi.validateur_dgi.dto;

import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.PaymentMethod;
import com.audit.dgi.validateur_dgi.domain.TemplateStyle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    @NotBlank
    private String originalFileName;

    @NotBlank
    private String fileType;

    @NotBlank
    private String invoiceNumber;

    @NotNull
    private LocalDate issueDate;

    private LocalDate dueDate;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    @Valid
    private IssuerDTO issuer;

    @NotNull
    @Valid
    private ClientDTO client;

    @NotNull
    private BigDecimal totalHt;

    @NotNull
    private BigDecimal totalTva;

    @NotNull
    private BigDecimal stampDuty;

    @NotNull
    private BigDecimal totalTtc;

    @NotNull
    private InvoiceStatus status;

    @NotNull
    private Boolean compliant;

    @NotNull
    private TemplateStyle chosenTemplate;

    @Valid
    @Builder.Default
    private List<InvoiceItemDTO> items = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<VatSummaryDTO> vatSummaries = new ArrayList<>();

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public IssuerDTO getIssuer() { return issuer; }
    public void setIssuer(IssuerDTO issuer) { this.issuer = issuer; }
    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }
    public BigDecimal getTotalHt() { return totalHt; }
    public void setTotalHt(BigDecimal totalHt) { this.totalHt = totalHt; }
    public BigDecimal getTotalTva() { return totalTva; }
    public void setTotalTva(BigDecimal totalTva) { this.totalTva = totalTva; }
    public BigDecimal getStampDuty() { return stampDuty; }
    public void setStampDuty(BigDecimal stampDuty) { this.stampDuty = stampDuty; }
    public BigDecimal getTotalTtc() { return totalTtc; }
    public void setTotalTtc(BigDecimal totalTtc) { this.totalTtc = totalTtc; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public Boolean getCompliant() { return compliant; }
    public void setCompliant(Boolean compliant) { this.compliant = compliant; }
    public TemplateStyle getChosenTemplate() { return chosenTemplate; }
    public void setChosenTemplate(TemplateStyle chosenTemplate) { this.chosenTemplate = chosenTemplate; }
    public List<InvoiceItemDTO> getItems() { return items; }
    public void setItems(List<InvoiceItemDTO> items) { this.items = items; }
    public List<VatSummaryDTO> getVatSummaries() { return vatSummaries; }
    public void setVatSummaries(List<VatSummaryDTO> vatSummaries) { this.vatSummaries = vatSummaries; }
}

