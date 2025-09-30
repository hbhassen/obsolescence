package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ProjectSnapshot;
import java.util.List;

public interface GithubScannerService {
    List<ProjectSnapshot> fetchProjects();
}
