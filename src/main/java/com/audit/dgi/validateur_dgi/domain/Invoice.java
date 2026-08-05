package com.audit.dgi.validateur_dgi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "issuer_name", nullable = false, length = 500)
    private String issuerName;

    @Column(name = "issuer_address", nullable = false, length = 500)
    private String issuerAddress;

    @Column(name = "issuer_ice", nullable = false, length = 15)
    private String issuerIce;

    @Column(name = "issuer_if", nullable = false, length = 8)
    private String issuerIf;

    @Column(name = "issuer_patente")
    private String issuerPatente;

    @Column(name = "issuer_rc")
    private String issuerRc;

    @Column(name = "issuer_cnss")
    private String issuerCnss;

    @Column(name = "client_name", nullable = false, length = 500)
    private String clientName;

    @Column(name = "client_address", nullable = false, length = 500)
    private String clientAddress;

    @Column(name = "client_ice", nullable = false, length = 15)
    private String clientIce;

    @Column(name = "total_ht", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalHt;

    @Column(name = "total_tva", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTva;

    @Column(name = "stamp_duty", nullable = false, precision = 19, scale = 2)
    private BigDecimal stampDuty;

    @Column(name = "total_ttc", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTtc;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING_AUDIT;

    @Column(name = "is_compliant", nullable = false)
    @Builder.Default
    private Boolean compliant = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "chosen_template", nullable = false)
    @Builder.Default
    private TemplateStyle chosenTemplate = TemplateStyle.MODERN;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceVatSummary> vatSummaries = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComplianceAudit> complianceAudits = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = InvoiceStatus.PENDING_AUDIT;
        }
        if (this.compliant == null) {
            this.compliant = Boolean.FALSE;
        }
        if (this.chosenTemplate == null) {
            this.chosenTemplate = TemplateStyle.MODERN;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
