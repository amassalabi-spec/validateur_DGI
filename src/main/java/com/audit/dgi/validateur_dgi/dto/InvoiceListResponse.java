package com.audit.dgi.validateur_dgi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceListResponse {
    private List<?> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

