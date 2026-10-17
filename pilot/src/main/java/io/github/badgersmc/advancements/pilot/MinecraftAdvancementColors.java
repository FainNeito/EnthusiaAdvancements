package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

/** Changes only frame-colored chat text on a copy, never shared display data. */
final class MinecraftAdvancementColors {
    private MinecraftAdvancementColors() {}

    static BaseComponent[] format(BaseComponent[] original, AdvancementFrameType frame) {
        if (original == null) return null;
        ChatColor replacement = frame == AdvancementFrameType.CHALLENGE
                ? ChatColor.DARK_RED : ChatColor.GOLD;
        BaseComponent[] result = new BaseComponent[original.length];
        for (int i = 0; i < original.length; i++) {
            result[i] = original[i].duplicate();
            recolor(result[i], frame.getColor(), replacement);
        }
        return result;
    }

    private static void recolor(BaseComponent component, ChatColor previous, ChatColor replacement) {
        if (previous.equals(component.getColorRaw())) component.setColor(replacement);
        if (component.getExtra() != null) {
            for (BaseComponent child : component.getExtra()) recolor(child, previous, replacement);
        }
    }
}
