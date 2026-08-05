package com.audit.dgi.validateur_dgi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ice", length = 15)
    private String ice;

    @Column(name = "if_number", length = 8)
    private String ifNumber;

    @Column(name = "patente")
    private String patente;

    @Column(name = "rc")
    private String rc;

    @Column(name = "cnss")
    private String cnss;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "tva_regime")
    private String tvaRegime;

    @Column(name = "template_accent", length = 20)
    private String templateAccent;

    @Column(name = "template_language", length = 4)
    private String templateLanguage;

    @Column(name = "template_mentions", length = 2000)
    private String templateMentions;

    @Column(name = "template_show_stamp_duty")
    private Boolean templateShowStampDuty;
}

