package com.example.dashboardradar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "obsolescence")
public class ObsolescenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String component;

    @Column(name = "current_version")
    private String currentVersion;

    @Column(name = "minimum_version")
    private String minimumVersion;

    @Column(name = "deprecated_before")
    private String deprecatedBefore;

    @Column(name = "end_of_support")
    private String endOfSupport;

    private String severity;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    public String getDeprecatedBefore() {
        return deprecatedBefore;
    }

    public void setDeprecatedBefore(String deprecatedBefore) {
        this.deprecatedBefore = deprecatedBefore;
    }

    public String getEndOfSupport() {
        return endOfSupport;
    }

    public void setEndOfSupport(String endOfSupport) {
        this.endOfSupport = endOfSupport;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }
}
