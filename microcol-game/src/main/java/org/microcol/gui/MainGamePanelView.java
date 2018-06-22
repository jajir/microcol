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

    @Inject
    public MainGamePanelView(final GamePanelView gamePanel, final StatusBarView statusBar,
            final RightPanelView rightPanelView) {
        box = new VBox();
        box.setId("mainPanel");
        HBox hBox = new HBox();
        hBox.setId("mainBox");

        Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        hBox.getChildren().addAll(gamePanel.getCanvas().getCanvasPane(), rightPanelView.getBox());

        box.getChildren().addAll(hBox, statusBar.getStatusBar());
    }

    public VBox getBox() {
        return box;
    }

}
