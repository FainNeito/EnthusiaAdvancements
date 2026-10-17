package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import io.github.badgersmc.advancements.pilot.ProjectionService.Node;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Constructs notification-silent displays and their resource-pack icons. */
final class PilotDisplays {

    private PilotDisplays() {}

    static AdvancementDisplay rootDisplay(ItemStack icon) {
        return new AdvancementDisplay(
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
        );
    }

    static AdvancementDisplay nodeDisplay(Node definition) {
        AdvancementFrameType frame = switch (definition.frame()) {
            case "TASK" -> AdvancementFrameType.TASK;
            case "GOAL" -> AdvancementFrameType.GOAL;
            case "CHALLENGE" -> AdvancementFrameType.CHALLENGE;
            default -> throw new IllegalArgumentException("Invalid frame");
        };
        return new AdvancementDisplay(
            createIcon(definition),
            definition.title(),
            frame,
            false,
            false,
            definition.x(),
            definition.y(),
            definition.description()
        );
    }

    static ItemStack createIcon(Node definition) {
        ItemStack icon = new ItemStack(definition.icon());
        if (
            definition.customModelData() == null &&
            definition.itemModel() == null
        ) {
            return icon;
        }
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) throw new IllegalArgumentException(
            "Icon does not support metadata: " + definition.icon()
        );
        applyCustomModelData(definition, meta);
        applyItemModel(definition, meta);
        icon.setItemMeta(meta);
        return icon;
    }

    private static void applyCustomModelData(Node definition, ItemMeta meta) {
        if (definition.customModelData() != null) {
            meta.setCustomModelData(definition.customModelData());
        }
    }

    private static void applyItemModel(Node definition, ItemMeta meta) {
        if (definition.itemModel() == null) return;
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
}
