package org.microcol.gui.util;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Abstract component for drawing background image.
 */
public abstract class AbstractBackgroundPanel implements JavaFxComponent, Repaintable {

    private final Pane mainPanel;

    private final Canvas canvas;

    private final ImageProvider imageProvider;

    @Inject
    public AbstractBackgroundPanel(final ImageProvider imageProvider) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        mainPanel = new Pane();
        canvas = new Canvas();
        mainPanel.getChildren().add(canvas);
        mainPanel.widthProperty().addListener((old, v1, v2) -> repaint());
        mainPanel.heightProperty().addListener((old, v1, v2) -> repaint());
        canvas.widthProperty().bind(mainPanel.widthProperty());
        canvas.heightProperty().bind(mainPanel.heightProperty());
        repaint();
    }

    @Override
    public void repaint() {
        final GraphicsContext gc = canvas.getGraphicsContext2D();
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
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getX(), canvas.getY());
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    /**
     * @return the canvas
     */
    protected Canvas getCanvas() {
        return canvas;
    }

    /**
     * @return the imageProvider
     */
    protected ImageProvider getImageProvider() {
        return imageProvider;
    }

}
