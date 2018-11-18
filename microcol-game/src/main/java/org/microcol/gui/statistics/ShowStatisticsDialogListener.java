package org.microcol.gui.statistics;

import org.microcol.gui.event.ShowStatisticsEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Show statistics dialog on proper event.
 */
@Listener
public final class ShowStatisticsDialogListener {

    private final StatisticsDialog statisticsDialog;

    @Inject
    ShowStatisticsDialogListener(final StatisticsDialog statisticsDialog) {
        this.statisticsDialog = Preconditions.checkNotNull(statisticsDialog);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onShowStatistics(final ShowStatisticsEvent event) {
        statisticsDialog.show();
    }

}
