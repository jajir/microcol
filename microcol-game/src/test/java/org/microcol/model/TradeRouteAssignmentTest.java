package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Trade route assignment on the shared F108 scenario. Dutch's galleon 3 stands
 * in Delft's port, and Delft is a coastal port of the Dutch player, so a sea
 * route from Delft to Europe is legal for it as it is for route creation.
 */
class TradeRouteAssignmentTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F108-trade-route-cargo.microcol");

    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final Location SEA = Location.of(29, 16);

    private static Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static UnitWithCargo ship(final Model model) {
        return (UnitWithCargo) model.getAllUnits().stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
    }

    private static TradeRoute createSeaRoute(final Model model,
            final Player dutch, final String name) {
        return model.createTradeRoute(dutch, name, TradeRouteKind.SEA, DELFT,
                List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
    }

    @Test
    void assignmentStoresFirstStopWithoutMovingTheShip() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        final Location before = ship.getLocation();
        final int movement = ship.getMovementThirds();

        assertEquals(Optional.empty(),
                model.getTradeRouteAssignmentProblem(ship, route, 0));
        model.assignTradeRoute(ship, route, 0);

        final TradeRouteAssignment assignment = model
                .getTradeRouteAssignment(ship).orElseThrow();
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(0, assignment.getStopIndex());
        assertEquals(Optional.empty(), assignment.getPause());
        assertEquals(before, ship.getLocation());
        assertEquals(movement, ship.getMovementThirds());
    }

    @Test
    void secondAssignmentIsRefusedAndKeepsTheFirst() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        model.assignTradeRoute(ship, route, 0);

        assertEquals(Optional.of(TradeRouteAssignmentProblem.CARRIER_BUSY),
                model.getTradeRouteAssignmentProblem(ship, route, 1));
        assertThrows(IllegalArgumentException.class,
                () -> model.assignTradeRoute(ship, route, 1));

        final TradeRouteAssignment assignment = model
                .getTradeRouteAssignment(ship).orElseThrow();
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(0, assignment.getStopIndex());
    }

    @Test
    void assignedCarrierCannotBeMovedByHand() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        model.assignTradeRoute(ship, route, 0);

        assertThrows(IllegalStateException.class,
                () -> model.moveUnit(ship, Path.of(List.of(SEA))));
    }

    @Test
    void savedAssignmentAndItsPauseRestoreWithoutMoving() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        final Model model = make(data);
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        model.assignTradeRoute(ship, route, 0);
        model.getTradeRouteAssignment(ship).orElseThrow()
                .setPause(TradeRoutePause.STOP_UNREACHABLE);
        final Location before = ship.getLocation();

        data.setModel(model.save());
        final Model restored = make(data);

        final TradeRouteAssignment assignment = restored
                .getTradeRouteAssignment(ship(restored)).orElseThrow();
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(0, assignment.getStopIndex());
        assertEquals(Optional.of(TradeRoutePause.STOP_UNREACHABLE),
                assignment.getPause());
        assertEquals(before, ship(restored).getLocation());
    }

    @Test
    void savedAssignmentNamingAMissingRouteIsRejected() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        final Model model = make(data);
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        model.assignTradeRoute(ship, createSeaRoute(model, dutch, "Circuit"),
                0);

        data.setModel(model.save());
        final UnitPo shipPo = data.getModel().getUnits().stream()
                .filter(unitPo -> unitPo.getId() == SHIP_ID).findFirst()
                .orElseThrow();
        shipPo.setTradeRouteAssignmentRouteId(99);

        assertThrows(RuntimeException.class, () -> make(data));
    }

    /** Every player takes one turn, returning the round to the first player. */
    private static void playRound(final Model model) {
        final int players = model.getPlayers().size();
        for (int i = 0; i < players; i++) {
            model.endTurn();
        }
    }

    @Test
    void wagonAndShipKindsAreNotInterchangeable() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute landRoute = new TradeRoute(42, dutch, "Land",
                TradeRouteKind.LAND, DELFT,
                List.of(TradeRouteStop.ofColony(DELFT)));

        assertEquals(Optional.of(TradeRouteAssignmentProblem.NOT_CARRIER),
                model.getTradeRouteAssignmentProblem(ship, landRoute, 0));
        assertEquals(Optional.empty(), model.getTradeRouteAssignment(ship));
    }

    @Test
    void nonCarrierIsRefusedWithoutChange() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        final Unit colonist = model.getAllUnits().stream()
                .filter(unit -> unit.getOwner() == dutch
                        && !(unit instanceof UnitWithCargo))
                .findFirst().orElseThrow();

        assertEquals(Optional.of(TradeRouteAssignmentProblem.NOT_CARRIER),
                model.getTradeRouteAssignmentProblem(colonist, route, 0));
        assertThrows(IllegalArgumentException.class,
                () -> model.assignTradeRoute(colonist, route, 0));
        assertEquals(Optional.empty(), model.getTradeRouteAssignment(colonist));
    }

    @Test
    void wrongOwnerRouteIsRefusedWithoutChange() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        // F108 has exactly two players: Dutch and Dutch's King. The King is the
        // explicit foreign owner of the route; its owner is checked by the
        // model
        // before any route lookup, so the route need not be stored.
        final Player other = model.getPlayers().stream()
                .filter(player -> player != dutch).findFirst().orElseThrow();
        assertEquals(2, model.getPlayers().size());
        final UnitWithCargo ship = ship(model);
        final TradeRoute foreign = new TradeRoute(42, other, "Foreign",
                TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
        final Location before = ship.getLocation();
        final int movement = ship.getMovementThirds();

        assertEquals(Optional.of(TradeRouteAssignmentProblem.NOT_OWNER),
                model.getTradeRouteAssignmentProblem(ship, foreign, 0));
        assertThrows(IllegalArgumentException.class,
                () -> model.assignTradeRoute(ship, foreign, 0));
        assertEquals(Optional.empty(), model.getTradeRouteAssignment(ship));
        assertEquals(before, ship.getLocation());
        assertEquals(movement, ship.getMovementThirds());
        assertFalse(((AbstractUnit) ship).hasStandingOrder());
    }

    @Test
    void invalidFirstStopIsRefusedWithoutChange() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");

        assertEquals(Optional.of(TradeRouteAssignmentProblem.STOP_UNAVAILABLE),
                model.getTradeRouteAssignmentProblem(ship, route, 2));
        assertEquals(Optional.of(TradeRouteAssignmentProblem.STOP_UNAVAILABLE),
                model.getTradeRouteAssignmentProblem(ship, route, -1));
        assertThrows(IllegalArgumentException.class,
                () -> model.assignTradeRoute(ship, route, 2));
        assertEquals(Optional.empty(), model.getTradeRouteAssignment(ship));
    }

    @Test
    void damagedCarrierShowsItsReasonAndKeepsItsAssignment() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        final Model model = make(data);
        final Player dutch = model.getColonyById(DELFT).orElseThrow()
                .getOwner();
        final UnitWithCargo ship = ship(model);
        final TradeRoute route = createSeaRoute(model, dutch, "Circuit");
        model.assignTradeRoute(ship, route, 0);

        model.damageShip((Ship) ship);
        assertTrue(((Ship) ship).isDamaged());
        playRound(model);

        final TradeRouteAssignment assignment = model
                .getTradeRouteAssignment(ship).orElseThrow();
        assertEquals(Optional.of(TradeRoutePause.CARRIER_DAMAGED),
                assignment.getPause());

        data.setModel(model.save());
        final Model restored = make(data);
        final UnitWithCargo restoredShip = ship(restored);
        assertTrue(((Ship) restoredShip).isDamaged());
        assertEquals(Optional.of(TradeRoutePause.CARRIER_DAMAGED),
                restored.getTradeRouteAssignment(restoredShip).orElseThrow()
                        .getPause());
    }
}
