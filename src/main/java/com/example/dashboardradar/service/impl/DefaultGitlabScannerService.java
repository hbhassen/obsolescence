package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.GitlabProperties;
import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.MergeRequestSnapshot;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import com.example.dashboardradar.service.MetadataAnalyzerService;
import com.example.dashboardradar.service.PlatformProjectScanner;
import com.example.dashboardradar.util.GradleFrameworkExtractor;
import com.example.dashboardradar.util.PackageJsonFrameworkExtractor;
import com.example.dashboardradar.util.XmlFrameworkExtractor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;

@Service
public class DefaultGitlabScannerService implements PlatformProjectScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGitlabScannerService.class);

    private final WebClient client;
    private final GitlabProperties properties;
    private final MetadataAnalyzerService analyzerService;

    public DefaultGitlabScannerService(WebClient.Builder builder, GitlabProperties properties,
            MetadataAnalyzerService analyzerService) {
        this.properties = properties;
        this.analyzerService = analyzerService;
        WebClient.Builder configuredBuilder = builder.baseUrl(properties.baseUrl());
        if (properties.token() != null && !properties.token().isBlank()) {
            configuredBuilder = configuredBuilder.defaultHeader("PRIVATE-TOKEN", properties.token());
        }
        this.client = configuredBuilder.build();
    }

    @Override
    public List<ProjectSnapshot> fetchProjects() {
        if (properties.group() == null || properties.group().isBlank()) {
            LOGGER.debug("No GitLab group configured - skipping GitLab collection");
            return List.of();
        }
        LOGGER.info("Fetching GitLab projects for group {}", properties.group());
        return Flux.fromIterable(fetchProjectsMetadata())
                .flatMap(project -> Flux.zip(
                        fetchBranches(project),
                        fetchMergeRequests(project),
                        fetchLanguages(project),
                        fetchFrameworks(project),
                        fetchRepositoryPaths(project)
                ).map(tuple -> buildSnapshot(project, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5())))
                .map(analyzerService::enrichWithStructure)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private List<GitlabProject> fetchProjectsMetadata() {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/groups/{group}/projects")
                        .queryParam("include_subgroups", properties.includeSubgroups())
                        .queryParam("per_page", properties.pageSize())
                        .queryParam("with_shared", false)
                        .build(properties.group()))
                .retrieve()
                .bodyToFlux(GitlabProject.class)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private Flux<List<BranchSnapshot>> fetchBranches(GitlabProject project) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{id}/repository/branches")
                        .queryParam("per_page", properties.pageSize())
                        .build(project.id()))
                .retrieve()
                .bodyToFlux(GitlabBranch.class)
                .map(branch -> new BranchSnapshot(
                        branch.name(),
                        Objects.equals(branch.name(), project.default_branch()),
                        branch.isProtected(),
                        branch.commit() != null ? branch.commit().committed_date() : null
                ))
                .collectList()
                .flux();
    }

    private Flux<List<MergeRequestSnapshot>> fetchMergeRequests(GitlabProject project) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{id}/merge_requests")
                        .queryParam("state", "all")
                        .queryParam("per_page", properties.pageSize())
                        .build(project.id()))
                .retrieve()
                .bodyToFlux(GitlabMergeRequest.class)
                .map(mr -> new MergeRequestSnapshot(
                        mr.id(),
                        mr.title(),
                        mr.author() != null ? mr.author().username() : null,
                        mr.reviewers() == null ? List.of() : mr.reviewers().stream()
                                .map(GitlabUser::username)
                                .collect(Collectors.toList()),
                        mr.created_at(),
                        mr.updated_at(),
                        mr.state()
                ))
                .collectList()
                .flux();
    }

    @SuppressWarnings("unchecked")
    private Flux<Map<String, Double>> fetchLanguages(GitlabProject project) {
        return client.get()
                .uri("/projects/{id}/languages", project.id())
                .retrieve()
                .bodyToMono(Map.class)
                .map(raw -> ((Map<String, Number>) raw).entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue())))
                .flux();
    }

    private Flux<List<String>> fetchFrameworks(GitlabProject project) {
        if (project.default_branch() == null || project.default_branch().isBlank()) {
            return Flux.just(List.of());
        }
        return Flux.fromIterable(Set.of("pom.xml", "build.gradle", "package.json"))
                .flatMap(file -> client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/projects/{id}/repository/files/{file}/raw")
                                .queryParam("ref", project.default_branch())
                        .build(project.id(), encodeFilePath(file)))
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(content -> parseFrameworks(file, content))
                        .onErrorReturn(List.of()))
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new)))
                .map(list -> List.copyOf(list))
                .flux();
    }

    private Flux<List<String>> fetchRepositoryPaths(GitlabProject project) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{id}/repository/tree")
                        .queryParam("recursive", true)
                        .queryParam("per_page", properties.pageSize())
                        .build(project.id()))
                .retrieve()
                .bodyToFlux(GitlabTreeEntry.class)
                .map(GitlabTreeEntry::path)
                .collectList()
                .flux();
    }

    private List<String> parseFrameworks(String fileName, String content) {
        if (content == null) {
            return List.of();
        }
        if ("pom.xml".equals(fileName)) {
            return XmlFrameworkExtractor.extractFromPom(content);
        } else if ("build.gradle".equals(fileName)) {
            return GradleFrameworkExtractor.extract(content);
        } else if ("package.json".equals(fileName)) {
            return PackageJsonFrameworkExtractor.extract(content);
        }
        return List.of();
    }

    private ProjectSnapshot buildSnapshot(GitlabProject project, List<BranchSnapshot> branches,
            List<MergeRequestSnapshot> mergeRequests, Map<String, Double> languages, List<String> frameworks,
            List<String> repositoryFiles) {
        return new ProjectSnapshot(
                String.valueOf(project.id()),
                project.name(),
                project.path_with_namespace(),
                project.namespace() != null ? project.namespace().full_path() : null,
                project.archived(),
                project.last_activity_at(),
                branches,
                mergeRequests,
                languages,
                frameworks,
                repositoryFiles,
                new RepositoryStructure(List.of(), List.of(), List.of())
        );
    }

    private String encodeFilePath(String file) {
        return UriUtils.encodePath(file, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String provider() {
        return "gitlab";
    }

    private record GitlabProject(long id, String name, String path_with_namespace, GitlabNamespace namespace,
                                 boolean archived, OffsetDateTime last_activity_at, String default_branch) {
    }

    private record GitlabNamespace(String full_path) {
    }

    private record GitlabBranch(String name, @JsonProperty("protected") boolean isProtected, GitlabBranchCommit commit) {
    }

    private record GitlabBranchCommit(OffsetDateTime committed_date) {
    }

    private record GitlabMergeRequest(long id, String title, GitlabUser author, List<GitlabUser> reviewers,
                                      OffsetDateTime created_at, OffsetDateTime updated_at, String state) {
    }

    private record GitlabUser(String username) {
    }

    private record GitlabTreeEntry(String path) {
    }
}

