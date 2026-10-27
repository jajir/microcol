package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

class ModalFrameTest {

    @Test
    void outlineAndClipFollowSmallLargeAndRepeatedLayouts() {
        final Pane content = new Pane();
        final ModalFrame frame = new ModalFrame(content);
        final SVGPath clip = (SVGPath) frame.getClip();
        assertEquals(((SVGPath) frame.getShape()).getContent(),
                clip.getContent());
        assertTrue(content.getStyleClass().contains("modal-content"));
        for (final int[] size : new int[][] { { 240, 160 }, { 960, 700 },
                { 500, 250 }, { 240, 160 } }) {
            frame.resize(size[0], size[1]);
            frame.layout();
            final Bounds bounds = clip.getBoundsInParent();
            assertEquals(0, bounds.getMinX(), 0.001);
            assertEquals(0, bounds.getMinY(), 0.001);
            assertEquals(size[0], bounds.getWidth(), 0.001);
            assertEquals(size[1], bounds.getHeight(), 0.001);
            assertTrue(clip
                    .contains(clip.parentToLocal(size[0] / 2, size[1] / 2)));
            assertFalse(clip.contains(clip.parentToLocal(0, 0)));
            assertFalse(clip.contains(clip.parentToLocal(size[0], size[1])));
            final Bounds inner = content.getBoundsInParent();
            for (final double x : new double[] { inner.getMinX(),
                    inner.getMaxX() }) {
                for (final double y : new double[] { inner.getMinY(),
                        inner.getMaxY() }) {
                    assertTrue(clip.contains(clip.parentToLocal(x, y)),
                            "Content remains inside the outline after resizing");
                }
            }
        }
    }
}
