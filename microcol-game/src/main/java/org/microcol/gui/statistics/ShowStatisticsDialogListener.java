package org.microcol.gui.statistics;

import org.microcol.gui.mainmenu.ShowStatisticsController;
import org.microcol.gui.mainmenu.ShowStatisticsEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Show statistics dialog on proper event.
 */
public class ShowStatisticsDialogListener {

    private final StatisticsDialog statisticsDialog;

    @Inject
    ShowStatisticsDialogListener(final StatisticsDialog statisticsDialog,
            final ShowStatisticsController showStatisticsController) {
        this.statisticsDialog = Preconditions.checkNotNull(statisticsDialog);
        showStatisticsController.addListener(this::onShowStatistics);
    }

    @SuppressWarnings("unused")
    private void onShowStatistics(final ShowStatisticsEvent event) {
        statisticsDialog.show();
    }

}
