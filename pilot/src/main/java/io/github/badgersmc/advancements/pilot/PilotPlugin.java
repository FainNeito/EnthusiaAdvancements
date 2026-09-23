package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/** Deliberately excludes the full distribution's bundled trees and reward executors. */
public final class PilotPlugin extends JavaPlugin implements ProjectionService {

    private UltimateAdvancementAPI api;
    private final Map<String, Tree> trees = new LinkedHashMap<>();

    private record Tree(
        Plugin owner,
        AdvancementTab tab,
        RootAdvancement root,
        Map<String, BaseAdvancement> nodes
    ) {}

    @Override
    public void onEnable() {
        api = UltimateAdvancementAPI.getInstance(this);
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
        if (existing != null && existing.owner() != owner) {
            throw new IllegalStateException("Namespace already owned");
        }
        validateDefinitions(definitions);
        if (existing != null) removeTree(owner, namespace);
        AdvancementTab tab = api.createAdvancementTab(namespace);
        try {
            RootAdvancement root = rootAdvancement(tab, icon);
            Map<String, BaseAdvancement> nodes = new LinkedHashMap<>();
            for (Node definition : definitions) {
                nodes.put(
                    definition.key(),
                    createNode(definition, root, nodes)
                );
            }
            tab.registerAdvancements(root, new HashSet<>(nodes.values()));
            trees.put(namespace, new Tree(owner, tab, root, nodes));
        } catch (RuntimeException ex) {
            api.unregisterAdvancementTab(namespace);
            throw ex;
        }
    }

    private static void validateDefinitions(List<Node> definitions) {
        // Validate before replacing a visible tree.
        var keys = new HashSet<String>();
        for (Node definition : definitions) {
            if (!keys.add(definition.key())) {
                throw new IllegalArgumentException(
                    "Duplicate key: " + definition.key()
                );
            }
            if (
                definition.parentKey() != null &&
                !keys.contains(definition.parentKey())
            ) {
                throw new IllegalArgumentException(
                    "Parent must precede child: " + definition.key()
                );
            }
        }
    }

    private static RootAdvancement rootAdvancement(
        AdvancementTab tab,
        ItemStack icon
    ) {
        // Progress sync never emits toasts or chat; live celebration is explicit.
        return new RootAdvancement(
            tab,
            "root",
            new AdvancementDisplay(
                icon,
                "Enthusia",
                AdvancementFrameType.TASK,
                false,
                false,
                0,
                0,
                List.of(
                    "Your Enthusia challenges",
                    "View requirements and rewards here.",
                    "Claim earned rewards with /rewards."
                )
            ),
            "minecraft:textures/block/stone.png"
        );
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
            new AdvancementDisplay(
                createIcon(definition),
                definition.title(),
                AdvancementFrameType.valueOf(definition.frame()),
                false,
                false,
                definition.x(),
                definition.y(),
                definition.description()
            ),
            parent,
            100
        );
    }

    private static ItemStack createIcon(Node definition) {
        ItemStack icon = new ItemStack(definition.icon());
        if (
            definition.customModelData() == null &&
            definition.itemModel() == null
        ) {
            return icon;
        }
        var meta = icon.getItemMeta();
        if (definition.customModelData() != null) {
            meta.setCustomModelData(definition.customModelData());
        }
        if (definition.itemModel() != null) {
            NamespacedKey itemModel = NamespacedKey.fromString(
                definition.itemModel()
            );
            if (itemModel == null) {
                throw new IllegalArgumentException(
                    "Invalid item model: " + definition.itemModel()
                );
            }
            meta.setItemModel(itemModel);
        }
        icon.setItemMeta(meta);
        return icon;
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
        Map<String, Integer> checked = checkedProgress(progress);
        if (!ready(player)) return;
        if (!tree.root().isGranted(player)) tree.root().grant(player, false);
        if (!tree.tab().isShownTo(player)) tree.tab().showTab(player);
        for (var entry : checked.entrySet()) {
            projectNode(tree, player, entry.getKey(), entry.getValue());
        }
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
        if (progress == null) throw new IllegalArgumentException(
            "Missing progress map"
        );
        Map<String, Integer> checked = new LinkedHashMap<>();
        for (var entry : progress.entrySet()) {
            if (
                entry.getKey() == null || entry.getValue() == null
            ) throw new IllegalArgumentException(
                "Progress keys and values must not be null"
            );
            checked.put(entry.getKey(), entry.getValue());
        }
        // Unknown keys and unavailable/out-of-range values keep their existing skip behavior.
        return Map.copyOf(checked);
    }

    static int clientProgress(int verifiedValue) {
        if (
            verifiedValue < 0 || verifiedValue > 1000
        ) throw new IllegalArgumentException("Invalid progress");
        return verifiedValue == 1000 ? 100 : Math.min(99, verifiedValue / 10);
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
        var message = node.getAnnounceMessage(player);
        if (message != null) for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.canSee(player)) viewer.spigot().sendMessage(message);
        }
    }
}
