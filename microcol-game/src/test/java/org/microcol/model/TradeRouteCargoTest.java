package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

/** Saved cargo policy of trade route stops; processing has its own tests. */
class TradeRouteCargoTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F107-trade-route-creation.microcol");

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
    void cargoListsHoldZeroToSixDistinctCargoGoods() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        final List<GoodsType> six = List.of(GoodsType.CORN, GoodsType.SUGAR,
                GoodsType.TOBACCO, GoodsType.COTTON, GoodsType.FUR,
                GoodsType.LUMBER);
        final TradeRoute configured = model.configureTradeRouteStopCargo(owner,
                route, 0, six, List.of());
        assertEquals(six, configured.getStops().get(0).getUnloadGoods());
        assertEquals(List.of(), configured.getStops().get(0).getLoadGoods());
        assertEquals(List.of(),
                model.configureTradeRouteStopCargo(owner, configured, 0,
                        List.of(), List.of()).getStops().get(0)
                        .getUnloadGoods());
    }

    @Test
    void invalidCargoListsAreRefusedAndLeaveTheRouteUnchanged() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        final List<GoodsType> seven = List.of(GoodsType.CORN, GoodsType.SUGAR,
                GoodsType.TOBACCO, GoodsType.COTTON, GoodsType.FUR,
                GoodsType.LUMBER, GoodsType.ORE);
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0, seven,
                        List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0,
                        List.of(GoodsType.CORN, GoodsType.CORN), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0,
                        List.of(GoodsType.HAMMERS), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> model.configureTradeRouteStopCargo(owner, route, 0, null,
                        List.of()));
        assertEquals(route, model.getTradeRoutes(owner).get(0));
        assertEquals(List.of(), model.getTradeRoutes(owner).get(0).getStops()
                .get(0).getUnloadGoods());
    }

    @Test
    void configuredOrderIsSavedAndRestoredExactly() {
        final Model model = fixtureModel();
        final Player owner = owner(model);
        final TradeRoute route = seaRoute(model, owner);
        model.configureTradeRouteStopCargo(owner, route, 1,
                List.of(GoodsType.ORE, GoodsType.CORN),
                List.of(GoodsType.TOOLS));
        final Model restored = load(model.save());
        final TradeRoute saved = restored.getTradeRoutes(owner(restored))
                .get(0);
        assertEquals(List.of(GoodsType.ORE, GoodsType.CORN),
                saved.getStops().get(1).getUnloadGoods());
        assertEquals(List.of(GoodsType.TOOLS),
                saved.getStops().get(1).getLoadGoods());
        assertEquals(List.of(), saved.getStops().get(0).getUnloadGoods());
    }
}
