package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.SourceControlProperties;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.service.PlatformProjectScanner;
import com.example.dashboardradar.service.ProjectScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CompositeProjectScannerService implements ProjectScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeProjectScannerService.class);

    private final Map<String, PlatformProjectScanner> scannersByProvider;
    private final SourceControlProperties properties;

    public CompositeProjectScannerService(List<PlatformProjectScanner> scanners, SourceControlProperties properties) {
        this.scannersByProvider = scanners.stream()
                .collect(Collectors.toMap(scanner -> scanner.provider().toLowerCase(Locale.ROOT), Function.identity()));
        this.properties = properties;
    }

    @Override
    public List<ProjectSnapshot> fetchProjects() {
        Set<String> enabledProviders = properties.enabledProviders().isEmpty()
                ? scannersByProvider.keySet()
                : properties.enabledProviders().stream()
                        .map(provider -> provider.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        List<ProjectSnapshot> aggregated = new ArrayList<>();
        for (String provider : enabledProviders) {
            PlatformProjectScanner scanner = scannersByProvider.get(provider);
            if (scanner == null) {
                LOGGER.warn("No SCM scanner registered for provider {}", provider);
                continue;
            }
            LOGGER.info("Collecting projects from {}", provider);
            aggregated.addAll(scanner.fetchProjects());
        }
        return aggregated;
    }
}

