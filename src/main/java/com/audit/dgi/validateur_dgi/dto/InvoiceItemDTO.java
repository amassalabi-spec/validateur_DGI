package com.audit.dgi.validateur_dgi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDTO {

    @NotNull
    private Integer lineNumber;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal unitPriceHt;

    @NotNull
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    private BigDecimal vatRate;

    @NotNull
    private BigDecimal totalLineHt;

    @NotNull
    private BigDecimal totalLineTva;

    @NotNull
    private BigDecimal totalLineTtc;

    private String cgiExemptionClause;

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceHt() { return unitPriceHt; }
    public void setUnitPriceHt(BigDecimal unitPriceHt) { this.unitPriceHt = unitPriceHt; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
    public BigDecimal getTotalLineHt() { return totalLineHt; }
    public void setTotalLineHt(BigDecimal totalLineHt) { this.totalLineHt = totalLineHt; }
    public BigDecimal getTotalLineTva() { return totalLineTva; }
    public void setTotalLineTva(BigDecimal totalLineTva) { this.totalLineTva = totalLineTva; }
    public BigDecimal getTotalLineTtc() { return totalLineTtc; }
    public void setTotalLineTtc(BigDecimal totalLineTtc) { this.totalLineTtc = totalLineTtc; }
    public String getCgiExemptionClause() { return cgiExemptionClause; }
    public void setCgiExemptionClause(String cgiExemptionClause) { this.cgiExemptionClause = cgiExemptionClause; }
}
