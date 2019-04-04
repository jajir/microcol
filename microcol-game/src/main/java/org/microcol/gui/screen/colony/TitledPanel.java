package org.microcol.gui.screen.colony;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Show panel with simple border and title. Title is places in top border line.
 * This is obsolete class. Later should be removed.
 */
public class TitledPanel extends VBox {

    private final HBox contentPane;

    public TitledPanel() {
        this(null);
    }

    public TitledPanel(final Node content) {
        contentPane = new HBox();
        if (content != null) {
            content.getStyleClass().add("bordered-titled-content");
            contentPane.getChildren().add(content);
        }
        getStyleClass().add("bordered-titled-border");
        getChildren().add(contentPane);
    }

    public Pane getContentPane() {
        return contentPane;
    }

}
