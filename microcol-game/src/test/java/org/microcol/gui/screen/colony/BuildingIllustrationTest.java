package org.microcol.gui.screen.colony;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;

class BuildingIllustrationTest {
    @Test
    void trimsTransparentMarginsAndPreservesEdgePixelsAndAspectRatio() {
        final WritableImage image = new WritableImage(320, 160);
        image.getPixelWriter().setArgb(100, 10, 0x01000000);
        image.getPixelWriter().setArgb(199, 149, 0xff000000);

        final var view = PanelColonyStructures.buildingImage(image);

        assertSame(image, view.getImage());
        assertEquals(new Rectangle2D(100, 10, 100, 140), view.getViewport());
        assertTrue(view.isPreserveRatio());
        assertEquals(44, view.getBoundsInLocal().getHeight(), 0.01);
        assertEquals(44 * 100.0 / 140, view.getBoundsInLocal().getWidth(),
                0.01);
    }

    @Test
    void keepsTransparentImageValidAndFitsWideIllustrations() {
        final WritableImage image = new WritableImage(320, 160);
        final var empty = PanelColonyStructures.buildingImage(image);
        assertNull(empty.getViewport());

        image.getPixelWriter().setArgb(0, 0, 0xff000000);
        image.getPixelWriter().setArgb(319, 159, 0xff000000);
        final var wide = PanelColonyStructures.buildingImage(image);
        assertEquals(new Rectangle2D(0, 0, 320, 160), wide.getViewport());
        assertEquals(64, wide.getBoundsInLocal().getWidth(), 0.01);
        assertEquals(32, wide.getBoundsInLocal().getHeight(), 0.01);
    }
}
