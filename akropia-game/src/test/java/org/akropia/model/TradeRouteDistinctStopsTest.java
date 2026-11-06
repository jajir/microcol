package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;

/**
 * A route with more than four distinct stops, edited as the route screen does
 * (R-0421). Dutch owns twelve land-connected colonies A1 to D3 (IDs 0 to 11) in
 * the F222 scenario.
 */
class TradeRouteDistinctStopsTest {

    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F222-colony-markers.akropia");

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static TradeRouteStop colony(final int id) {
        return TradeRouteStop.ofColony(id);
    }

    private static List<String> names(final Model model,
            final List<TradeRouteStop> stops) {
        return stops.stream().map(stop -> model
                .getColonyById(stop.getColonyId()).orElseThrow().getName())
                .collect(Collectors.toList());
    }

    @Test
    void fiveDistinctStopsAreAddedReorderedRemovedAndRestored() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        final Model model = load(data);
        final Player dutch = model.getColonyById(0).orElseThrow().getOwner();
        final TradeRoute created = model.createTradeRoute(dutch, "Grid",
                TradeRouteKind.LAND, 0, List.of(colony(1)));

        // Add choices never repeat a chosen stop.
        assertEquals(
                List.of("A1", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "D1",
                        "D2", "D3"),
                names(model, model.getTradeRouteAddableStops(dutch, created,
                        created.getStops())));
        final List<TradeRouteStop> five = List.of(colony(1), colony(4),
                colony(7), colony(10), colony(11));
        final List<String> offered = names(model,
                model.getTradeRouteAddableStops(dutch, created, five));
        assertEquals(List.of("A1", "A3", "B1", "B3", "C1", "C3", "D1"),
                offered);
        TradeRoute route = model.updateTradeRoute(dutch, created, five,
                TradeRouteMode.TOUCH_AND_GO);
        assertEquals(List.of("A2", "B2", "C2", "D2", "D3"),
                names(model, route.getStops()));

        // Reorder D3 to the front, then remove B2; goods follow their stop.
        route = model.updateTradeRoute(dutch, route,
                List.of(colony(11).withCargo(List.of(), List.of(GoodsType.ORE)),
                        colony(1), colony(4), colony(7), colony(10)),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        route = model.updateTradeRoute(dutch, route,
                List.of(route.getStops().get(0), colony(1), colony(7),
                        colony(10), colony(0)),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        assertEquals(List.of("D3", "A2", "C2", "D2", "A1"),
                names(model, route.getStops()));
        assertEquals(List.of(GoodsType.ORE),
                route.getStops().get(0).getLoadGoods());

        // A repeated stop in a new list is refused next to itself and the
        // stored route stays as it was.
        assertEquals(Optional.of(TradeRouteProblem.SAME_STOP),
                model.getTradeRouteUpdateProblem(dutch, route,
                        List.of(colony(1), colony(1)),
                        TradeRouteMode.TOUCH_AND_GO));

        data.setModel(model.save());
        final Model restored = load(data);
        final TradeRoute saved = restored
                .getTradeRoutes(
                        restored.getColonyById(0).orElseThrow().getOwner())
                .get(0);
        assertEquals(List.of("D3", "A2", "C2", "D2", "A1"),
                names(restored, saved.getStops()));
        assertEquals(TradeRouteMode.WAIT_FOR_COMPLETION, saved.getMode());
    }
}
