package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.github.badgersmc.advancements.pilot.ProjectionService.Node;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinecraftAdvancementColorsTest {
    @BeforeAll static void initializeDisplayAdapter() {
        UaaDisplayFixture.initialize();
    }

    private BaseComponent[] realAnnouncement(String title, AdvancementFrameType frame) throws Exception {
        ItemStack icon = mock(ItemStack.class);
        when(icon.clone()).thenReturn(icon);
        Node node = new Node("test", null, title, List.of("§bHover remains aqua"),
            Material.PAPER, frame.name(), 0, 0);
        var display = new ParsedAdvancementDisplay(icon, node, frame);
        Advancement advancement = mock(Advancement.class, CALLS_REAL_METHODS);
        Field field = Advancement.class.getDeclaredField("display");
        field.setAccessible(true);
        field.set(advancement, display);
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Player");
        return advancement.getAnnounceMessage(player);
    }

    @Test void realProviderMessageOverridesExplicitTitleColorsForTasksAndGoals() throws Exception {
        for (var frame : new AdvancementFrameType[]{AdvancementFrameType.TASK, AdvancementFrameType.GOAL}) {
            BaseComponent[] original = realAnnouncement("§bTrail §dStarter", frame);
            BaseComponent[] result = MinecraftAdvancementColors.format(original, frame,
                new MinecraftAdvancementColors.Announcement("enthusia", "tags/test", "§bTrail §dStarter"));
            assertEquals(ChatColor.WHITE, result[0].getColor());
            assertEquals(ChatColor.GOLD, result[1].getColor());
            assertTrue(result[2].getExtra().stream().allMatch(part -> ChatColor.GOLD.equals(part.getColor())));
            assertEquals(ChatColor.GOLD, result[3].getColor());
            assertEquals(BaseComponent.toPlainText(original), BaseComponent.toPlainText(result));
            assertEquals(original[1].getHoverEvent(), result[1].getHoverEvent());
            assertTrue(original[2].toLegacyText().contains("§b"));
        }
    }

    @Test void realChallengeOverridesRgbAndLegacyTitleColors() throws Exception {
        BaseComponent[] original = realAnnouncement("§x§1§2§3§4§5§6Pillar §aCommunity", AdvancementFrameType.CHALLENGE);
        BaseComponent[] result = MinecraftAdvancementColors.format(original, AdvancementFrameType.CHALLENGE,
            new MinecraftAdvancementColors.Announcement("enthusia", "reputation/pillar", "title"));
        assertEquals(ChatColor.DARK_RED, result[1].getColor());
        assertTrue(result[2].getExtra().stream().allMatch(part -> ChatColor.DARK_RED.equals(part.getColor())));
        assertEquals(ChatColor.DARK_RED, result[3].getColor());
        assertEquals(ChatColor.WHITE, result[0].getColor());
    }

    @Test void realHolidayTitleAndWatcherColorsRemainAsBefore() throws Exception {
        for (String key : new String[]{"pumpkin_king", "seen_the_watcher"}) {
            BaseComponent[] original = realAnnouncement("§x§a§7§a§d§b§4Holiday §dName", AdvancementFrameType.CHALLENGE);
            BaseComponent[] result = MinecraftAdvancementColors.format(original, AdvancementFrameType.CHALLENGE,
                new MinecraftAdvancementColors.Announcement("holidays", key, "§x§a§7§a§d§b§4Holiday"));
            assertEquals(original[2].toLegacyText(), result[2].toLegacyText());
            assertEquals(original[1].getHoverEvent(), result[1].getHoverEvent());
        }
    }

    @Test void tasksAndGoalsAreGold() {
        for (var frame : new AdvancementFrameType[]{AdvancementFrameType.TASK, AdvancementFrameType.GOAL}) {
            TextComponent title = new TextComponent("[Dear Diary...]");
            title.setColor(ChatColor.GREEN);
            assertEquals(ChatColor.GOLD, MinecraftAdvancementColors.format(new BaseComponent[]{title}, frame)[0].getColor());
            assertEquals(ChatColor.GREEN, title.getColor());
        }
    }

    @Test void challengesAreDarkRedWithNestedTextAndEventsPreserved() {
        TextComponent title = new TextComponent("[");
        title.setColor(ChatColor.DARK_PURPLE);
        TextComponent nested = new TextComponent("Pillar of the community]");
        nested.setColor(ChatColor.DARK_PURPLE);
        nested.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/rewards"));
        title.addExtra(nested);
        BaseComponent result = MinecraftAdvancementColors.format(new BaseComponent[]{title}, AdvancementFrameType.CHALLENGE)[0];
        assertEquals(ChatColor.DARK_RED, result.getColor());
        assertEquals(ChatColor.DARK_RED, result.getExtra().getFirst().getColor());
        assertEquals(nested.getClickEvent(), result.getExtra().getFirst().getClickEvent());
        assertEquals(title.toPlainText(), result.toPlainText());
        assertEquals(ChatColor.DARK_PURPLE, nested.getColor());
    }

    @Test void neutralTextAndUnrelatedColorsStayUnchanged() {
        TextComponent message = new TextComponent("Player has made the advancement ");
        message.setColor(ChatColor.WHITE);
        TextComponent extra = new TextComponent("custom");
        extra.setColor(ChatColor.AQUA);
        message.addExtra(extra);
        BaseComponent result = MinecraftAdvancementColors.format(new BaseComponent[]{message}, AdvancementFrameType.TASK)[0];
        assertEquals(ChatColor.WHITE, result.getColor());
        assertEquals(ChatColor.AQUA, result.getExtra().getFirst().getColor());
        assertNull(MinecraftAdvancementColors.format(null, AdvancementFrameType.TASK));
    }

    @Test void watcherBracketsFollowConfiguredTitleWithoutChangingOtherAdvancements() {
        String title = "§x§a§7§a§d§b§4Seen the Watcher";
        for (String key : new String[]{"seen_the_watcher", "dont_blink", "forgotten_melody"}) {
            for (var frame : new AdvancementFrameType[]{AdvancementFrameType.GOAL, AdvancementFrameType.CHALLENGE}) {
                assertWatcherBrackets(key, frame, title);
            }
        }
    }

    @Test void otherAdvancementsAndMissingTitlesKeepFrameMapping() {
        String title = "Watcher";
        TextComponent ordinary = new TextComponent("[Pumpkin King]");
        ordinary.setColor(AdvancementFrameType.CHALLENGE.getColor());
        assertEquals(ChatColor.DARK_RED, MinecraftAdvancementColors.format(new BaseComponent[]{ordinary}, AdvancementFrameType.CHALLENGE, new MinecraftAdvancementColors.Announcement("holidays", "pumpkin_king", title))[0].getColor());
        assertEquals(ChatColor.DARK_RED, MinecraftAdvancementColors.format(new BaseComponent[]{ordinary}, AdvancementFrameType.CHALLENGE, new MinecraftAdvancementColors.Announcement("other", "seen_the_watcher", title))[0].getColor());
        assertEquals(ChatColor.DARK_RED, MinecraftAdvancementColors.format(new BaseComponent[]{ordinary}, AdvancementFrameType.CHALLENGE, new MinecraftAdvancementColors.Announcement("holidays", "seen_the_watcher", null))[0].getColor());
    }

    private void assertWatcherBrackets(String key, AdvancementFrameType frame, String title) {
        TextComponent bracket = new TextComponent("[");
        bracket.setColor(frame.getColor());
        bracket.addExtra(TextComponent.fromLegacyText(title)[0]);
        TextComponent closing = new TextComponent("]");
        closing.setColor(frame.getColor());
        closing.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/rewards"));
        bracket.addExtra(closing);
        var result = MinecraftAdvancementColors.format(new BaseComponent[]{bracket}, frame, new MinecraftAdvancementColors.Announcement("holidays", key, title))[0];
        assertEquals(ChatColor.of("#A7ADB4").getColor(), result.getColor().getColor());
        assertEquals(ChatColor.of("#A7ADB4").getColor(), result.getExtra().getLast().getColor().getColor());
        assertEquals(ChatColor.of("#A7ADB4").getColor(), result.getExtra().getFirst().getColor().getColor());
        assertEquals("[Seen the Watcher]", result.toPlainText());
        assertEquals(closing.getClickEvent(), result.getExtra().getLast().getClickEvent());
        assertEquals(frame.getColor(), bracket.getColor());
    }
}
