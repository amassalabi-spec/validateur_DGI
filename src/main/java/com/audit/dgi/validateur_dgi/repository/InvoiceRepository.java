package com.audit.dgi.validateur_dgi.repository;

import com.audit.dgi.validateur_dgi.domain.Invoice;
import com.audit.dgi.validateur_dgi.domain.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByCompanyId(Long companyId, Pageable pageable);
    Page<Invoice> findByCompanyIdAndStatus(Long companyId, InvoiceStatus status, Pageable pageable);
    List<Invoice> findAllByCompanyId(Long companyId);
    Optional<Invoice> findByIdAndCompanyId(Long id, Long companyId);
}

