package com.example.dashboardradar.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dashboardradar.config.ObsolescenceMatrixProperties;
import com.example.dashboardradar.config.ObsolescenceMatrixProperties.ComponentRule;
import com.example.dashboardradar.config.ObsolescenceMatrixProperties.Severity;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultObsolescenceDetectorServiceTest {

    @Test
    void detectsBelowMinimumVersions() {
        ObsolescenceMatrixProperties properties = new ObsolescenceMatrixProperties();
        properties.setComponents(Map.of(
                "spring", new ComponentRule("spring-boot", "3.0.0", "2024-01-01", "2024-12-31", Severity.MAJOR)
        ));
        DefaultObsolescenceDetectorService service = new DefaultObsolescenceDetectorService(properties);

        ProjectSnapshot snapshot = new ProjectSnapshot(
                "1",
                "demo",
                "org/demo",
                "org",
                false,
                OffsetDateTime.now(),
                List.of(),
                List.of(),
                Map.of(),
                List.of("org.springframework.boot:spring-boot-starter:2.7.5"),
                List.of(),
                new RepositoryStructure(List.of(), List.of(), List.of())
        );

        var report = service.detect(snapshot);
        assertThat(report.components()).hasSize(1);
        assertThat(report.components().get(0).status()).isEqualTo("BELOW_MINIMUM");
    }
}
