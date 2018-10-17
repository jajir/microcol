package org.microcol.gui.util;

import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Component paint background and one centered panel. MainPanel will be centered
 * and have css class 'game-menu-holder'.
 */
public class CenteredPage implements JavaFxComponent, UpdatableLanguage {

    private final StackPane mainParentPanel;

    private final HBox centeredContent;

    private final VBox outerBox;

    private JavaFxComponent background;

    private JavaFxComponent mainPanel;

    CenteredPage() {
        centeredContent = new HBox();
        centeredContent.getStyleClass().add("game-menu-holder");

        final HBox inner = new HBox();
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().add(centeredContent);

        outerBox = new VBox();
        outerBox.setAlignment(Pos.CENTER);
        outerBox.getChildren().add(inner);

        mainParentPanel = new StackPane();
        mainParentPanel.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
        mainParentPanel.getChildren().add(outerBox);
    }

    public ObservableList<Node> getChildren() {
        return mainParentPanel.getChildren();
    }

    public void setBackground(final JavaFxComponent background) {
        this.background = Preconditions.checkNotNull(background);
        mainParentPanel.getChildren().clear();
        mainParentPanel.getChildren().add(background.getContent());
        mainParentPanel.getChildren().add(outerBox);
    }

    public void setMainPanel(final JavaFxComponent mainPanel) {
        this.mainPanel = Preconditions.checkNotNull(mainPanel);
        centeredContent.getChildren().clear();
        centeredContent.getChildren().add(mainPanel.getContent());
    }

    @Override
    public Region getContent() {
        return mainParentPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        if (background instanceof UpdatableLanguage) {
            ((UpdatableLanguage) background).updateLanguage(i18n);
        }
        if (mainPanel instanceof UpdatableLanguage) {
            ((UpdatableLanguage) mainPanel).updateLanguage(i18n);
        }
    }

}
