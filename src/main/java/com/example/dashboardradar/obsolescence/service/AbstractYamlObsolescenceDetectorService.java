package com.example.dashboardradar.obsolescence.service;

import com.example.dashboardradar.obsolescence.model.ComponentVersion;
import com.example.dashboardradar.obsolescence.model.ObsolescenceFinding;
import com.example.dashboardradar.obsolescence.model.ObsolescenceMatrix;
import com.example.dashboardradar.obsolescence.model.ObsolescenceRule;
import com.example.dashboardradar.obsolescence.model.ObsolescenceSeverity;
import com.example.dashboardradar.obsolescence.model.ObsolescenceStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public abstract class AbstractYamlObsolescenceDetectorService implements ObsolescenceDetectorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractYamlObsolescenceDetectorService.class);

    private final ResourceLoader resourceLoader;
    private final String resourcePath;
    private final Clock clock;
    private final ObjectMapper yamlMapper;
    private Map<String, ObsolescenceRule> rules;

    protected AbstractYamlObsolescenceDetectorService(ResourceLoader resourceLoader, String resourcePath, Clock clock) {
        this.resourceLoader = resourceLoader;
        this.resourcePath = resourcePath;
        this.clock = clock;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        loadRules();
    }

    private void loadRules() {
        Resource resource = resourceLoader.getResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            ObsolescenceMatrix matrix = yamlMapper.readValue(inputStream, ObsolescenceMatrix.class);
            this.rules = Optional.ofNullable(matrix.frameworks())
                    .orElse(Map.of())
                    .entrySet()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(entry -> normalize(entry.getKey()), Map.Entry::getValue));
            LOGGER.info("Loaded {} obsolescence rules from {}", rules.size(), resourcePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load obsolescence matrix from " + resourcePath, e);
        }
    }

    @Override
    public List<ObsolescenceFinding> evaluate(Collection<ComponentVersion> components) {
        return components.stream()
                .map(component -> evaluateComponent(component.component(), component.version()))
                .toList();
    }

    private ObsolescenceFinding evaluateComponent(String componentName, String detectedVersion) {
        String normalizedKey = normalize(componentName);
        ObsolescenceRule rule = rules.get(normalizedKey);
        if (rule == null) {
            return new ObsolescenceFinding(
                    componentName,
                    detectedVersion,
                    "N/A",
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    ObsolescenceSeverity.LOW,
                    ObsolescenceStatus.UNKNOWN,
                    "No obsolescence rule available for component " + componentName);
        }

        ObsolescenceStatus status = determineStatus(detectedVersion, rule);
        String message = buildMessage(componentName, detectedVersion, rule, status);
        return new ObsolescenceFinding(
                componentName,
                detectedVersion,
                rule.minimumVersion(),
                rule.latestVersion(),
                Optional.ofNullable(rule.deprecatedBefore()),
                Optional.ofNullable(rule.endOfSupport()),
                rule.severity(),
                status,
                message);
    }

    private ObsolescenceStatus determineStatus(String detectedVersion, ObsolescenceRule rule) {
        if (rule.endOfSupport() != null && LocalDate.now(clock).isAfter(rule.endOfSupport())) {
            return ObsolescenceStatus.END_OF_SUPPORT;
        }
        if (rule.deprecatedBefore() != null && LocalDate.now(clock).isAfter(rule.deprecatedBefore())) {
            return ObsolescenceStatus.DEPRECATED;
        }
        if (isVersionLowerThan(detectedVersion, rule.minimumVersion())) {
            return ObsolescenceStatus.OUTDATED;
        }
        return ObsolescenceStatus.UP_TO_DATE;
    }

    private String buildMessage(
            String componentName, String detectedVersion, ObsolescenceRule rule, ObsolescenceStatus status) {
        return switch (status) {
            case END_OF_SUPPORT -> String.format(
                    Locale.ROOT,
                    "%s %s is past end-of-support (%s). Immediate upgrade recommended (severity: %s).",
                    componentName,
                    detectedVersion,
                    rule.endOfSupport(),
                    rule.severity());
            case DEPRECATED -> String.format(
                    Locale.ROOT,
                    "%s %s is deprecated since %s. Plan an upgrade before end-of-support (%s).",
                    componentName,
                    detectedVersion,
                    rule.deprecatedBefore(),
                    rule.endOfSupport());
            case OUTDATED -> String.format(
                    Locale.ROOT,
                    "%s %s is below the minimum supported version %s (severity: %s).",
                    componentName,
                    detectedVersion,
                    rule.minimumVersion(),
                    rule.severity());
            case UP_TO_DATE -> rule.latestVersion()
                    .map(latest -> String.format(
                            Locale.ROOT,
                            "%s %s complies with the matrix. Latest known version is %s.",
                            componentName,
                            detectedVersion,
                            latest))
                    .orElse(String.format(
                            Locale.ROOT, "%s %s complies with the matrix.", componentName, detectedVersion));
            case UNKNOWN -> "No obsolescence rule available.";
        };
    }

    private boolean isVersionLowerThan(String detectedVersion, String minimumVersion) {
        String[] detectedParts = detectedVersion.split("\\.");
        String[] minimumParts = minimumVersion.split("\\.");
        int maxLength = Math.max(detectedParts.length, minimumParts.length);
        for (int i = 0; i < maxLength; i++) {
            int detectedPart = i < detectedParts.length ? parseVersionPart(detectedParts[i]) : 0;
            int minimumPart = i < minimumParts.length ? parseVersionPart(minimumParts[i]) : 0;
            if (detectedPart < minimumPart) {
                return true;
            }
            if (detectedPart > minimumPart) {
                return false;
            }
        }
        return false;
    }

    private int parseVersionPart(String part) {
        String numeric = part.replaceAll("[^0-9]", "");
        if (numeric.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(numeric);
    }

    private String normalize(String key) {
        return key.toLowerCase(Locale.ROOT).replace(" ", "-");
    }
}
