package io.github.badgersmc.advancements.pilot;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Optional adapter to the vanilla Discord advancement resource-pack renderer. */
final class DiscordAdvancementIcon {
    private DiscordAdvancementIcon() {}

    static RenderJob prepare(ClassLoader loader, Player player, ItemStack icon, String frame)
        throws ReflectiveOperationException {
        String addon = "com.loohp.interactivechatdiscordsrvaddon.";
        Class<?> type = Class.forName(addon + "objectholders.AdvancementType", true, loader);
        Object style = type.getMethod("fromName", String.class).invoke(null, frame);
        if (style == null) throw new IllegalArgumentException("Unknown advancement frame");
        Class<?> factory = Class.forName(
            "com.loohp.interactivechat.objectholders.ICPlayerFactory", true, loader);
        Object viewer = factory.getMethod("getICPlayer", Player.class).invoke(null, player);
        Class<?> offline = Class.forName(
            "com.loohp.interactivechat.objectholders.OfflineICPlayer", true, loader);
        var render = Class.forName(addon + "graphics.ImageGeneration", true, loader)
            .getMethod("getAdvancementIcon", ItemStack.class, type, boolean.class, offline);
        ItemStack snapshot = icon.clone();
        return () -> encode((BufferedImage) render.invoke(null, snapshot, style, true, viewer));
    }

    static byte[] encode(BufferedImage image) throws IOException {
        validateImage(image);
        var output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) throw new IOException("PNG encoder unavailable");
        if (output.size() > 1_048_576) throw new IOException("Oversized advancement attachment");
        return output.toByteArray();
    }

    private static void validateImage(BufferedImage image) throws IOException {
        if (image == null) throw new IOException("Missing advancement thumbnail");
        if (image.getWidth() > 512 || image.getHeight() > 512)
            throw new IOException("Oversized advancement thumbnail");
    }

    @FunctionalInterface
    interface RenderJob {
        byte[] render() throws ReflectiveOperationException, IOException;
    }
}
