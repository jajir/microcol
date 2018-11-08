package org.microcol.gui;

import org.microcol.gui.mainmenu.ShowGoalsEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When show mission goals event come class show dialog.
 */
@Listener
public final class MissionGoalsShowListener {

    private final MissionGoalsDialog missionGoalsDialog;

    @Inject
    MissionGoalsShowListener(final MissionGoalsDialog missionGoalsDialog) {
        this.missionGoalsDialog = Preconditions.checkNotNull(missionGoalsDialog);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onShowGoals(final ShowGoalsEvent event) {
        missionGoalsDialog.show();
    }

}
