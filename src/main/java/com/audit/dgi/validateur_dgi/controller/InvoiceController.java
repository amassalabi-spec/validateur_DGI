package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.dto.InvoiceListResponse;
import com.audit.dgi.validateur_dgi.engine.AuditReport;
import com.audit.dgi.validateur_dgi.service.InvoiceService;
import com.audit.dgi.validateur_dgi.service.generator.PdfGeneratorService;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGenerator;
    private final SessionUserService sessionUserService;

    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository, PdfGeneratorService pdfGenerator, SessionUserService sessionUserService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.pdfGenerator = pdfGenerator;
        this.sessionUserService = sessionUserService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {
        var result = invoiceService.upload(file, sessionUserService.currentCompanyId());
        return ResponseEntity.ok().body(result);
    }

    @GetMapping
    public ResponseEntity<InvoiceListResponse> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String status) {
        Long companyId = sessionUserService.currentCompanyId();
        PageRequest pr = PageRequest.of(page, size);
        Page<Invoice> pageResult;

        if (status == null) {
            pageResult = invoiceRepository.findByCompanyId(companyId, pr);
        } else {
            pageResult = invoiceRepository.findByCompanyIdAndStatus(companyId,
                    Enum.valueOf(com.audit.dgi.validateur_dgi.domain.InvoiceStatus.class, status), pr);
        }

        InvoiceListResponse response = InvoiceListResponse.builder()
                .content(pageResult.getContent())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) throws Exception {
        Invoice inv = invoiceRepository.findByIdAndCompanyId(id, sessionUserService.currentCompanyId()).orElseThrow();
        byte[] pdf = pdfGenerator.generateInvoicePdf(inv, inv.getChosenTemplate());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

