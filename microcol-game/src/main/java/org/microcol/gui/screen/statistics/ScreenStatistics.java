package org.microcol.gui.screen.statistics;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.ScreenMenu;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

/**
 * Screen with market in Europe. It allows to buy various goods.
 */
@Singleton
public class ScreenStatistics extends AbstractScreenMenu implements GameScreen {

    private final StatisticsPanel statisticsPanel;

    private final StatisticsView statisticsView;

    private final I18n i18n;

    public static final String STYLE_SHEET_STATISTICS = ScreenMenu.class
            .getResource("/gui/Statistics.css").toExternalForm();

    @Inject
    ScreenStatistics(final I18n i18n, final StatisticsPanel statisticsPanel,
            final StatisticsView statisticsView) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.statisticsView = Preconditions.checkNotNull(statisticsView);
        statisticsView.getContent().getStylesheets().add(STYLE_SHEET_STATISTICS);
        this.statisticsPanel = Preconditions.checkNotNull(statisticsPanel);
        statisticsView.setMenuPanel(statisticsPanel);
    }

    @Override
    public Region getContent() {
        return statisticsView.getContent();
    }

    @Override
    public void beforeShow() {
        statisticsView.beforeShow();
    }

    @Override
    public void beforeHide() {
        statisticsView.beforeHide();
    }

    protected I18n getI18n() {
        return i18n;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        statisticsPanel.updateLanguage(i18n);
    }

}
