package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class DiscordAdvancementNoticeTest {
    @Test
    void challengeCardContainsOnlyObjectiveWithoutFormattingCodes() {
        var notice = DiscordAdvancementNotice.from("FainNeito", node(
            "CHALLENGE", "<gold>One Thousand Hours</gold>",
            List.of("&cReach 1,000 active hours", "Reward: <gold>3,000 raw gold</gold>")
        ));
        assertEquals("FainNeito has completed the challenge One Thousand Hours", notice.heading());
        assertEquals("Reach 1,000 active hours", notice.description());
        assertEquals(0xAA0000, notice.color());
    }

    @Test
    void taskAndGoalHaveDistinctHeadingsAndNoDiscordMentions() {
        var task = DiscordAdvancementNotice.from("Player", node("TASK", "First Step", List.of("@everyone")));
        var goal = DiscordAdvancementNotice.from("Player", node("GOAL", "Milestone", List.of("Ready")));
        assertTrue(task.heading().contains("has made the advancement"));
        assertTrue(goal.heading().contains("has reached the goal"));
        assertFalse(task.description().contains("@"));
        assertEquals(0xFFAA00, task.color());
        assertEquals(0xFFAA00, goal.color());
    }

    @Test void diaryIsGoldAndShowsOnlyTheObjective() {
        var notice = DiscordAdvancementNotice.from("Player", node("TASK", "Dear Diary...",
            List.of("§7DiaryKeeper", "§7Requirements:", "§fReceive your personal diary for the first time.",
                "§7Progress is read from DiaryKeeper.", "§7Rewards: Claim with /rewards.")));
        assertEquals("Receive your personal diary for the first time.", notice.description());
        assertEquals(0xFFAA00, notice.color());
    }

    @Test void pillarIsDarkRedAndShowsOnlyTheObjective() {
        var notice = DiscordAdvancementNotice.from("Player", node("CHALLENGE", "Pillar of the Community",
            List.of("Enthusia Reputation", "Requirements:", "Reach +20 overall reputation at any point.",
                "Progress is read from EnthusiaCommend.", "Rewards: Claim with /rewards.")));
        assertEquals("Reach +20 overall reputation at any point.", notice.description());
        assertEquals(0xAA0000, notice.color());
    }

    @Test void nativeCardDoesNotRepeatRequirementsOrClaimDetails() {
        var notice = DiscordAdvancementNotice.from("Player", node("TASK", "Respawn Enjoyer",
            List.of("Die 250 times.", "Requirements:", "Deaths: 250", "Rewards:",
                "Tag: respawn_enjoyer", "Claim with /rewards. Completion is not payment.")));
        assertEquals("Die 250 times.", notice.description());
    }

    @Test void summaryIsBoundedAndEmptyInputStaysEmpty() {
        assertEquals("", DiscordAdvancementNotice.from("Player",
            node("TASK", "Empty", List.of())).description());
        var longNotice = DiscordAdvancementNotice.from("Player",
            node("TASK", "Long", List.of("x".repeat(500))));
        assertEquals(240, longNotice.description().length());
        assertTrue(longNotice.description().endsWith("…"));
    }

    @Test void embeddedNewlinesDoNotLeakRewardDetails() {
        var notice = DiscordAdvancementNotice.from("Player", node("TASK", "Example",
            List.of("Complete the objective.\nRewards:\n100 gold", "Tag: example")));
        assertEquals("Complete the objective.", notice.description());
    }

    private static ProjectionService.Node node(String frame, String title, List<String> description) {
        return new ProjectionService.Node("sample", null, title, description, Material.CLOCK, frame, 1, 0);
    }
}
