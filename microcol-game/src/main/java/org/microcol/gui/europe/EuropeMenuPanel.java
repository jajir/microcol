package org.microcol.gui.europe;

import org.microcol.gui.StatusBar;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.ContentWithStatusBar;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.layout.Region;

@Singleton
public class EuropeMenuPanel implements JavaFxComponent, UpdatableLanguage, Repaintable {

    public static final String STYLE_SHEET_EUROPE = EuropeDialog.class
            .getResource("/gui/Europe.css").toExternalForm();

    private final ContentWithStatusBar contentWithStatusBar;

    private final EuropePanel europePanel;

    @Inject
    EuropeMenuPanel(final EuropePanel europePanel, final CenteredPage centeredPage,
            final @Named("Europe") StatusBar statusBar,
            final ContentWithStatusBar contentWithStatusBar,
            final EuropeBackground europeBackground, final I18n i18n) {
        this.contentWithStatusBar = Preconditions.checkNotNull(contentWithStatusBar);
        this.europePanel = Preconditions.checkNotNull(europePanel);
        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_EUROPE);
        centeredPage.setBackground(europeBackground);
        centeredPage.setMainPanel(europePanel);
        contentWithStatusBar.setContent(centeredPage);
        contentWithStatusBar.setStatusBar(statusBar);

        updateLanguage(i18n);
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

}
