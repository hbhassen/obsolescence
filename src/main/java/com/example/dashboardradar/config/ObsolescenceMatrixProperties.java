package com.example.dashboardradar.config;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.obsolescence")
public record ObsolescenceMatrixProperties(
        Map<String, ComponentRule> components
) {
    public record ComponentRule(
            String component,
            String minimumVersion,
            String deprecatedBefore,
            String endOfSupport,
            Severity severity
    ) {
        public LocalDate deprecatedBeforeDate() {
            return deprecatedBefore == null || deprecatedBefore.isBlank()
                    ? null
                    : LocalDate.parse(deprecatedBefore);
        }

        public LocalDate endOfSupportDate() {
            return endOfSupport == null || endOfSupport.isBlank()
                    ? null
                    : LocalDate.parse(endOfSupport);
        }
    }

    public enum Severity {
        MINOR,
        MAJOR,
        CRITICAL
    }
}
