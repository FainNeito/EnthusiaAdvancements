package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Selects a silent celebration toast without changing the registered tree frame. */
final class LastWatchToastPolicy {
    private LastWatchToastPolicy() {}

    static AdvancementFrameType toastFrame(String namespace, String key, AdvancementFrameType actualFrame) {
        return "holidays".equals(namespace) && "last_watch".equals(key) && actualFrame == AdvancementFrameType.CHALLENGE
            ? AdvancementFrameType.TASK : actualFrame;
    }

    @FunctionalInterface
    interface CustomToast {
        void show(Player player, ItemStack icon, String title, AdvancementFrameType frame);
    }

    static void show(BaseAdvancement node, Player player, CustomToast custom, AdvancementFrameType toastFrame) {
        var display = node.getDisplay();
        var actualFrame = display.getFrame();
        if (toastFrame == actualFrame) {
            node.displayToastToPlayer(player);
        } else {
            custom.show(player, display.getIcon(), display.getTitle(), toastFrame);
        }
    }
}
