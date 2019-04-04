package org.microcol.gui.util;

import com.google.inject.Inject;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Composed component from Pane and canvas. It insert resizable canvas into
 * pane. When pane change size than canvas size is accordingly adjusted. It's
 * necessary because canvas can't be sized in css.
 */
public final class CanvasComponent implements JavaFxComponent {

    private final Pane canvasPane;

    private final Canvas canvas;

    @Inject
    public CanvasComponent() {
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

    @Override
    public Region getContent() {
        return canvasPane;
    }

}
