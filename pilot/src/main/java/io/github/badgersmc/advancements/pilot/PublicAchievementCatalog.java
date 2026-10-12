package io.github.badgersmc.advancements.pilot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registered provider definitions only; no defaults, grants or player progress. */
final class PublicAchievementCatalog {
    private PublicAchievementCatalog() {}
    static Map<String, Object> build(Map<String, List<ProjectionService.Node>> trees, Instant capturedAt) {
        List<Map<String, Object>> items = new ArrayList<>();
        trees.forEach((namespace, nodes) -> nodes.forEach(node -> {
            if (items.size() >= 1000) throw new IllegalStateException("Public catalogue exceeds bounds");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", namespace + ":" + node.key());
            row.put("name", node.title());
            row.put("description", String.join("\n", node.description()));
            row.put("category", namespace);
            row.put("icon", node.icon().name().toLowerCase(java.util.Locale.ROOT));
            // Rewards and completion totals belong to providers, not the renderer.
            row.put("reward", "");
            row.put("completed", null);
            items.add(row);
        }));
        return Map.of("schemaVersion", 1, "generatedAt", capturedAt.toString(),
            "limited", true, "source", "server_export", "items", items);
    }
}
