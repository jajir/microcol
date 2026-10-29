package org.microcol.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/** Shared heading and actions for every in-game screen. */
public final class GameScreenHeader extends StackPane {

    private Label title;
    private final HBox content = new HBox();

    public GameScreenHeader() {
        getStyleClass().add("screen-toolbar");
        content.getStyleClass().add("game-bar-content");
        getChildren().add(content);
        setTitle(new Label());
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(final Label heading) {
        if (title != null) {
            content.getChildren().remove(title);
        }
        title = heading;
        title.getStyleClass().add("screen-title");
        title.setWrapText(false);
        title.setMinWidth(0);
        title.visibleProperty().bind(title.textProperty().isNotEmpty());
        title.managedProperty().bind(title.visibleProperty());
        content.getChildren().add(0, title);
    }

    public void setActions(final Node actions) {
        content.getChildren().setAll(title, actions);
        actions.getStyleClass().add("screen-actions");
        HBox.setHgrow(actions, Priority.ALWAYS);
        if (actions instanceof Region) {
            ((Region) actions).setMinWidth(0);
            ((Region) actions).setMaxWidth(Double.MAX_VALUE);
        }
    }

    public double getHeadingWidth() {
        return title.isManaged()
                ? Math.min(title.prefWidth(-1), title.maxWidth(-1))
                        + content.getSpacing()
                : 0;
    }
}
