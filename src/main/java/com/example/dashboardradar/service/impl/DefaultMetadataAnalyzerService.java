package com.example.dashboardradar.service.impl;

import com.example.dashboardradar.config.StructureRulesProperties;
import com.example.dashboardradar.model.ProjectSnapshot;
import com.example.dashboardradar.model.RepositoryStructure;
import com.example.dashboardradar.service.MetadataAnalyzerService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DefaultMetadataAnalyzerService implements MetadataAnalyzerService {

    private final StructureRulesProperties properties;

    public DefaultMetadataAnalyzerService(StructureRulesProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProjectSnapshot enrichWithStructure(ProjectSnapshot snapshot) {
        StructureRulesProperties.StackRule rule = detectRule(snapshot);
        if (rule == null) {
            return snapshot;
        }
        List<String> repositoryFiles = snapshot.repositoryFiles() == null ? List.of() : snapshot.repositoryFiles();
        Set<String> files = Set.copyOf(repositoryFiles);
        List<String> missing = rule.mandatoryPaths() == null ? List.of() : rule.mandatoryPaths().stream()
                .filter(path -> files.stream().noneMatch(file -> file.startsWith(path)))
                .toList();
        List<String> present = rule.mandatoryPaths() == null ? List.of() : rule.mandatoryPaths().stream()
                .filter(path -> files.stream().anyMatch(file -> file.startsWith(path)))
                .toList();
        List<String> ciFiles = rule.ciFiles() == null ? List.of() : rule.ciFiles().stream()
                .filter(files::contains)
                .toList();
        RepositoryStructure structure = new RepositoryStructure(present, missing, ciFiles);
        return new ProjectSnapshot(
                snapshot.id(),
                snapshot.name(),
                snapshot.fullName(),
                snapshot.parentGroup(),
                snapshot.archived(),
                snapshot.lastActivity(),
                snapshot.branches(),
                snapshot.mergeRequests(),
                snapshot.languages(),
                snapshot.frameworks(),
                snapshot.repositoryFiles(),
                structure
        );
    }

    private StructureRulesProperties.StackRule detectRule(ProjectSnapshot snapshot) {
        if (properties.stacks() == null || properties.stacks().isEmpty()) {
            return null;
        }
        Map<String, Double> languages = snapshot.languages();
        List<String> frameworks = snapshot.frameworks();
        return properties.stacks().entrySet().stream()
                .filter(entry -> matchesLanguages(entry.getValue(), languages))
                .filter(entry -> matchesFrameworks(entry.getValue(), frameworks))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesLanguages(StructureRulesProperties.StackRule rule, Map<String, Double> languages) {
        if (rule.allowedLanguages() == null || rule.allowedLanguages().isEmpty()) {
            return true;
        }
        if (languages == null || languages.isEmpty()) {
            return false;
        }
        Set<String> normalized = languages.keySet().stream()
                .map(lang -> lang.toLowerCase().replace(" ", ""))
                .collect(Collectors.toSet());
        return rule.allowedLanguages().stream()
                .map(lang -> lang.toLowerCase().replace(" ", ""))
                .anyMatch(normalized::contains);
    }

    private boolean matchesFrameworks(StructureRulesProperties.StackRule rule, List<String> frameworks) {
        if (rule.forbiddenFrameworks() != null) {
            Set<String> forbidden = rule.forbiddenFrameworks().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            if (frameworks != null && frameworks.stream()
                    .map(String::toLowerCase)
                    .anyMatch(forbidden::contains)) {
                return false;
            }
        }
        if (rule.allowedFrameworks() == null || rule.allowedFrameworks().isEmpty()) {
            return true;
        }
        if (frameworks == null || frameworks.isEmpty()) {
            return false;
        }
        Set<String> normalizedFrameworks = frameworks.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return rule.allowedFrameworks().stream()
                .map(String::toLowerCase)
                .anyMatch(normalizedFrameworks::contains);
    }
}
