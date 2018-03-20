package org.microcol.gui;

import org.microcol.gui.gamepanel.GamePanelView;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Panel hold whole game screen without status bar.
 */
public class MainGamePanelView {

    private final VBox box;

    /**
     * When game starts canvas size is not available. It's available later from
     * canvasPane. Event before correct size is known canvas have to be painted.
     * Because of that there should be some maximum canvas size. With unlimited
     * canvas size game freeze because canvas is too big and slow to paint.
     * 
     * TODO JJ try to remove max canvas size
     */
    public final static int MAX_CANVAS_SIDE_LENGTH = 5000;

    @Inject
    public MainGamePanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
            final RightPanelView rightPanelView) {
        box = new VBox();
        box.setId("mainPanel");
        HBox hBox = new HBox();
        hBox.setId("mainBox");

        Pane canvasPane = new Pane();
        canvasPane.setId("canvas");
        canvasPane.getChildren().add(gamePanel.getCanvas());
        gamePanel.getCanvas().widthProperty().bind(canvasPane.widthProperty());
        gamePanel.getCanvas().heightProperty().bind(canvasPane.heightProperty());

        canvasPane.widthProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < MAX_CANVAS_SIDE_LENGTH) {
                gamePanel.getVisibleArea().setCanvasWidth(newValue.intValue());
            }
        });
        canvasPane.heightProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < MAX_CANVAS_SIDE_LENGTH) {
                gamePanel.getVisibleArea().setCanvasHeight(newValue.intValue());
            }
        });

        Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        hBox.getChildren().addAll(canvasPane, rightPanelView.getBox());

        box.getChildren().addAll(hBox, statusBar.getStatusBar());
    }

    public VBox getBox() {
        return box;
    }

}
