package com.example.dashboardradar.model;

import java.time.OffsetDateTime;
import java.util.List;

public record MergeRequestSnapshot(
        long id,
        String title,
        String author,
        List<String> reviewers,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String state
) {
}
