package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Processing of a route stop on the shared F108 scenario. Dutch's galleon 3 has
 * six holds and stands in Delft with [ORE 60, RUM 100, passenger, SUGAR 100,
 * TOBACCO 100, COTTON 100], so unloading ORE frees the only hold that can take
 * new goods. Delft's basic warehouse (100 of each goods) holds CORN 100, HORSE
 * 50, MUSKET 50, TOOLS 50 and no ORE.
 */
class TradeRouteStopProcessingTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F108-trade-route-cargo.microcol");

    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final Location PORT = Location.of(28, 16);
    private static final Location SEA = Location.of(29, 16);

    @TempDir
    Path directory;

    private static GameModelPo prepared(final Consumer<ModelPo> prepare) {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        prepare.accept(data.getModel());
        return data;
    }

    /** One loaded game plus the handles every test needs. */
    private final class World {

        private final GameModelPo data;
        private final Model model;
        private final Player dutch;
        private final UnitWithCargo ship;
        private final Colony delft;
        private int routes;

        World() {
            this(prepared(po -> {
            }));
        }

        World(final Consumer<ModelPo> prepare) {
            this(prepared(prepare));
        }

        World(final GameModelPo data) {
            this.data = data;
            this.model = Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            this.delft = model.getColonyById(DELFT).orElseThrow();
            this.dutch = delft.getOwner();
            this.ship = (UnitWithCargo) dutch.getUnits().stream()
                    .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                    .orElseThrow();
        }

        /** Sea route Europe, Delft whose given stop has the given goods. */
        TradeRoute route(final int stop, final List<GoodsType> unload,
                final List<GoodsType> load) {
            routes++;
            final TradeRoute created = model.createTradeRoute(dutch,
                    "Route " + routes, TradeRouteKind.SEA, DELFT,
                    List.of(TradeRouteStop.europe(),
                            TradeRouteStop.ofColony(DELFT)));
            return model.configureTradeRouteStopCargo(dutch, created, stop,
                    unload, load);
        }

        TradeRouteStopResult process(final TradeRoute route, final int stop) {
            return model.processTradeRouteStop(ship, route, stop);
        }

        CargoSlot slot(final int index) {
            return ship.getCargo().getSlotByIndex(index);
        }

        Optional<Goods> held(final int index) {
            return slot(index).getGoods();
        }

        int stock(final GoodsType type) {
            return delft.getWarehouse().getGoods(type).getAmount();
        }

        /** Whole saved state as written to disk, for exact comparisons. */
        String snapshot(final String name) throws Exception {
            final Path file = directory.resolve(name);
            data.setModel(model.save());
            new GameModelPoDao().saveToFile(file.toString(), data);
            return Files.readString(file);
        }

        void putShipInEurope() {
            model.getEurope().getPort().placeShipToPort(ship);
            assertTrue(ship.isAtEuropePort());
        }
    }

    private static void assertTransfer(final TradeRouteTransfer actual,
            final boolean unload, final GoodsType type, final int amount,
            final TradeRouteTransferReason reason) {
        assertEquals(unload, actual.isUnload());
        assertEquals(type, actual.getGoodsType());
        assertEquals(amount, actual.getAmount());
        assertEquals(reason, actual.getReason());
    }

    private static void assertProcessed(final TradeRouteStopResult result,
            final int entries) {
        assertEquals(Optional.empty(), result.getProblem());
        assertEquals(entries, result.getTransfers().size());
    }

    @Test
    void unloadSpaceIsReusedByTheLaterLoad() {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        // Before: no hold can take musket, because every free hold is taken.
        // All six holds are occupied, so no hold can take musket yet.
        assertEquals(6, w.ship.getCargo().getSlots().size());
        assertTrue(Model.roomForGoods(w.ship, GoodsType.MUSKET).isEmpty());
        assertEquals(Goods.of(GoodsType.ORE, 60), w.held(0).orElseThrow());

        final TradeRouteStopResult result = w.process(route, 1);

        assertProcessed(result, 2);
        assertTransfer(result.getTransfers().get(0), true, GoodsType.ORE, 60,
                TradeRouteTransferReason.MOVED);
        assertTransfer(result.getTransfers().get(1), false, GoodsType.MUSKET,
                50, TradeRouteTransferReason.MOVED);
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(0, w.stock(GoodsType.MUSKET));
        assertEquals(Goods.of(GoodsType.MUSKET, 50), w.held(0).orElseThrow());
        assertEquals(Goods.of(GoodsType.RUM, 100), w.held(1).orElseThrow());
        assertTrue(w.slot(2).isLoadedUnit());
        assertEquals(Goods.of(GoodsType.SUGAR, 100), w.held(3).orElseThrow());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), w.held(4).orElseThrow());
        assertEquals(Goods.of(GoodsType.COTTON, 100), w.held(5).orElseThrow());
        assertTrue(w.ship.getProcessedTradeRouteVisit().isPresent());
    }

    @Test
    void loadWithoutUnloadFindsNoRoomAndIsAFiniteNoOp() throws Exception {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(), List.of(GoodsType.CORN));
        final String before = w.snapshot("before.json");

        final TradeRouteStopResult result = w.process(route, 1);

        assertProcessed(result, 1);
        assertTransfer(result.getTransfers().get(0), false, GoodsType.CORN, 0,
                TradeRouteTransferReason.HOLD_FULL);
        assertEquals(100, w.stock(GoodsType.CORN));
        // Only the processed-visit mark differs from the saved state before.
        assertTrue(w.ship.getProcessedTradeRouteVisit().isPresent());
        assertNotEquals(before, w.snapshot("after.json"));
    }

    @Test
    void listOrderDecidesWhoGetsTheOnlyFreeHold() {
        final World musketFirst = new World();
        final TradeRoute first = musketFirst.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET, GoodsType.HORSE));
        final TradeRouteStopResult a = musketFirst.process(first, 1);
        assertProcessed(a, 3);
        assertTransfer(a.getTransfers().get(1), false, GoodsType.MUSKET, 50,
                TradeRouteTransferReason.MOVED);
        assertTransfer(a.getTransfers().get(2), false, GoodsType.HORSE, 0,
                TradeRouteTransferReason.HOLD_FULL);
        assertEquals(Goods.of(GoodsType.MUSKET, 50),
                musketFirst.held(0).orElseThrow());
        assertEquals(50, musketFirst.stock(GoodsType.HORSE));

        final World horseFirst = new World();
        final TradeRoute second = horseFirst.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.HORSE, GoodsType.MUSKET));
        final TradeRouteStopResult b = horseFirst.process(second, 1);
        assertProcessed(b, 3);
        assertTransfer(b.getTransfers().get(1), false, GoodsType.HORSE, 50,
                TradeRouteTransferReason.MOVED);
        assertTransfer(b.getTransfers().get(2), false, GoodsType.MUSKET, 0,
                TradeRouteTransferReason.HOLD_FULL);
        assertEquals(Goods.of(GoodsType.HORSE, 50),
                horseFirst.held(0).orElseThrow());
        assertEquals(50, horseFirst.stock(GoodsType.MUSKET));
    }

    @Test
    void absentEmptyFullAndPartialEntriesAreExplicit() {
        // Absent on board, then a full unload.
        final World absent = new World();
        final TradeRouteStopResult first = absent.process(absent.route(1,
                List.of(GoodsType.CIGARS, GoodsType.RUM), List.of()), 1);
        assertProcessed(first, 2);
        assertTransfer(first.getTransfers().get(0), true, GoodsType.CIGARS, 0,
                TradeRouteTransferReason.NOTHING_ON_BOARD);
        assertTransfer(first.getTransfers().get(1), true, GoodsType.RUM, 100,
                TradeRouteTransferReason.MOVED);
        assertTrue(absent.held(1).isEmpty());

        // Warehouse without any room: nothing moves and the hold keeps it.
        final World full = new World();
        full.delft.getWarehouse().addGoods(Goods.of(GoodsType.ORE,
                full.delft.getWarehouse().getFreeStorage(GoodsType.ORE)));
        final TradeRouteStopResult second = full
                .process(full.route(1, List.of(GoodsType.ORE), List.of()), 1);
        assertTransfer(second.getTransfers().get(0), true, GoodsType.ORE, 0,
                TradeRouteTransferReason.DESTINATION_FULL);
        assertEquals(Goods.of(GoodsType.ORE, 60), full.held(0).orElseThrow());

        // Room for only ten of the sixty.
        final World partial = new World();
        final ColonyWarehouse store = partial.delft.getWarehouse();
        store.addGoods(Goods.of(GoodsType.ORE,
                store.getFreeStorage(GoodsType.ORE) - 10));
        final TradeRouteStopResult third = partial.process(
                partial.route(1, List.of(GoodsType.ORE), List.of()), 1);
        assertTransfer(third.getTransfers().get(0), true, GoodsType.ORE, 10,
                TradeRouteTransferReason.PARTIAL_DESTINATION_FULL);
        assertEquals(Goods.of(GoodsType.ORE, 50),
                partial.held(0).orElseThrow());
        assertEquals(0, store.getFreeStorage(GoodsType.ORE));

        // Empty warehouse source after the hold was freed.
        final World empty = new World();
        final TradeRouteStopResult fourth = empty.process(empty.route(1,
                List.of(GoodsType.ORE), List.of(GoodsType.COTTON)), 1);
        assertTransfer(fourth.getTransfers().get(1), false, GoodsType.COTTON, 0,
                TradeRouteTransferReason.SOURCE_EMPTY);
        assertTrue(empty.held(0).isEmpty());

        // The hold fills before the warehouse stock is exhausted.
        final World crowded = new World();
        crowded.delft.getWarehouse().addGoods(Goods.of(GoodsType.ORE, 70));
        final TradeRouteStopResult fifth = crowded.process(
                crowded.route(1, List.of(), List.of(GoodsType.ORE)), 1);
        assertTransfer(fifth.getTransfers().get(0), false, GoodsType.ORE, 40,
                TradeRouteTransferReason.PARTIAL_HOLD_FULL);
        assertEquals(Goods.of(GoodsType.ORE, 100),
                crowded.held(0).orElseThrow());
        assertEquals(30, crowded.stock(GoodsType.ORE));
    }

    @Test
    void sameVisitCannotBeReplayedByAnyRepeatChangeOrReassignment() {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        assertProcessed(w.process(route, 1), 2);

        // Repeated command.
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(route, 1).getProblem());
        // Another stop index of the same visit.
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(route, 0).getProblem());
        // Reconfigured stop, and a different route.
        final TradeRoute changed = w.model.configureTradeRouteStopCargo(w.dutch,
                route, 1, List.of(GoodsType.MUSKET), List.of());
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(changed, 1).getProblem());
        final TradeRoute other = w.route(1, List.of(GoodsType.MUSKET),
                List.of());
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(other, 1).getProblem());
        // Placing the ship again where it already stands is not a departure.
        w.ship.placeToLocation(w.ship.getLocation());
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(changed, 1).getProblem());

        assertEquals(Goods.of(GoodsType.MUSKET, 50), w.held(0).orElseThrow());
        assertEquals(0, w.stock(GoodsType.MUSKET));
        assertEquals(60, w.stock(GoodsType.ORE));
    }

    @Test
    void processedVisitSurvivesSaveAndDiskRestoreAndProcessesNothing()
            throws Exception {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        assertProcessed(w.process(route, 1), 2);
        final String settled = w.snapshot("settled.json");

        final World restored = new World(new GameModelPoDao()
                .loadFromFile(directory.resolve("settled.json").toFile()));

        final TradeRouteVisit visit = restored.ship
                .getProcessedTradeRouteVisit().orElseThrow();
        assertEquals(route.getId(), visit.getRouteId());
        assertEquals(1, visit.getStopIndex());
        assertEquals(Optional.of(PORT), visit.getLocation());
        // Loading performed no transfer: the saved state is reproduced exactly.
        assertEquals(settled, restored.snapshot("restored.json"));
        final TradeRoute restoredRoute = restored.model
                .getTradeRoutes(restored.dutch).get(0);
        assertEquals(List.of(GoodsType.ORE),
                restoredRoute.getStops().get(1).getUnloadGoods());
        assertEquals(List.of(GoodsType.MUSKET),
                restoredRoute.getStops().get(1).getLoadGoods());
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                restored.process(restoredRoute, 1).getProblem());
        assertEquals(settled, restored.snapshot("after-refusal.json"));

        // A saved visit that names no route is not accepted.
        assertThrows(IllegalArgumentException.class, () -> new World(po -> {
            po.getUnits().stream().filter(unit -> unit.getId() == SHIP_ID)
                    .forEach(unit -> {
                        unit.setTradeRouteVisitRouteId(7);
                        unit.setTradeRouteVisitStopIndex(0);
                        unit.setTradeRouteVisitLocation(PORT);
                    });
        }));
        // A saved visit at another square than the ship stands on is refused.
        assertThrows(IllegalArgumentException.class, () -> new World(po -> {
            po.getUnits().stream().filter(unit -> unit.getId() == SHIP_ID)
                    .forEach(unit -> {
                        unit.setTradeRouteVisitRouteId(0);
                        unit.setTradeRouteVisitStopIndex(1);
                        unit.setTradeRouteVisitLocation(SEA);
                    });
        }));
        // An incomplete saved visit is refused rather than dropped.
        assertThrows(IllegalArgumentException.class, () -> new World(po -> {
            po.getUnits().stream().filter(unit -> unit.getId() == SHIP_ID)
                    .forEach(unit -> unit.setTradeRouteVisitStopIndex(1));
        }));
    }

    @Test
    void departureAndRearrivalAllowTheNextRealVisit() {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE), List.of());
        final TradeRouteStopResult firstVisit = w.process(route, 1);
        assertTransfer(firstVisit.getTransfers().get(0), true, GoodsType.ORE,
                60, TradeRouteTransferReason.MOVED);

        w.ship.placeToLocation(SEA);
        assertTrue(w.ship.getProcessedTradeRouteVisit().isEmpty());
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.process(route, 1).getProblem());
        w.ship.placeToLocation(PORT);
        w.slot(0).addGoods(Goods.of(GoodsType.ORE, 20));

        final TradeRouteStopResult secondVisit = w.process(route, 1);
        assertProcessed(secondVisit, 1);
        assertTransfer(secondVisit.getTransfers().get(0), true, GoodsType.ORE,
                20, TradeRouteTransferReason.MOVED);
        assertEquals(80, w.stock(GoodsType.ORE));
        assertTrue(w.ship.getProcessedTradeRouteVisit().isPresent());
    }

    @Test
    void refusedProcessingChangesNothingInTheSavedState() throws Exception {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        final TradeRoute land = w.model.createTradeRoute(w.dutch, "Land",
                TradeRouteKind.LAND, 0,
                List.of(TradeRouteStop.ofColony(DELFT)));
        final String before = w.snapshot("before.json");

        // Wrong transport kind, a unit that carries nothing, bad indexes.
        assertEquals(Optional.of(TradeRouteStopProblem.CARRIER_UNAVAILABLE),
                w.process(land, 0).getProblem());
        final Unit colonist = w.dutch.getUnits().stream()
                .filter(unit -> !(unit instanceof UnitWithCargo)).findFirst()
                .orElseThrow();
        assertEquals(Optional.of(TradeRouteStopProblem.CARRIER_UNAVAILABLE),
                w.model.processTradeRouteStop(colonist, route, 1).getProblem());
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.process(route, -1).getProblem());
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.process(route, 2).getProblem());
        // The ship is in Delft, not in Europe.
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.process(route, 0).getProblem());
        assertEquals(before, w.snapshot("after-position.json"));

        // Stale route: a reconfigured stop replaces the stored route.
        final TradeRoute current = w.model.configureTradeRouteStopCargo(w.dutch,
                route, 1, List.of(GoodsType.ORE), List.of());
        final String reconfigured = w.snapshot("reconfigured.json");
        assertEquals(Optional.of(TradeRouteStopProblem.ROUTE_UNAVAILABLE),
                w.process(route, 1).getProblem());
        assertThrows(IllegalArgumentException.class,
                () -> w.model.configureTradeRouteStopCargo(w.dutch, route, 1,
                        List.of(), List.of()));
        assertEquals(reconfigured, w.snapshot("after-stale.json"));

        // Wrong owner of the route.
        final Player king = w.model.getPlayerByName("Dutch's King");
        assertThrows(IllegalArgumentException.class,
                () -> w.model.configureTradeRouteStopCargo(king, current, 1,
                        List.of(), List.of()));
        assertEquals(reconfigured, w.snapshot("after-owner.json"));

        // Away from the stop.
        w.ship.placeToLocation(SEA);
        final String away = w.snapshot("away.json");
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.process(current, 1).getProblem());
        assertEquals(away, w.snapshot("after-away.json"));
    }

    @Test
    void europeSaleUsesTheLiveQuoteTaxTreasuryAndAccount() {
        final World w = new World();
        w.putShipInEurope();
        final TradeRoute route = w.route(0, List.of(GoodsType.RUM), List.of());
        final EuropeTradeQuote quote = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.RUM, 100), false);
        final int gold = w.dutch.getGold();
        final EuropeTradeAccount account = w.model.getEurope()
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.RUM);

        final TradeRouteStopResult result = w.process(route, 0);

        assertProcessed(result, 1);
        assertTransfer(result.getTransfers().get(0), true, GoodsType.RUM, 100,
                TradeRouteTransferReason.MOVED);
        assertEquals(quote.getTotal(),
                result.getTransfers().get(0).getGoldChange());
        assertEquals(gold + quote.getTotal(), w.dutch.getGold());
        final EuropeTradeAccount after = w.model.getEurope()
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.RUM);
        assertEquals(account.getSoldQuantity() + 100, after.getSoldQuantity());
        assertEquals(account.getSoldGold() + quote.getTotal(),
                after.getSoldGold());
        assertEquals(account.getBoughtQuantity(), after.getBoughtQuantity());
        assertTrue(w.held(1).isEmpty());
        assertEquals(100, w.stock(GoodsType.CORN));
    }

    @Test
    void europePurchaseFillsTheHoldWithTheLiveQuoteAndTax() {
        final World w = new World();
        w.putShipInEurope();
        w.slot(0).removeCargo(Goods.of(GoodsType.ORE, 60));
        w.dutch.setGold(100000);
        final TradeRoute route = w.route(0, List.of(),
                List.of(GoodsType.MUSKET));
        final EuropeTradeQuote quote = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.MUSKET, 100), true);
        final EuropeTradeAccount account = w.model.getEurope()
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.MUSKET);

        final TradeRouteStopResult result = w.process(route, 0);

        assertProcessed(result, 1);
        assertTransfer(result.getTransfers().get(0), false, GoodsType.MUSKET,
                100, TradeRouteTransferReason.MOVED);
        assertEquals(-quote.getTotal(),
                result.getTransfers().get(0).getGoldChange());
        assertEquals(100000 - quote.getTotal(), w.dutch.getGold());
        assertEquals(Goods.of(GoodsType.MUSKET, 100), w.held(0).orElseThrow());
        final EuropeTradeAccount after = w.model.getEurope()
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.MUSKET);
        assertEquals(account.getBoughtQuantity() + 100,
                after.getBoughtQuantity());
        assertEquals(account.getBoughtGold() + quote.getTotal(),
                after.getBoughtGold());
    }

    @Test
    void scarceFundsGoToTheGoodsListedFirst() {
        for (final GoodsType[] order : new GoodsType[][] {
                { GoodsType.MUSKET, GoodsType.HORSE },
                { GoodsType.HORSE, GoodsType.MUSKET } }) {
            final World w = new World();
            w.putShipInEurope();
            w.slot(0).removeCargo(Goods.of(GoodsType.ORE, 60));
            w.slot(1).removeCargo(Goods.of(GoodsType.RUM, 100));
            final TradeRoute route = w.route(0, List.of(),
                    List.of(order[0], order[1]));
            // Exactly enough for forty units of the first, nothing for more.
            final int money = w.dutch
                    .quoteEuropeTrade(Goods.of(order[0], 40), true).getTotal();
            w.dutch.setGold(money);

            final TradeRouteStopResult result = w.process(route, 0);

            assertProcessed(result, 2);
            assertTransfer(result.getTransfers().get(0), false, order[0], 40,
                    TradeRouteTransferReason.PARTIAL_FUNDS);
            assertEquals(-money, result.getTransfers().get(0).getGoldChange());
            assertTransfer(result.getTransfers().get(1), false, order[1], 0,
                    TradeRouteTransferReason.INSUFFICIENT_FUNDS);
            assertEquals(0, w.dutch.getGold());
            assertEquals(Goods.of(order[0], 40), w.held(0).orElseThrow());
            assertTrue(w.held(1).isEmpty());
        }
    }

    @Test
    void europeIsClosedAfterIndependenceAndNothingMoves() throws Exception {
        final World w = new World(po -> po.getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .forEach(player -> player.setDeclaredIndependence(true)));
        w.putShipInEurope();
        final TradeRoute route = w.route(0, List.of(GoodsType.RUM),
                List.of(GoodsType.MUSKET));
        final int gold = w.dutch.getGold();

        final TradeRouteStopResult result = w.process(route, 0);

        assertProcessed(result, 2);
        assertTransfer(result.getTransfers().get(0), true, GoodsType.RUM, 0,
                TradeRouteTransferReason.EUROPE_CLOSED);
        assertTransfer(result.getTransfers().get(1), false, GoodsType.MUSKET, 0,
                TradeRouteTransferReason.EUROPE_CLOSED);
        assertEquals(gold, w.dutch.getGold());
        assertEquals(Goods.of(GoodsType.RUM, 100), w.held(1).orElseThrow());
    }

    @Test
    void rejectedEntryKeepsEarlierSettledTransfersAndStopsLaterOnes()
            throws Exception {
        final World w = new World();
        w.putShipInEurope();
        final TradeRoute route = w.route(0,
                List.of(GoodsType.RUM, GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        // The rum sale brings the treasury exactly to the largest int; the ore
        // sale would overflow it, which the sale refuses before changing
        // anything.
        final int rum = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.RUM, 100), false)
                .getTotal();
        w.dutch.setGold(Integer.MAX_VALUE - rum);

        final TradeRouteStopResult result = w.process(route, 0);

        assertProcessed(result, 3);
        assertTransfer(result.getTransfers().get(0), true, GoodsType.RUM, 100,
                TradeRouteTransferReason.MOVED);
        assertEquals(rum, result.getTransfers().get(0).getGoldChange());
        assertTransfer(result.getTransfers().get(1), true, GoodsType.ORE, 0,
                TradeRouteTransferReason.REJECTED);
        assertTrue(result.getTransfers().get(1).getDetail().isPresent());
        assertTransfer(result.getTransfers().get(2), false, GoodsType.MUSKET, 0,
                TradeRouteTransferReason.NOT_ATTEMPTED);
        assertEquals(Integer.MAX_VALUE, w.dutch.getGold());
        assertTrue(w.held(1).isEmpty());
        assertEquals(Goods.of(GoodsType.ORE, 60), w.held(0).orElseThrow());
        // The settled sale cannot be repeated by a retry.
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.process(route, 0).getProblem());
        assertEquals(Integer.MAX_VALUE, w.dutch.getGold());
    }

    @Test
    void cargoChangesAreStoredEvenThoughStopEqualityIgnoresThem() {
        final World w = new World();
        final TradeRoute plain = w.route(1, List.of(), List.of());
        final TradeRoute configured = w.model.configureTradeRouteStopCargo(
                w.dutch, plain, 1, List.of(GoodsType.ORE),
                List.of(GoodsType.HORSE, GoodsType.MUSKET));
        // Stops are the same stops, yet the stored route carries the lists.
        assertEquals(plain.getStops(), configured.getStops());
        final TradeRoute stored = w.model.getTradeRoutes(w.dutch).get(0);
        assertSame(configured, stored);
        assertEquals(List.of(GoodsType.HORSE, GoodsType.MUSKET),
                stored.getStops().get(1).getLoadGoods());
        assertEquals(List.of(), stored.getStops().get(0).getLoadGoods());
        assertEquals(plain.getId(), stored.getId());
        assertEquals(plain.getName(), stored.getName());
    }

    @Test
    void carriersAtAStopAreListedWithTheirReadiness() {
        final World w = new World();
        final TradeRoute route = w.route(1, List.of(GoodsType.ORE), List.of());
        assertEquals(List.of(w.ship),
                w.model.getTradeRouteCarriers(w.dutch, route, 1));
        assertEquals(List.of(),
                w.model.getTradeRouteCarriers(w.dutch, route, 0));
        assertEquals(List.of(),
                w.model.getTradeRouteCarriers(w.dutch, route, 5));
        assertEquals(Optional.empty(),
                w.model.getTradeRouteStopProblem(w.ship, route, 1));
        w.process(route, 1);
        assertEquals(List.of(w.ship),
                w.model.getTradeRouteCarriers(w.dutch, route, 1));
        assertEquals(Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED),
                w.model.getTradeRouteStopProblem(w.ship, route, 1));
    }
}
