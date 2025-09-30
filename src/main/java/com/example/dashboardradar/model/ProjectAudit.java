package com.example.dashboardradar.model;

public record ProjectAudit(
        ProjectSnapshot snapshot,
        ComplianceReport compliance,
        ObsolescenceReport obsolescence
) {
}
