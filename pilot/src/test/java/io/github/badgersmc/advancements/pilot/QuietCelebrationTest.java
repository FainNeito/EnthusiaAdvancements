package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

/** REQ-PILOT-QUIET-CELEBRATION-001. */
class QuietCelebrationTest {
    private final PilotPlugin service = mock(PilotPlugin.class, CALLS_REAL_METHODS);
    private final Plugin owner = mock(Plugin.class);
    private final Player player = mock(Player.class);
    private final BaseAdvancement node = mock(BaseAdvancement.class);

    private void install() throws Exception {
        UltimateAdvancementAPI api = mock(UltimateAdvancementAPI.class, RETURNS_DEEP_STUBS);
        when(player.isOnline()).thenReturn(true);
        when(api.isLoaded(player)).thenReturn(true);
        when(api.getTeamProgression(player).getSize()).thenReturn(1);
        AdvancementDisplay display = mock(AdvancementDisplay.class);
        when(node.getDisplay()).thenReturn(display);
        when(node.isGranted(player)).thenReturn(true);
        set("api", api);
        Class<?> type = Arrays.stream(PilotPlugin.class.getDeclaredClasses())
            .filter(candidate -> candidate.getSimpleName().equals("Tree")).findFirst().orElseThrow();
        var constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object tree = constructor.newInstance(owner, mock(AdvancementTab.class), mock(RootAdvancement.class),
            Map.of("node", node), null);
        set("trees", new LinkedHashMap<>(Map.of("test", tree)));
        set("discord", mock(DiscordAdvancementAnnouncer.class));
    }

    private void set(String name, Object value) throws Exception {
        var field = PilotPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    private static Method quiet() throws NoSuchMethodException {
        return ProjectionService.class.getMethod(
            "celebrate", Plugin.class, String.class, Player.class, String.class, boolean.class);
    }

    @Test
    void quietCelebrationShowsToastButNoChatOrDiscord() throws Exception {
        install();
        try (var bukkit = mockStatic(Bukkit.class); var toast = mockStatic(LastWatchToastPolicy.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            quiet().invoke(service, owner, "test", player, "node", false);
            toast.verify(() -> LastWatchToastPolicy.show(any(), any(), any(), any()), times(1));
            bukkit.verify(Bukkit::getOnlinePlayers, never());
            verify(player, never()).getWorld();
        }
    }

    @Test
    void quietCelebrationKeepsOwnershipAndGrantedChecks() throws Exception {
        install();
        when(node.isGranted(player)).thenReturn(false);
        try (var bukkit = mockStatic(Bukkit.class); var toast = mockStatic(LastWatchToastPolicy.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            quiet().invoke(service, owner, "test", player, "node", false);
            toast.verify(() -> LastWatchToastPolicy.show(any(), any(), any(), any()), never());
        }
    }

    @Test
    void announcingVariantDelegatesToTheExistingCall() throws Exception {
        ProjectionService provider = mock(ProjectionService.class, CALLS_REAL_METHODS);
        assertDoesNotThrow(() -> quiet().invoke(provider, owner, "test", player, "node", true));
        verify(provider).celebrate(owner, "test", player, "node");
    }
}
