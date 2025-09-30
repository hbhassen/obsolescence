package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ProjectAudit;

public interface PersistenceService {
    void persist(ProjectAudit audit);
}
