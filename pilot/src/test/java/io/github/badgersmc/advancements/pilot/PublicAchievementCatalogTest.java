package io.github.badgersmc.advancements.pilot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PublicAchievementCatalogTest {
    @Test void registeredDefinitionsKeepUnknownRewardsAndCompletionsUnknown() {
        var node = new ProjectionService.Node("live", null, "Live title", List.of("Live description"), Material.DIAMOND, "TASK", 0, 0);
        var snapshot = PublicAchievementCatalog.build(Map.of("provider", List.of(node)), Instant.EPOCH);
        var row = (Map<?, ?>) ((List<?>) snapshot.get("items")).getFirst();
        assertEquals("provider:live", row.get("id"));
        assertEquals("diamond", row.get("icon"));
        assertNull(row.get("completed"));
        assertEquals("", row.get("reward"));
        assertEquals(true, snapshot.get("limited"));
        assertFalse(row.containsKey("owner"));
        assertFalse(row.containsKey("playerProgress"));
    }
    @Test void emptyRendererDoesNotPublishBundledDefaultTrees() {
        assertEquals(List.of(), PublicAchievementCatalog.build(Map.of(), Instant.EPOCH).get("items"));
    }
}
