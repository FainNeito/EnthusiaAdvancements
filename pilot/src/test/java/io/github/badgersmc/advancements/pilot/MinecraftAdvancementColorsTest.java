package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

class MinecraftAdvancementColorsTest {
    @BeforeAll static void initializeDisplayAdapter() {
        UaaDisplayFixture.initialize();
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
}
