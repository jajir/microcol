package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;

class CarrierRouteChangeTest {
    private Model model() {
        return Model.make(new GameModelPoDao().loadFromFile(new File(
                "../akropia-test/src/test/scenarios/F109-trade-route-assignment.akropia"))
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
    }

    private TradeRoute route(final Model model, final String name) {
        return model.createTradeRoute(model.getUnitById(4).getOwner(), name,
                TradeRouteKind.LAND, 0, List.of(TradeRouteStop.ofColony(1),
                        TradeRouteStop.ofColony(0)));
    }

    private String save(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void selectsCurrentStopWithoutMovingAndReplacementSurvivesContinue() {
        final Model model = model();
        final Unit wagon = model.getUnitById(4);
        final TradeRoute first = route(model, "First");
        final TradeRoute second = route(model, "Second");
        final Location location = wagon.getLocation();
        final int movement = wagon.getMovementThirds();
        model.changeTradeRoute(wagon, first, null);
        final TradeRouteAssignment previous = model
                .getTradeRouteAssignment(wagon).orElseThrow();
        assertEquals(1, previous.getStopIndex());
        previous.setPause(TradeRoutePause.STOP_UNREACHABLE);
        final String beforeSame = save(model);
        model.changeTradeRoute(wagon, first, previous);
        assertEquals(beforeSame, save(model));
        model.changeTradeRoute(wagon, second, previous);
        assertEquals(second.getId(), model.getTradeRouteAssignment(wagon)
                .orElseThrow().getRouteId());
        assertEquals(1, model.getTradeRouteAssignment(wagon).orElseThrow()
                .getStopIndex());
        assertEquals(location, wagon.getLocation());
        assertEquals(movement, wagon.getMovementThirds());
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(save(model), save(loaded));
    }

    @Test
    void staleAndInvalidReplacementPreserveEntireState() {
        final Model model = model();
        final Unit wagon = model.getUnitById(4);
        final TradeRoute first = route(model, "First");
        final TradeRoute second = route(model, "Second");
        model.changeTradeRoute(wagon, first, null);
        final TradeRouteAssignment previous = model
                .getTradeRouteAssignment(wagon).orElseThrow();
        final String before = save(model);
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRoute(wagon, second, null));
        assertEquals(before, save(model));
        model.deleteTradeRoute(wagon.getOwner(), second);
        final String deleted = save(model);
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRoute(wagon, second, previous));
        assertEquals(deleted, save(model));
    }

    @Test
    void zeroMovementCanReceiveRouteButGoToCannotBeOverwritten() {
        final Model model = model();
        final Unit wagon = model.getUnitById(4);
        final TradeRoute route = route(model, "First");
        ((AbstractUnit) wagon).setMovementThirds(0);
        model.changeTradeRoute(wagon, route, null);
        assertEquals(0, wagon.getMovementThirds());
        model.cancelTradeRouteAssignment(wagon);
        model.issueGoTo(wagon,
                model.getColonyById(1).orElseThrow().getLocation());
        final String before = save(model);
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRoute(wagon, route, null));
        assertEquals(before, save(model));
    }

    @Test
    void shipJoinsNearestPortAndCrossingRejectsEveryReplacement() {
        final Model model = model();
        final AbstractUnit ship = (AbstractUnit) model.getUnitById(3);
        final TradeRoute route = model.createTradeRoute(ship.getOwner(), "Sea",
                TradeRouteKind.SEA, 1,
                List.of(TradeRouteStop.europe(), TradeRouteStop.ofColony(1)));
        model.changeTradeRoute(ship, route, null);
        assertEquals(1, model.getTradeRouteAssignment(ship).orElseThrow()
                .getStopIndex());
        ship.placeToLocation(Location.of(49, 16));
        ship.placeToHighSeas(true);
        final TradeRouteAssignment expected = model
                .getTradeRouteAssignment(ship).orElseThrow();
        final String before = save(model);
        assertEquals(TradeRouteAssignmentProblem.VOYAGE_IN_PROGRESS,
                model.getTradeRouteChangeProblem(ship, route).orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRoute(ship, route, expected));
        assertEquals(before, save(model));
    }

    @Test
    void cheaperRoadWinsOverGeometricallyCloserStopAndTiesUseRouteOrder() {
        final Model model = model();
        final AbstractUnit wagon = (AbstractUnit) model.getUnitById(4);
        final TradeRoute route = model.createTradeRoute(wagon.getOwner(),
                "Road", TradeRouteKind.LAND, 1,
                List.of(TradeRouteStop.ofColony(0),
                        TradeRouteStop.ofColony(1)));
        wagon.placeToLocation(Location.of(26, 17));
        for (final Location at : List.of(Location.of(26, 17),
                Location.of(27, 17), Location.of(28, 16))) {
            model.getMap().buildRoad(at);
        }
        assertEquals(1,
                model.getNearestTradeRouteStop(wagon, route).orElseThrow());
        model.getMap().buildRoad(Location.of(26, 16));
        wagon.placeToLocation(Location.of(27, 17));
        assertEquals(0,
                model.getNearestTradeRouteStop(wagon, route).orElseThrow());
    }

    @Test
    void noReachableStopPreservesTheWholeExistingAssignment() {
        final Model model = model();
        final AbstractUnit wagon = (AbstractUnit) model.getUnitById(4);
        final TradeRoute first = route(model, "First");
        final TradeRoute second = route(model, "Second");
        model.changeTradeRoute(wagon, first, null);
        wagon.placeToLocation(Location.of(45, 15));
        final TradeRouteAssignment previous = model
                .getTradeRouteAssignment(wagon).orElseThrow();
        final String before = save(model);
        assertTrue(model.getNearestTradeRouteStop(wagon, second).isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRoute(wagon, second, previous));
        assertEquals(before, save(model));
    }

    @Test
    void europePortWinsAtZeroCostAndCargoIsPreservedOnReplacement() {
        final Model model = model();
        final AbstractUnit ship = (AbstractUnit) model.getUnitById(3);
        final TradeRoute route = model.createTradeRoute(ship.getOwner(),
                "Europe", TradeRouteKind.SEA, 1,
                List.of(TradeRouteStop.europe(), TradeRouteStop.ofColony(1)));
        ship.placeToEuropePort(model.getEurope().getPort());
        final String cargo = new Gson().toJson(model.save().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow()
                .getCargo());
        model.changeTradeRoute(ship, route, null);
        assertEquals(0, model.getTradeRouteAssignment(ship).orElseThrow()
                .getStopIndex());
        assertTrue(ship.isAtEuropePort());
        assertEquals(cargo,
                new Gson().toJson(model.save().getUnits().stream()
                        .filter(unit -> unit.getId() == 3).findFirst()
                        .orElseThrow().getCargo()));
    }

}
