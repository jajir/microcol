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

/**
 * Editing, canceling and deleting trade routes on the shared F109 scenario. New
 * Amsterdam (colony 0) and Delft (colony 1) are two squares apart over grass.
 * Wagon 4 stands free in New Amsterdam; wagon 6 stands there too and starts
 * with a saved assignment to the "Idle cart" route, paused at Delft.
 */
class TradeRouteEditTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F109-trade-route-assignment.microcol");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int FREE_WAGON_ID = 4;
    private static final int IDLE_WAGON_ID = 6;
    private static final String ORE_CART = "Ore Cart";

    private static Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Unit wagon(final Model model, final int id) {
        return model.getAllUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static Player dutch(final Model model) {
        return model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner();
    }

    /** Land route from New Amsterdam to Delft and back, with blank cargo. */
    private static TradeRoute createOreCart(final Model model) {
        return model.createTradeRoute(dutch(model), ORE_CART,
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
    }

    @Test
    void cancelReturnsOnlyTheCancelledCarrierToManualOrders() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = dutch(model);
        final TradeRoute oreCart = createOreCart(model);
        final Unit free = wagon(model, FREE_WAGON_ID);
        final Unit idle = wagon(model, IDLE_WAGON_ID);
        model.assignTradeRoute(free, oreCart, 1);
        final Location before = free.getLocation();

        assertEquals(Optional.empty(), model.getTradeRouteCancelProblem(free));
        model.cancelTradeRouteAssignment(free);

        assertEquals(Optional.empty(), model.getTradeRouteAssignment(free));
        assertEquals(Optional.of(TradeRouteAssignmentProblem.NO_ASSIGNMENT),
                model.getTradeRouteCancelProblem(free));
        assertEquals(before, free.getLocation());
        assertEquals(0,
                model.getTradeRouteAssignedCarriers(dutch, oreCart).size());
        // The other carrier keeps following the route it had before.
        final TradeRoute idleCart = model.getTradeRoutes(dutch).get(0);
        assertEquals(0, idleCart.getId());
        assertEquals(List.of(idle),
                model.getTradeRouteAssignedCarriers(dutch, idleCart));
    }

    @Test
    void deletingARouteClearsEveryAssignmentWithoutMovingOrPaying() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = dutch(model);
        final TradeRoute oreCart = createOreCart(model);
        final Unit free = wagon(model, FREE_WAGON_ID);
        final Unit idle = wagon(model, IDLE_WAGON_ID);
        model.cancelTradeRouteAssignment(idle);
        model.assignTradeRoute(free, oreCart, 1);
        model.assignTradeRoute(idle, oreCart, 1);
        final Location freeBefore = free.getLocation();
        final Location idleBefore = idle.getLocation();
        final int gold = dutch.getGold();
        assertEquals(2,
                model.getTradeRouteAssignedCarriers(dutch, oreCart).size());

        assertEquals(Optional.empty(),
                model.getTradeRouteDeleteProblem(dutch, oreCart));
        model.deleteTradeRoute(dutch, oreCart);

        assertEquals(Optional.empty(), model.getTradeRouteAssignment(free));
        assertEquals(Optional.empty(), model.getTradeRouteAssignment(idle));
        assertEquals(freeBefore, free.getLocation());
        assertEquals(idleBefore, idle.getLocation());
        assertEquals(gold, dutch.getGold());
        assertTrue(model.getTradeRoutes(dutch).stream()
                .noneMatch(route -> route.getId() == oreCart.getId()));
        assertEquals(Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE),
                model.getTradeRouteDeleteProblem(dutch, oreCart));
    }

    @Test
    void staleRouteAndWrongOwnerEditsAreRefusedWithoutChange() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = dutch(model);
        final TradeRoute oreCart = createOreCart(model);
        final TradeRoute edited = model.configureTradeRouteStopCargo(dutch,
                oreCart, 0, List.of(GoodsType.ORE), List.of());
        final Player king = model.getPlayerStore().getPlayers().stream()
                .filter(player -> player != dutch).findFirst().orElseThrow();
        final TradeRouteStop newAmsterdam = TradeRouteStop
                .ofColony(NEW_AMSTERDAM);

        // The reference taken before the cargo edit is stale.
        assertEquals(Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE), model
                .getTradeRouteStopEditProblem(dutch, oreCart, 0, newAmsterdam));
        assertThrows(IllegalArgumentException.class, () -> model
                .editTradeRouteStop(dutch, oreCart, 0, newAmsterdam));
        assertEquals(Optional.of(TradeRouteProblem.NOT_OWNER), model
                .getTradeRouteStopEditProblem(king, edited, 0, newAmsterdam));
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE), model
                .getTradeRouteStopEditProblem(dutch, edited, 2, newAmsterdam));
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE),
                model.getTradeRouteStopEditProblem(dutch, edited, 0,
                        TradeRouteStop.ofColony(99)));

        assertEquals(2, edited.getStops().size());
        assertEquals(DELFT, edited.getStops().get(0).getColonyId());
        assertEquals(NEW_AMSTERDAM, edited.getStops().get(1).getColonyId());
        assertEquals(List.of(GoodsType.ORE),
                edited.getStops().get(0).getUnloadGoods());
    }

    @Test
    void cargoEditKeepsRouteIdentityAndTheCarrierWaiting() {
        final Model model = make(new GameModelPoDao().loadFromFile(SCENARIO));
        final Player dutch = dutch(model);
        final TradeRoute oreCart = createOreCart(model);
        final Unit free = wagon(model, FREE_WAGON_ID);
        model.assignTradeRoute(free, oreCart, 1);

        final TradeRoute edited = model.configureTradeRouteStopCargo(dutch,
                oreCart, 0, List.of(GoodsType.ORE), List.of(GoodsType.MUSKET));

        assertEquals(oreCart.getId(), edited.getId());
        assertEquals(ORE_CART, edited.getName());
        assertEquals(List.of(GoodsType.MUSKET),
                edited.getStops().get(0).getLoadGoods());
        final TradeRouteAssignment assignment = model
                .getTradeRouteAssignment(free).orElseThrow();
        assertEquals(oreCart.getId(), assignment.getRouteId());
        assertEquals(1, assignment.getStopIndex());
    }

    @Test
    void savedBoundaryCopyRestoresAndAMissingCopyIsRejected() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        final Model model = make(data);
        final Unit idle = wagon(model, IDLE_WAGON_ID);
        final List<TradeRouteStop> copy = model.getTradeRouteAssignment(idle)
                .orElseThrow().getStops();
        assertEquals(1, copy.size());
        assertEquals(DELFT, copy.get(0).getColonyId());

        data.setModel(model.save());
        final Model restored = make(data);
        final List<TradeRouteStop> restoredCopy = restored
                .getTradeRouteAssignment(wagon(restored, IDLE_WAGON_ID))
                .orElseThrow().getStops();
        assertEquals(DELFT, restoredCopy.get(0).getColonyId());

        final UnitPo idlePo = data.getModel().getUnits().stream()
                .filter(unitPo -> unitPo.getId() == IDLE_WAGON_ID).findFirst()
                .orElseThrow();
        idlePo.setTradeRouteAssignmentStops(null);
        assertThrows(RuntimeException.class, () -> make(data));
    }
}
