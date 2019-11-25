package org.microcol.gui.background;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Paint color stripe.
 *
 */
class StripePainter {

    private final GraphicsContext gc;

    private final int canvasWidth;

    private final Color color;

    private int startAtY;

    private int height;

    StripePainter(final GraphicsContext gc, final int canvasWidth, final Color color) {
        this.gc = Preconditions.checkNotNull(gc);
        this.canvasWidth = Preconditions.checkNotNull(canvasWidth);
        this.color = Preconditions.checkNotNull(color);
    }

    void paintStripe() {
        gc.setFill(color);
        gc.fillRect(0, startAtY, canvasWidth, height);
    }

    /**
     * @param startAtY
     *            the startAtY to set
     */
    void setStartAtY(int startAtY) {
        this.startAtY = startAtY;
    }

    /**
     * @param height
     *            the height to set
     */
    void setHeight(int height) {
        this.height = height;
    }

}
