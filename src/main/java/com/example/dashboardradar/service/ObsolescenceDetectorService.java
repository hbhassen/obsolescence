package com.example.dashboardradar.service;

import com.example.dashboardradar.model.ObsolescenceReport;
import com.example.dashboardradar.model.ProjectSnapshot;

public interface ObsolescenceDetectorService {
    ObsolescenceReport detect(ProjectSnapshot snapshot);
}
