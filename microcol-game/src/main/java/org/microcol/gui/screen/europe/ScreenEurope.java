package org.microcol.gui.screen.europe;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Singleton
public class ScreenEurope implements GameScreen, Repaintable {

    public static final String STYLE_SHEET_EUROPE = ScreenEurope.class
            .getResource("/gui/Europe.css").toExternalForm();

    private final InGamePage page;

    private final EuropePanel europePanel;

    @Inject
    ScreenEurope(final EuropePanel europePanel, final InGamePage page,
            final EuropeButtonsPanel buttons) {
        this.page = Preconditions.checkNotNull(page);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        page.setStatusSource(Source.EUROPE);

        europePanel.getContent().getStylesheets().add(STYLE_SHEET_EUROPE);
        VBox.setVgrow(europePanel.getContent(), Priority.ALWAYS);
        page.getContent().getStyleClass().add("illustrated-page");
        page.setToolbar(buttons.getContent());
        page.setContent(europePanel.getContent());
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        page.setTitle(i18n.get(Europe.title));
        europePanel.updateLanguage(i18n);
        page.updateLanguage(i18n);
    }

    @Override
    public void repaint() {
        europePanel.repaint();
    }

    @Override
    public void beforeShow() {
        page.beforeShow();
        repaint();
    }

    @Override
    public void beforeHide() {
        // Intentionally do nothing.
    }

}
