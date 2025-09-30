package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ProjectSnapshot;

public interface MetadataAnalyzerService {
    ProjectSnapshot enrichWithStructure(ProjectSnapshot snapshot);
}
