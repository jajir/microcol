package org.microcol.gui.util;

import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import com.google.inject.Inject;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared frame: controls, independently scrolling content and live status. */
public final class InGamePage implements JavaFxComponent, UpdatableLanguage {
    private final BorderPane root = new BorderPane();
    private final VBox body = new VBox(20);
    private final Label title = new Label();
    private final StatusBar statusBar;
    private final I18n i18n;

    @Inject
    InGamePage(final StatusBar statusBar, final I18n i18n) {
        this.statusBar = statusBar;
        this.i18n = i18n;
        statusBar.setShowEventsFromSource(Source.EUROPE);
        root.getStyleClass().add("in-game-page");
        root.getStylesheets().add(InGamePage.class
                .getResource("/gui/InGamePage.css").toExternalForm());
        title.getStyleClass().add("screen-title");
        title.setWrapText(true);
        title.setVisible(false);
        title.setManaged(false);
        body.getStyleClass().add("screen-body");
        body.getChildren().add(title);
        final ScrollPane scroll = new ScrollPane(body);
        scroll.getStyleClass().add("screen-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        root.setCenter(scroll);
        root.setBottom(statusBar.getContent());
    }

    public void setToolbar(final Node toolbar) {
        toolbar.getStyleClass().add("screen-toolbar");
        root.setTop(toolbar);
    }

    public void setContent(final Region content) {
        body.getChildren().setAll(title, content);
    }

    public void setTitle(final String text) {
        title.setText(text);
        title.setVisible(true);
        title.setManaged(true);
    }

    public void setStatusSource(final Source source) {
        statusBar.setShowEventsFromSource(source);
    }

    public void beforeShow() {
        statusBar.updateLanguage(i18n);
    }

    @Override
    public void updateLanguage(final I18n texts) {
        statusBar.updateLanguage(texts);
    }

    @Override
    public Region getContent() {
        return root;
    }
}
