package org.microcol.gui.screen.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.Direction;
import org.microcol.model.FleetReport.ForeignShip;
import org.microcol.model.FleetReport.OwnShip;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlaceShipRepairPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.common.eventbus.EventBus;

/**
 * R-0202 navigation guard: Show opens an own ship's Europe or tile, and a
 * visible foreign ship's tile, only when the live report of the running game
 * still holds the ship. A replaced game, a stopped game, a lost owner, a ship
 * out of sight and a ship under repair open nothing and change no tile
 * selection. The world is the saved free-play map, which has the real Dutch and
 * French players and open ocean around the square (36, 8); the ships added to
 * it use IDs above every saved unit.
 */
class FleetNavigatorTest {

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String DUTCH = "Dutch";

    private static final String FRANCE = "France";

    private static final int OWN_PORT_ID = 9001;

    private static final int OWN_MAP_ID = 9002;

    private static final int FOREIGN_ID = 9003;

    private static final int REPAIR_ID = 9004;

    /** Ocean square of the Dutch caravel; no saved unit stands near it. */
    private static final Location OWN_TILE = Location.of(36, 8);

    /** Ocean square next to the caravel, inside its sight radius of two. */
    private static final Location FOREIGN_TILE = Location.of(37, 8);

    /**
     * Ocean square four squares from the caravel, outside every Dutch sight.
     */
    private static final Location FAR_TILE = Location.of(32, 4);

    private final EventBus eventBus = mock(EventBus.class);

    private final GameModelController game = mock(GameModelController.class);

    private final SelectedTileManager selectedTiles = new SelectedTileManager(
            eventBus);

    private final FleetNavigator navigator = new FleetNavigator(game, eventBus,
            selectedTiles);

