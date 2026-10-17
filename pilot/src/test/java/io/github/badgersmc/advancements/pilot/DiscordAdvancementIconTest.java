package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class DiscordAdvancementIconTest {
    @Test void missingOptionalRendererDoesNotRequireAnAddonDependency() {
        assertThrows(ClassNotFoundException.class, () -> DiscordAdvancementIcon.prepare(
            new ClassLoader(null) {}, null, null, "TASK"));
    }
    @Test void encodesTransparentFramedThumbnailLosslessly() throws Exception {
        var image = new BufferedImage(52, 52, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(12, 20, 0xff55c977);
        var decoded = ImageIO.read(new ByteArrayInputStream(DiscordAdvancementIcon.encode(image)));
        assertEquals(52, decoded.getWidth());
        assertEquals(52, decoded.getHeight());
        assertEquals(0xff55c977, decoded.getRGB(12, 20));
        assertEquals(0, decoded.getRGB(0, 0));
    }
    @Test void rejectsMissingOrOversizedImages() {
        assertThrows(IOException.class, () -> DiscordAdvancementIcon.encode(null));
        assertThrows(IOException.class, () -> DiscordAdvancementIcon.encode(
            new BufferedImage(513, 52, BufferedImage.TYPE_INT_ARGB)));
        assertThrows(IOException.class, () -> DiscordAdvancementIcon.encode(
            new BufferedImage(52, 513, BufferedImage.TYPE_INT_ARGB)));
    }
}
