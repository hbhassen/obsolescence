package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.ObsolescenceMatrixProperties;
import com.example.dashboardradar.config.ObsolescenceMatrixProperties.ComponentRule;
import com.example.dashboardradar.model.ObsolescenceReport;
import com.example.dashboardradar.model.ObsolescenceReport.ComponentStatus;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.service.ObsolescenceDetectorService;
import com.example.dashboardradar.util.SemanticVersionComparator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DefaultObsolescenceDetectorService implements ObsolescenceDetectorService {

    private final ObsolescenceMatrixProperties properties;

    public DefaultObsolescenceDetectorService(ObsolescenceMatrixProperties properties) {
        this.properties = properties;
    }

    @Override
    public ObsolescenceReport detect(ProjectSnapshot snapshot) {
        if (properties.components() == null || properties.components().isEmpty()) {
            return new ObsolescenceReport(List.of());
        }
        List<ComponentStatus> statuses = new ArrayList<>();
        for (ComponentRule rule : properties.components().values()) {
            String currentVersion = resolveVersion(snapshot, rule.component());
            String status = evaluateStatus(rule, currentVersion);
            statuses.add(new ComponentStatus(
                    rule.component(),
                    currentVersion,
                    rule.minimumVersion(),
                    rule.deprecatedBefore(),
                    rule.endOfSupport(),
                    rule.severity() != null ? rule.severity().name() : ObsolescenceMatrixProperties.Severity.MINOR.name(),
                    status
            ));
        }
        return new ObsolescenceReport(statuses);
    }

    private String resolveVersion(ProjectSnapshot snapshot, String component) {
        if (component == null || component.isBlank()) {
            return null;
        }
        List<String> frameworks = snapshot.frameworks() == null ? List.of() : snapshot.frameworks();
        Optional<String> frameworkMatch = frameworks.stream()
                .filter(framework -> framework.toLowerCase().contains(component.toLowerCase()))
                .findFirst();
        if (frameworkMatch.isPresent()) {
            return extractVersion(frameworkMatch.get());
        }
        Map<String, Double> languages = snapshot.languages();
        if (languages != null && languages.keySet().stream().anyMatch(lang -> lang.equalsIgnoreCase(component))) {
            // Language detected but no version information
            return null;
        }
        return null;
    }

    private String extractVersion(String descriptor) {
        if (descriptor.contains("@")) {
            String[] parts = descriptor.split("@");
            return parts.length > 1 ? parts[1] : null;
        }
        String[] segments = descriptor.split(":");
        return segments.length > 2 ? segments[2] : null;
    }

    private String evaluateStatus(ComponentRule rule, String currentVersion) {
        LocalDate today = LocalDate.now();
        if (currentVersion == null || currentVersion.isBlank()) {
            if (rule.endOfSupportDate() != null && today.isAfter(rule.endOfSupportDate())) {
                return "UNKNOWN_VERSION_END_OF_SUPPORT";
            }
            return "UNKNOWN_VERSION";
        }
        if (rule.minimumVersion() != null
                && SemanticVersionComparator.compare(currentVersion, rule.minimumVersion()) < 0) {
            return "BELOW_MINIMUM";
        }
        if (rule.endOfSupportDate() != null && today.isAfter(rule.endOfSupportDate())) {
            return "END_OF_SUPPORT";
        }
        if (rule.deprecatedBeforeDate() != null && today.isAfter(rule.deprecatedBeforeDate())) {
            return "DEPRECATED";
        }
        return "COMPLIANT";
    }
}
