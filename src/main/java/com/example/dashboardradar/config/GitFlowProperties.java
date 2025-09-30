package com.example.dashboardradar.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.gitflow")
public record GitFlowProperties(
        Set<String> requiredBranches,
        List<String> allowedBranchPatterns,
        Set<String> protectedBranches,
        Set<String> allowedDefaultBranches,
        boolean requireProtectedDefaultBranch,
        int maxBranchInactivityDays
) {

    public GitFlowProperties {
        if (requiredBranches == null || requiredBranches.isEmpty()) {
            requiredBranches = Set.of("main", "master", "develop");
        }
        requiredBranches = toLowerCase(requiredBranches);
        if (allowedBranchPatterns == null || allowedBranchPatterns.isEmpty()) {
            allowedBranchPatterns = List.of(
                    "main",
                    "master",
                    "develop",
                    "release/.+",
                    "feature/.+",
                    "hotfix/.+",
                    "bugfix/.+"
            );
        }
        if (protectedBranches == null) {
            protectedBranches = Set.of("main", "master", "develop");
        }
        protectedBranches = toLowerCase(protectedBranches);
        if (allowedDefaultBranches == null || allowedDefaultBranches.isEmpty()) {
            allowedDefaultBranches = Set.of("main", "master");
        }
        allowedDefaultBranches = toLowerCase(allowedDefaultBranches);
        if (maxBranchInactivityDays < 0) {
            maxBranchInactivityDays = 0;
        }
    }

    private static Set<String> toLowerCase(Set<String> values) {
        return values.stream()
                .map(value -> value == null ? null : value.toLowerCase(Locale.ROOT))
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());
    }
}

