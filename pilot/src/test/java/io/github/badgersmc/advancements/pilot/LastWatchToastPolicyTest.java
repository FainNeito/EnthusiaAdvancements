package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.*;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class LastWatchToastPolicyTest {
    @BeforeAll static void initialize() { UaaDisplayFixture.initialize(); }

    @Test void onlyTheLastWatchChallengeUsesAQuietTaskToast() {
        assertEquals(AdvancementFrameType.TASK, LastWatchToastPolicy.toastFrame("holidays", "last_watch", AdvancementFrameType.CHALLENGE));
        assertEquals(AdvancementFrameType.CHALLENGE, LastWatchToastPolicy.toastFrame("holidays", "other", AdvancementFrameType.CHALLENGE));
        assertEquals(AdvancementFrameType.CHALLENGE, LastWatchToastPolicy.toastFrame("other", "last_watch", AdvancementFrameType.CHALLENGE));
        assertEquals(AdvancementFrameType.GOAL, LastWatchToastPolicy.toastFrame("holidays", "last_watch", AdvancementFrameType.GOAL));
        assertEquals(AdvancementFrameType.TASK, LastWatchToastPolicy.toastFrame("holidays", "last_watch", AdvancementFrameType.TASK));
        assertEquals(AdvancementFrameType.CHALLENGE, LastWatchToastPolicy.toastFrame("Holidays", "last_watch", AdvancementFrameType.CHALLENGE));
        assertEquals(AdvancementFrameType.CHALLENGE, LastWatchToastPolicy.toastFrame(null, "last_watch", AdvancementFrameType.CHALLENGE));
    }

    @Test void targetUsesCustomToastWithoutMutatingNodeAndOtherChallengeUsesOriginalToast() {
        var node = mock(BaseAdvancement.class);
        var display = mock(AdvancementDisplay.class);
        var player = mock(Player.class); var icon = mock(ItemStack.class);
        when(node.getDisplay()).thenReturn(display);
        when(display.getFrame()).thenReturn(AdvancementFrameType.CHALLENGE);
        when(display.getIcon()).thenReturn(icon);
        when(display.getTitle()).thenReturn("The Last Watch");
        var calls = new java.util.ArrayList<AdvancementFrameType>();
        LastWatchToastPolicy.show(node, player, (p, i, t, f) -> {
            assertSame(player, p); assertSame(icon, i); assertEquals("The Last Watch", t); calls.add(f);
        }, LastWatchToastPolicy.toastFrame("holidays", "last_watch", AdvancementFrameType.CHALLENGE));
        assertEquals(java.util.List.of(AdvancementFrameType.TASK), calls);
        verify(node, never()).displayToastToPlayer(any());
        verify(display).getFrame(); verify(display).getIcon(); verify(display).getTitle();
        verifyNoMoreInteractions(display);
        LastWatchToastPolicy.show(node, player, (p, i, t, f) -> fail("unexpected custom toast"),
            LastWatchToastPolicy.toastFrame("holidays", "other", AdvancementFrameType.CHALLENGE));
        verify(node).displayToastToPlayer(player);
    }
}
