package com.example.dashboardradar.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
public class ProjectEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "parent_group")
    private String parentGroup;

    @Column(name = "is_archived")
    private boolean archived;

    @Column(name = "last_activity")
    private OffsetDateTime lastActivity;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BranchEntity> branches = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MergeRequestEntity> mergeRequests = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TechStackEntity> techStacks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ObsolescenceEntity> obsolescences = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FileCheckEntity> fileChecks = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(String parentGroup) {
        this.parentGroup = parentGroup;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public OffsetDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public List<BranchEntity> getBranches() {
        return branches;
    }

    public void setBranches(List<BranchEntity> branches) {
        this.branches = branches;
    }

    public List<MergeRequestEntity> getMergeRequests() {
        return mergeRequests;
    }

    public void setMergeRequests(List<MergeRequestEntity> mergeRequests) {
        this.mergeRequests = mergeRequests;
    }

    public List<TechStackEntity> getTechStacks() {
        return techStacks;
    }

    public void setTechStacks(List<TechStackEntity> techStacks) {
        this.techStacks = techStacks;
    }

    public List<ObsolescenceEntity> getObsolescences() {
        return obsolescences;
    }

    public void setObsolescences(List<ObsolescenceEntity> obsolescences) {
        this.obsolescences = obsolescences;
    }

    public List<FileCheckEntity> getFileChecks() {
        return fileChecks;
    }

    public void setFileChecks(List<FileCheckEntity> fileChecks) {
        this.fileChecks = fileChecks;
    }
}
