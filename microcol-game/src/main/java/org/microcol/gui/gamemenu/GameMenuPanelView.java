package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * In main area shows game welcome page.
 */
public final class GameMenuPanelView implements JavaFxComponent {

    private final StackPane mainPanel;

    @Inject
    GameMenuPanelView(final ButtonsPanelView buttonsPanelView,
            final BackgroundPanel backgroundPanel) {
        final Label title = new Label("MicroCol");
        title.getStyleClass().add("main-title");

        mainPanel = new StackPane();
        mainPanel.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
        mainPanel.getChildren().add(backgroundPanel.getContent());
        mainPanel.getChildren().add(buttonsPanelView.getContent());
        mainPanel.getChildren().add(title);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
