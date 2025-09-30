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
@Table(name = "file_check")
public class FileCheckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jenkins_ok")
    private boolean jenkinsOk;

    @Column(name = "dockerfile_ok")
    private boolean dockerfileOk;

    @Column(name = "has_ci_file")
    private boolean hasCiFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isJenkinsOk() {
        return jenkinsOk;
    }

    public void setJenkinsOk(boolean jenkinsOk) {
        this.jenkinsOk = jenkinsOk;
    }

    public boolean isDockerfileOk() {
        return dockerfileOk;
    }

    public void setDockerfileOk(boolean dockerfileOk) {
        this.dockerfileOk = dockerfileOk;
    }

    public boolean isHasCiFile() {
        return hasCiFile;
    }

    public void setHasCiFile(boolean hasCiFile) {
        this.hasCiFile = hasCiFile;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }
}
