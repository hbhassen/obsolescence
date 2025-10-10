package com.example.dashboardradar.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashboard.obsolescence")
public class ObsolescenceMatrixProperties {

    /**
     * Base location of the YAML matrices. Supports Spring Resource semantics (e.g. classpath:obsolescence).
     */
    private String matrixBasePath = "classpath:obsolescence";

    /**
     * Declarative obsolescence rules keyed by component identifier.
     */
    private Map<String, ComponentRule> components = new LinkedHashMap<>();

    public String getMatrixBasePath() {
        return matrixBasePath;
    }

    public void setMatrixBasePath(String matrixBasePath) {
        this.matrixBasePath = matrixBasePath;
    }

    public Map<String, ComponentRule> getComponents() {
        return components;
    }

    public void setComponents(Map<String, ComponentRule> components) {
        this.components = components;
    }

    public enum Severity {
        MINOR,
        MODERATE,
        MAJOR,
        CRITICAL
    }

    public static class ComponentRule {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

        private String component;
        private String minimumVersion;
        private String deprecatedBefore;
        private String endOfSupport;
        private Severity severity = Severity.MINOR;

        public ComponentRule() {}

        public ComponentRule(
                String component,
                String minimumVersion,
                String deprecatedBefore,
                String endOfSupport,
                Severity severity) {
            this.component = component;
            this.minimumVersion = minimumVersion;
            this.deprecatedBefore = deprecatedBefore;
            this.endOfSupport = endOfSupport;
            if (severity != null) {
                this.severity = severity;
            }
        }

        public String component() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String minimumVersion() {
            return minimumVersion;
        }

        public void setMinimumVersion(String minimumVersion) {
            this.minimumVersion = minimumVersion;
        }

        public String deprecatedBefore() {
            return deprecatedBefore;
        }

        public void setDeprecatedBefore(String deprecatedBefore) {
            this.deprecatedBefore = deprecatedBefore;
        }

        public String endOfSupport() {
            return endOfSupport;
        }

        public void setEndOfSupport(String endOfSupport) {
            this.endOfSupport = endOfSupport;
        }

        public Severity severity() {
            return severity;
        }

        public void setSeverity(Severity severity) {
            if (severity != null) {
                this.severity = severity;
            }
        }

        public LocalDate deprecatedBeforeDate() {
            return parseDate(deprecatedBefore);
        }

        public LocalDate endOfSupportDate() {
            return parseDate(endOfSupport);
        }

        private LocalDate parseDate(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(value, DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd but got: " + value, ex);
            }
        }
    }
}
