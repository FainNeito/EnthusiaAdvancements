package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/** Deliberately excludes the full distribution's bundled trees and reward executors. */
public final class PilotPlugin extends JavaPlugin implements ProjectionService {

    private UltimateAdvancementAPI api;
    private final Map<String, Tree> trees = new LinkedHashMap<>();
    private DiscordAdvancementAnnouncer discord;

    private record Tree(
        Plugin owner,
        AdvancementTab tab,
        RootAdvancement root,
        Map<String, BaseAdvancement> nodes,
        Registration registration
    ) {}

    @Override
    public void onEnable() {
        saveDefaultConfig();
        api = UltimateAdvancementAPI.getInstance(this);
        if (getConfig().getBoolean("discord.enabled", false) &&
            getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            discord = new DiscordAdvancementAnnouncer(this);
        }
        getServer()
            .getServicesManager()
            .register(
                ProjectionService.class,
                this,
                this,
                ServicePriority.Normal
            );
        getLogger().info(
            "Projection-only pilot enabled. No default challenge trees or rewards are loaded."
        );
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        if (api != null) api.unregisterPluginAdvancementTabs();
        trees.clear();
        discord = null;
    }

    private void mainThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException(
            "Projection API requires the server thread"
        );
    }

    @Override
    public void registerTree(
        Plugin owner,
        String namespace,
        ItemStack icon,
        List<Node> definitions
    ) {
        mainThread();
        Objects.requireNonNull(owner, "Missing owner");
        Tree existing = trees.get(namespace);
        assertTreeOwner(existing, owner);
        Registration registration = Registration.validated(
            namespace,
            icon,
            definitions
        );
        replaceTree(owner, namespace, existing, registration);
    }

    private void replaceTree(
        Plugin owner,
        String namespace,
        Tree existing,
        Registration registration
    ) {
        replaceExistingTree(owner, namespace, existing);
        try {
            trees.put(
                namespace,
                createRegisteredTree(owner, namespace, registration)
            );
        } catch (RuntimeException ex) {
            restorePreviousTree(namespace, existing, ex);
            throw ex;
        }
    }

    private Tree createRegisteredTree(
        Plugin owner,
        String namespace,
        Registration registration
    ) {
        AdvancementTab tab = api.createAdvancementTab(namespace);
        try {
            return registerDefinitions(owner, registration, tab);
        } catch (RuntimeException failure) {
            try {
                api.unregisterAdvancementTab(namespace);
            } catch (RuntimeException cleanupFailure) {
                if (cleanupFailure != failure) failure.addSuppressed(
                    cleanupFailure
                );
            }
            throw failure;
        }
    }

    private void restorePreviousTree(
        String namespace,
        Tree existing,
        RuntimeException failure
    ) {
        if (existing == null) return;
        try {
            // Unregistered UAA tabs are disposed; reconstruct, never reuse the old tab.
            trees.put(
                namespace,
                createRegisteredTree(
                    existing.owner(),
                    namespace,
                    existing.registration()
                )
            );
        } catch (RuntimeException recoveryFailure) {
            if (recoveryFailure != failure) failure.addSuppressed(
                recoveryFailure
            );
        }
    }

    private static void assertTreeOwner(Tree existing, Plugin owner) {
        if (existing != null && existing.owner() != owner) {
            throw new IllegalStateException("Namespace already owned");
        }
    }

    private void replaceExistingTree(
        Plugin owner,
        String namespace,
        Tree existing
    ) {
        if (existing != null) removeTree(owner, namespace);
    }

    private static Tree registerDefinitions(
        Plugin owner,
        Registration registration,
        AdvancementTab tab
    ) {
        RootAdvancement root = rootAdvancement(tab, registration.icon());
        Map<String, BaseAdvancement> nodes = new LinkedHashMap<>();
        for (Node definition : registration.definitions()) {
            nodes.put(definition.key(), createNode(definition, root, nodes));
        }
        tab.registerAdvancements(root, new HashSet<>(nodes.values()));
        return new Tree(owner, tab, root, nodes, registration);
    }

    private static RootAdvancement rootAdvancement(
        AdvancementTab tab,
        ItemStack icon
    ) {
        // Progress sync never emits toasts or chat; live celebration is explicit.
        return new RootAdvancement(
            tab,
            "root",
            rootDisplay(icon),
            "minecraft:textures/block/stone.png"
        );
    }

    static AdvancementDisplay rootDisplay(ItemStack icon) {
        return PilotDisplays.rootDisplay(icon);
    }

    private static BaseAdvancement createNode(
        Node definition,
        RootAdvancement root,
        Map<String, BaseAdvancement> nodes
    ) {
        Advancement parent =
            definition.parentKey() == null
                ? root
                : nodes.get(definition.parentKey());
        return new BaseAdvancement(
            definition.key(),
            nodeDisplay(definition),
            parent,
            100
        );
    }

    static AdvancementDisplay nodeDisplay(Node definition) {
        return PilotDisplays.nodeDisplay(definition);
    }

    @Override
    public void removeTree(Plugin owner, String namespace) {
        mainThread();
        Tree tree = trees.get(namespace);
        if (tree != null && tree.owner() == owner) {
            api.unregisterAdvancementTab(namespace);
            trees.remove(namespace);
        }
    }

    @Override
    public boolean ready(Player player) {
        // Never project one player's personal completion into a shared UAA team.
        return (
            player.isOnline() &&
            api.isLoaded(player) &&
            api.getTeamProgression(player).getSize() == 1
        );
    }

    @Override
    public void project(
        Plugin owner,
        String namespace,
        Player player,
        Map<String, Integer> progress
    ) {
        mainThread();
        Tree tree = ownedTree(owner, namespace);
        if (tree == null) return;
        // Snapshot and validate every entry before granting the root, showing a tab, or changing nodes.
        Map<String, Integer> checked = ProjectionChecks.checkedProgress(
            progress
        );
        if (!ready(player)) return;
        showTree(tree, player);
        for (var entry : checked.entrySet()) {
            projectNode(tree, player, entry.getKey(), entry.getValue());
        }
    }

    private static void showTree(Tree tree, Player player) {
        if (!tree.root().isGranted(player)) tree.root().grant(player, false);
        if (!tree.tab().isShownTo(player)) tree.tab().showTab(player);
    }

    private static void projectNode(
        Tree tree,
        Player player,
        String key,
        int value
    ) {
        BaseAdvancement node = tree.nodes().get(key);
        if (node == null || value < 0 || value > 1000) return;
        int clientValue = clientProgress(value);
        if (node.getProgression(player) != clientValue) {
            node.setProgression(player, clientValue, false);
        }
    }

    private Tree ownedTree(Plugin owner, String namespace) {
        if (owner == null) throw new IllegalArgumentException(
            "Missing projection owner"
        );
        Tree tree = trees.get(namespace);
        if (
            tree != null && tree.owner() != owner
        ) throw new IllegalArgumentException(
            "Namespace is owned by a different provider: " + namespace
        );
        return tree;
    }

    static Map<String, Integer> checkedProgress(Map<String, Integer> progress) {
        return ProjectionChecks.checkedProgress(progress);
    }

    static int clientProgress(int verifiedValue) {
        return ProjectionChecks.clientProgress(verifiedValue);
    }

    @Override
    public void celebrate(
        Plugin owner,
        String namespace,
        Player player,
        String key
    ) {
        mainThread();
        Tree tree = ownedTree(owner, namespace);
        if (tree == null || !ready(player)) return;
        BaseAdvancement node = tree.nodes().get(key);
        if (node == null || !node.isGranted(player)) return;
        node.displayToastToPlayer(player);
        if (
            Boolean.FALSE.equals(
                player
                    .getWorld()
                    .getGameRuleValue(
                        com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.SHOW_ADVANCEMENT_MESSAGES_GAMERULE
                    )
            )
        ) return;
        var message = MinecraftAdvancementColors.format(
            node.getAnnounceMessage(player), node.getDisplay().getFrame()
        );
        if (message != null) for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.canSee(player)) viewer.spigot().sendMessage(message);
        }
        if (discord != null && message != null) {
            tree.registration().definitions().stream()
                .filter(definition -> definition.key().equals(key))
                .findFirst()
                .ifPresent(definition -> discord.announce(player, namespace, definition));
        }
    }
}
