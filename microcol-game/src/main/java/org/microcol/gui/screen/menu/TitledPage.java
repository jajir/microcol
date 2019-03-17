package org.microcol.gui.screen.menu;

import org.microcol.gui.util.JavaFxComponent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Provide centered content panel with title. Content panel is usually used for
 * menu.
 */
public final class TitledPage implements JavaFxComponent {

    public static final String MAIN_TITLE_ID = "main-title";

    private final StackPane mainPanel;

    private final Label title;

    private final HBox box;

    @Inject
    TitledPage() {
        title = new Label("[not defined]");
        title.getStyleClass().add(MAIN_TITLE_ID);
        title.setId(MAIN_TITLE_ID);

        box = new HBox();
        box.getStyleClass().add("game-menu-holder");

        mainPanel = new StackPane();
        mainPanel.getChildren().add(title);
        mainPanel.getChildren().add(box);
    }

    public void setContent(final JavaFxComponent menuPanel) {
        Preconditions.checkNotNull(menuPanel);
        box.getChildren().clear();
        box.getChildren().add(menuPanel.getContent());
    }

    public void setTitle(final String menuTitle) {
        title.setText(menuTitle);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
