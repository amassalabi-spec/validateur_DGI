package com.audit.dgi.validateur_dgi.service.parser;

import com.audit.dgi.validateur_dgi.dto.InvoiceData;

public interface InvoiceParsingService {
    InvoiceData parse(String rawText);
}

