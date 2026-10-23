package org.microcol.gui.screen.statistics;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.Colony;
import org.microcol.model.LaborLocation;
import org.microcol.model.LaborMember;
import org.microcol.model.Model;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Opens the place of one person of the labor report (R-0198) with the existing
 * colony screen, map tile selection and Europe screen. The person is read again
 * from the live model before anything opens, so a person that has left the
 * report opens nothing. Navigation never changes a unit or spends a move.
 */
final class LaborNavigator {

    /** Outcome of one request, shown to the player by the panel. */
    enum Result {
        /** The destination screen or tile was requested. */
        OPENED,
        /** The person is no longer part of the nation's report. */
        UNAVAILABLE,
        /** The person is still there, but its place has no destination. */
        NO_DESTINATION
    }

    private final GameModelController game;
    private final EventBus eventBus;
    private final SelectedTileManager selectedTileManager;

    @Inject
    LaborNavigator(final GameModelController game, final EventBus eventBus,
            final SelectedTileManager selectedTileManager) {
        this.game = Preconditions.checkNotNull(game);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
    }

    /**
     * Revalidate the person shown in the panel against the current report and
     * open its place. Unit IDs are only unique within one game, so a request
     * made from rows of another game than the running one is refused. Inside
     * the same game the person is found again by its ID, so a changed type or
     * place is followed and a person that has left the nation is refused.
     *
     * @param shownModel model the panel built its rows from
     * @param shown      person as the panel showed it
     * @return what happened
     */
    Result open(final Model shownModel, final LaborMember shown) {
        final Optional<LaborMember> member = currentMember(shownModel,
                shown.getUnitId());
        if (member.isEmpty()) {
            return Result.UNAVAILABLE;
        }
        final LaborLocation location = member.get().getLocation();
        if (!location.isNavigable()) {
            return Result.NO_DESTINATION;
        }
        switch (location.getKind()) {
        case COLONY:
            return openColony(location.getColony());
        case MAP:
        case NATIVE_COURSE:
        case NATIVE_MISSION:
            return openTile(location.getLocation());
        case EUROPE:
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
            return Result.OPENED;
        default:
            return Result.NO_DESTINATION;
        }
    }

    private Optional<LaborMember> currentMember(final Model shownModel,
            final int unitId) {
        if (!game.isGameModelReady() || game.getModel() != shownModel) {
            return Optional.empty();
        }
        return shownModel.getLaborReport(game.getHumanPlayer())
                .findMember(unitId);
    }

    private Result openColony(final Optional<Colony> colony) {
        final Optional<Colony> owned = colony.filter(Colony::isValid).filter(
                found -> found.getOwner().equals(game.getHumanPlayer()));
        if (owned.isEmpty()) {
            return Result.NO_DESTINATION;
        }
        eventBus.post(new ShowScreenEvent(Screen.COLONY, owned.get()));
        return Result.OPENED;
    }

    /**
     * Select the tile like a click does, then return to the map. The tile
     * selection also selects the unit standing there, as on the map.
     */
    private Result openTile(final Optional<Location> tile) {
        if (tile.isEmpty()) {
            return Result.NO_DESTINATION;
        }
        selectedTileManager.setSelectedTile(tile.get(),
                ScrollToFocusedTile.smoothScroll);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
        return Result.OPENED;
    }
}
