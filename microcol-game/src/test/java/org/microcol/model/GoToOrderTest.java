package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.turnevent.TurnEventGoTo;
import org.microcol.model.unit.Ship;

/**
 * Persistent go-to orders. Every scenario is built deterministically before the
 * model is loaded: a grass strip east of New Amsterdam (the mission map has
 * only ocean there), no rivers, no trees on the strip, and the roads the test
 * names. Saved games are changed in their data, never in the running model.
 */
class GoToOrderTest {
    private static final String MISSION = "/maps/default-campaign-mission-2-thrive.microcol";

    /** New Amsterdam, colony 0 of the mission, reached over the strip. */
    private static final Location COLONY = Location.of(25, 14);
    /** Natural grass next to the colony; its only land approach. */
    private static final Location LANDING = Location.of(26, 15);

    private static Location strip(final int x) {
        return Location.of(x, 15);
    }

    private GameModelPo mission() {
        return new GameModelPoDao().loadFromClassPath(MISSION);
    }

    /**
     * Grass on x 27..32 of row 15, no rivers, trees on the strip, only roads.
     */
    private GameModelPo scenario(final Set<Location> roads) {
        final GameModelPo data = mission();
        final WorldMapPo map = data.getModel().getMap();
        final Map<Location, TerrainType> terrain = map.getTerrainMap();
        final Set<Location> trees = new HashSet<>(map.getTreeSet());
        for (int x = 27; x <= 32; x++) {
            terrain.put(strip(x), TerrainType.GRASSLAND);
            trees.remove(strip(x));
        }
        map.setTerrainType(terrain);
        map.setTrees(trees);
        map.setRivers(Map.of());
        map.setRiverSegments(List.of());
        map.setRoads(roads);
        return data;
    }

    private GameModelPo scenario() {
        return scenario(Set.of());
    }

    /** The mission galleon at sea east of New Amsterdam, full movement. */
    private GameModelPo shipScenario() {
        final GameModelPo data = mission();
        final UnitPo ship = unitPo(data, 3);
        ship.setPlaceHighSeas(null);
        ship.setPlaceEuropePort(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(Location.of(26, 14));
        place.setOrientation(Direction.east);
        ship.setPlaceMap(place);
        ship.setAvailableMoves(6);
        return data;
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Saves the model into the data, lets the test edit it, and reloads it. */
    private Model reload(final Model model, final GameModelPo data,
            final Consumer<GameModelPo> edit) {
        data.setModel(model.save());
        edit.accept(data);
        return load(data);
    }

    private static void terrain(final GameModelPo data, final Location location,
            final TerrainType type) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(location, type);
        data.getModel().getMap().setTerrainType(terrain);
    }

    /** A new colonist of New Amsterdam's owner on the map. */
    private Unit colonist(final Model model, final Location at,
            final int thirds) {
        final Unit unit = model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
        unit.placeToLocation(at);
        ((AbstractUnit) unit).setMovementThirds(thirds);
        return unit;
    }

    /**
     * Ends turns until the owner of the unit starts its next owner turn. The
     * owner is read from the unit of the model in use, because a reload creates
     * new player objects.
     */
    private void nextOwnerTurn(final Model model, final Unit unit) {
        final Player owner = unit.getOwner();
        model.endTurn();
        for (int turns = 0; turns < 8
                && model.getCurrentPlayer() != owner; turns++) {
            model.endTurn();
        }
        assertSame(owner, model.getCurrentPlayer());
    }

    private long notices(final Model model, final Unit unit,
            final TurnEventGoTo.Kind kind) {
        return model.getTurnEventsLocalizedMessages(unit.getOwner()).stream()
                .filter(event -> event instanceof TurnEventGoTo)
                .filter(event -> ((TurnEventGoTo) event).getKind() == kind)
                .count();
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    // ---- Issuing and exact costs
    // ---------------------------------------------

    @Test
    void threeTurnJourneyFollowsSharedCostsAndArrivesOnce() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(31), 3);
        final Location target = strip(28);
        assertTrue(model.canStartGoTo(colonist));
        assertTrue(model.canIssueGoTo(colonist, target));
        model.issueGoTo(colonist, target);
        // The issuing turn spends its allowance on the first square only.
        assertEquals(strip(30), colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
        assertEquals(Optional.of(target), model.getGoToDestination(colonist));
        assertEquals(Optional.of("28, 15"),
                model.getGoToDestinationName(colonist));
        assertFalse(model.isGoToPaused(colonist));

        nextOwnerTurn(model, colonist);
        assertEquals(strip(29), colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
        assertTrue(model.getGoToDestination(colonist).isPresent());

        nextOwnerTurn(model, colonist);
        assertEquals(target, colonist.getLocation());
        assertTrue(model.getGoToDestination(colonist).isEmpty());
        assertEquals(0, colonist.getMovementThirds());

        // Arrival cleared the order: the unit stays put and is manual again.
        nextOwnerTurn(model, colonist);
        assertEquals(target, colonist.getLocation());
        assertTrue(colonist.getMovementThirds() > 0);
        assertTrue(model.canStartGoTo(colonist));
    }

    @Test
    void zeroAllowanceIssueKeepsOrderAndMovesOnNextOwnerTurn() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(31), 0);
        model.issueGoTo(colonist, strip(28));
        assertEquals(strip(31), colonist.getLocation());
        assertEquals(Optional.of(strip(28)),
                model.getGoToDestination(colonist));
        assertFalse(model.isGoToPaused(colonist));

        nextOwnerTurn(model, colonist);
        assertEquals(strip(30), colonist.getLocation());
        assertEquals(Optional.of(strip(28)),
                model.getGoToDestination(colonist));
    }

