package org.microcol.gui.util;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Provide centered content panel with title. Content panel is usually used for
 * menu. Content of title page will be encapsulated in panel with css
 * 'titled-content'.
 */
public final class TitledPage implements JavaFxComponent {

    public static final String MAIN_TITLE_STYLE_CLASS = "main-title";

    private final StackPane mainPanel;

    private final Label title;

    private final HBox box;

    @Inject
    TitledPage() {
        title = new Label("[not defined]");
        title.getStyleClass().add(MAIN_TITLE_STYLE_CLASS);

        box = new HBox();
        box.getStyleClass().add("titled-content");

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