    /**
     * The free-play world with a Dutch galleon in the European port, a Dutch
     * caravel on the ocean, a French galleon next to it and a Dutch merchantman
     * under repair. The map saves one more Dutch galleon, which is left alone.
     */
    private static Model world() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath(FREE_PLAY);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(FRANCE);
        final List<UnitPo> units = data.getModel().getUnits();
        units.add(inPort(OWN_PORT_ID, UnitType.GALLEON, DUTCH));
        units.add(onMap(OWN_MAP_ID, UnitType.CARAVEL, DUTCH, OWN_TILE));
        units.add(onMap(FOREIGN_ID, UnitType.GALLEON, FRANCE, FOREIGN_TILE));
        units.add(repairing(REPAIR_ID));
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Fields every saved unit carries; a ship adds its own place. */
    private static UnitPo unit(final int id, final UnitType type,
            final String owner) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(owner);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        unit.setCargo(new CargoPo());
        for (int hold = 0; hold < type.getCargoCapacity(); hold++) {
            unit.getCargo().getSlots().add(new CargoSlotPo());
        }
        return unit;
    }

    private static UnitPo inPort(final int id, final UnitType type,
            final String owner) {
        final UnitPo ship = unit(id, type, owner);
        ship.setPlaceEuropePort(new PlaceEuropePortPo());
        return ship;
    }

    private static UnitPo onMap(final int id, final UnitType type,
            final String owner, final Location at) {
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.north);
        final UnitPo ship = unit(id, type, owner);
        ship.setPlaceMap(place);
        return ship;
    }

    /**
     * A Dutch ship in the transfer to the European repair, shaped like the ship
     * of the F54 fixture: no movement and no other place.
     */
    private static UnitPo repairing(final int id) {
        final PlaceShipRepairPo repair = new PlaceShipRepairPo();
        repair.setOrigin(OWN_TILE);
        repair.setOriginWasPort(false);
        repair.setEuropeDestination(true);
        repair.setDestinationColonyId(null);
        repair.setPhase("TRANSFER");
        repair.setRemainingPhaseUpdates(1);
        repair.setRepairDuration(2);
        final UnitPo ship = unit(id, UnitType.MERCHANTMAN, DUTCH);
        ship.setMovementThirds(0);
        ship.setPlaceShipRepair(repair);
        return ship;
    }

    private void run(final Model running) {
        when(game.isGameModelReady()).thenReturn(true);
        when(game.getModel()).thenReturn(running);
        when(game.getHumanPlayer()).thenReturn(running.getPlayerByName(DUTCH));
    }

    private static OwnShip own(final Model model, final int unitId) {
        return model.getFleetReport(model.getPlayerByName(DUTCH))
                .findOwnShip(unitId).orElseThrow();
    }

    private static ForeignShip foreign(final Model model) {
        return model.getFleetReport(model.getPlayerByName(DUTCH))
                .findForeignShip(FOREIGN_ID).orElseThrow();
    }

    @Test
    void anOwnPortShipOpensEuropeOnce() {
        final Model shown = world();
        run(shown);

        assertEquals(FleetNavigator.Result.OPENED,
                navigator.openOwn(shown, own(shown, OWN_PORT_ID)));
        verify(eventBus, times(1)).post(any());
    }

    @Test
    void anOwnShipOnTheMapOpensItsTile() {
        final Model shown = world();
        run(shown);

        assertEquals(FleetNavigator.Result.OPENED,
                navigator.openOwn(shown, own(shown, OWN_MAP_ID)));
        assertEquals(OWN_TILE, selectedTiles.getSelectedTile().orElseThrow());
        // The tile selection and the map screen are two events.
        verify(eventBus, times(2)).post(any());
    }

    @Test
    void aVisibleForeignShipOpensItsTileWithoutAnyOwnState() {
        final Model shown = world();
        run(shown);

        assertEquals(FleetNavigator.Result.OPENED,
                navigator.openForeign(shown, foreign(shown)));
        assertEquals(FOREIGN_TILE,
                selectedTiles.getSelectedTile().orElseThrow());
    }

    @Test
    void aShipUnderRepairOpensNothingAndChangesNoSelection() {
        final Model shown = world();
        run(shown);

        assertEquals(FleetNavigator.Result.NO_DESTINATION,
                navigator.openOwn(shown, own(shown, REPAIR_ID)));
        assertFalse(selectedTiles.getSelectedTile().isPresent());
        verifyZeroInteractions(eventBus);
    }

    @Test
    void rowsOfAReplacedGameOpenNothingEvenWithTheSameUnitIds() {
        final Model shown = world();
        final Model replacement = world();
        assertTrue(replacement.getUnitById(OWN_MAP_ID) != null,
                "The replacement game has a ship with the same ID");
        run(replacement);

        assertEquals(FleetNavigator.Result.UNAVAILABLE,
                navigator.openOwn(shown, own(shown, OWN_MAP_ID)));
        assertEquals(FleetNavigator.Result.UNAVAILABLE,
                navigator.openForeign(shown, foreign(shown)));
        assertFalse(selectedTiles.getSelectedTile().isPresent());
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aStoppedGameOpensNothing() {
        final Model shown = world();
        run(shown);
        when(game.isGameModelReady()).thenReturn(false);

        assertEquals(FleetNavigator.Result.UNAVAILABLE,
                navigator.openOwn(shown, own(shown, OWN_MAP_ID)));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aShipThatChangedOwnerIsRefusedAsOwnShip() {
        final Model shown = world();
        final OwnShip row = own(shown, OWN_MAP_ID);
        run(shown);
        shown.getUnitById(OWN_MAP_ID).takeOver(shown.getPlayerByName(FRANCE));

        assertEquals(FleetNavigator.Result.UNAVAILABLE,
                navigator.openOwn(shown, row));
        assertFalse(selectedTiles.getSelectedTile().isPresent());
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aForeignShipOutOfSightIsRefusedAfterItMoves() {
        final Model shown = world();
        final ForeignShip row = foreign(shown);
        run(shown);
        shown.getUnitById(FOREIGN_ID).placeToLocation(FAR_TILE);
        assertFalse(shown.getPlayerByName(DUTCH).isCurrentlyVisible(FAR_TILE),
                "The far square must be out of the owner's sight");

        assertEquals(FleetNavigator.Result.UNAVAILABLE,
                navigator.openForeign(shown, row));
        assertFalse(selectedTiles.getSelectedTile().isPresent());
        verifyZeroInteractions(eventBus);
    }
}
