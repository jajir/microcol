package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.TradeRoutePo;
import org.akropia.model.store.TradeRouteStopPo;

class TradeRouteTest {

    /** Shared scenario: New Amsterdam (0) inland, Delft (1) on the coast. */
    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F107-trade-route-creation.akropia");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;

    @TempDir
    Path directory;

    private GameModelPo fixture() {
        return new GameModelPoDao().loadFromFile(SCENARIO);
    }

    private Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Player owner(final Model model) {
        return model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner();
    }

    private static TradeRouteStop colony(final int id) {
        return TradeRouteStop.ofColony(id);
    }

    private static TradeRouteStop europe() {
        return TradeRouteStop.europe();
    }

    @Test
    void landRouteWithOneStopKeepsIdentityNameAndOrder() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        final TradeRoute route = model.createTradeRoute(owner, " Wagon Road ",
                TradeRouteKind.LAND, NEW_AMSTERDAM, List.of(colony(DELFT)));
        assertEquals(0, route.getId());
        assertEquals("Wagon Road", route.getName());
        assertEquals(TradeRouteKind.LAND, route.getKind());
        assertSame(owner, route.getOwner());
        assertEquals(NEW_AMSTERDAM, route.getStartColonyId());
        assertEquals(List.of(colony(DELFT)), route.getStops());
        assertEquals(List.of(route), model.getTradeRoutes(owner));
        assertEquals(1, model.save().getNextTradeRouteId());
    }

    @Test
    void routesHaveNoStopLimitButNeedAtLeastOneStop() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        // The former four-stop limit is gone (R-0421): six alternating stops
        // are a legal sea route.
        final List<TradeRouteStop> six = List.of(europe(), colony(DELFT),
                europe(), colony(DELFT), europe(), colony(DELFT));
        assertEquals(Optional.empty(), model.getTradeRouteProblem(owner, "Six",
                TradeRouteKind.SEA, DELFT, six));
        final TradeRoute route = model.createTradeRoute(owner, "Six",
                TradeRouteKind.SEA, DELFT, six);
        assertEquals(six, route.getStops());
        assertEquals(TradeRouteMode.TOUCH_AND_GO, route.getMode());
        assertEquals(Optional.of(TradeRouteProblem.STOP_COUNT),
                model.getTradeRouteProblem(owner, "None", TradeRouteKind.SEA,
                        DELFT, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> model.createTradeRoute(owner, "None", TradeRouteKind.SEA,
                        DELFT, List.of()));
        assertEquals(1, model.getTradeRoutes(owner).size());
        // The saved six stops and the mode reload exactly.
        final Model reloaded = load(model.save());
        final TradeRoute restored = reloaded.getTradeRoutes(owner(reloaded))
                .get(0);
        assertEquals(six, restored.getStops());
        assertEquals(TradeRouteMode.TOUCH_AND_GO, restored.getMode());
    }

    @Test
    void savedRouteWithoutModeIsRejected() {
        final Model model = load(fixture().getModel());
        model.createTradeRoute(owner(model), "Mode", TradeRouteKind.SEA, DELFT,
                List.of(europe(), colony(DELFT)));
        final ModelPo saved = model.save();
        saved.getTradeRoutes().get(0).setMode(null);
        assertThrows(IllegalArgumentException.class, () -> load(saved));
    }

    @Test
    void eachLegIsCheckedFromThePreviousStop() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        // Delft reaches Europe by sea, but Europe cannot reach inland New
        // Amsterdam, so the second leg fails even though the first passes.
        assertEquals(Optional.of(TradeRouteProblem.STOP_NOT_REACHABLE),
                model.getTradeRouteProblem(owner, "Leg", TradeRouteKind.SEA,
                        DELFT, List.of(europe(), colony(NEW_AMSTERDAM))));
        assertEquals(List.of(europe()), model.getTradeRouteStopOptions(owner,
                TradeRouteKind.SEA, DELFT, List.of()));
        assertEquals(List.of(colony(DELFT)), model.getTradeRouteStopOptions(
                owner, TradeRouteKind.LAND, NEW_AMSTERDAM, List.of()));
        // After Europe and back in Delft, only Europe is a legal next stop.
        assertEquals(List.of(europe()), model.getTradeRouteStopOptions(owner,
                TradeRouteKind.SEA, DELFT, List.of(europe(), colony(DELFT))));
    }

    @Test
    void landWagonsNeverVisitEuropeAndSeaStartsNeedAPort() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        assertEquals(Optional.of(TradeRouteProblem.EUROPE_BY_LAND),
                model.getTradeRouteProblem(owner, "Land", TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of(europe())));
        assertEquals(Optional.of(TradeRouteProblem.START_NOT_PORT),
                model.getTradeRouteProblem(owner, "Sea", TradeRouteKind.SEA,
                        NEW_AMSTERDAM, List.of(europe())));
        // The editor shows the port reason before any stop is chosen.
        assertEquals(Optional.of(TradeRouteProblem.START_NOT_PORT),
                model.getTradeRouteProblem(owner, "Sea", TradeRouteKind.SEA,
                        NEW_AMSTERDAM, List.of()));
        assertFalse(model.getTradeRouteStopOptions(owner, TradeRouteKind.LAND,
                NEW_AMSTERDAM, List.of()).contains(europe()));
    }

    @Test
    void staleAndForeignTargetsAreRejected() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        final Player king = model.getPlayerByName("Dutch's King");
        assertEquals(Optional.of(TradeRouteProblem.NOT_OWNER),
                model.getTradeRouteProblem(king, "King", TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of(colony(DELFT))));
        assertEquals(Optional.of(TradeRouteProblem.START_UNAVAILABLE),
                model.getTradeRouteProblem(owner, "Gone", TradeRouteKind.LAND,
                        99, List.of(colony(DELFT))));
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE),
                model.getTradeRouteProblem(owner, "Gone", TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of(colony(99))));
        assertEquals(Optional.of(TradeRouteProblem.SAME_STOP),
                model.getTradeRouteProblem(owner, "Same", TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of(colony(NEW_AMSTERDAM))));
    }

    @Test
    void nameBoundariesTrimmingAndNormalizationApply() {
        final Model model = load(fixture().getModel());
        final Player owner = owner(model);
        assertEquals(Optional.of(TradeRouteProblem.NAME_EMPTY),
                model.getTradeRouteProblem(owner, "    ", TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of(colony(DELFT))));
        assertEquals(Optional.of(TradeRouteProblem.NAME_TOO_LONG),
                model.getTradeRouteProblem(owner, "a".repeat(65),
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(colony(DELFT))));
        assertEquals(Optional.of(TradeRouteProblem.NAME_INVALID),
                model.getTradeRouteProblem(owner, "bad\tname",
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(colony(DELFT))));
        // 64 code points, including one astral character, is allowed.
        final String longest = "a".repeat(63) + "🌊";
        final TradeRoute route = model.createTradeRoute(owner, longest,
                TradeRouteKind.LAND, NEW_AMSTERDAM, List.of(colony(DELFT)));
        assertEquals(longest, route.getName());
        model.createTradeRoute(owner, "Café Road", TradeRouteKind.LAND,
                NEW_AMSTERDAM, List.of(colony(DELFT)));
        assertEquals(Optional.of(TradeRouteProblem.NAME_DUPLICATE),
                model.getTradeRouteProblem(owner, "  café ROAD ",
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(colony(DELFT))));
        assertEquals(Optional.of(TradeRouteProblem.NAME_TOO_LONG),
                model.getTradeRouteProblem(owner, longest + "a",
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(colony(DELFT))));
    }

    @Test
    void rejectedCreationChangesNothingAndPreviewMovesNoUnit()
            throws Exception {
        final GameModelPo data = fixture();
        final Model model = load(data.getModel());
        final Player owner = owner(model);
        model.createTradeRoute(owner, "Existing", TradeRouteKind.LAND,
                NEW_AMSTERDAM, List.of(colony(DELFT)));
        // The saved form covers every unit (workers, cargo), gold and routes.
        final String before = snapshot(model, data, "before.akropia");

        model.getTradeRouteStopOptions(owner, TradeRouteKind.SEA, DELFT,
                List.of());
        assertThrows(IllegalArgumentException.class,
                () -> model.createTradeRoute(owner, "Existing",
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(colony(DELFT))));
        assertThrows(IllegalArgumentException.class,
                () -> model.createTradeRoute(owner, "Stale",
                        TradeRouteKind.LAND, 99, List.of(colony(DELFT))));
        assertThrows(IllegalArgumentException.class,
                () -> model.createTradeRoute(owner, "Sea", TradeRouteKind.SEA,
                        NEW_AMSTERDAM, List.of(europe())));

        assertEquals(before, snapshot(model, data, "after.akropia"));
        assertEquals(1, model.getTradeRoutes(owner).size());
    }

    @Test
    void sameNameIsAllowedForAnotherOwnerAndRefusedForTheSameOwner() {
        // France is a second colonial player in this scenario. Restoration
        // applies the same per-owner rule that creation applies.
        final ModelPo base = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.akropia")
                .getModel();
        base.setNextColonyId(Math.max(1, base.getNextColonyId()));
        base.setNextTradeRouteId(2);
        base.setTradeRoutes(List.of(route(0, "Dutch", "Silver Road"),
                route(1, "France", "Silver Road")));
        final Model model = load(base);
        assertEquals(1,
                model.getTradeRoutes(model.getPlayerByName("Dutch")).size());
        assertEquals(1,
                model.getTradeRoutes(model.getPlayerByName("France")).size());

        final ModelPo duplicate = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.akropia")
                .getModel();
        duplicate.setNextColonyId(Math.max(1, duplicate.getNextColonyId()));
        duplicate.setNextTradeRouteId(2);
        duplicate.setTradeRoutes(List.of(route(0, "Dutch", "Silver Road"),
                route(1, "Dutch", "silver road")));
        assertThrows(IllegalArgumentException.class, () -> load(duplicate));
    }

    private static TradeRoutePo route(final int id, final String owner,
            final String name) {
        final TradeRouteStopPo stop = new TradeRouteStopPo();
        stop.setEurope(false);
        stop.setColonyId(0);
        stop.setUnloadGoods(List.of());
        stop.setLoadGoods(List.of());
        final TradeRoutePo po = new TradeRoutePo();
        po.setId(id);
        po.setOwnerName(owner);
        po.setName(name);
        po.setKind(TradeRouteKind.LAND);
        po.setMode(TradeRouteMode.TOUCH_AND_GO);
        po.setStartColonyId(0);
        po.setStops(List.of(stop));
        return po;
    }

    @Test
    void savedItineraryReloadsWithSameIdsOrderAndNames() {
        final GameModelPo data = fixture();
        final Model model = load(data.getModel());
        final Player owner = owner(model);
        model.createTradeRoute(owner, "Ocean", TradeRouteKind.SEA, DELFT,
                List.of(europe(), colony(DELFT), europe()));
        model.createTradeRoute(owner, "Inland", TradeRouteKind.LAND,
                NEW_AMSTERDAM, List.of(colony(DELFT), colony(NEW_AMSTERDAM)));
        final Path file = directory.resolve("routes.akropia");
        data.setModel(model.save());
        new GameModelPoDao().saveToFile(file.toString(), data);

        final Model restored = load(
                new GameModelPoDao().loadFromFile(file.toFile()).getModel());
        final Player restoredOwner = owner(restored);
        final List<TradeRoute> routes = restored.getTradeRoutes(restoredOwner);
        assertEquals(2, routes.size());
        assertEquals(0, routes.get(0).getId());
        assertEquals("Ocean", routes.get(0).getName());
        assertEquals(TradeRouteKind.SEA, routes.get(0).getKind());
        assertEquals(List.of(europe(), colony(DELFT), europe()),
                routes.get(0).getStops());
        assertEquals(1, routes.get(1).getId());
        assertEquals(List.of(colony(DELFT), colony(NEW_AMSTERDAM)),
                routes.get(1).getStops());
        assertEquals(2, restored.save().getNextTradeRouteId());
    }

    @Test
    void malformedRequiredRouteStateIsRejectedWithoutDefaults() {
        final ModelPo missingId = fixture().getModel();
        missingId.setNextTradeRouteId(null);
        assertThrows(IllegalArgumentException.class, () -> load(missingId));

        final ModelPo missingList = fixture().getModel();
        missingList.setTradeRoutes(null);
        assertThrows(IllegalArgumentException.class, () -> load(missingList));

        final ModelPo bothStops = fixture().getModel();
        final TradeRoutePo route = new TradeRoutePo();
        route.setId(0);
        route.setOwnerName("Dutch");
        route.setName("Broken");
        route.setKind(TradeRouteKind.SEA);
        route.setMode(TradeRouteMode.TOUCH_AND_GO);
        route.setStartColonyId(DELFT);
        final TradeRouteStopPo stop = new TradeRouteStopPo();
        stop.setEurope(true);
        stop.setColonyId(DELFT);
        stop.setUnloadGoods(List.of());
        stop.setLoadGoods(List.of());
        route.setStops(List.of(stop));
        bothStops.setTradeRoutes(List.of(route));
        assertThrows(IllegalArgumentException.class, () -> load(bothStops));

        // A saved stop without its goods lists is not defaulted to blank.
        final ModelPo missingCargo = fixture().getModel();
        final TradeRoutePo unlisted = route(0, "Dutch", "Unlisted");
        unlisted.getStops().get(0).setUnloadGoods(null);
        missingCargo.setTradeRoutes(List.of(unlisted));
        assertThrows(IllegalArgumentException.class, () -> load(missingCargo));
    }

    private String snapshot(final Model model, final GameModelPo data,
            final String name) throws Exception {
        final Path file = directory.resolve(name);
        data.setModel(model.save());
        new GameModelPoDao().saveToFile(file.toString(), data);
        return Files.readString(file);
    }
}
