package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.GithubProperties;
import com.example.dashboardradar.model.BranchSnapshot;
import com.example.dashboardradar.model.MergeRequestSnapshot;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import com.example.dashboardradar.service.PlatformProjectScanner;
import com.example.dashboardradar.util.GradleFrameworkExtractor;
import com.example.dashboardradar.util.PackageJsonFrameworkExtractor;
import com.example.dashboardradar.util.XmlFrameworkExtractor;
import com.example.dashboardradar.service.MetadataAnalyzerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class DefaultGithubScannerService implements PlatformProjectScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGithubScannerService.class);
    private final WebClient client;
    private final GithubProperties properties;
    private final MetadataAnalyzerService analyzerService;

    public DefaultGithubScannerService(WebClient.Builder builder, GithubProperties properties,
            MetadataAnalyzerService analyzerService) {
        this.properties = properties;
        this.analyzerService = analyzerService;
        WebClient.Builder configuredBuilder = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (properties.token() != null && !properties.token().isBlank()) {
            configuredBuilder = configuredBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token());
        }
        this.client = configuredBuilder.build();
    }

    @Override
    public List<ProjectSnapshot> fetchProjects() {
        LOGGER.info("Fetching GitHub repositories for organization {}", properties.organization());
        return Flux.fromIterable(fetchRepositories())
                .flatMap(repo -> Flux.zip(
                        fetchBranches(repo),
                        fetchMergeRequests(repo),
                        fetchLanguages(repo),
                        fetchFrameworks(repo),
                        fetchRepositoryPaths(repo)
                ).map(tuple -> buildSnapshot(repo, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5())))
                .map(analyzerService::enrichWithStructure)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public String provider() {
        return "github";
    }

    private List<GithubRepository> fetchRepositories() {
        String org = Objects.requireNonNull(properties.organization(), "dashboard.github.organization must be defined");
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orgs/{org}/repos")
                        .queryParam("per_page", properties.pageSize())
                        .build(org))
                .retrieve()
                .bodyToFlux(GithubRepository.class)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private Flux<List<BranchSnapshot>> fetchBranches(GithubRepository repo) {
        return client.get()
                .uri("/repos/{fullName}/branches?per_page=" + properties.pageSize(), repo.full_name())
                .retrieve()
                .bodyToFlux(GithubBranch.class)
                .map(branch -> new BranchSnapshot(
                        branch.name(),
                        Objects.equals(branch.name(), repo.default_branch()),
                        branch.isProtected(),
                        branch.commit() != null && branch.commit().commit() != null
                                ? branch.commit().commit().author().date()
                                : null
                ))
                .collectList()
                .flux();
    }

    private Flux<List<MergeRequestSnapshot>> fetchMergeRequests(GithubRepository repo) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{fullName}/pulls")
                        .queryParam("state", "all")
                        .queryParam("per_page", properties.pageSize())
                        .build(repo.full_name()))
                .retrieve()
                .bodyToFlux(GithubPullRequest.class)
                .map(pr -> new MergeRequestSnapshot(
                        pr.id(),
                        pr.title(),
                        pr.user() != null ? pr.user().login() : null,
                        pr.requested_reviewers() == null ? List.of() : pr.requested_reviewers().stream()
                                .map(GithubUser::login)
                                .collect(Collectors.toList()),
                        pr.created_at(),
                        pr.updated_at(),
                        pr.state()
                ))
                .collectList()
                .flux();
    }

    @SuppressWarnings("unchecked")
    private Flux<Map<String, Double>> fetchLanguages(GithubRepository repo) {
        return client.get()
                .uri("/repos/{fullName}/languages", repo.full_name())
                .retrieve()
                .bodyToMono(Map.class)
                .map(raw -> ((Map<String, Number>) raw).entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue())))
                .flux();
    }

    private Flux<List<String>> fetchFrameworks(GithubRepository repo) {
        return Flux.fromIterable(Set.of("pom.xml", "build.gradle", "package.json"))
                .flatMap(file -> client.get()
                        .uri("/repos/{fullName}/contents/{file}", repo.full_name(), file)
                        .retrieve()
                        .bodyToMono(GithubContent.class)
                        .map(content -> parseFrameworks(file, content))
                        .onErrorReturn(List.of()))
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new)))
                .map(list -> List.copyOf(list))
                .flux();
    }

    private List<String> parseFrameworks(String fileName, GithubContent content) {
        if (content == null || content.content() == null) {
            return List.of();
        }
        String decoded = new String(Base64.getDecoder().decode(content.content()));
        if ("pom.xml".equals(fileName)) {
            return XmlFrameworkExtractor.extractFromPom(decoded);
        } else if ("build.gradle".equals(fileName)) {
            return GradleFrameworkExtractor.extract(decoded);
        } else if ("package.json".equals(fileName)) {
            return PackageJsonFrameworkExtractor.extract(decoded);
        }
        return List.of();
    }

    private ProjectSnapshot buildSnapshot(GithubRepository repo, List<BranchSnapshot> branches,
            List<MergeRequestSnapshot> mergeRequests, Map<String, Double> languages, List<String> frameworks,
            List<String> repositoryFiles) {
        return new ProjectSnapshot(
                String.valueOf(repo.id()),
                repo.name(),
                repo.full_name(),
                repo.owner() != null ? repo.owner().login() : null,
                repo.archived(),
                repo.updated_at(),
                branches,
                mergeRequests,
                languages,
                frameworks,
                repositoryFiles,
                new RepositoryStructure(List.of(), List.of(), List.of())
        );
    }

    private Flux<List<String>> fetchRepositoryPaths(GithubRepository repo) {
        return client.get()
                .uri("/repos/{fullName}/git/trees/{branch}?recursive=1", repo.full_name(), repo.default_branch())
                .retrieve()
                .bodyToMono(GithubTree.class)
                .map(tree -> tree.tree() == null ? List.<String>of() : tree.tree().stream().map(GithubTreeEntry::path).toList())
                .onErrorReturn(List.of())
                .flux();
    }

    private record GithubRepository(long id, String name, String full_name, GithubUser owner,
                                    boolean archived, OffsetDateTime updated_at, String default_branch) {
    }

    private record GithubUser(String login) {
    }

    private record GithubBranch(String name, @JsonProperty("protected") boolean isProtected, GithubBranchCommit commit) {
    }

    private record GithubBranchCommit(GithubCommit commit) {
    }

    private record GithubCommit(GithubCommitAuthor author) {
    }

    private record GithubCommitAuthor(OffsetDateTime date) {
    }

    private record GithubPullRequest(long id, String title, GithubUser user, List<GithubUser> requested_reviewers,
                                     OffsetDateTime created_at, OffsetDateTime updated_at, String state) {
    }

    private record GithubContent(String name, String content) {
    }

    private record GithubTree(List<GithubTreeEntry> tree) {
    }

    private record GithubTreeEntry(String path, String type) {
    }
}
