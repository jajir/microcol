package org.microcol.gui.screen.game.gamepanel;

import com.google.inject.Inject;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/**
 * Composed component from Pane and canvas. It insert resizable canvas into
 * pane. When pane change size than canvas size is accordingly adjusted. It's
 * necessary because canvas can't be sized in css.
 * <p>
 * Component support onReady method which allows to do some graphical work just
 * when component is read to show some content.
 * </p>
 */
public final class PaneCanvas {

    /**
     * It helps consider if canvas size is reasonable. When canvas side length
     * is bigger than this it's not correct size.
     */
    public final static int MAX_CANVAS_SIDE_LENGTH = 10000;

    private final Pane canvasPane;

    private final Canvas canvas;

    @Inject
    public PaneCanvas() {
        canvas = new Canvas();

        canvasPane = new Pane();
        canvasPane.setId("canvas");
        canvasPane.getChildren().add(canvas);

        // connect canvas size to canvas pane size
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return canvasPane.widthProperty();
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return canvasPane.heightProperty();
    }

    /**
     * @return the canvasPane
     */
    public Pane getCanvasPane() {
        return canvasPane;
    }

    /**
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

}
