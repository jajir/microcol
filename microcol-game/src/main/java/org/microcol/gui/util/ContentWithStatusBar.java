package org.microcol.gui.util;

import org.microcol.gui.StatusBarView;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Component brings together content panel and status bar. Status bar is placed
 * below content.
 */
public class ContentWithStatusBar implements JavaFxComponent, UpdatableLanguage {

    private final StatusBarView statusBar;

    private JavaFxComponent content;

    private final VBox mainBox;

    @Inject
    ContentWithStatusBar(final @Named("Europe") StatusBarView statusBar) {
        this.statusBar = Preconditions.checkNotNull(statusBar);
        mainBox = new VBox();
        mainBox.setId("mainPanel");
        mainBox.getChildren().add(statusBar.getContent());
    }

    public void setContent(final JavaFxComponent content) {
        this.content = Preconditions.checkNotNull(content);
        mainBox.getChildren().clear();
        mainBox.getChildren().add(content.getContent());
        mainBox.getChildren().add(statusBar.getContent());
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        tryToUpdateCompoonent(content, i18n);
    }

}
