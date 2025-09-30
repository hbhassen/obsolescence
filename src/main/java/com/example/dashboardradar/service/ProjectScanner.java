package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ProjectSnapshot;
import java.util.List;

/**
 * Contract used to fetch {@link ProjectSnapshot} instances from a source code management platform.
 */
public interface ProjectScanner {

    /**
     * Retrieves the projects visible for the configured platform.
     *
     * @return the collected projects (never {@code null})
     */
    List<ProjectSnapshot> fetchProjects();
}

