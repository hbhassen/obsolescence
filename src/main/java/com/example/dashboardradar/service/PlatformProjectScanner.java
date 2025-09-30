package com.example.dashboardradar.service;

/**
 * Specialisation of {@link ProjectScanner} associated to a single SCM provider.
 */
public interface PlatformProjectScanner extends ProjectScanner {

    /**
     * Unique identifier of the SCM provider supported by the implementation (e.g. {@code github} or {@code gitlab}).
     *
     * @return the provider identifier in lower case
     */
    String provider();
}

