package com.audit.dgi.validateur_dgi.controller;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import com.audit.dgi.validateur_dgi.domain.Company;
import com.audit.dgi.validateur_dgi.dto.ClientDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceItemDTO;
import com.audit.dgi.validateur_dgi.dto.InvoiceData;
import com.audit.dgi.validateur_dgi.dto.IssuerDTO;
import com.audit.dgi.validateur_dgi.engine.AuditReport;
import com.audit.dgi.validateur_dgi.engine.DgiAuditEngine;
import com.audit.dgi.validateur_dgi.repository.CompanyRepository;
import com.audit.dgi.validateur_dgi.repository.InvoiceRepository;
import com.audit.dgi.validateur_dgi.security.SessionUserService;
import com.audit.dgi.validateur_dgi.service.InvoiceService;
import com.audit.dgi.validateur_dgi.service.generator.PdfGeneratorService;
import com.audit.dgi.validateur_dgi.service.parser.PoiExtractionService;
import com.audit.dgi.validateur_dgi.service.parser.SpringAiParsingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {

	private final PoiExtractionService extractionService;
	private final SpringAiParsingService parsingService;
	private final DgiAuditEngine auditEngine;
	private final InvoiceService invoiceService;
	private final PdfGeneratorService pdfGeneratorService;
	private final SessionUserService sessionUserService;
	private final InvoiceRepository invoiceRepository;
	private final CompanyRepository companyRepository;

	public WorkspaceController(PoiExtractionService extractionService,
							   SpringAiParsingService parsingService,
							   DgiAuditEngine auditEngine,
							   InvoiceService invoiceService,
							   PdfGeneratorService pdfGeneratorService,
							   SessionUserService sessionUserService,
							   InvoiceRepository invoiceRepository,
							   CompanyRepository companyRepository) {
		this.extractionService = extractionService;
		this.parsingService = parsingService;
		this.auditEngine = auditEngine;
		this.invoiceService = invoiceService;
		this.pdfGeneratorService = pdfGeneratorService;
		this.sessionUserService = sessionUserService;
		this.invoiceRepository = invoiceRepository;
		this.companyRepository = companyRepository;
	}

	@ModelAttribute("invoice")
	public InvoiceData invoiceModel() {
		InvoiceData invoice = new InvoiceData();
		invoice.setIssuer(new IssuerDTO());
		invoice.setClient(new ClientDTO());
		if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
			invoice.getItems().add(new InvoiceItemDTO());
			invoice.getItems().add(new InvoiceItemDTO());
			invoice.getItems().add(new InvoiceItemDTO());
		}
		return invoice;
	}

	@ModelAttribute("auditReport")
	public AuditReport auditReportModel() {
		return new AuditReport();
	}

	@GetMapping
	public String workspace(Model model) {
		model.addAttribute("invoice", invoiceModel());
		model.addAttribute("auditReport", auditReportModel());
		return "workspace";
	}

	@PostMapping("/upload")
	public String upload(@RequestParam("file") MultipartFile file, Model model) throws Exception {
		String rawText = extractionService.extractText(file);
		InvoiceData invoice = parsingService.parse(rawText);
		AuditReport auditReport = auditEngine.executeAudit(invoice);
		model.addAttribute("invoice", invoice);
		model.addAttribute("auditReport", auditReport);
		model.addAttribute("rawText", rawText);
		model.addAttribute("fileName", file.getOriginalFilename());
		return "workspace";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("invoice") InvoiceData invoice,
					   @RequestParam(required = false) String action,
					   RedirectAttributes redirectAttributes) throws Exception {
		Long companyId = sessionUserService.currentCompanyId();
		AuditReport auditReport = auditEngine.executeAudit(invoice);
		Invoice saved = invoiceService.saveInvoice(invoice, companyId, auditReport);
		if ("REJECT".equalsIgnoreCase(action)) {
			saved.setStatus(InvoiceStatus.NON_COMPLIANT);
			saved.setCompliant(false);
			invoiceRepository.save(saved);
			redirectAttributes.addFlashAttribute("rejected", true);
			return "redirect:/invoices";
		}
		saved.setStatus(auditReport.hasErrors() ? InvoiceStatus.NON_COMPLIANT : InvoiceStatus.COMPLIANT);
		saved.setCompliant(!auditReport.hasErrors());
		invoiceRepository.save(saved);
		Company company = companyRepository.findById(companyId).orElse(null);
		pdfGeneratorService.generateInvoicePdf(saved, saved.getChosenTemplate(), company);
		redirectAttributes.addFlashAttribute("savedInvoiceId", saved.getId());
		return "redirect:/invoices";
	}
}

