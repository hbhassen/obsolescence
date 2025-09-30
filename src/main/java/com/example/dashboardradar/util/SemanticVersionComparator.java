package com.example.dashboardradar.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SemanticVersionComparator {

    private SemanticVersionComparator() {
    }

    public static int compare(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        List<Integer> leftParts = parse(left);
        List<Integer> rightParts = parse(right);
        int length = Math.max(leftParts.size(), rightParts.size());
        for (int i = 0; i < length; i++) {
            int l = i < leftParts.size() ? leftParts.get(i) : 0;
            int r = i < rightParts.size() ? rightParts.get(i) : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    private static List<Integer> parse(String version) {
        String sanitized = version.replaceAll("[^0-9.]+", "");
        if (sanitized.isBlank()) {
            return List.of(0);
        }
        return Arrays.stream(sanitized.split("\\."))
                .filter(part -> !part.isBlank())
                .map(part -> part.replaceAll("[^0-9]", ""))
                .filter(part -> !part.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
