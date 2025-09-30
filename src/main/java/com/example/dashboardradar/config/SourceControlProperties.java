package com.example.dashboardradar.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.sources")
public record SourceControlProperties(List<String> enabledProviders) {

    public SourceControlProperties {
        if (enabledProviders == null || enabledProviders.isEmpty()) {
            enabledProviders = List.of("github");
        } else {
            enabledProviders = enabledProviders.stream()
                    .map(value -> value == null ? null : value.trim())
                    .filter(value -> value != null && !value.isBlank())
                    .toList();
            if (enabledProviders.isEmpty()) {
                enabledProviders = List.of("github");
            }
        }
    }
}

