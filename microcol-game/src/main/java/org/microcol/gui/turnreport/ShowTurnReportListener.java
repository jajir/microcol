package org.microcol.gui.turnreport;

import org.microcol.gui.mainmenu.ShowTurnReportEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Bind showTurnReportEvent to turn report dialog.
 */
@Listener
public final class ShowTurnReportListener {

    private final TurnReportDialog turnReportDialog;

    @Inject
    ShowTurnReportListener(final TurnReportDialog turnReportDialog) {
        this.turnReportDialog = Preconditions.checkNotNull(turnReportDialog);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onShowTurnReportController(final ShowTurnReportEvent event) {
        turnReportDialog.show();
    }

}
