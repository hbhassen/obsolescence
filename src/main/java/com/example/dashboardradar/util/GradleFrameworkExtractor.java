package com.example.dashboardradar.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GradleFrameworkExtractor {

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("['\"]([a-zA-Z0-9_.-]+:[a-zA-Z0-9_.-]+(?::[a-zA-Z0-9_.-]+)?)['\"]");

    private GradleFrameworkExtractor() {
    }

    public static List<String> extract(String content) {
        Matcher matcher = DEPENDENCY_PATTERN.matcher(content);
        Set<String> frameworks = new HashSet<>();
        while (matcher.find()) {
            frameworks.add(matcher.group(1));
        }
        return List.copyOf(frameworks);
    }
}
