package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

class MapOutlineArtworkTest {

    @Test
    void forestsAndCoastsKeepReadableInkAtActualTileSize() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrain().preload(cache);
        // Coverage catches a thin export or a solid blob at the actual map
        // scale.
        verifyInk(cache.getImage(ImageLoaderTerrain.IMG_TREE_1), 300, 440);
        verifyInk(cache.getImage(ImageLoaderTerrain.IMG_TREE_2), 140, 190);
        verifyInk(cache.getImage("type_1_1"), 120, 170);
        verifyInk(cache.getImage("type_2_1"), 120, 170);
    }

    private void verifyInk(final Image image, final double minimum,
            final double maximum) {
        assertEquals(45, image.getWidth());
        assertEquals(45, image.getHeight());
        double ink = 0;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                final Color pixel = image.getPixelReader().getColor(x, y);
                if (Math.max(pixel.getRed(), Math.max(pixel.getGreen(),
                        pixel.getBlue())) < 80.0 / 255) {
                    ink += pixel.getOpacity();
                }
            }
        }
        assertTrue(ink >= minimum && ink <= maximum,
                "Readable outline coverage: " + ink);
    }
}
