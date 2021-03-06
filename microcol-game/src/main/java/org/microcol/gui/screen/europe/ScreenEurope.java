package org.microcol.gui.screen.europe;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.ContentWithStatusBar;
import org.microcol.gui.util.Repaintable;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.layout.Region;

@Singleton
public class ScreenEurope implements GameScreen, Repaintable {

    public static final String STYLE_SHEET_EUROPE = ScreenEurope.class
            .getResource("/gui/Europe.css").toExternalForm();

    private final ContentWithStatusBar contentWithStatusBar;

    private final EuropePanel europePanel;
    
    @Inject
    ScreenEurope(final EuropePanel europePanel, final CenteredPage centeredPage,
            final @Named("Europe") StatusBar statusBar,
            final ContentWithStatusBar contentWithStatusBar,
            final EuropeBackground europeBackground) {
        this.contentWithStatusBar = Preconditions.checkNotNull(contentWithStatusBar);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        statusBar.setShowEventsFromSource(Source.EUROPE);
        
        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_EUROPE);
        centeredPage.setBackground(europeBackground);
        centeredPage.setMainPanel(europePanel);
        contentWithStatusBar.setContent(centeredPage);
        contentWithStatusBar.setStatusBar(statusBar);
    }

    @Override
    public Region getContent() {
        return contentWithStatusBar.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        contentWithStatusBar.updateLanguage(i18n);
    }

    @Override
    public void repaint() {
        europePanel.repaint();
    }

    @Override
    public void beforeShow() {
        repaint();
    }

    @Override
    public void beforeHide() {
        // Intentionally do nothing.
    }

}
