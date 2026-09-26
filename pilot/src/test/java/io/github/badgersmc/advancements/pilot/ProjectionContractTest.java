package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProjectionContractTest {

    @BeforeAll
    static void initializeDisplayAdapter() {
        UaaDisplayFixture.initialize();
    }

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

    private static ProjectionService.Node node(String key, String frame) {
        return new ProjectionService.Node(
            key,
            null,
            "x",
            List.of(),
            Material.CLOCK,
            frame,
            1,
            0
        );
    }

    @Test
    void reservedKeyIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            node("root", "TASK")
        );
    }

    @Test
    void spacedKeyIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            node("has spaces", "TASK")
        );
    }

    @Test
    void unknownFrameIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            node("valid", "UNKNOWN")
        );
    }

    @Test
    void nullFrameIsRejectedAsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> node("valid", null));
    }

    @Test
    void nullDescriptionIsRejectedAsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "valid",
                null,
                "x",
                null,
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
    }

    @Test
    void nullDescriptionEntryIsRejectedAsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
            new ProjectionService.Node(
                "valid",
                null,
                "x",
                java.util.Arrays.asList("ok", null),
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
    }

    @Test
    void descriptionsAreCopied() {
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
    }

    @Test
    void customModelDataIsAccepted() {
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
    }

    @Test
    void invalidCustomModelDataIsRejected() {
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
    }

    @Test
    void itemModelIsAccepted() {
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
    }

    @Test
    void invalidItemModelIsRejected() {
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
    }

    @Test
    void projectionSourceCannotExecuteRewards() throws Exception {
        String source = pilotSource();
        assertFalse(source.contains("dispatchCommand("));
        assertFalse(source.contains("giveReward("));
    }

    @Test
    void projectionSourceUsesConfiguredIcons() throws Exception {
        String source = pilotSource();
        assertTrue(
            source.contains("setCustomModelData(definition.customModelData())")
        );
        assertTrue(source.contains("setItemModel(itemModel)"));
    }

    @Test
    void displaysDisableAutomaticAnnouncements() {
        try (
            var items = org.mockito.Mockito.mockConstruction(
                ItemStack.class,
                (item, context) ->
                    org.mockito.Mockito.when(item.clone()).thenReturn(item)
            )
        ) {
            AdvancementDisplay root = PilotPlugin.rootDisplay(
                new ItemStack(Material.CLOCK)
            );
            assertFalse(root.doesShowToast());
            assertFalse(root.doesAnnounceToChat());
            for (String frame : List.of("TASK", "GOAL", "CHALLENGE")) {
                AdvancementDisplay child = PilotPlugin.nodeDisplay(
                    node("child", frame)
                );
                assertFalse(child.doesShowToast());
                assertFalse(child.doesAnnounceToChat());
            }
        }
    }

    private static String pilotSource() throws Exception {
        return Files.readString(
            Path.of(
                "src/main/java/io/github/badgersmc/advancements/pilot/PilotPlugin.java"
            )
        );
    }
}
