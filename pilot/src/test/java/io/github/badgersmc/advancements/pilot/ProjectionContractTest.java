package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ProjectionContractTest {

    @Test
    void clientProgressCannotRoundIncompleteUpToCompleted() {
        assertEquals(0, PilotPlugin.clientProgress(0));
        assertEquals(50, PilotPlugin.clientProgress(500));
        assertEquals(99, PilotPlugin.clientProgress(999));
        assertEquals(100, PilotPlugin.clientProgress(1000));
        assertThrows(IllegalArgumentException.class, () ->
            PilotPlugin.clientProgress(-1)
        );
    }

    @Test
    void invalidKeysAndFramesAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "root",
                null,
                "x",
                List.of(),
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "has spaces",
                null,
                "x",
                List.of(),
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "valid",
                null,
                "x",
                List.of(),
                Material.CLOCK,
                "UNKNOWN",
                1,
                0
            )
        );
    }

    @Test
    void descriptionsAreCopiedAndNoRewardExecutorIsPresent() throws Exception {
        var lines = new java.util.ArrayList<>(List.of("Requirement", "Reward"));
        var node = new ProjectionService.Node(
            "test",
            null,
            "x",
            lines,
            Material.CLOCK,
            "GOAL",
            1,
            2
        );
        lines.clear();
        assertEquals(2, node.description().size());
        assertNull(node.customModelData());

        var custom = new ProjectionService.Node(
            "custom",
            null,
            "x",
            List.of(),
            Material.WRITABLE_BOOK,
            815002,
            "TASK",
            1,
            2
        );
        assertEquals(815002, custom.customModelData());
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "bad-custom",
                null,
                "x",
                List.of(),
                Material.PAPER,
                0,
                "TASK",
                1,
                2
            )
        );

        var itemModel = new ProjectionService.Node(
            "item-model",
            null,
            "x",
            List.of(),
            Material.WRITABLE_BOOK,
            "enthusia:journal_quill",
            "TASK",
            1,
            2
        );
        assertEquals("enthusia:journal_quill", itemModel.itemModel());
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "bad-item-model",
                null,
                "x",
                List.of(),
                Material.PAPER,
                "Bad Namespace",
                "TASK",
                1,
                2
            )
        );

        String source = Files.readString(
            Path.of(
                "src/main/java/io/github/badgersmc/advancements/pilot/PilotPlugin.java"
            )
        );
        assertFalse(source.contains("dispatchCommand("));
        assertFalse(source.contains("giveReward("));
        assertTrue(
            source.contains("setCustomModelData(definition.customModelData())")
        );
        assertTrue(source.contains("setItemModel(itemModel)"));
        var automaticAnnouncements = java.util.regex.Pattern.compile(
            "false\\s*,\\s*false"
        ).matcher(source).results().count();
        assertEquals(2, automaticAnnouncements,
            "Both root and child displays must disable automatic toast and chat");
    }
}
