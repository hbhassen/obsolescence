package com.example.dashboardradar.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Holds the SCM providers that should be used for the current batch execution.
 * <p>
 * The selection is stored in a {@link ThreadLocal} because the batch job is executed
 * on the main application thread. This avoids leaking the selection between two
 * successive job runs.
 */
@Component
public class ProviderSelectionManager {

    private static final ThreadLocal<Set<String>> SELECTED_PROVIDERS = new ThreadLocal<>();

    public void selectProviders(Collection<String> providers) {
        if (providers == null) {
            clear();
            return;
        }
        Set<String> normalized = providers.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            clear();
        } else {
            SELECTED_PROVIDERS.set(normalized);
        }
    }

    public void selectProviders(String providers) {
        if (providers == null) {
            clear();
            return;
        }
        selectProviders(Arrays.asList(providers.split(",")));
    }

    public Optional<Set<String>> getSelectedProviders() {
        return Optional.ofNullable(SELECTED_PROVIDERS.get());
    }

    public void clear() {
        SELECTED_PROVIDERS.remove();
    }
}
