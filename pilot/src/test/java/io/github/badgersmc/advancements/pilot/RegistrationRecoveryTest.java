package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegistrationRecoveryTest {

    @BeforeAll
    static void initializeDisplayAdapter() {
        UaaDisplayFixture.initialize();
    }

    private record Fixture(
        PilotPlugin service,
        Plugin owner,
        UltimateAdvancementAPI api
    ) {}

    private static Fixture fixture() throws Exception {
        PilotPlugin service = mock(PilotPlugin.class, CALLS_REAL_METHODS);
        UltimateAdvancementAPI api = mock(
            UltimateAdvancementAPI.class,
            RETURNS_DEEP_STUBS
        );
        var apiField = PilotPlugin.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(service, api);
        var trees = PilotPlugin.class.getDeclaredField("trees");
        trees.setAccessible(true);
        trees.set(service, new LinkedHashMap<>());
        return new Fixture(service, mock(Plugin.class), api);
    }

    private static List<ProjectionService.Node> nodes(String key) {
        return List.of(
            new ProjectionService.Node(
                key,
                null,
                key,
                List.of(),
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
    }

    private static void register(Fixture f, String key) {
        f.service().registerTree(
            f.owner(),
            "test",
            new ItemStack(Material.CLOCK),
            nodes(key)
        );
    }

    private static void project(Fixture f, String key) {
        Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);
        when(f.api().isLoaded(player)).thenReturn(true);
        when(f.api().getTeamProgression(player).getSize()).thenReturn(1);
        f.service().project(f.owner(), "test", player, Map.of(key, 500));
    }

    @Test
    void creationFailureRebuildsPreviousTree() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("creation failed");
        var original = mock(AdvancementTab.class);
        var restored = mock(AdvancementTab.class);
        when(f.api().createAdvancementTab("test"))
            .thenReturn(original)
            .thenThrow(failure)
            .thenReturn(restored);
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(RootAdvancement.class);
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            register(f, "old");
            assertSame(
                failure,
                assertThrows(IllegalStateException.class, () ->
                    register(f, "new")
                )
            );
            verify(f.api(), times(3)).createAdvancementTab("test");
            verify(restored).registerAdvancements(
                any(RootAdvancement.class),
                anySet()
            );
            project(f, "old");
            verify(children.constructed().getLast()).setProgression(
                any(Player.class),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(false)
            );
        }
    }

    @Test
    void registrationFailureDisposesNewTabAndRebuildsPreviousTree()
        throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("registration failed");
        var original = mock(AdvancementTab.class);
        var replacement = mock(AdvancementTab.class);
        var restored = mock(AdvancementTab.class);
        when(f.api().createAdvancementTab("test")).thenReturn(
            original,
            replacement,
            restored
        );
        doThrow(failure)
            .when(replacement)
            .registerAdvancements(any(RootAdvancement.class), anySet());
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(RootAdvancement.class);
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            register(f, "old");
            assertSame(
                failure,
                assertThrows(IllegalStateException.class, () ->
                    register(f, "new")
                )
            );
            verify(f.api(), times(2)).unregisterAdvancementTab("test");
            verify(restored).registerAdvancements(
                any(RootAdvancement.class),
                anySet()
            );
            project(f, "old");
            verify(children.constructed().getLast()).setProgression(
                any(Player.class),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(false)
            );
        }
    }

    @Test
    void recoveryFailureIsSuppressedAndLeavesNoDisposedTreeRegistered()
        throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("replacement failed");
        var recovery = new IllegalStateException("recovery failed");
        when(f.api().createAdvancementTab("test"))
            .thenReturn(mock(AdvancementTab.class))
            .thenThrow(failure)
            .thenThrow(recovery);
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(RootAdvancement.class);
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            register(f, "old");
            assertSame(
                failure,
                assertThrows(IllegalStateException.class, () ->
                    register(f, "new")
                )
            );
            assertSame(recovery, failure.getSuppressed()[0]);
            project(f, "old");
            verifyNoInteractions(
                roots.constructed().getFirst(),
                children.constructed().getFirst()
            );
        }
    }

    @Test
    void invalidDisplayIsRejectedBeforeRemovingExistingTree() throws Exception {
        var f = fixture();
        when(f.api().createAdvancementTab("test")).thenReturn(
            mock(AdvancementTab.class)
        );
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(RootAdvancement.class);
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            register(f, "old");
            var invalid = new ProjectionService.Node(
                "bad",
                null,
                "bad",
                List.of(),
                Material.CLOCK,
                "TASK",
                Float.NaN,
                0
            );
            assertThrows(IllegalArgumentException.class, () ->
                f
                    .service()
                    .registerTree(
                        f.owner(),
                        "test",
                        new ItemStack(Material.CLOCK),
                        List.of(invalid)
                    )
            );
            verify(f.api(), never()).unregisterAdvancementTab("test");
            project(f, "old");
            verify(children.constructed().getFirst()).setProgression(
                any(Player.class),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(false)
            );
        }
    }

    private static ItemStack icon(int amount) {
        var item = mock(ItemStack.class);
        when(item.getAmount()).thenReturn(amount);
        when(item.clone()).thenAnswer(invocation -> icon(item.getAmount()));
        return item;
    }

    @Test
    void recoveryUsesSnapshotsInsteadOfMutatedProviderInputs()
        throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("replacement failed");
        when(f.api().createAdvancementTab("test"))
            .thenReturn(mock(AdvancementTab.class))
            .thenThrow(failure)
            .thenReturn(mock(AdvancementTab.class));
        var sourceIcon = icon(1);
        var sourceDefinitions = new ArrayList<>(nodes("old"));
        var restoredAmounts = new ArrayList<Integer>();
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(
                RootAdvancement.class,
                (root, context) ->
                    restoredAmounts.add(
                        ((AdvancementDisplay) context.arguments().get(2))
                            .getIcon()
                            .getAmount()
                    )
            );
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            f.service().registerTree(
                f.owner(),
                "test",
                sourceIcon,
                sourceDefinitions
            );
            sourceDefinitions.clear();
            when(sourceIcon.getAmount()).thenReturn(64);
            assertSame(
                failure,
                assertThrows(IllegalStateException.class, () ->
                    register(f, "new")
                )
            );
            assertEquals(List.of(1, 1), restoredAmounts);
            project(f, "old");
            verify(children.constructed().getLast()).setProgression(
                any(Player.class),
                org.mockito.ArgumentMatchers.eq(50),
                org.mockito.ArgumentMatchers.eq(false)
            );
        }
    }

    @Test
    void firstRegistrationFailureDoesNotInventPreviousTree() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("registration failed");
        var tab = mock(AdvancementTab.class);
        when(f.api().createAdvancementTab("test")).thenReturn(tab);
        doThrow(failure)
            .when(tab)
            .registerAdvancements(any(RootAdvancement.class), anySet());
        try (
            var bukkit = mockStatic(Bukkit.class);
            var items = mockConstruction(ItemStack.class, (item, context) ->
                when(item.clone()).thenReturn(item)
            );
            var roots = mockConstruction(RootAdvancement.class);
            var children = mockConstruction(BaseAdvancement.class)
        ) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            assertSame(
                failure,
                assertThrows(IllegalStateException.class, () ->
                    register(f, "new")
                )
            );
            verify(f.api()).createAdvancementTab("test");
            verify(f.api()).unregisterAdvancementTab("test");
            project(f, "new");
            verifyNoInteractions(
                roots.constructed().getFirst(),
                children.constructed().getFirst()
            );
        }
    }
}
