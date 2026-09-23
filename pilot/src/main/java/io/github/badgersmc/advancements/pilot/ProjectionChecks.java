package io.github.badgersmc.advancements.pilot;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Input checks shared by tree registration and read-only progress projection. */
final class ProjectionChecks {

    private ProjectionChecks() {}

    static void validateDefinitions(List<ProjectionService.Node> definitions) {
        // Validate before replacing a visible tree.
        var keys = new HashSet<String>();
        for (ProjectionService.Node definition : definitions) {
            if (!keys.add(definition.key())) {
                throw new IllegalArgumentException("Duplicate key: " + definition.key());
            }
            if (definition.parentKey() != null && !keys.contains(definition.parentKey())) {
                throw new IllegalArgumentException("Parent must precede child: " + definition.key());
            }
        }
    }

    static Map<String, Integer> checkedProgress(Map<String, Integer> progress) {
        if (progress == null) throw new IllegalArgumentException("Missing progress map");
        Map<String, Integer> checked = new LinkedHashMap<>();
        for (var entry : progress.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("Progress keys and values must not be null");
            }
            checked.put(entry.getKey(), entry.getValue());
        }
        // Unknown keys and unavailable/out-of-range values keep their existing skip behavior.
        return Map.copyOf(checked);
    }

    static int clientProgress(int verifiedValue) {
        if (verifiedValue < 0 || verifiedValue > 1000) {
            throw new IllegalArgumentException("Invalid progress");
        }
        return verifiedValue == 1000 ? 100 : Math.min(99, verifiedValue / 10);
    }
}
