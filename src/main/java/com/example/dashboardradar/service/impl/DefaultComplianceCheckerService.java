package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.GitFlowProperties;
import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.ComplianceReport;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import com.example.dashboardradar.service.ComplianceCheckerService;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DefaultComplianceCheckerService implements ComplianceCheckerService {

    private final GitFlowProperties properties;
    private final List<Pattern> allowedPatterns;

    public DefaultComplianceCheckerService(GitFlowProperties properties) {
        this.properties = properties;
        this.allowedPatterns = properties.allowedBranchPatterns().stream()
                .map(Pattern::compile)
                .toList();
    }

    @Override
    public ComplianceReport checkCompliance(ProjectSnapshot snapshot) {
        List<String> gitFlowViolations = new ArrayList<>();
        List<BranchSnapshot> branches = snapshot.branches() == null ? List.of() : snapshot.branches();
        Set<String> branchNames = branches.stream()
                .map(branch -> branch.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
        for (String required : properties.requiredBranches()) {
            if (branchNames.stream().noneMatch(name -> name.equalsIgnoreCase(required))) {
                gitFlowViolations.add("Missing required branch: " + required);
            }
        }
        gitFlowViolations.addAll(checkProtectedBranches(branches));
        gitFlowViolations.addAll(checkDefaultBranch(branches));
        gitFlowViolations.addAll(checkBranchActivity(branches));
        List<String> namingViolations = branches.stream()
                .map(BranchSnapshot::name)
                .filter(name -> allowedPatterns.stream().noneMatch(pattern -> pattern.matcher(name).matches()))
                .map(name -> "Branch does not respect naming convention: " + name)
                .toList();
        RepositoryStructure structure = snapshot.structure();
        boolean hasCi = structure != null && !structure.ciFiles().isEmpty();
        return new ComplianceReport(structure, gitFlowViolations, namingViolations, hasCi);
    }

    private List<String> checkProtectedBranches(List<BranchSnapshot> branches) {
        if (properties.protectedBranches().isEmpty()) {
            return List.of();
        }
        Set<String> requiredProtected = properties.protectedBranches().stream()
                .map(branch -> branch.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return branches.stream()
                .filter(branch -> requiredProtected.contains(branch.name().toLowerCase(Locale.ROOT)))
                .filter(branch -> !branch.isProtected())
                .map(branch -> "Branch should be protected according to Git Flow policy: " + branch.name())
                .toList();
    }

    private List<String> checkDefaultBranch(List<BranchSnapshot> branches) {
        return branches.stream()
                .filter(BranchSnapshot::isDefault)
                .findFirst()
                .map(defaultBranch -> {
                    List<String> violations = new ArrayList<>();
                    if (!properties.allowedDefaultBranches().contains(defaultBranch.name().toLowerCase(Locale.ROOT))) {
                        violations.add("Default branch should be one of: " + properties.allowedDefaultBranches());
                    }
                    if (properties.requireProtectedDefaultBranch() && !defaultBranch.isProtected()) {
                        violations.add("Default branch must be protected");
                    }
                    return violations;
                })
                .orElseGet(() -> List.of("No default branch detected"));
    }

    private List<String> checkBranchActivity(List<BranchSnapshot> branches) {
        int maxInactivity = properties.maxBranchInactivityDays();
        if (maxInactivity <= 0) {
            return List.of();
        }
        OffsetDateTime limitDate = OffsetDateTime.now().minus(maxInactivity, ChronoUnit.DAYS);
        return branches.stream()
                .filter(branch -> branch.lastCommitDate() != null && branch.lastCommitDate().isBefore(limitDate))
                .map(branch -> "Branch inactive for more than " + maxInactivity + " days: " + branch.name())
                .toList();
    }
}
