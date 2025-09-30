package com.example.dashboardradar.model;

import java.time.OffsetDateTime;

public record BranchSnapshot(
        String name,
        boolean isDefault,
        boolean isProtected,
        OffsetDateTime lastCommitDate
) {
}
