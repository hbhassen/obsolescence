package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.entity.BranchEntity;
import com.example.dashboardradar.entity.FileCheckEntity;
import com.example.dashboardradar.entity.MergeRequestEntity;
import com.example.dashboardradar.entity.ObsolescenceEntity;
import com.example.dashboardradar.entity.ProjectEntity;
import com.example.dashboardradar.entity.TechStackEntity;
import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.MergeRequestSnapshot;
import com.example.dashboardradar.model.ObsolescenceReport.ComponentStatus;
import com.example.dashboardradar.model.ProjectAudit;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.repository.ProjectRepository;
import com.example.dashboardradar.service.PersistenceService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JpaPersistenceService implements PersistenceService {

    private final ProjectRepository projectRepository;

    public JpaPersistenceService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public void persist(ProjectAudit audit) {
        ProjectSnapshot snapshot = audit.snapshot();
        ProjectEntity entity = projectRepository.findById(snapshot.id()).orElseGet(ProjectEntity::new);
        entity.setId(snapshot.id());
        entity.setName(snapshot.name());
        entity.setFullName(snapshot.fullName());
        entity.setParentGroup(snapshot.parentGroup());
        entity.setArchived(snapshot.archived());
        entity.setLastActivity(snapshot.lastActivity());

        entity.getBranches().clear();
        List<BranchSnapshot> branches = snapshot.branches() == null ? List.of() : snapshot.branches();
        for (BranchSnapshot branch : branches) {
            BranchEntity branchEntity = new BranchEntity();
            branchEntity.setName(branch.name());
            branchEntity.setDefault(branch.isDefault());
            branchEntity.setProtected(branch.isProtected());
            branchEntity.setLastCommitDate(branch.lastCommitDate());
            branchEntity.setProject(entity);
            entity.getBranches().add(branchEntity);
        }

        entity.getMergeRequests().clear();
        List<MergeRequestSnapshot> mergeRequests = snapshot.mergeRequests() == null ? List.of() : snapshot.mergeRequests();
        for (MergeRequestSnapshot mergeRequest : mergeRequests) {
            MergeRequestEntity mergeEntity = new MergeRequestEntity();
            mergeEntity.setTitle(mergeRequest.title());
            mergeEntity.setAuthor(mergeRequest.author());
            List<String> reviewers = mergeRequest.reviewers() == null ? List.of() : mergeRequest.reviewers();
            mergeEntity.setReviewers(String.join(",", reviewers));
            mergeEntity.setCreatedAt(mergeRequest.createdAt());
            mergeEntity.setUpdatedAt(mergeRequest.updatedAt());
            mergeEntity.setState(mergeRequest.state());
            mergeEntity.setProject(entity);
            entity.getMergeRequests().add(mergeEntity);
        }

        entity.getTechStacks().clear();
        if (snapshot.languages() != null) {
            snapshot.languages().forEach((language, share) -> {
                TechStackEntity techStack = new TechStackEntity();
                techStack.setLanguage(language);
                techStack.setVersion(share != null ? share.toString() : null);
                techStack.setProject(entity);
                entity.getTechStacks().add(techStack);
            });
        }
        if (snapshot.frameworks() != null) {
            snapshot.frameworks().forEach(framework -> {
                TechStackEntity techStack = new TechStackEntity();
                techStack.setFramework(extractFrameworkName(framework));
                String version = extractVersion(framework);
                techStack.setVersion(version);
                techStack.setProject(entity);
                entity.getTechStacks().add(techStack);
            });
        }

        entity.getObsolescences().clear();
        if (audit.obsolescence() != null) {
            for (ComponentStatus status : audit.obsolescence().components()) {
                ObsolescenceEntity obsolescenceEntity = new ObsolescenceEntity();
                obsolescenceEntity.setComponent(status.component());
                obsolescenceEntity.setCurrentVersion(status.currentVersion());
                obsolescenceEntity.setMinimumVersion(status.minimumVersion());
                obsolescenceEntity.setDeprecatedBefore(status.deprecatedBefore());
                obsolescenceEntity.setEndOfSupport(status.endOfSupport());
                obsolescenceEntity.setSeverity(status.severity());
                obsolescenceEntity.setStatus(status.status());
                obsolescenceEntity.setProject(entity);
                entity.getObsolescences().add(obsolescenceEntity);
            }
        }

        entity.getFileChecks().clear();
        FileCheckEntity fileCheck = new FileCheckEntity();
        List<String> repositoryFiles = snapshot.repositoryFiles() == null ? List.of() : snapshot.repositoryFiles();
        fileCheck.setJenkinsOk(repositoryFiles.stream().anyMatch(path -> path.toLowerCase().contains("jenkinsfile")));
        fileCheck.setDockerfileOk(repositoryFiles.stream().anyMatch(path -> path.equalsIgnoreCase("Dockerfile")));
        fileCheck.setHasCiFile(audit.compliance() != null && audit.compliance().hasCIPipeline());
        fileCheck.setProject(entity);
        entity.getFileChecks().add(fileCheck);

        projectRepository.save(entity);
    }

    private String extractFrameworkName(String descriptor) {
        if (descriptor.contains("@")) {
            return descriptor.substring(0, descriptor.indexOf("@"));
        }
        String[] segments = descriptor.split(":");
        if (segments.length > 2) {
            return segments[0] + ":" + segments[1];
        }
        return descriptor;
    }

    private String extractVersion(String descriptor) {
        if (descriptor.contains("@")) {
            String[] parts = descriptor.split("@");
            return parts.length > 1 ? parts[1] : null;
        }
        String[] segments = descriptor.split(":");
        return segments.length > 2 ? segments[2] : null;
    }
}
