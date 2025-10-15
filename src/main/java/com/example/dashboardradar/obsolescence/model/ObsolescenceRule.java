package com.example.dashboardradar.obsolescence.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Optional;

public record ObsolescenceRule(
        String component,
        @JsonProperty("minimum-version") String minimumVersion,
        @JsonProperty("latest-version") String latestVersion,
        @JsonProperty("deprecated-before") LocalDate deprecatedBefore,
        @JsonProperty("end-of-support") LocalDate endOfSupport,
        ObsolescenceSeverity severity) {

    public Optional<String> latestVersionOptional() {
        return Optional.ofNullable(latestVersion);
    }
}
