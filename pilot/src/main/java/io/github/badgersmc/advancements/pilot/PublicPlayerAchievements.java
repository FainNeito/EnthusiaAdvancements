package io.github.badgersmc.advancements.pilot;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
/** Completed public labels only; missing/unloaded/shared identities remain unknown. */
final class PublicPlayerAchievements {
    record Completion(String name, List<String> completed) {}
    static Map<String, Object> build(List<UUID> requested, Function<UUID, Completion> lookup, Instant capturedAt) {
        if (requested == null || requested.isEmpty() || requested.size() > 50 || requested.stream().anyMatch(Objects::isNull)
            || new HashSet<>(requested).size() != requested.size()) throw new IllegalArgumentException("Invalid requested identities");
        List<Map<String, Object>> items = new ArrayList<>();
        for (UUID id : requested) {
            Completion result = lookup.apply(id); if (result == null) continue;
            if (result.completed().size() > 200) throw new IllegalStateException("Public completion limit exceeded");
            items.add(Map.of("id", id.toString(), "name", result.name(), "achievementsKnown", true,
                "achievements", List.copyOf(result.completed())));
        }
        return Map.of("generatedAt", capturedAt.toString(), "scope", "registered_renderer_projection", "limited", true, "items", items);
    }
}
