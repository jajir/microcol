package org.microcol.gui.screen.turnreport;

import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

/**
 * Screen with market in Europe. It allows to buy various goods.
 */
@Singleton
public class ScreenTurnReport implements GameScreen {

    private final TurnReportPanel turnReportPanel;

    private final TurnReportView turnReportView;

    public static final String STYLE_SHEET_TURN_REPORT = ScreenTurnReport.class
            .getResource("/gui/TurnReport.css").toExternalForm();

    @Inject
    ScreenTurnReport(final TurnReportPanel turnReportPanel, final TurnReportView turnReportView) {
        this.turnReportView = Preconditions.checkNotNull(turnReportView);
        turnReportView.getContent().getStylesheets().add(STYLE_SHEET_TURN_REPORT);
        this.turnReportPanel = Preconditions.checkNotNull(turnReportPanel);
        turnReportView.setMenuPanel(turnReportPanel);
    }

    @Override
    public Region getContent() {
        return turnReportView.getContent();
    }

    @Override
    public void beforeShow() {
        turnReportView.beforeShow();
    }

    @Override
    public void beforeHide() {
        turnReportView.beforeHide();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        turnReportPanel.updateLanguage(i18n);
    }

}
