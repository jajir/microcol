package org.microcol.gui.background;

import org.microcol.gui.Point;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Paint background with given image. Image is repeated from centered which is
 * placed at [0,0]. Other images are added to fill given canvas size.
 */
public class RepeatingBackgroundPainter {

    private final Image backgroundImage;

    public RepeatingBackgroundPainter(final Image backgroundImage) {
        this.backgroundImage = Preconditions.checkNotNull(backgroundImage);
    }

    public void paint(final GraphicsContext gc, final Point canvasSize) {
        final Point canvasCenter = canvasSize.divide(2);
        final Point imageSize = Point.of(backgroundImage.getWidth(), backgroundImage.getHeight());
        for (int y = canvasCenter.getY(); y < canvasSize.getY(); y += imageSize.getY()) {
            paintRow(gc, y, canvasSize, imageSize);
        }
        for (int y = canvasCenter.getY(); y > 0; y -= imageSize.getY()) {
            paintRow(gc, y - imageSize.getY(), canvasSize, imageSize);
        }
    }

    private void paintRow(final GraphicsContext gc, final int y, final Point canvasSize,
            final Point imageSize) {
        final Point canvasCenter = canvasSize.divide(2);
        for (int x = canvasCenter.getX(); x < canvasSize.getX(); x += imageSize.getX()) {
            gc.drawImage(backgroundImage, x, y);
        }
        for (int x = canvasCenter.getX(); x > 0; x -= imageSize.getX()) {
            gc.drawImage(backgroundImage, x - imageSize.getX(), y);
        }
    }
}
