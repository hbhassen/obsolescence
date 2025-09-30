package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.ComplianceReport;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import com.example.dashboardradar.service.ComplianceCheckerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class DefaultComplianceCheckerService implements ComplianceCheckerService {

    private static final Set<String> REQUIRED_BRANCHES = Set.of("main", "master", "develop");
    private static final List<Pattern> ALLOWED_BRANCH_PATTERNS = List.of(
            Pattern.compile("main"),
            Pattern.compile("master"),
            Pattern.compile("develop"),
            Pattern.compile("release/.+"),
            Pattern.compile("feature/.+"),
            Pattern.compile("hotfix/.+"),
            Pattern.compile("bugfix/.+")
    );

    @Override
    public ComplianceReport checkCompliance(ProjectSnapshot snapshot) {
        List<String> gitFlowViolations = new ArrayList<>();
        List<BranchSnapshot> branches = snapshot.branches() == null ? List.of() : snapshot.branches();
        List<String> branchNames = branches.stream().map(BranchSnapshot::name).toList();
        for (String required : REQUIRED_BRANCHES) {
            if (branchNames.stream().noneMatch(name -> name.equalsIgnoreCase(required))) {
                gitFlowViolations.add("Missing required branch: " + required);
            }
        }
        List<String> namingViolations = branches.stream()
                .map(BranchSnapshot::name)
                .filter(name -> ALLOWED_BRANCH_PATTERNS.stream().noneMatch(pattern -> pattern.matcher(name).matches()))
                .map(name -> "Branch does not respect naming convention: " + name)
                .toList();
        RepositoryStructure structure = snapshot.structure();
        boolean hasCi = structure != null && !structure.ciFiles().isEmpty();
        return new ComplianceReport(structure, gitFlowViolations, namingViolations, hasCi);
    }
}
