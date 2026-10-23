package org.microcol.gui.screen.statistics;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.Colony;
import org.microcol.model.ColonyAdvisorReport.ColonyStatus;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Opens one colony of the colony advisor (R-0200) with the existing colony
 * screen. The colony is read again from the live game before anything opens, so
 * a colony that is no longer an owned valid colony opens nothing. Navigation
 * never changes a colony, a project or a unit.
 */
final class ColonyAdvisorNavigator {

    /** Outcome of one request, shown to the player by the panel. */
    enum Result {
        /** The colony screen was requested. */
        OPENED,
        /**
         * The colony is no longer an owned valid colony of the running game.
         */
        UNAVAILABLE
    }

    private final GameModelController game;
    private final EventBus eventBus;

    @Inject
    ColonyAdvisorNavigator(final GameModelController game,
            final EventBus eventBus) {
        this.game = Preconditions.checkNotNull(game);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    /**
     * Revalidate the colony shown in the panel against the running game and
     * open it. Colony IDs repeat across games, so a request made from rows of
     * another game is refused by model identity before the colony is looked at.
     *
     * @param shownModel model the panel built its rows from
     * @param shown      colony as the panel showed it
     * @return what happened
     */
    Result open(final Model shownModel, final ColonyStatus shown) {
        if (!game.isGameModelReady() || game.getModel() != shownModel) {
            return Result.UNAVAILABLE;
        }
        final Player human = game.getHumanPlayer();
        final Optional<Colony> owned = human.getColonies().stream()
                .filter(colony -> colony == shown.getColony())
                .filter(colony -> colony.getOwner() == human)
                .filter(Colony::isValid).findFirst();
        if (owned.isEmpty()) {
            return Result.UNAVAILABLE;
        }
        eventBus.post(new ShowScreenEvent(Screen.COLONY, owned.get()));
        return Result.OPENED;
    }
}
