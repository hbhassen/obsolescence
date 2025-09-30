package com.example.dashboardradar.obsolescence.model;

import java.util.Objects;

/**
 * Represents a detected component version for a given project.
 */
public record ComponentVersion(String component, String version) {

    public ComponentVersion {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(version, "version must not be null");
    }
}
