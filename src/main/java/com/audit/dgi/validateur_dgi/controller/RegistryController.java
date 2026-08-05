package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import com.audit.dgi.validateur_dgi.service.generator.PdfGeneratorService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/invoices")
public class RegistryController {

    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final SessionUserService sessionUserService;
    private final CompanyRepository companyRepository;

    public RegistryController(InvoiceRepository invoiceRepository,
                              PdfGeneratorService pdfGeneratorService,
                              SessionUserService sessionUserService,
                              CompanyRepository companyRepository) {
        this.invoiceRepository = invoiceRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.sessionUserService = sessionUserService;
        this.companyRepository = companyRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String registry(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String debut,
                           @RequestParam(required = false) String fin,
                           Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        Specification<Invoice> spec = buildSpec(companyId, q, status, debut, fin);
        Page<Invoice> invoices = invoiceRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate")));
        model.addAttribute("page", invoices);
        model.addAttribute("invoices", invoices.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoices.getTotalPages());
        model.addAttribute("totalElements", invoices.getTotalElements());
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        return "registry";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {
        Long companyId = sessionUserService.currentCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        model.addAttribute("invoice", invoice);
        return "invoice-detail";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) throws Exception {
        Long companyId = sessionUserService.currentCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        Company company = companyRepository.findById(companyId).orElse(null);
        byte[] pdf = pdfGeneratorService.generateInvoicePdf(invoice, invoice.getChosenTemplate(), company);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String debut,
                                         @RequestParam(required = false) String fin) {
        Long companyId = sessionUserService.currentCompanyId();
        Specification<Invoice> spec = buildSpec(companyId, q, status, debut, fin);
        List<Invoice> invoices = invoiceRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "issueDate"));
        String csv = toCsv(invoices);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registre-dgi.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long companyId = sessionUserService.currentCompanyId();
        invoiceRepository.findByIdAndCompanyId(id, companyId).ifPresent(invoiceRepository::delete);
        redirectAttributes.addFlashAttribute("deleted", true);
        return "redirect:/invoices";
    }

    private Specification<Invoice> buildSpec(Long companyId, String q, String status, String debut, String fin) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("invoiceNumber")), like),
                        cb.like(cb.lower(root.get("clientName")), like),
                        cb.like(cb.lower(root.get("clientIce")), like)
                ));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), InvoiceStatus.valueOf(status)));
            }
            if (debut != null && !debut.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), LocalDate.parse(debut)));
            }
            if (fin != null && !fin.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), LocalDate.parse(fin)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String toCsv(List<Invoice> invoices) {
        StringBuilder sb = new StringBuilder("N°;Date;Client;ICE;Total HT;Total TVA;Timbre;Total TTC;Mode;Statut;Conforme;Anomalies\n");
        for (Invoice i : invoices) {
            sb.append(esc(i.getInvoiceNumber())).append(';')
                    .append(i.getIssueDate()).append(';')
                    .append(esc(i.getClientName())).append(';')
                    .append(esc(i.getClientIce())).append(';')
                    .append(nb(i.getTotalHt())).append(';')
                    .append(nb(i.getTotalTva())).append(';')
                    .append(nb(i.getStampDuty())).append(';')
                    .append(nb(i.getTotalTtc())).append(';')
                    .append(esc(methodLabel(i.getPaymentMethod()))).append(';')
                    .append(i.getStatus()).append(';')
                    .append(Boolean.TRUE.equals(i.getCompliant()) ? "OUI" : "NON").append(';')
                    .append(i.getComplianceAudits() == null ? 0 : i.getComplianceAudits().size())
                    .append('\n');
        }
        return sb.toString();
    }

    private String methodLabel(Object m) {
        return switch (String.valueOf(m)) {
            case "CASH" -> "Espèces";
            case "TRANSFER" -> "Virement";
            case "CHEQUE" -> "Chèque";
            case "BILL_OF_EXCHANGE" -> "Effet";
            default -> "Autre";
        };
    }

    private String nb(BigDecimal b) {
        return b == null ? "0" : b.toPlainString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace(";", ",").replace("\"", "'");
    }
}
