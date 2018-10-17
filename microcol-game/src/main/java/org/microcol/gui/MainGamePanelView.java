package org.microcol.gui;

import org.microcol.gui.gamepanel.PaneCanvas;
import org.microcol.gui.util.JavaFxComponent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel hold whole game screen without status bar.
 */
public final class MainGamePanelView implements JavaFxComponent {

    private final VBox box;

    // FIXME use class composing panel and status bar
    // TODO rename to gameMainPanel and move to gamepanel package

    @Inject
    public MainGamePanelView(final @Named("GamePanel") StatusBarView statusBar,
            final RightPanelView rightPanelView, final PaneCanvas paneCanvas) {
        box = new VBox();
        box.setId("mainPanel");
        HBox hBox = new HBox();
        hBox.setId("mainBox");

        Pane rightPane = new Pane();
        rightPane.setId("rightPanel");
        Label l2 = new Label("blue right panel");
        rightPane.getChildren().add(l2);

        hBox.getChildren().addAll(paneCanvas.getCanvasPane(), rightPanelView.getBox());

        box.getChildren().addAll(hBox, statusBar.getContent());
    }

    @Override
    public Region getContent() {
        return box;
    }

}
