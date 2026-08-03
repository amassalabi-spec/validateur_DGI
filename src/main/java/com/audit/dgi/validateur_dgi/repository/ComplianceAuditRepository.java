package com.audit.dgi.validateur_dgi.repository;

import com.audit.dgi.validateur_dgi.domain.ComplianceAudit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ComplianceAuditRepository extends JpaRepository<ComplianceAudit, Long> {

    @Query("select ca.ruleCode as rule, count(ca) as cnt " +
            "from ComplianceAudit ca group by ca.ruleCode order by count(ca) desc")
    List<Object[]> findTopErrors(Pageable pageable);

    @Query("select ca.ruleCode as rule, count(ca) as cnt " +
            "from ComplianceAudit ca where ca.invoice.companyId = :companyId " +
            "group by ca.ruleCode order by count(ca) desc")
    List<Object[]> findTopErrorsByCompany(Long companyId, Pageable pageable);
}
