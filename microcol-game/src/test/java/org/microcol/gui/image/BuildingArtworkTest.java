package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.microcol.model.ConstructionType;

class BuildingArtworkTest {
    @Test
    void everyBuildingLevelHasDistinctTransparentArtwork() throws Exception {
        final Set<Integer> pixels = new HashSet<>();
        for (final ConstructionType type : ConstructionType.ALL) {
            final var url = getClass().getResource(
                    "/images/colony-buildings/" + type.name() + ".png");
            assertNotNull(url, type.name());
            final BufferedImage image = ImageIO.read(url);
            assertEquals(320, image.getWidth(), type.name());
            assertEquals(160, image.getHeight(), type.name());
            assertTrue(image.getColorModel().hasAlpha(), type.name());
            assertEquals(0, image.getRGB(0, 0) >>> 24, type.name());
            final int[] data = image.getRGB(0, 0, 320, 160, null, 0, 320);
            assertTrue(java.util.Arrays.stream(data)
                    .anyMatch(pixel -> (pixel >>> 24) > 200), type.name());
            assertTrue(pixels.add(java.util.Arrays.hashCode(data)),
                    "Reused artwork: " + type.name());
        }
        assertEquals(41, pixels.size());
    }
}
