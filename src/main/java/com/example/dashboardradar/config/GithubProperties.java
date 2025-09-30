package com.example.dashboardradar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.github")
public record GithubProperties(
        String token,
        String organization,
        String baseUrl,
        int pageSize
) {
    public GithubProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.github.com";
        }
        if (pageSize <= 0) {
            pageSize = 50;
        }
    }
}
