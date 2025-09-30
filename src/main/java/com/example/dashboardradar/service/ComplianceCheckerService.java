package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ComplianceReport;
import com.example.dashboardradar.model.ProjectSnapshot;

public interface ComplianceCheckerService {
    ComplianceReport checkCompliance(ProjectSnapshot snapshot);
}
