package org.microcol.gui.turnreport;

import org.microcol.gui.mainmenu.ShowTurnReportController;
import org.microcol.gui.mainmenu.ShowTurnReportEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Bind showTurnReportEvent to turn report dialog.
 */
public class ShowTurnReportListener {

    private final TurnReportDialog turnReportDialog;

    @Inject
    ShowTurnReportListener(final ShowTurnReportController showTurnReportController,
            final TurnReportDialog turnReportDialog) {
        this.turnReportDialog = Preconditions.checkNotNull(turnReportDialog);
        showTurnReportController.addListener(this::onShowTurnReportController);
    }

    @SuppressWarnings("unused")
    private void onShowTurnReportController(final ShowTurnReportEvent event) {
        turnReportDialog.show();
    }

}
