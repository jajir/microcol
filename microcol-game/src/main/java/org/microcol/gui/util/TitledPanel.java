package org.microcol.gui.util;

import org.microcol.i18n.I18n;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Show panel with simple border and title. Title is places in top border line.
 */
public class TitledPanel extends VBox implements UpdatableLanguage {

    private final HBox contentPane;

    private final Label title;

    public TitledPanel() {
        this(null, null);
    }

    public TitledPanel(final String titleString) {
        this(titleString, null);
    }

    public TitledPanel(final String titleString, final Node content) {
        title = new Label();
        title.getStyleClass().add("bordered-titled-title");
        setTitle(titleString);

        contentPane = new HBox();
        if (content != null) {
            content.getStyleClass().add("bordered-titled-content");
            contentPane.getChildren().add(content);
        }
        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(title, contentPane);
    }

    @Override
    public void updateLanguage(final I18n i18n) {

    }

    public void setTitle(final String titleString) {
        title.setText(titleString);
    }

    public Pane getContentPane() {
        return contentPane;
    }

}
