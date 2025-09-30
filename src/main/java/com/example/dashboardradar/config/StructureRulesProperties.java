package com.example.dashboardradar.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.structure")
public record StructureRulesProperties(
        Map<String, StackRule> stacks
) {
    public record StackRule(
            List<String> mandatoryPaths,
            List<String> ciFiles,
            List<String> forbiddenFrameworks,
            List<String> allowedFrameworks,
            List<String> allowedLanguages
    ) {
    }
}
