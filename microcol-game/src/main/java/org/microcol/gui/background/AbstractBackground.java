package org.microcol.gui.background;

import org.microcol.gui.Point;
import org.microcol.gui.util.CanvasComponent;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;

import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Abstract component for drawing background image.
 */
public abstract class AbstractBackground implements JavaFxComponent, Repaintable {

    private final CanvasComponent canvasComponent;

    @Inject
    public AbstractBackground() {
        canvasComponent = new CanvasComponent();
        canvasComponent.getContent().widthProperty().addListener((old, v1, v2) -> repaint());
        canvasComponent.getContent().heightProperty().addListener((old, v1, v2) -> repaint());
    }

    @Override
    public void repaint() {
        final GraphicsContext gc = canvasComponent.getCanvas().getGraphicsContext2D();
        paint(gc);
    }

    /**
     * Method should be overridden. By default just draw white background.
     *
     * @param gc
     *            required 2D graphics context
     */
    public void paint(final GraphicsContext gc) {
        final Point canvas = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        paintBackground(gc, canvas, Color.WHITE);
    }

    @Override
    public Region getContent() {
        return canvasComponent.getContent();
    }

    /**
     * @return the canvas
     */
    protected Canvas getCanvas() {
        return canvasComponent.getCanvas();
    }

    /**
     * Paint background of some area with solid color.
     *
     * @param gc
     *            required
     * @param areaSize
     *            required area size
     * @param color
     *            required color
     */
    protected void paintBackground(final GraphicsContext gc, final Point areaSize,
            final Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, areaSize.getX(), areaSize.getY());
    }

}
