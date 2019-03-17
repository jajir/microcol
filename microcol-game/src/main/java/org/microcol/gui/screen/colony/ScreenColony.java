package org.microcol.gui.screen.colony;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.StatusBar;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.ContentWithStatusBar;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.layout.Region;

@Singleton
public class ScreenColony implements GameScreen {

    public static final String STYLE_SHEET_COLONY = ScreenColony.class
            .getResource("/gui/Colony.css").toExternalForm();

    private final ContentWithStatusBar contentWithStatusBar;

    private final ColonyPanel colonyPanel;

    @Inject
    ScreenColony(final ColonyPanel colonyPanel, final @Named("Colony") StatusBar statusBar,
            final CenteredPage centeredPage, final ContentWithStatusBar contentWithStatusBar,
            final ColonyBackground colonyBackground) {
        this.contentWithStatusBar = Preconditions.checkNotNull(contentWithStatusBar);
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        statusBar.setShowEventsFromSource(Source.COLONY);

        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_COLONY);
        centeredPage.setBackground(colonyBackground);
        centeredPage.setMainPanel(colonyPanel);
        contentWithStatusBar.setContent(centeredPage);
        contentWithStatusBar.setStatusBar(statusBar);
    }

    /**
     * Show given colony.
     *
     * @param colony
     *            required colony object
     */
    public void setColony(final Colony colony) {
        colonyPanel.showColony(colony);
        colonyPanel.repaint();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        contentWithStatusBar.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return contentWithStatusBar.getContent();
    }

    @Override
    public void beforeShow() {
        // Intentionally do nothing.
    }

    @Override
    public void beforeHide() {
        // Intentionally do nothing.
    }

}
