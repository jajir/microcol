package org.akropia.gui.screen.turnreport;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.Listener;
import org.akropia.model.Model;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.turnevent.TurnEventCriticality;
import org.akropia.model.turnevent.TurnEventGoTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * When player is on turn than verify if there are any turn events and when they
 * are than show dialog to show them. Routine events open the dialog only while
 * the turn summary preference is on; critical events always do.
 * <p>
 * Precedence on a fresh human turn: the report opens first. When Europe opening
 * on arrival is on and a ship arrived, Europe waits until the report is closed;
 * without a report the arrival opens Europe directly. Only one screen is posted
 * at turn start. Continuing a saved game never repeats this.
 * </p>
 */
@Listener
public final class ShowTurnEvensOnTurnStartedEvent {

    private final static Logger logger = LoggerFactory
            .getLogger(ShowTurnEvensOnTurnStartedEvent.class);

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    private final GamePreferences gamePreferences;

    private final EuropeOpeningRequest europeOpeningRequest;

    @Inject
    ShowTurnEvensOnTurnStartedEvent(final EventBus eventBus,
            final GameModelController gameModelController,
            final GamePreferences gamePreferences,
            final EuropeOpeningRequest europeOpeningRequest) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.europeOpeningRequest = Preconditions
                .checkNotNull(europeOpeningRequest);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        if (!isCorrectPlayer(event)) {
            return;
        }
        // A request belongs to the turn that made it; nothing carries over.
        europeOpeningRequest.clear();
        if (!event.isFreshStart()) {
            return;
        }
        final boolean openEurope = gamePreferences.isOpenEuropeOnArrival()
                && isShipArrivedInEurope();
        if (isReportDue()) {
            logger.debug("Turn started and turn event dialog will be shown.");
            eventBus.post(new ShowScreenEvent(Screen.TURN_REPORT));
            if (openEurope) {
                europeOpeningRequest.request();
            }
        } else if (openEurope) {
            logger.debug("Turn started and Europe opens after arrival.");
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
    }

    private boolean isCorrectPlayer(final TurnStartedEvent event) {
        return gameModelController.getHumanPlayer().equals(event.getPlayer());
    }

    /**
     * Reads only the saved notices; opening the report never changes them.
     */
    private boolean isReportDue() {
        final Model model = gameModelController.getModel();
        if (model
                .getTurnEventsLocalizedMessages(
                        gameModelController.getHumanPlayer())
                .stream().noneMatch(TeService::isVisibleNotice)) {
            return false;
        }
        if (gamePreferences.isShowTurnSummary()) {
            return true;
        }
        return model
                .getTurnEventsLocalizedMessages(
                        gameModelController.getHumanPlayer())
                .stream().anyMatch(TurnEventCriticality::isCritical);
    }

    private boolean isShipArrivedInEurope() {
        return gameModelController.getModel()
                .getTurnEventsLocalizedMessages(
                        gameModelController.getHumanPlayer())
                .stream().filter(TurnEventGoTo.class::isInstance)
                .map(TurnEventGoTo.class::cast).anyMatch(event -> event
                        .getKind() == TurnEventGoTo.Kind.ARRIVED_EUROPE);
    }
}
