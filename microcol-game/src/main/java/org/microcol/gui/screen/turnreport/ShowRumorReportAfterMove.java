package org.microcol.gui.screen.turnreport;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.turnevent.TurnEventCoastalBattery;
import org.microcol.model.turnevent.TurnEventLostCityRumor;
import org.microcol.model.turnevent.TurnEventPatrolInterception;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Shows the turn report right after a human move that resolved a lost-city
 * rumor, a patrol slowdown or a coastal battery shot, so the actual result is
 * visible at once. The result itself is already saved in the model; this
 * listener only presents it. Manual moves, landings and go-to walks of the turn
 * all end with a finished move. Notices that were already there when the turn
 * started or the game was continued are not shown again; the turn report button
 * still lists them.
 */
@Listener
public final class ShowRumorReportAfterMove {

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    /** Rumor notices of the human player that were already presented. */
    private volatile int presentedNotices;

    @Inject
    ShowRumorReportAfterMove(final EventBus eventBus,
            final GameModelController gameModelController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        presentedNotices = countNotices();
    }

    @Subscribe
    private void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (!event.getUnit().getOwner().isHuman()) {
            return;
        }
        final int notices = countNotices();
        final boolean added = notices > presentedNotices;
        presentedNotices = notices;
        if (added) {
            eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT));
        }
    }

    private int countNotices() {
        return (int) gameModelController.getModel()
                .getTurnEventsLocalizedMessages(
                        gameModelController.getHumanPlayer())
                .stream()
                .filter(event -> event instanceof TurnEventLostCityRumor
                        || event instanceof TurnEventPatrolInterception
                        || event instanceof TurnEventCoastalBattery)
                .count();
    }
}
