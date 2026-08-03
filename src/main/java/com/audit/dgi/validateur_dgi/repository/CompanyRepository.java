package com.audit.dgi.validateur_dgi.repository;

import com.audit.dgi.validateur_dgi.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}

