package io.github.badgersmc.advancements.pilot;

import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Display-only API. Providers remain authoritative for progress and reward claims. */
public interface ProjectionService {
    record Node(
        String key,
        String parentKey,
        String title,
        List<String> description,
        Material icon,
        Integer customModelData,
        String itemModel,
        String frame,
        float x,
        float y
    ) {
        public Node(
            String key,
            String parentKey,
            String title,
            List<String> description,
            Material icon,
            String frame,
            float x,
            float y
        ) {
            this(
                key,
                parentKey,
                title,
                description,
                icon,
                null,
                null,
                frame,
                x,
                y
            );
        }

        public Node(
            String key,
            String parentKey,
            String title,
            List<String> description,
            Material icon,
            Integer customModelData,
            String frame,
            float x,
            float y
        ) {
            this(
                key,
                parentKey,
                title,
                description,
                icon,
                customModelData,
                null,
                frame,
                x,
                y
            );
        }

        public Node(
            String key,
            String parentKey,
            String title,
            List<String> description,
            Material icon,
            String itemModel,
            String frame,
            float x,
            float y
        ) {
            this(
                key,
                parentKey,
                title,
                description,
                icon,
                null,
                itemModel,
                frame,
                x,
                y
            );
        }

        public Node {
            if (
                key == null ||
                !key.matches("[a-z0-9/._-]+") ||
                "root".equals(key)
            ) throw new IllegalArgumentException(
                "Invalid or reserved advancement key: " + key
            );
            if (
                title == null || title.isBlank()
            ) throw new IllegalArgumentException("Missing title");
            description = List.copyOf(description);
            if (icon == null) icon = Material.CLOCK;
            if (
                customModelData != null && customModelData <= 0
            ) throw new IllegalArgumentException("Invalid custom model data");
            if (
                itemModel != null &&
                !itemModel.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
            ) throw new IllegalArgumentException(
                "Invalid item model: " + itemModel
            );
            if (
                !List.of("TASK", "GOAL", "CHALLENGE").contains(frame)
            ) throw new IllegalArgumentException("Invalid frame");
        }
    }

    void registerTree(
        Plugin owner,
        String namespace,
        ItemStack icon,
        List<Node> nodes
    );
    void removeTree(Plugin owner, String namespace);
    boolean ready(Player player);
    void project(
        Plugin owner,
        String namespace,
        Player player,
        Map<String, Integer> progress
    );
    void celebrate(Plugin owner, String namespace, Player player, String key);

    /** Legacy entry points reject ownerless writes rather than bypassing namespace ownership. */
    @Deprecated
    default void project(
        String namespace,
        Player player,
        Map<String, Integer> progress
    ) {
        throw new UnsupportedOperationException(
            "Projection requires the registered owner; update the provider plugin."
        );
    }

    @Deprecated
    default void celebrate(String namespace, Player player, String key) {
        throw new UnsupportedOperationException(
            "Celebration requires the registered owner; update the provider plugin."
        );
    }
}
