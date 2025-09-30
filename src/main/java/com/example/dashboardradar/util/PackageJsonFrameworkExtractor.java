package com.example.dashboardradar.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PackageJsonFrameworkExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageJsonFrameworkExtractor.class);

    private PackageJsonFrameworkExtractor() {
    }

    public static List<String> extract(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(content);
            Set<String> dependencies = new HashSet<>();
            collectDependencies(node.get("dependencies"), dependencies);
            collectDependencies(node.get("devDependencies"), dependencies);
            return List.copyOf(dependencies);
        } catch (Exception ex) {
            LOGGER.warn("Unable to parse package.json", ex);
            return List.of();
        }
    }

    private static void collectDependencies(JsonNode node, Set<String> container) {
        if (node == null) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String version = entry.getValue().asText();
            if (version != null && !version.isBlank()) {
                container.add(entry.getKey() + "@" + version);
            } else {
                container.add(entry.getKey());
            }
        }
    }
}
