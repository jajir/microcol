package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * In main area shows game welcome page.
 * TODO combine it with CenteredPage.
 */
public final class MenuHolderPanel implements JavaFxComponent {

    private final StackPane mainPanel;

    private final Label title;

    private final HBox box;

    @Inject
    MenuHolderPanel(final BackgroundPanel2 backgroundPanel) {
        title = new Label("[not defined]");
        title.getStyleClass().add("main-title");

        box = new HBox();
        box.getStyleClass().add("game-menu-holder");

        final HBox inner = new HBox();
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().add(box);

        final VBox outerBox = new VBox();
        outerBox.setAlignment(Pos.CENTER);
        outerBox.getChildren().add(inner);

        mainPanel = new StackPane();
        mainPanel.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
        mainPanel.getChildren().add(backgroundPanel.getContent());
        mainPanel.getChildren().add(title);
        mainPanel.getChildren().add(outerBox);
    }

    public void setMenuPanel(final Region menuPanel) {
        Preconditions.checkNotNull(menuPanel);
        box.getChildren().clear();
        box.getChildren().add(menuPanel);
    }

    public void setTitle(final String menuTitle) {
        title.setText(menuTitle);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
