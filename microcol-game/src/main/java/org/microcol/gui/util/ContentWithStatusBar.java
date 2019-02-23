package org.microcol.gui.util;

import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Component brings together content panel and status bar. Status bar is placed
 * below content.
 */
public class ContentWithStatusBar implements JavaFxComponent, UpdatableLanguage {

    private StatusBar statusBar;

    private JavaFxComponent content;

    private final VBox mainBox;

    @Inject
    ContentWithStatusBar() {
        mainBox = new VBox();
        mainBox.setId("mainPanel");
    }

    public void setStatusBar(final StatusBar statusBar) {
        this.statusBar = Preconditions.checkNotNull(statusBar);
        initComponent();
    }

    public void setContent(final JavaFxComponent content) {
        this.content = Preconditions.checkNotNull(content);
        initComponent();
    }

    private void initComponent() {
        mainBox.getChildren().clear();
        if (content != null) {
            mainBox.getChildren().add(content.getContent());
        }
        if (statusBar != null) {
            mainBox.getChildren().add(statusBar.getContent());
        }
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        tryToUpdateComponent(content, i18n);
    }

}
