package com.example.dashboardradar.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ProjectSnapshot(
        String id,
        String name,
        String fullName,
        String parentGroup,
        boolean archived,
        OffsetDateTime lastActivity,
        List<BranchSnapshot> branches,
        List<MergeRequestSnapshot> mergeRequests,
        Map<String, Double> languages,
        List<String> frameworks,
        List<String> repositoryFiles,
        RepositoryStructure structure
) {
}
