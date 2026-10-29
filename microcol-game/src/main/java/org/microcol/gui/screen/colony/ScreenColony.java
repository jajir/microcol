package org.microcol.gui.screen.colony;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.InGamePage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

@Singleton
public class ScreenColony implements GameScreen {

    private static final String STYLE_SHEET_COLONY = ScreenColony.class
            .getResource("/gui/Colony.css").toExternalForm();

    private final InGamePage page;

    private final ColonyPanel colonyPanel;

    @Inject
    ScreenColony(final ColonyPanel colonyPanel, final CenteredPage centeredPage,
            final InGamePage page, final ColonyBackground colonyBackground,
            final ColonyButtonsPanel buttons, final PanelColonyName colonyName,
            final ColonyDialogCallback callback) {
        this.page = Preconditions.checkNotNull(page);
        this.colonyPanel = Preconditions.checkNotNull(colonyPanel);
        page.setStatusSource(Source.COLONY);

        centeredPage.getContent().getStylesheets().add(STYLE_SHEET_COLONY);
        centeredPage.setBackground(colonyBackground);
        centeredPage.setMainPanel(colonyPanel);
        centeredPage.getContent().setStyle("");
        centeredPage.getContent().setMinSize(950, 740);
        centeredPage.getContent().setPrefSize(950, 740);
        page.getContent().getStyleClass().add("illustrated-page");
        page.setToolbar(buttons.getContent());
        page.setTitle((javafx.scene.control.Label) colonyName.getContent());
        page.setContent(centeredPage.getContent());
        page.getContent().addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        callback.close();
                        event.consume();
                    }
                });
    }

    /**
     * Show given colony.
     *
     * @param colony required colony object
     */
    public void setColony(final Colony colony) {
        colonyPanel.showColony(colony);
        colonyPanel.repaint();
    }

    @Override
    public void updateLanguage(I18n i18n) {
        colonyPanel.updateLanguage(i18n);
        page.updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void beforeShow() {
        page.beforeShow();
    }

    @Override
    public void beforeHide() {
        // Intentionally do nothing.
    }

}
