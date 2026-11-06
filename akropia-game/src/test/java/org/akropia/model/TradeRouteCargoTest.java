package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ModelPo;

/** Saved cargo policy of trade route stops; processing has its own tests. */
class TradeRouteCargoTest {

    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F107-trade-route-creation.akropia");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;

    private Model fixtureModel() {
        return load(new GameModelPoDao().loadFromFile(SCENARIO).getModel());
    }

    private Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player owner(final Model model) {
        return model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner();
    }

    /** Europe and back to Delft: a legal sea route with two stops. */
    private static TradeRoute seaRoute(final Model model, final Player owner) {
        return model.createTradeRoute(owner, "Cargo", TradeRouteKind.SEA, DELFT,
                List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
    }

    @Test
    void newStopsStartWithBlankCargoPolicy() {
        final Model model = fixtureModel();
        final TradeRoute route = seaRoute(model, owner(model));
        for (final TradeRouteStop stop : route.getStops()) {
            assertEquals(List.of(), stop.getUnloadGoods());
            assertEquals(List.of(), stop.getLoadGoods());
        }
    }

    @Test
    void everyTransportableGoodsMayCarryAnInstruction() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        // The former limit of six goods per direction is gone (R-0421): all
        // sixteen transportable goods can be unloaded at one stop, or split
        // between unloading and loading.
        assertEquals(16, TradeRouteStop.TRANSPORTABLE_GOODS.size());
        final TradeRoute configured = model.configureTradeRouteStopCargo(owner,
                route, 0, TradeRouteStop.TRANSPORTABLE_GOODS, List.of());
        assertEquals(TradeRouteStop.TRANSPORTABLE_GOODS,
                configured.getStops().get(0).getUnloadGoods());
        final List<GoodsType> seven = List.of(GoodsType.GRAIN, GoodsType.OLIVES,
                GoodsType.GRAPES, GoodsType.WOOL, GoodsType.CLAY,
                GoodsType.LUMBER, GoodsType.ORE);
        final List<GoodsType> rest = TradeRouteStop.TRANSPORTABLE_GOODS
                .subList(7, 16);
        final TradeRoute split = model.configureTradeRouteStopCargo(owner,
                configured, 0, rest, seven);
        assertEquals(seven, split.getStops().get(0).getLoadGoods());
        assertEquals(rest, split.getStops().get(0).getUnloadGoods());
        assertEquals(
                List.of(), model
                        .configureTradeRouteStopCargo(owner, split, 0,
                                List.of(), List.of())
                        .getStops().get(0).getUnloadGoods());
    }

    @Test
    void invalidCargoListsAreRefusedAndLeaveTheRouteUnchanged() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0,
                        List.of(GoodsType.GRAIN, GoodsType.GRAIN), List.of()));
        // Hammers, torches and owls are not cargo.
        for (final GoodsType notCargo : List.of(GoodsType.HAMMERS,
                GoodsType.TORCHES, GoodsType.OWLS)) {
            assertThrows(IllegalArgumentException.class,
                    () -> model.configureTradeRouteStopCargo(owner, route, 0,
                            List.of(), List.of(notCargo)));
        }
        // One goods is never loaded and unloaded at the same stop.
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0,
                        List.of(GoodsType.ORE), List.of(GoodsType.ORE)));
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0, null,
                        List.of()));
        assertSame(route, model.getTradeRoutes(owner).get(0));
        assertEquals(List.of(), model.getTradeRoutes(owner).get(0).getStops()
                .get(0).getUnloadGoods());
    }

    @Test
    void cargoIsKeptInFixedGoodsOrderAndRestoredExactly() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        model.configureTradeRouteStopCargo(owner, route, 1,
                List.of(GoodsType.ORE, GoodsType.GRAIN),
                List.of(GoodsType.TOOLS));
        final Model restored = load(model.save());
        final TradeRoute saved = restored.getTradeRoutes(owner(restored))
                .get(0);
        // Grain precedes ore in the colony goods order, whatever the list
        // order was.
        assertEquals(List.of(GoodsType.GRAIN, GoodsType.ORE),
                saved.getStops().get(1).getUnloadGoods());
        assertEquals(List.of(GoodsType.TOOLS),
                saved.getStops().get(1).getLoadGoods());
        assertEquals(List.of(), saved.getStops().get(0).getUnloadGoods());
    }

    @Test
    void savedStopThatLoadsAndUnloadsOneGoodsIsRejected() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        model.configureTradeRouteStopCargo(owner, seaRoute(model, owner), 1,
                List.of(GoodsType.ORE), List.of());
        final org.akropia.model.store.ModelPo saved = model.save();
        saved.getTradeRoutes().get(0).getStops().get(1)
                .setLoadGoods(List.of(GoodsType.ORE));
        assertThrows(IllegalArgumentException.class, () -> load(saved));
    }
}
