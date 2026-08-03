package com.audit.dgi.validateur_dgi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "severity", nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private AuditSeverity severity;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "actual_value", length = 2000)
    private String actualValue;

    @Column(name = "expected_value", length = 2000)
    private String expectedValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
