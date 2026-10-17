package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.Arrays;
import java.util.Objects;

/** Changes only frame-colored chat text on a copy, never shared display data. */
final class MinecraftAdvancementColors {
    private MinecraftAdvancementColors() {}
    record Announcement(String namespace, String key, String title) {}

    static BaseComponent[] format(BaseComponent[] original, AdvancementFrameType frame) {
        return format(original, frame, null);
    }

    static BaseComponent[] format(BaseComponent[] original, AdvancementFrameType frame, Announcement announcement) {
        if (original == null) return null;
        ChatColor replacement = palette(frame, announcement);
        BaseComponent[] result = new BaseComponent[original.length];
        for (int i = 0; i < original.length; i++) {
            result[i] = original[i].duplicate();
            recolor(result[i], frame.getColor(), replacement);
        }
        return result;
    }

    private static ChatColor palette(AdvancementFrameType frame, Announcement announcement) {
        ChatColor fallback = frame == AdvancementFrameType.CHALLENGE ? ChatColor.DARK_RED : ChatColor.GOLD;
        if (announcement == null || !"holidays".equals(announcement.namespace()) || !isWatcher(announcement.key())) return fallback;
        return titleColor(announcement.title(), fallback);
    }

    private static ChatColor titleColor(String title, ChatColor fallback) {
        if (title == null || title.isBlank()) return fallback;
        return Arrays.stream(TextComponent.fromLegacyText(title))
                .filter(part -> !part.toPlainText().isEmpty())
                .map(BaseComponent::getColorRaw)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(fallback);
    }

    private static boolean isWatcher(String key) {
        return "seen_the_watcher".equals(key) || "dont_blink".equals(key) || "forgotten_melody".equals(key);
    }

    private static void recolor(BaseComponent component, ChatColor previous, ChatColor replacement) {
        if (previous.equals(component.getColorRaw())) component.setColor(replacement);
        if (component.getExtra() != null) {
            for (BaseComponent child : component.getExtra()) recolor(child, previous, replacement);
        }
    }
}
