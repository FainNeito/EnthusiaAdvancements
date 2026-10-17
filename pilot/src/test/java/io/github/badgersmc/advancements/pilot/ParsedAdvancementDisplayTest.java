package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.List;
import io.github.badgersmc.advancements.pilot.ProjectionService.Node;
import org.bukkit.Material;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParsedAdvancementDisplayTest {
    @BeforeAll static void initialize() { UaaDisplayFixture.initialize(); }
    private ItemStack icon() {
        ItemStack icon = mock(ItemStack.class);
        when(icon.clone()).thenReturn(icon);
        return icon;
    }
    @Test void upstreamHoverReproducesRawSectionCodeText() {
        ItemStack icon = icon();
        var upstream = new AdvancementDisplay(icon, "§x§f§5§f§5§f§5Seen the Watcher",
            AdvancementFrameType.CHALLENGE, false, false, 0, 0, List.of("It has seen you too."));
        assertTrue(BaseComponent.toPlainText(upstream.getChatDescription()).contains("§"),
            "Reproduce UAA's raw legacy-code hover text before applying the adapter");
    }
    @Test void rgbTitleAndDescriptionAreParsedForChatAndHover() {
        var node = new Node("watch", null, "§x§f§5§f§5§f§5Seen the Watcher",
            List.of("§x§9§a§a§0§a§6It has seen you too."), Material.ENDER_EYE, "CHALLENGE", 0, 0);
        var display = new ParsedAdvancementDisplay(icon(), node, AdvancementFrameType.CHALLENGE);
        var title = display.getChatTitle();
        var hover = display.getChatDescription();
        assertEquals(1, title.length);
        assertEquals(1, hover.length);
        assertEquals("Seen the Watcher", BaseComponent.toPlainText(title));
        assertEquals("Seen the Watcher\nIt has seen you too.", BaseComponent.toPlainText(hover));
        assertFalse(BaseComponent.toPlainText(hover).contains("§"));
        assertTrue(title[0].toLegacyText().contains("§x§f§5§f§5§f§5"));
        assertTrue(hover[0].toLegacyText().contains("§x§9§a§a§0§a§6"));
        ((TextComponent) title[0]).setText("changed");
        assertEquals("Seen the Watcher", BaseComponent.toPlainText(display.getChatTitle()));
    }
}
