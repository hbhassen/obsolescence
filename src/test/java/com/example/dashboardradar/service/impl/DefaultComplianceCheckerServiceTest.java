package com.example.dashboardradar.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultComplianceCheckerServiceTest {

    private final DefaultComplianceCheckerService service = new DefaultComplianceCheckerService();

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
}
