package com.audit.dgi.validateur_dgi.engine;

import com.audit.dgi.validateur_dgi.domain.AuditSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditReport {

    @Builder.Default
    private List<DgiRule.RuleResult> results = new ArrayList<>();

    private boolean compliant;

    public boolean hasErrors() {
        return results.stream().anyMatch(result -> !result.isValid() && result.severity() == AuditSeverity.ERROR);
    }

    public List<DgiRule.RuleResult> getResults() { return results; }
    public void setResults(List<DgiRule.RuleResult> results) { this.results = results; }
    public boolean isCompliant() { return compliant; }
    public void setCompliant(boolean compliant) { this.compliant = compliant; }
}

