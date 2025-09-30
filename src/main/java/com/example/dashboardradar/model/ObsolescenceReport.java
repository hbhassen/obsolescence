package com.example.dashboardradar.model;

import java.util.List;

public record ObsolescenceReport(
        List<ComponentStatus> components
) {
    public record ComponentStatus(
            String component,
            String currentVersion,
            String minimumVersion,
            String deprecatedBefore,
            String endOfSupport,
            String severity,
            String status
    ) {
    }
}
