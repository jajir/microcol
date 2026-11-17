package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.junit.jupiter.api.Test;

class TradeRouteDestinationTest {
    private final Model model = Model.make(new GameModelPoDao()
            .loadFromFile(new File(
                    "../akropia-test/src/test/scenarios/F110-trade-route-edit.akropia"))
            .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
    private final Player owner = model.getColonyById(0).orElseThrow()
            .getOwner();
    private final TradeRoute route = model.getTradeRoutes(owner).stream()
            .filter(value -> value.getName().equals("Ore cart")).findFirst()
            .orElseThrow();

    private Unit carrier(final int id) {
        return model.getAllUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    @Test
    void selectionChangesOnlyOneCarrierWithoutMovingOrProcessingCargo() {
        final Unit first = carrier(4);
        final Unit second = carrier(6);
        model.assignTradeRoute(first, route, 0);
        model.assignTradeRoute(second, route, 0);
        final Location position = first.getLocation();
        model.changeTradeRouteDestination(first, route, 1);
        assertEquals(1, model.getTradeRouteAssignment(first).orElseThrow()
                .getStopIndex());
        assertEquals(0, model.getTradeRouteAssignment(second).orElseThrow()
                .getStopIndex());
        assertEquals(position, first.getLocation());
        assertTrue(
                ((AbstractUnit) first).getProcessedTradeRouteVisit().isEmpty());
        assertEquals(90, model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.ORE).getAmount());
        model.advanceTradeRouteAssignments(owner);
        assertEquals(0, model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.ORE).getAmount());
        assertEquals(java.util.Optional.of(Goods.of(GoodsType.ORE, 90)),
                ((org.akropia.model.unit.UnitWithCargo) first).getCargo()
                        .getSlotByIndex(0).getGoods());

    }

    @Test
    void invalidOrStaleSelectionHasNoPartialEffects() {
        final Unit first = carrier(4);
        model.assignTradeRoute(first, route, 0);
        final TradeRouteAssignment assignment = model
                .getTradeRouteAssignment(first).orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRouteDestination(first, route, -1));
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRouteDestination(first, route, 2));
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRouteDestination(carrier(6), route, 0));
        model.updateTradeRoute(owner, route, route.getStops(), route.getMode());
        assertThrows(IllegalArgumentException.class,
                () -> model.changeTradeRouteDestination(first, route, 1));
        assertSame(assignment,
                model.getTradeRouteAssignment(first).orElseThrow());
        assertEquals(0, assignment.getStopIndex());
    }

    @Test
    void removedLastDestinationWrapsAndKeepsOtherCarrierTarget() {
        model.assignTradeRoute(carrier(4), route, 1);
        model.assignTradeRoute(carrier(6), route, 0);
        final TradeRoute updated = model.updateTradeRoute(owner, route,
                List.of(route.getStops().get(0)), route.getMode());
        assertEquals(updated.getStops(), model
                .getTradeRouteAssignment(carrier(4)).orElseThrow().getStops());
        assertEquals(route.getStops().get(0),
                model.getTradeRouteHeadingStop(carrier(4)).orElseThrow());
        assertEquals(route.getStops().get(0),
                model.getTradeRouteHeadingStop(carrier(6)).orElseThrow());
    }

    @Test
    void removedDestinationChoosesFollowingSurvivorInOriginalOrder() {
        final TradeRoute three = model.updateTradeRoute(
                owner, route, List.of(route.getStops().get(0),
                        route.getStops().get(1), TradeRouteStop.ofColony(2)),
                route.getMode());
        model.assignTradeRoute(carrier(4), three, 1);
        model.updateTradeRoute(owner, three,
                List.of(three.getStops().get(2), three.getStops().get(0)),
                three.getMode());
        assertEquals(TradeRouteStop.ofColony(2),
                model.getTradeRouteHeadingStop(carrier(4)).orElseThrow());
    }
}
