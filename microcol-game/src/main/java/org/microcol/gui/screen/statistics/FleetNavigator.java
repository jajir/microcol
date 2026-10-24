package org.microcol.gui.screen.statistics;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.FleetReport;
import org.microcol.model.FleetReport.ForeignShip;
import org.microcol.model.FleetReport.OwnShip;
import org.microcol.model.LaborLocation;
import org.microcol.model.Location;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Opens the place of one ship of the fleet report (R-0202) with the existing
 * map tile selection and Europe screen. The ship is read again from the live
 * report before anything opens: an own ship must still belong to the nation,
 * and a foreign ship must still be in present sight. Navigation never changes a
 * ship, a cargo or a voyage.
 */
final class FleetNavigator {

    /** Outcome of one request, shown to the player by the panel. */
    enum Result {
        /** The destination screen or tile was requested. */
        OPENED,
        /** The ship is no longer part of the report. */
        UNAVAILABLE,
        /** The ship is still there, but its place has no destination. */
        NO_DESTINATION
    }

    private final GameModelController game;
    private final EventBus eventBus;
    private final SelectedTileManager selectedTileManager;

    @Inject
    FleetNavigator(final GameModelController game, final EventBus eventBus,
            final SelectedTileManager selectedTileManager) {
        this.game = Preconditions.checkNotNull(game);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
    }

    /**
     * Revalidate an own ship shown in the panel against the running game and
     * open its place. Unit IDs repeat across games, so rows of another game are
     * refused by model identity before the ship is looked at.
     *
     * @param shownModel model the panel built its rows from
     * @param shown      own ship as the panel showed it
     * @return what happened
     */
    Result openOwn(final Model shownModel, final OwnShip shown) {
        final Optional<OwnShip> ship = currentReport(shownModel)
                .flatMap(report -> report.findOwnShip(shown.getUnitId()));
        if (ship.isEmpty()) {
            return Result.UNAVAILABLE;
        }
        final LaborLocation location = ship.get().getLocation();
        if (!location.isNavigable()) {
            return Result.NO_DESTINATION;
        }
        switch (location.getKind()) {
        case MAP:
            return openTile(location.getLocation());
        case EUROPE:
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
            return Result.OPENED;
        default:
            return Result.NO_DESTINATION;
        }
    }

    /**
     * Revalidate a foreign ship shown in the panel. Only a ship that is in
     * present sight now is found, so its tile opens only while it is visible.
     *
     * @param shownModel model the panel built its rows from
     * @param shown      foreign ship as the panel showed it
     * @return what happened
     */
    Result openForeign(final Model shownModel, final ForeignShip shown) {
        final Optional<ForeignShip> ship = currentReport(shownModel)
                .flatMap(report -> report.findForeignShip(shown.getUnitId()));
        if (ship.isEmpty()) {
            return Result.UNAVAILABLE;
        }
        return openTile(Optional.of(ship.get().getLocation()));
    }

    private Optional<FleetReport> currentReport(final Model shownModel) {
        if (!game.isGameModelReady() || game.getModel() != shownModel) {
            return Optional.empty();
        }
        return Optional.of(shownModel.getFleetReport(game.getHumanPlayer()));
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