    @Test
    void roadStepsCostThirdsAndTheWalkStopsBeforeAnUnaffordableStep() {
        final Set<Location> roads = Set.of(strip(31), strip(30), strip(29),
                strip(28), strip(27));
        final Model model = load(scenario(roads));
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoToColony(colonist, model.getColonyById(0).orElseThrow());
        // Three road steps cost one third each; the fourth is on a bare grass
        // approach that the remaining allowance cannot pay.
        assertEquals(strip(28), colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());

        nextOwnerTurn(model, colonist);
        // One more road third, then the bare square costs three thirds and the
        // two thirds left cannot pay it, so the walk stops without spending.
        assertEquals(strip(27), colonist.getLocation());
        assertEquals(2, colonist.getMovementThirds());
        assertFalse(model.isGoToPaused(colonist));
        assertTrue(model.getGoToDestination(colonist).isPresent());

        nextOwnerTurn(model, colonist);
        assertEquals(LANDING, colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());

        nextOwnerTurn(model, colonist);
        assertEquals(COLONY, colonist.getLocation());
        assertTrue(model.getGoToDestination(colonist).isEmpty());
    }

    @Test
    void orderedUnitIsNotOfferedForManualOrdersOrSelection() {
        final Set<Location> roads = Set.of(strip(31), strip(30), strip(29),
                strip(28), strip(27));
        final Model model = load(scenario(roads));
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoTo(colonist, LANDING);
        nextOwnerTurn(model, colonist);
        // Mid-route with movement left: the unit still follows its order.
        assertTrue(colonist.getMovementThirds() > 0);
        assertTrue(model.getGoToDestination(colonist).isPresent());
        assertNotSame(colonist, model.getFirstSelectableUnit().orElse(null));
        assertFalse(model.getNextUnitForCurrentPlayer(colonist)
                .map(next -> next == colonist).orElse(false));
        assertFalse(model.canStartGoTo(colonist));
        assertFalse(model.canSetSentry(colonist));
        assertFalse(model.canFortify(colonist));
        assertFalse(model.canSkipUnitTurn(colonist));
        assertEquals(Optional.of(OrdinationProblem.ACTIVE_ORDER),
                model.getOrdinationProblem(colonist));
        assertFalse(model.canIssueGoTo(colonist, strip(28)));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.setSentry(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.fortify(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.skipUnitTurn(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(colonist, strip(28)));
        // Manual movement must cancel the order first; nothing moves.
        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(colonist, Path.of(List.of(strip(27)))));
        assertThrows(IllegalStateException.class,
                () -> model.moveUnitAsFarAsPossible(colonist,
                        Path.of(List.of(strip(27)))));
        assertEquals(before, snapshot(model));
    }

    // ---- Rejected requests change nothing
    // -------------------------------------

    @Test
    void invalidForeignStaleAndIncompatibleRequestsChangeNothing() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(31), 3);
        final String before = snapshot(model);
        // Own square, an ocean square without a route, and a null colony.
        assertFalse(model.canIssueGoTo(colonist, strip(31)));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(colonist, strip(31)));
        assertFalse(model.canIssueGoTo(colonist, Location.of(40, 5)));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(colonist, Location.of(40, 5)));
        assertThrows(NullPointerException.class,
                () -> model.issueGoToColony(colonist, null));
        // A unit aboard a ship is not on the map.
        final Unit aboard = model.getUnitById(16);
        assertFalse(model.canStartGoTo(aboard));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(aboard, strip(31)));
        // Queries never change an order or the unit.
        model.getGoToColonies(colonist);
        model.getGoToDestination(colonist);
        model.isGoToPaused(colonist);
        model.canCancelGoTo(colonist);
        assertThrows(IllegalStateException.class,
                () -> model.cancelGoTo(colonist));
        assertEquals(before, snapshot(model));

        // A unit of another player is refused while it is not their turn.
        final Unit foreign = colonist(model, strip(30), 3);
        foreign.takeOver(model.getPlayerByName("Dutch's King"));
        final String foreignBefore = snapshot(model);
        assertFalse(model.canStartGoTo(foreign));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(foreign, strip(28)));
        assertEquals(foreignBefore, snapshot(model));

        // A unit that is no longer in the game is a stale request.
        final Unit stale = colonist(model, strip(29), 3);
        model.destroyUnit(stale);
        assertFalse(model.canStartGoTo(stale));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(stale, strip(28)));
        assertThrows(IllegalStateException.class,
                () -> model.cancelGoTo(stale));
    }

    @Test
    void sentryUnitIsRefusedAndKeepsItsOrder() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(31), 3);
        model.setSentry(colonist);
        final String before = snapshot(model);
        assertFalse(model.canStartGoTo(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.issueGoTo(colonist, strip(28)));
        assertEquals(before, snapshot(model));
        model.activateSentry(colonist);
        assertTrue(model.canIssueGoTo(colonist, strip(28)));
    }

    // ---- Cancel
    // ----------------------------------------------------------------

    @Test
    void cancelClearsOrderWithoutMovingOrRestoringMovement() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoTo(colonist, strip(28));
        final int movementBefore = colonist.getMovementThirds();
        assertTrue(model.canCancelGoTo(colonist));
        model.cancelGoTo(colonist);
        assertTrue(model.getGoToDestination(colonist).isEmpty());
        assertEquals(strip(30), colonist.getLocation());
        assertEquals(movementBefore, colonist.getMovementThirds());
        assertFalse(model.canCancelGoTo(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.cancelGoTo(colonist));

        nextOwnerTurn(model, colonist);
        assertEquals(strip(30), colonist.getLocation());
        assertTrue(model.getGoToDestination(colonist).isEmpty());
    }

    // ---- Save and Continue
    // -----------------------------------------------------

    @Test
    void savedMidRouteOrderContinuesWithoutAdvancingOnLoad() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoTo(colonist, strip(28));
        final String before = snapshot(model);

        data.setModel(model.save());
        final Model restored = load(data);
        final Unit loaded = restored.getUnitById(colonist.getId());
        assertEquals(strip(30), loaded.getLocation());
        assertEquals(0, loaded.getMovementThirds());
        assertEquals(Optional.of(strip(28)),
                restored.getGoToDestination(loaded));
        assertFalse(restored.isGoToPaused(loaded));
        // Loading moved nothing, repeated no arrival and rolled nothing.
        assertEquals(before, snapshot(restored));

        nextOwnerTurn(restored, loaded);
        assertEquals(strip(29), loaded.getLocation());
    }

    @Test
    void invalidSavedOrdersAreRejectedOnLoadWithoutGuessing() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoTo(colonist, strip(28));
        final int id = colonist.getId();
        data.setModel(model.save());

        final Consumer<Consumer<UnitPo>> rejected = edit -> {
            final GameModelPo copy = new GameModelPoDao()
                    .loadFromClassPath(MISSION);
            copy.setModel(model.save());
            edit.accept(unitPo(copy, id));
            assertThrows(IllegalArgumentException.class, () -> load(copy));
        };
        // An order without its destination, off the map, or on the unit itself.
        rejected.accept(unit -> unit.setGoToDestination(null));
        rejected.accept(unit -> unit.setGoToDestination(Location.of(999, 999)));
        rejected.accept(unit -> unit.setGoToDestination(strip(30)));
        // A missing name or a negative colony identity.
        rejected.accept(unit -> unit.setGoToDestinationName(null));
        rejected.accept(unit -> unit.setGoToColonyId(-1));
    }

    // ---- Reroute, pause and resume
    // ---------------------------------------------

    @Test
    void obstructionIsRoutedAroundAndKeepsTheSameTarget() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(32), 3);
        model.issueGoTo(colonist, strip(28));
        assertEquals(strip(31), colonist.getLocation());

        // The square ahead becomes sea; a second row offers another way.
        final Model blocked = reload(model, data, edited -> {
            terrain(edited, strip(30), TerrainType.OCEAN);
            terrain(edited, Location.of(30, 16), TerrainType.GRASSLAND);
            terrain(edited, Location.of(29, 16), TerrainType.GRASSLAND);
        });
        final Unit rerouted = blocked.getUnitById(colonist.getId());
        nextOwnerTurn(blocked, rerouted);
        assertEquals(Location.of(30, 16), rerouted.getLocation());
        assertEquals(Optional.of(strip(28)),
                blocked.getGoToDestination(rerouted));
        assertFalse(blocked.isGoToPaused(rerouted));
        assertEquals(0, notices(blocked, rerouted, TurnEventGoTo.Kind.PAUSED));

        nextOwnerTurn(blocked, rerouted);
        assertEquals(29, rerouted.getLocation().getX());
        nextOwnerTurn(blocked, rerouted);
        assertEquals(strip(28), rerouted.getLocation());
        assertTrue(blocked.getGoToDestination(rerouted).isEmpty());
    }

    @Test
    void pauseKeepsTargetNoticesOnceAcrossSaveAndResumesWhenRouteReturns() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(31), 3);
        model.issueGoTo(colonist, strip(28));
        final int id = colonist.getId();

        // The destination becomes sea, so no legal route remains.
        final Model paused = reload(model, data,
                edited -> terrain(edited, strip(28), TerrainType.OCEAN));
        Unit pausedColonist = paused.getUnitById(id);
        nextOwnerTurn(paused, pausedColonist);
        assertEquals(strip(30), pausedColonist.getLocation());
        assertTrue(paused.isGoToPaused(pausedColonist));
        assertEquals(Optional.of(strip(28)),
                paused.getGoToDestination(pausedColonist));
        assertEquals(1,
                notices(paused, pausedColonist, TurnEventGoTo.Kind.PAUSED));

        // Save and Continue in the same turn keeps the paused order and notice
        // and never raises the notice again.
        final Model continued = reload(paused, data, edited -> {
        });
        final Unit continuedColonist = continued.getUnitById(id);
        assertTrue(continued.isGoToPaused(continuedColonist));
        assertEquals(strip(30), continuedColonist.getLocation());
        assertEquals(1, notices(continued, continuedColonist,
                TurnEventGoTo.Kind.PAUSED));

        // Next owner turn: still paused, retained target, no duplicate notice.
        nextOwnerTurn(continued, continuedColonist);
        assertTrue(continued.isGoToPaused(continuedColonist));
        assertEquals(Optional.of(strip(28)),
                continued.getGoToDestination(continuedColonist));
        assertEquals(strip(30), continuedColonist.getLocation());
        assertEquals(0, notices(continued, continuedColonist,
                TurnEventGoTo.Kind.PAUSED));
        // The player can still cancel a paused order.
        assertTrue(continued.canCancelGoTo(continuedColonist));

        // The route returns: the order resumes from where it paused.
        final Model resumed = reload(continued, data,
                edited -> terrain(edited, strip(28), TerrainType.GRASSLAND));
        final Unit resumedColonist = resumed.getUnitById(id);
        nextOwnerTurn(resumed, resumedColonist);
        assertEquals(strip(29), resumedColonist.getLocation());
        assertFalse(resumed.isGoToPaused(resumedColonist));
    }

    // ---- Named destinations
    // ----------------------------------------------------

    @Test
    void namedColonyJourneyTakesThreeOwnerTurnsAndArrivesInTheColony() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(28), 3);
        final Colony newAmsterdam = model.getColonyById(0).orElseThrow();
        assertTrue(model.getGoToColonies(colonist).contains(newAmsterdam));
        assertTrue(model.getGoToColonies(colonist).stream()
                .allMatch(colony -> colony.getOwner() == colonist.getOwner()));
        model.issueGoToColony(colonist, newAmsterdam);
        assertEquals(strip(27), colonist.getLocation());
        assertEquals(Optional.of(COLONY), model.getGoToDestination(colonist));
        assertEquals(Optional.of("New Amsterdam"),
                model.getGoToDestinationName(colonist));

        nextOwnerTurn(model, colonist);
        assertEquals(LANDING, colonist.getLocation());
        assertTrue(model.getGoToDestination(colonist).isPresent());

        nextOwnerTurn(model, colonist);
        assertEquals(COLONY, colonist.getLocation());
        assertTrue(model.getGoToDestination(colonist).isEmpty());
    }

    @Test
    void removedNamedColonyCancelsTheOrderWithAPersistedReason() {
        final Model model = load(scenario());
        final Unit colonist = colonist(model, strip(28), 3);
        model.issueGoToColony(colonist, model.getColonyById(0).orElseThrow());
        assertEquals(strip(27), colonist.getLocation());

        model.destroyColony(model.getColonyById(0).orElseThrow());
        nextOwnerTurn(model, colonist);
        assertTrue(model.getGoToDestination(colonist).isEmpty());
        assertEquals(strip(27), colonist.getLocation());
        assertEquals(1,
                notices(model, colonist, TurnEventGoTo.Kind.CANCELLED_REMOVED));
        // The unit is manual again and no further turn moves it.
        nextOwnerTurn(model, colonist);
        assertEquals(strip(27), colonist.getLocation());
        assertEquals(0,
                notices(model, colonist, TurnEventGoTo.Kind.CANCELLED_REMOVED));
    }

    @Test
    void hostileNamedColonyCancelsTheOrderWithoutEnteringIt() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(28), 3);
        model.issueGoToColony(colonist, model.getColonyById(0).orElseThrow());
        final int id = colonist.getId();

        // The Crown now owns New Amsterdam, as after a lost defense battle.
        final Model lost = reload(model, data, edited -> {
            edited.getModel().getColonies().stream()
                    .filter(colony -> colony.getId() == 0).findFirst()
                    .orElseThrow().setOwnerName("Dutch's King");
            // Its residents: field workers 0-2 and building workers 4 and 5.
            for (final int worker : new int[] { 0, 1, 2, 4, 5 }) {
                unitPo(edited, worker).setOwnerId("Dutch's King");
            }
        });
        final Unit loaded = lost.getUnitById(id);
        assertTrue(lost.getGoToDestination(loaded).isPresent());
        nextOwnerTurn(lost, loaded);
        assertTrue(lost.getGoToDestination(loaded).isEmpty());
        assertEquals(strip(27), loaded.getLocation());
        assertEquals(1, notices(lost, loaded,
                TurnEventGoTo.Kind.CANCELLED_NOT_FRIENDLY));
        // The reason survives Save and Continue within that turn.
        final Model continued = reload(lost, data, edited -> {
        });
        assertEquals(1, notices(continued, continued.getUnitById(id),
                TurnEventGoTo.Kind.CANCELLED_NOT_FRIENDLY));
        // A colony of someone else is never offered as a destination.
        assertTrue(lost.getGoToColonies(loaded).stream()
                .noneMatch(colony -> colony.getId() == 0));
    }

    @Test
    void staleColonyChoiceAndMovedColonyIdentityAreRejected() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit colonist = colonist(model, strip(28), 3);
        final Colony newAmsterdam = model.getColonyById(0).orElseThrow();
        model.destroyColony(newAmsterdam);
        final String before = snapshot(model);
        // The colony the player chose no longer exists.
        assertThrows(IllegalStateException.class,
                () -> model.issueGoToColony(colonist, newAmsterdam));
        assertEquals(before, snapshot(model));

        // A saved named order must point at its colony's real square.
        final GameModelPo savedData = scenario();
        final Model second = load(savedData);
        final Unit other = colonist(second, strip(28), 3);
        second.issueGoToColony(other, second.getColonyById(0).orElseThrow());
        final int id = other.getId();
        savedData.setModel(second.save());
        unitPo(savedData, id).setGoToDestination(strip(30));
        assertThrows(IllegalArgumentException.class, () -> load(savedData));
    }

    // ---- Ships
    // -----------------------------------------------------------------

    @Test
    void shipFollowsASeaOrderOverThreeTurnsWithoutDepartingTheMap() {
        final Model model = load(shipScenario());
        final Ship ship = (Ship) model.getUnitById(3);
        final Location target = Location.of(44, 14);
        assertTrue(model.canStartGoTo(ship));
        assertTrue(model.canIssueGoTo(ship, target));
        // The high-sea strip beyond the map edge is a voyage, not a go-to.
        assertFalse(model.canIssueGoTo(ship, Location.of(50, 14)));
        model.issueGoTo(ship, target);
        // Diagonal and straight sea steps cost the same, so the row of an
        // intermediate square is a tie choice of the shared finder. Every
        // cheapest route gains one column per step: six steps per turn.
        assertEquals(32, ship.getLocation().getX());
        assertEquals(12, ship.getLocation().getDistanceChebyshev(target));
        assertEquals(0, ship.getMovementThirds());
        assertTrue(model.getGoToDestination(ship).isPresent());

        nextOwnerTurn(model, ship);
        final Location midway = ship.getLocation();
        assertEquals(38, midway.getX());
        assertEquals(6, midway.getDistanceChebyshev(target));
        assertEquals(0, ship.getMovementThirds());
        assertTrue(model.getGoToDestination(ship).isPresent());
        assertFalse(ship.isAtHighSea());
        // Save and Continue keep a ship order and do not sail on load.
        final Model restored = reload(model, shipScenario(), edited -> {
        });
        final Ship loaded = (Ship) restored.getUnitById(3);
        assertEquals(midway, loaded.getLocation());
        assertEquals(0, loaded.getMovementThirds());
        assertEquals(Optional.of(target), restored.getGoToDestination(loaded));

        nextOwnerTurn(restored, loaded);
        assertEquals(target, loaded.getLocation());
        assertTrue(restored.getGoToDestination(loaded).isEmpty());
        assertFalse(loaded.isAtHighSea());
    }

    @Test
    void damagedShipLosesItsOrderWithAPersistedReason() {
        final GameModelPo data = shipScenario();
        final ConstructionPo drydock = new ConstructionPo();
        drydock.setType(ConstructionType.DRYDOCK);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(drydock);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        model.issueGoTo(ship, Location.of(44, 14));
        assertTrue(model.getGoToDestination(ship).isPresent());

        model.damageShip(ship);
        assertTrue(ship.isDamaged());
        assertTrue(model.getGoToDestination(ship).isEmpty());
        assertEquals(1,
                notices(model, ship, TurnEventGoTo.Kind.CANCELLED_DAMAGED));
        assertFalse(model.canStartGoTo(ship));
    }

    // ---- Concurrent departure during a user-interface query
    // ------------------

    @Test
    void routeQueryReadsThePlaceOnceWhileTheUnitLeavesTheMap() {
        final Model model = load(scenario());
        final AbstractUnit colonist = (AbstractUnit) colonist(model, strip(31),
                3);
        final Optional<List<Location>> expected = colonist
                .findGoToRoute(strip(28));
        assertTrue(expected.isPresent());
        final Place mapPlace = colonist.getPlace();
        // The candidate check has passed, then the moving thread departs: every
        // fresh location query now fails, as in the reported user-interface
        // refresh. Only the place read once by the query is safe.
        final AbstractUnit reader = spy(colonist);
        doThrow(new IllegalStateException("Unit have to be at map"))
                .when(reader).getLocation();
        doReturn(mapPlace).when(reader).getPlace();
        assertEquals(expected, reader.findGoToRoute(strip(28)));
        // A snapshot taken after the departure gives no route and no failure.
        doReturn(mock(PlaceHighSea.class)).when(reader).getPlace();
        assertEquals(Optional.empty(), reader.findGoToRoute(strip(28)));
        // The real unit and its ordinary go-to admission are unchanged.
        assertEquals(strip(31), colonist.getLocation());
        assertTrue(model.canIssueGoTo(colonist, strip(28)));
        assertFalse(model.canIssueGoTo(colonist, strip(31)));
    }
}
