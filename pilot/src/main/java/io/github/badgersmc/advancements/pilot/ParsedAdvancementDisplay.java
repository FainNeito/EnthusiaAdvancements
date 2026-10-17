package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import io.github.badgersmc.advancements.pilot.ProjectionService.Node;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

/** UAA stores raw legacy strings in chat components; parse RGB before sending hover text. */
final class ParsedAdvancementDisplay extends AdvancementDisplay {
    ParsedAdvancementDisplay(ItemStack icon, Node node, AdvancementFrameType frame) {
        super(icon, node.title(), frame, false, false, node.x(), node.y(), node.description());
    }

    private BaseComponent[] parsed(String text) {
        // UAA requires one root component for its announcement hover layout.
        return new BaseComponent[]{new TextComponent(TextComponent.fromLegacyText(text))};
    }

    @Override public BaseComponent[] getChatTitle() {
        return parsed(getFrame().getColor() + getTitle());
    }

    @Override public BaseComponent[] getChatDescription() {
        String color = getFrame().getColor().toString();
        String lines = getDescription().stream().map(line -> color + line).collect(Collectors.joining("\n"));
        return parsed(color + getTitle() + (lines.isEmpty() ? "" : "\n" + lines));
    }
}
