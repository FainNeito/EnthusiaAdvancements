package io.github.badgersmc.advancements.pilot;

import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class PrivateCelebrationTest extends RegistrationTestSupport {
    @Test void privateCompletionNeverUsesPublicCelebrationOrBroadcast() throws Exception {
        try (var privateNodes = mockConstruction(PrivateAdvancement.class)) {
            var f = fixture();
            var definition = new ProjectionService.Node("secret", null, "Secret", List.of(), Material.PAPER, "CHALLENGE", 1, 0);
            f.service().registerTreeWithPrivateNodes(f.owner(), "test", new ItemStack(Material.CLOCK), List.of(definition), Set.of("secret"));
            var node = privateNodes.constructed().getFirst();
            Player player = mock(Player.class);
            when(player.isOnline()).thenReturn(true);
            when(f.api().isLoaded(player)).thenReturn(true);
            var team = f.api().getTeamProgression(player);
            when(team.getSize()).thenReturn(1);
            when(node.isGranted(player)).thenReturn(true);
            when(node.isVisible(team)).thenReturn(false, true);
            f.service().celebrate(f.owner(), "test", player, "secret");
            f.service().celebratePrivate(f.owner(), "test", player, "secret");
            verify(node, never()).displayToastToPlayer(player);
            f.service().celebratePrivate(f.owner(), "test", player, "secret");
            verify(node, times(1)).displayToastToPlayer(player);
            bukkit.verify(Bukkit::getOnlinePlayers, never());
        }
    }
    @Test void privateSnapshotsRefreshTheTabWithoutWritingUaaProgress() throws Exception {
        try (var privateNodes = mockConstruction(PrivateAdvancement.class)) {
            var f = fixture();
            var definition = new ProjectionService.Node("secret", null, "Secret", List.of(), Material.PAPER, "CHALLENGE", 1, 0);
            var tab = mock(com.fren_gor.ultimateAdvancementAPI.AdvancementTab.class);
            when(f.api().createAdvancementTab("test")).thenReturn(tab);
            f.service().registerTreeWithPrivateNodes(f.owner(), "test", new ItemStack(Material.CLOCK), List.of(definition), Set.of("secret"));
            var node = privateNodes.constructed().getFirst();
            Player player = mock(Player.class); var id = java.util.UUID.randomUUID();
            when(player.getUniqueId()).thenReturn(id); when(player.isOnline()).thenReturn(true);
            when(f.api().isLoaded(player)).thenReturn(true);
            when(f.api().getTeamProgression(player).getSize()).thenReturn(1);
            f.service().project(f.owner(), "test", player, java.util.Map.of("secret", 1000));
            verify(node).authorize(id, true);
            verify(node, never()).setProgression(any(Player.class), anyInt(), anyBoolean());
            verify(tab).updateAdvancementsToTeam(player);
            f.service().project(f.owner(), "test", player, java.util.Map.of());
            verify(node, atLeastOnce()).authorize(id, false);
            verify(tab, times(2)).updateAdvancementsToTeam(player);
        }
    }

}
