package com.audit.dgi.validateur_dgi.dto;

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
public class VatSummaryDTO {

    @NotNull
    private BigDecimal vatRate;

    @NotNull
    private BigDecimal baseHt;

    @NotNull
    private BigDecimal vatAmount;
}

