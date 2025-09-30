package com.example.dashboardradar.model;

import java.util.List;

public record ComplianceReport(
        RepositoryStructure structure,
        List<String> gitFlowViolations,
        List<String> branchNamingViolations,
        boolean hasCIPipeline
) {
}
