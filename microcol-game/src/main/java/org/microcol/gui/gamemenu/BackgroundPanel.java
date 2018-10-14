package org.microcol.gui.gamemenu;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Contains background image.
 */
public class BackgroundPanel implements JavaFxComponent, Repaintable {

    private final Pane mainPanel;

    private final Canvas canvas;

    final ImageProvider imageProvider;

    @Inject
    public BackgroundPanel(final ImageProvider imageProvider) {
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

    void paint(final GraphicsContext gc) {
        final Image image = imageProvider.getImage(ImageProvider.IMG_SUNSET);
        final Point center = Point.of(canvas.getWidth(), canvas.getHeight()).divide(2);
        final Point imageSize = Point.of(image.getWidth(), image.getHeight()).divide(2);
        final Point p = center.substract(imageSize);
        gc.drawImage(image, p.getX(), p.getY());
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    /**
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

}
