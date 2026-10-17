package io.github.badgersmc.advancements.pilot;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.ArrayList;
import org.bukkit.ChatColor;

/** Bounded, plain-text content for one explicit live advancement celebration. */
record DiscordAdvancementNotice(String heading, String description, int color) {
    private static final Pattern MINI_MESSAGE_TAG = Pattern.compile("<[/!#a-zA-Z][^>]*>");
    private static final Pattern HEX_COLOR = Pattern.compile("(?i)&#[0-9a-f]{6}");
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)&[0-9a-fk-or]");

    static DiscordAdvancementNotice from(String playerName, ProjectionService.Node node) {
        String frame = node.frame().toUpperCase(Locale.ROOT);
        String kind = switch (frame) {
            case "GOAL" -> "has reached the goal";
            case "CHALLENGE" -> "has completed the challenge";
            default -> "has made the advancement";
        };
        int color = frame.equals("CHALLENGE") ? 0xAA0000 : 0xFFAA00;
        String heading = bounded(clean(playerName) + " " + kind + " " + clean(node.title()), 256);
        String description = bounded(summarize(node.description()), 240);
        return new DiscordAdvancementNotice(heading, description, color);
    }

    private static String summarize(List<String> lines) {
        var objectives = new ArrayList<String>();
        var requirements = new ArrayList<String>();
        boolean inRequirements = false;
        description:
        for (String raw : lines) {
            for (String part : clean(raw).split("\\n")) {
                String line = part.trim();
                String lower = line.toLowerCase(Locale.ROOT);
                if (line.isBlank()) continue;
                if (lower.startsWith("reward:") || lower.startsWith("rewards:")) break description;
                if (lower.equals("requirements:")) {
                    inRequirements = true;
                    continue;
                }
                if (lower.startsWith("progress") || lower.startsWith("source:")
                    || lower.startsWith("claim with ") || lower.startsWith("gold:")
                    || lower.contains("completion is not payment")) continue;
                if (!inRequirements && isProviderLabel(line)) continue;
                (inRequirements ? requirements : objectives).add(line);
            }
        }
        var selected = objectives.isEmpty() ? requirements : objectives;
        return String.join(" ", selected.stream().limit(2).toList());
    }

    private static boolean isProviderLabel(String line) {
        return switch (line) {
            case "DiaryKeeper", "Enthusia Reputation", "EnthusiaCommend",
                "EnthusiaExpress", "Warzone Duels", "PlayTime", "PlayTimePlugin" -> true;
            default -> false;
        };
    }

    private static String clean(String value) {
        if (value == null) return "";
        String plain = ChatColor.stripColor(MINI_MESSAGE_TAG.matcher(value).replaceAll(""));
        plain = HEX_COLOR.matcher(plain).replaceAll("");
        plain = LEGACY_COLOR.matcher(plain).replaceAll("");
        return plain.replace('@', '＠').replace('\r', ' ').trim();
    }

    private static String bounded(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
