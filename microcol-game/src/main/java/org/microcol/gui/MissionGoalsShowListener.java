package org.microcol.gui;

import org.microcol.gui.mainmenu.ShowGoalsController;
import org.microcol.gui.mainmenu.ShowGoalsEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * When show mission goals event come class show dialog.
 */
public final class MissionGoalsShowListener {

    private final MissionGoalsDialog missionGoalsDialog;

    @Inject
    MissionGoalsShowListener(final MissionGoalsDialog missionGoalsDialog,
            final ShowGoalsController showGoalsController) {
        this.missionGoalsDialog = Preconditions.checkNotNull(missionGoalsDialog);
        showGoalsController.addListener(this::onShowGoals);
    }

    @SuppressWarnings("unused")
    private void onShowGoals(final ShowGoalsEvent event) {
        missionGoalsDialog.show();
    }

}
