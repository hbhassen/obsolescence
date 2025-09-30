package com.example.dashboardradar.model;

import java.util.List;

public record RepositoryStructure(
        List<String> expectedPathsPresent,
        List<String> missingPaths,
        List<String> ciFiles
) {
    public boolean isCompliant() {
        return missingPaths.isEmpty();
    }
}
