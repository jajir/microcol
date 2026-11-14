package org.akropia.gui.util;

import org.akropia.gui.screen.game.components.StatusBar;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.akropia.i18n.I18n;
import com.google.inject.Inject;
import javafx.geometry.Pos;
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
    private final GameScreenHeader header = new GameScreenHeader();
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
        body.getStyleClass().add("screen-body");
        body.setAlignment(Pos.TOP_CENTER);
        root.setTop(header);
        final ScrollPane scroll = new ScrollPane(body);
        scroll.getStyleClass().add("screen-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        root.setCenter(scroll);
        root.setBottom(statusBar.getContent());
    }

    /**
     * Keep page content centered while its scroll viewport fills the window.
     */
    public static VBox centerContent(final Region content) {
        content.getStyleClass().add("screen-content-limit");
        final VBox container = new VBox(content);
        container.setAlignment(Pos.TOP_CENTER);
        container.setMinWidth(0);
        return container;
    }

    public void setToolbar(final Node toolbar) {
        header.setActions(toolbar);
    }

    public void setContent(final Region content) {
        content.getStyleClass().add("screen-content-limit");
        body.getChildren().setAll(content);
    }

    public void setTitle(final Label title) {
        header.setTitle(title);
    }

    public void setTitle(final String text) {
        header.getTitle().setText(text);
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
