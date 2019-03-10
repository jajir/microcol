package org.microcol.gui.screen.colony;

import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Show panel with simple border and title. Title is places in top border line.
 * 
 * 
 * 
 * TODO replace it with simple content. Remove UpdatableLanguage
 */
public class TitledPanel extends VBox implements UpdatableLanguage {

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

    @Override
    public void updateLanguage(final I18n i18n) {

    }

    public Pane getContentPane() {
        return contentPane;
    }

}
