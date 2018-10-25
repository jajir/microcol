package org.microcol.gui.colony;

import org.microcol.gui.StatusBar;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.event.StatusBarMessageEvent.Source;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.ContentWithStatusBar;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.layout.Region;

@Singleton
public class ColonyMenuPanel implements JavaFxComponent, UpdatableLanguage {

    public static final String STYLE_SHEET_COLONY = EuropeDialog.class
            .getResource("/gui/Colony.css").toExternalForm();

    private final ContentWithStatusBar contentWithStatusBar;

    private final ColonyPanel colonyPanel;

    @Inject
    ColonyMenuPanel(final ColonyPanel colonyPanel, final @Named("Colony") StatusBar statusBar,
            final CenteredPage centeredPage, final ContentWithStatusBar contentWithStatusBar,
            final ColonyBackground colonyBackground, final I18n i18n) {
        this.contentWithStatusBar = Preconditions.checkNotNull(contentWithStatusBar);
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        statusBar.setShowEventsFromSource(Source.COLONY);

        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_COLONY);
        centeredPage.setBackground(colonyBackground);
        centeredPage.setMainPanel(colonyPanel);
        contentWithStatusBar.setContent(centeredPage);
        contentWithStatusBar.setStatusBar(statusBar);

        updateLanguage(i18n);
    }

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

}
