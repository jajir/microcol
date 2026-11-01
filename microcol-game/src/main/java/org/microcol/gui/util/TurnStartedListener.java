package org.microcol.gui.util;

import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.model.campaign.po.RecoveryCheckpointPolicy;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When a prepared human turn starts, the shared saving owner stores it as the
 * next turn snapshot and in the selected recovery slots.
 */
@Listener
public final class TurnStartedListener {

    private final GamePreferences gamePreferences;

    private final PersistingService persistingService;

    @Inject
    TurnStartedListener(final GamePreferences gamePreferences,
            final PersistingService persistingService) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingService = Preconditions.checkNotNull(persistingService);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        persistingService.checkpoint(event.getModel(),
                RecoveryCheckpointPolicy.slotsForTurn(
                        event.getModel().getCalendar(), event.isFreshStart(),
                        event.getPlayer().isHuman(),
                        gamePreferences.isAutosaveEnabled()));
    }

}
