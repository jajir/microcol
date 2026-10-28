package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.gui.util.StreamReader;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

class MapOutlineArtworkTest {

    @ParameterizedTest
    @CsvSource({ "8, 1", "9, 1", "8, 2", "9, 2" })
    void coastEndsKeepLandFillWithoutPerpendicularInk(final int column,
            final int row) {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrain().preload(cache);
        ImageWrapper coast = ImageWrapper
                .of(cache.getImage("type_" + column + "_" + row));
        final ImageWrapper opposite = coast.getImageRotareRight()
                .getImageRotareRight();
        ImageWrapper channel = coast.addImage(opposite);
        int edgeX = 44, edgeY = 1, insideX = 43, insideY = 1;
        int inkX = 44, inkY = 4;
        for (int rotation = 0; rotation < 4; rotation++) {
            for (final ImageWrapper shape : new ImageWrapper[] { coast,
                    channel }) {
                final Color edge = shape.get().getPixelReader().getColor(edgeX,
                        edgeY);
                assertEquals(1, edge.getOpacity());
                assertEquals(
                        shape.get().getPixelReader().getColor(insideX, insideY),
                        edge,
                        "Land fill must reach the tile seam without a dark tick");
                assertTrue(
                        shape.get().getPixelReader().getColor(inkX, inkY)
                                .getBrightness() < 0.4,
                        "The continuous coastline keeps its ink");
            }
            final int oldEdgeX = edgeX, oldInsideX = insideX, oldInkX = inkX;
            edgeX = 44 - edgeY;
            edgeY = oldEdgeX;
            insideX = 44 - insideY;
            insideY = oldInsideX;
            inkX = 44 - inkY;
            inkY = oldInkX;
            coast = coast.getImageRotareRight();
            channel = channel.getImageRotareRight();
        }
    }

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
