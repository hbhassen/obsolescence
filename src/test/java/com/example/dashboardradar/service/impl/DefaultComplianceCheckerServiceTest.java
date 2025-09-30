package com.example.dashboardradar.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dashboardradar.config.GitFlowProperties;
import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultComplianceCheckerServiceTest {

    private final GitFlowProperties properties = new GitFlowProperties(
            Set.of("main", "master", "develop"),
            List.of("main", "master", "develop", "feature/.+", "hotfix/.+", "release/.+", "bugfix/.+"),
            Set.of("main", "master", "develop"),
            Set.of("main", "master"),
            true,
            30
    );
    private final DefaultComplianceCheckerService service = new DefaultComplianceCheckerService(properties);

    @Test
    void detectsMissingBranchesAndNamingViolations() {
        ProjectSnapshot snapshot = new ProjectSnapshot(
                "1",
                "demo",
                "org/demo",
                "org",
                false,
                OffsetDateTime.now(),
                List.of(new BranchSnapshot("main", true, true, OffsetDateTime.now()),
                        new BranchSnapshot("feature/new-api", false, false, OffsetDateTime.now()),
                        new BranchSnapshot("badbranch", false, false, OffsetDateTime.now())),
                List.of(),
                Map.of("Java", 100.0),
                List.of(),
                List.of("src/main/java"),
                new RepositoryStructure(List.of("src/main/java"), List.of("src/test/java"), List.of(".github/workflows/build.yml"))
        );

        var report = service.checkCompliance(snapshot);

        assertThat(report.gitFlowViolations()).contains("Missing required branch: develop");
        assertThat(report.branchNamingViolations()).anyMatch(msg -> msg.contains("badbranch"));
        assertThat(report.hasCIPipeline()).isTrue();
    }

    @Test
    void detectsUnprotectedDefaultBranchAndStaleBranch() {
        OffsetDateTime sixtyDaysAgo = OffsetDateTime.now().minusDays(60);
        ProjectSnapshot snapshot = new ProjectSnapshot(
                "2",
                "demo",
                "group/demo",
                "group",
                false,
                OffsetDateTime.now(),
                List.of(
                        new BranchSnapshot("main", true, false, OffsetDateTime.now()),
                        new BranchSnapshot("develop", false, true, sixtyDaysAgo)
                ),
                List.of(),
                Map.of(),
                List.of(),
                List.of(),
                new RepositoryStructure(List.of(), List.of(), List.of())
        );

        var report = service.checkCompliance(snapshot);

        assertThat(report.gitFlowViolations()).anyMatch(msg -> msg.contains("Default branch must be protected"));
        assertThat(report.gitFlowViolations()).anyMatch(msg -> msg.contains("Branch inactive"));
    }
}
