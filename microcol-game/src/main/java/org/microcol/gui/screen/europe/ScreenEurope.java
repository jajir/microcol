package org.microcol.gui.screen.europe;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

@Singleton
public class ScreenEurope implements GameScreen, Repaintable {

    public static final String STYLE_SHEET_EUROPE = ScreenEurope.class
            .getResource("/gui/Europe.css").toExternalForm();

    private final InGamePage page;

    private final EuropePanel europePanel;

    @Inject
    ScreenEurope(final EuropePanel europePanel, final CenteredPage centeredPage,
            final InGamePage page, final EuropeBackground europeBackground,
            final EuropeButtonsPanel buttons) {
        this.page = Preconditions.checkNotNull(page);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        page.setStatusSource(Source.EUROPE);

        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_EUROPE);
        centeredPage.setBackground(europeBackground);
        centeredPage.setMainPanel(europePanel);
        centeredPage.getContent().setStyle("");
        centeredPage.getContent().setMinSize(950, 740);
        centeredPage.getContent().setPrefSize(950, 740);
        page.getContent().getStyleClass().add("illustrated-page");
        page.setToolbar(buttons.getContent());
        page.setContent(centeredPage.getContent());
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
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
