package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class PrivateProjectionTest {
    @org.junit.jupiter.api.BeforeAll static void initialize() throws Exception {
        UaaDisplayFixture.initialize();
        try (var reflection = mockStatic(com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil.class)) {
            reflection.when(() -> com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil.getWrapperClass(AdvancementWrapper.class))
                .thenReturn(TestWrapper.class);
            Class.forName(AdvancementWrapper.class.getName(), true, AdvancementWrapper.class.getClassLoader());
        }
    }
    public static final class TestWrapper extends AdvancementWrapper {
        public TestWrapper(com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper k,
                com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper d, int max) {}
        public TestWrapper(com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper k,
                AdvancementWrapper p, com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper d, int max) {}
        public com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper getKey() { return null; }
        public AdvancementWrapper getParent() { return null; }
        public com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper getDisplay() { return null; }
        public int getMaxProgression() { return 100; }
        public Object toNMS() { return null; }
    }
    @Test void privateKeysMustExistAndReferToLeaves() {
        var hidden = new ProjectionService.Node("secret", null, "Secret", List.of(), Material.PAPER, "CHALLENGE", 1, 0);
        var child = new ProjectionService.Node("child", "secret", "Child", List.of(), Material.PAPER, "TASK", 2, 0);
        assertDoesNotThrow(() -> ProjectionChecks.validatePrivateNodes(List.of(hidden), java.util.Set.of("secret")));
        assertThrows(IllegalArgumentException.class, () -> ProjectionChecks.validatePrivateNodes(List.of(hidden, child), java.util.Set.of("secret")));
        assertThrows(IllegalArgumentException.class, () -> ProjectionChecks.validatePrivateNodes(List.of(hidden), java.util.Set.of("unknown")));
    }

    @Test void preservesAllPreviousNodeConstructorDescriptors() throws Exception {
        var node = ProjectionService.Node.class;
        assertNotNull(node.getConstructor(String.class, String.class, String.class, List.class, Material.class, String.class, float.class, float.class));
        assertNotNull(node.getConstructor(String.class, String.class, String.class, List.class, Material.class, Integer.class, String.class, float.class, float.class));
        assertNotNull(node.getConstructor(String.class, String.class, String.class, List.class, Material.class, String.class, String.class, float.class, float.class));
        assertNotNull(node.getConstructor(String.class, String.class, String.class, List.class, Material.class, Integer.class, String.class, String.class, float.class, float.class));
    }

    @Test void stalePacketEntriesAreRemovedForUnapprovedOrSharedTeamsAndErrorsClearTheMap() throws Exception {
        PrivateAdvancement secret = mock(PrivateAdvancement.class, CALLS_REAL_METHODS);
        var field = PrivateAdvancement.class.getDeclaredField("eligible");
        field.setAccessible(true);
        field.set(secret, ConcurrentHashMap.<UUID>newKeySet());
        var wrapper = mock(AdvancementWrapper.class);
        var other = mock(AdvancementWrapper.class);
        doReturn(wrapper).when(secret).getNMSWrapper();
        var team = mock(TeamProgression.class);
        var player = UUID.randomUUID();
        when(team.getSize()).thenReturn(1);
        when(team.getAMember()).thenReturn(player);
        doReturn(100).when(secret).getProgression(team);
        Map<AdvancementWrapper, Integer> packet = new HashMap<>();
        packet.put(wrapper, 100); packet.put(other, 42);
        secret.onUpdate(team, packet);
        assertFalse(packet.containsKey(wrapper));
        assertEquals(42, packet.get(other));
        secret.authorize(player, true);
        secret.onUpdate(team, packet);
        assertEquals(100, packet.get(wrapper));
        when(team.getSize()).thenReturn(2);
        secret.onUpdate(team, packet);
        assertFalse(packet.containsKey(wrapper));
        when(team.getSize()).thenReturn(1);
        doThrow(new IllegalStateException("visibility failure")).when(secret).getProgression(team);
        packet.put(wrapper, 100);
        secret.onUpdate(team, packet);
        assertFalse(packet.containsKey(wrapper));
        doThrow(new IllegalStateException("wrapper failure")).when(secret).getNMSWrapper();
        packet.put(wrapper, 100);
        secret.onUpdate(team, packet);
        assertTrue(packet.isEmpty());
    }

    @Test void authoritativeSnapshotsAuthorizeOnlyCompleteAndRevokeStaleState() throws Exception {
        PrivateAdvancement secret = mock(PrivateAdvancement.class, CALLS_REAL_METHODS);
        var field = PrivateAdvancement.class.getDeclaredField("eligible");
        field.setAccessible(true); field.set(secret, ConcurrentHashMap.<UUID>newKeySet());
        var player = mock(org.bukkit.entity.Player.class);
        var id = UUID.randomUUID(); when(player.getUniqueId()).thenReturn(id);
        var team = mock(TeamProgression.class); when(team.getSize()).thenReturn(1); when(team.getAMember()).thenReturn(id);
        doReturn(100).when(secret).getProgression(team);
        doReturn(0).when(secret).getProgression(player);
        doNothing().when(secret).setProgression(any(org.bukkit.entity.Player.class), anyInt(), eq(false));
        Map<String, com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement> nodes = Map.of("secret", secret);
        PilotPlugin.projectPrivate(nodes, player, Map.of("secret", 999));
        assertFalse(secret.isVisible(team));
        PilotPlugin.projectPrivate(nodes, player, Map.of("secret", 1000));
        assertTrue(secret.isVisible(team));
        PilotPlugin.revokePrivate(nodes, id);
        assertFalse(secret.isVisible(team));
        PilotPlugin.projectPrivate(nodes, player, Map.of());
        assertFalse(secret.isVisible(team));
        PilotPlugin.projectPrivate(nodes, player, Map.of("secret", 1001));
        assertFalse(secret.isVisible(team));
        verify(secret, atLeastOnce()).setProgression(player, 100, false);
    }

}
