package com.example.dashboardradar.obsolescence.model;

import java.time.LocalDate;
import java.util.Optional;

public record ObsolescenceFinding(
        String component,
        String detectedVersion,
        String minimumVersion,
        Optional<String> latestVersion,
        Optional<LocalDate> deprecatedBefore,
        Optional<LocalDate> endOfSupport,
        ObsolescenceSeverity severity,
        ObsolescenceStatus status,
        String message) {
}
