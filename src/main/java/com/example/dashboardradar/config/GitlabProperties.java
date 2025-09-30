package com.example.dashboardradar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.gitlab")
public record GitlabProperties(
        String token,
        String group,
        String baseUrl,
        int pageSize,
        boolean includeSubgroups
) {

    public GitlabProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://gitlab.com/api/v4";
        }
        if (pageSize <= 0) {
            pageSize = 50;
        }
    }
}

