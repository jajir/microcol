package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.turnevent.TurnEventCriticality;
import org.microcol.model.turnevent.TurnEventTradeRouteTransfer;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Turn notices of goods that a trade route actually moved (R-0392), on the
 * shared F108 scenario. Dutch's galleon 3 stands in the port of Delft (colony
 * 1) with [ORE 60, RUM 100, passenger, SUGAR 100, TOBACCO 100, COTTON 100].
 * Delft's warehouse holds CORN 100, HORSE 50, MUSKET 50 and no ORE or RUM.
 */
class TradeRouteTurnEventTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F108-trade-route-cargo.microcol");

    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final int EUROPE_STOP = 0;
    private static final int DELFT_STOP = 1;

    @TempDir
    Path directory;

    /** One loaded game plus the handles every test needs. */
    private final class World {

        private final GameModelPo data;
        private final Model model;
        private final Player dutch;
        private final UnitWithCargo ship;
        private final Colony delft;

        World() {
            this(new GameModelPoDao().loadFromFile(SCENARIO));
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
        TradeRoute route(final String name, final int stop,
                final List<GoodsType> unload, final List<GoodsType> load) {
            final TradeRoute created = model.createTradeRoute(dutch, name,
                    TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                            TradeRouteStop.ofColony(DELFT)));
            return model.configureTradeRouteStopCargo(dutch, created, stop,
                    unload, load);
        }

        /** Explicit processing by the player, as the cargo dialog does. */
        TradeRouteStopResult process(final TradeRoute route, final int stop) {
            return model.processTradeRouteStop(ship, route, stop);
        }

        /**
         * The processing the route navigator runs when the ship reaches the
         * stop on its own, after the same live checks.
         */
        TradeRouteStopResult arrive(final TradeRoute route, final int stop) {
            assertEquals(Optional.empty(),
                    model.getTradeRouteStopProblem(ship, route, stop));
            final TradeRouteStop target = route.getStops().get(stop);
            return new TradeRouteStopProcessor(model, ship, route, target,
                    new TradeRouteVisit(route.getId(), stop,
                            target.isEurope() ? null : ship.getLocation()),
                    true).run();
        }

        CargoSlot slot(final int index) {
            return ship.getCargo().getSlotByIndex(index);
        }

        void putShipInEurope() {
            model.getEurope().getPort().placeShipToPort(ship);
            assertTrue(ship.isAtEuropePort());
        }

        List<TurnEventTradeRouteTransfer> notices(final Player player) {
            return model.getTurnEventStore().getForPlayer(player).stream()
                    .filter(TurnEventTradeRouteTransfer.class::isInstance)
                    .map(TurnEventTradeRouteTransfer.class::cast)
                    .collect(Collectors.toList());
        }

        List<TurnEventTradeRouteTransfer> notices() {
            return notices(dutch);
        }

        /** Whole saved state as written to disk, for exact comparisons. */
        String snapshot(final String name) throws Exception {
            final Path file = directory.resolve(name);
            data.setModel(model.save());
            new GameModelPoDao().saveToFile(file.toString(), data);
            return Files.readString(file);
        }
    }

    private static void verifyNotice(final TurnEventTradeRouteTransfer notice,
            final TradeRoute route, final int stop, final boolean unload,
            final Goods goods, final int gold) {
        assertEquals("Dutch", notice.getPlayerName());
        assertEquals(route.getId(), notice.getRouteId());
        assertEquals(route.getName(), notice.getRouteName());
        assertEquals(SHIP_ID, notice.getCarrierId());
        assertEquals(UnitType.GALLEON, notice.getCarrierType());
        assertEquals(stop, notice.getStopIndex());
        assertEquals(unload, notice.isUnload());
        assertEquals(goods, notice.getGoods());
        assertEquals(gold, notice.getGoldChange());
        assertFalse(TurnEventCriticality.isCritical(notice),
                "A route transfer is a routine notice");
    }

    private static void verifyDelft(final TurnEventTradeRouteTransfer notice) {
        assertFalse(notice.isEurope());
        assertEquals(Optional.of(DELFT), notice.getColonyId());
        assertEquals(Optional.of("Delft"), notice.getColonyName());
    }

    private static void verifyEurope(final TurnEventTradeRouteTransfer notice) {
        assertTrue(notice.isEurope());
        assertEquals(Optional.empty(), notice.getColonyId());
        assertEquals(Optional.empty(), notice.getColonyName());
    }

    @Test
    void colonyUnloadAndLoadAreReportedInOrderWithoutGold() {
        final World w = new World();
        final TradeRoute route = w.route("Coast", DELFT_STOP,
                List.of(GoodsType.ORE, GoodsType.CIGARS, GoodsType.RUM),
                List.of(GoodsType.HORSE, GoodsType.MUSKET, GoodsType.CORN));
        final int gold = w.dutch.getGold();

        w.arrive(route, DELFT_STOP);

        // Cigars are not on board and corn finds no third free hold: no row.
        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(4, notices.size());
        verifyNotice(notices.get(0), route, DELFT_STOP, true,
                Goods.of(GoodsType.ORE, 60), 0);
        verifyNotice(notices.get(1), route, DELFT_STOP, true,
                Goods.of(GoodsType.RUM, 100), 0);
        verifyNotice(notices.get(2), route, DELFT_STOP, false,
                Goods.of(GoodsType.HORSE, 50), 0);
        verifyNotice(notices.get(3), route, DELFT_STOP, false,
                Goods.of(GoodsType.MUSKET, 50), 0);
        notices.forEach(TradeRouteTurnEventTest::verifyDelft);
        assertEquals(gold, w.dutch.getGold());
        // Only the owner of the carrier receives the notices.
        w.model.getPlayers().stream().filter(player -> player != w.dutch)
                .forEach(player -> assertTrue(w.notices(player).isEmpty(),
                        "Another player must not see the notices"));
    }

    @Test
    void europeSaleAndPurchaseAreSeparateNoticesWithActualGold() {
        final World w = new World();
        w.putShipInEurope();
        w.slot(0).removeCargo(Goods.of(GoodsType.ORE, 60));
        w.dutch.setGold(100000);
        final TradeRoute route = w.route("Rum Run", EUROPE_STOP,
                List.of(GoodsType.RUM), List.of(GoodsType.MUSKET));
        final int sale = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.RUM, 100), false)
                .getTotal();

        final TradeRouteStopResult result = w.arrive(route, EUROPE_STOP);

        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(2, notices.size());
        verifyNotice(notices.get(0), route, EUROPE_STOP, true,
                Goods.of(GoodsType.RUM, 100), sale);
        verifyNotice(notices.get(1), route, EUROPE_STOP, false,
                Goods.of(GoodsType.MUSKET, 200),
                result.getTransfers().get(1).getGoldChange());
        notices.forEach(TradeRouteTurnEventTest::verifyEurope);
        assertTrue(sale > 0);
        assertTrue(notices.get(1).getGoldChange() < 0);
        // The two signed amounts are exactly the treasury change after tax.
        assertEquals(100000 + sale + notices.get(1).getGoldChange(),
                w.dutch.getGold());
    }

    @Test
    void oneGoodsSpreadOverHoldsIsOneNoticeWithTheWholeSale() {
        final World w = new World();
        w.putShipInEurope();
        w.slot(0).removeCargo(Goods.of(GoodsType.ORE, 60));
        w.slot(0).addGoods(Goods.of(GoodsType.RUM, 50));
        final TradeRoute route = w.route("Rum Run", EUROPE_STOP,
                List.of(GoodsType.RUM), List.of());
        final int gold = w.dutch.getGold();

        final TradeRouteStopResult result = w.arrive(route, EUROPE_STOP);

        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), route, EUROPE_STOP, true,
                Goods.of(GoodsType.RUM, 150),
                result.getTransfers().get(0).getGoldChange());
        assertEquals(gold + notices.get(0).getGoldChange(), w.dutch.getGold());
    }

    @Test
    void partialPurchaseReportsOnlyWhatWasPaidFor() {
        final World w = new World();
        w.putShipInEurope();
        w.slot(0).removeCargo(Goods.of(GoodsType.ORE, 60));
        w.slot(1).removeCargo(Goods.of(GoodsType.RUM, 100));
        final TradeRoute route = w.route("Arms", EUROPE_STOP, List.of(),
                List.of(GoodsType.MUSKET, GoodsType.HORSE));
        final int money = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.MUSKET, 40), true)
                .getTotal();
        w.dutch.setGold(money);

        w.arrive(route, EUROPE_STOP);

        // The horse that could not be afforded is not a transaction.
        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), route, EUROPE_STOP, false,
                Goods.of(GoodsType.MUSKET, 40), -money);
        assertEquals(0, w.dutch.getGold());
    }

    @Test
    void settledHoldIsKeptWhenTheNextHoldOfTheSameEntryIsRejected() {
        final World w = new World();
        w.putShipInEurope();
        w.slot(1).removeCargo(Goods.of(GoodsType.RUM, 100));
        w.slot(1).addGoods(Goods.of(GoodsType.ORE, 100));
        final TradeRoute route = w.route("Ore Run", EUROPE_STOP,
                List.of(GoodsType.ORE, GoodsType.SUGAR),
                List.of(GoodsType.MUSKET));
        // The first hold's sale fills the treasury to the largest int; the
        // second hold's sale would overflow it and is refused unchanged.
        final int first = w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.ORE, 60), false)
                .getTotal();
        w.dutch.setGold(Integer.MAX_VALUE - first);

        final TradeRouteStopResult result = w.arrive(route, EUROPE_STOP);

        assertEquals(TradeRouteTransferReason.REJECTED,
                result.getTransfers().get(0).getReason());
        assertEquals(60, result.getTransfers().get(0).getAmount());
        assertEquals(TradeRouteTransferReason.NOT_ATTEMPTED,
                result.getTransfers().get(1).getReason());
        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), route, EUROPE_STOP, true,
                Goods.of(GoodsType.ORE, 60), first);
        assertEquals(Integer.MAX_VALUE, w.dutch.getGold());
    }

    @Test
    void emptyAndBlockedAutomaticVisitsAddNoNotice() {
        final World w = new World();
        // Nothing to unload and no free hold to load into.
        final TradeRoute idle = w.route("Idle", DELFT_STOP,
                List.of(GoodsType.CIGARS), List.of(GoodsType.CORN));
        final TradeRouteStopResult result = w.arrive(idle, DELFT_STOP);
        assertEquals(TradeRouteTransferReason.NOTHING_ON_BOARD,
                result.getTransfers().get(0).getReason());
        assertEquals(TradeRouteTransferReason.HOLD_FULL,
                result.getTransfers().get(1).getReason());
        assertTrue(w.notices().isEmpty());

        // Europe is closed after independence: nothing moves, nothing is
        // reported.
        final World closed = new World(declared());
        closed.putShipInEurope();
        final TradeRoute trade = closed.route("Closed", EUROPE_STOP,
                List.of(GoodsType.RUM), List.of(GoodsType.MUSKET));
        final int gold = closed.dutch.getGold();
        final TradeRouteStopResult refused = closed.arrive(trade, EUROPE_STOP);
        assertEquals(TradeRouteTransferReason.EUROPE_CLOSED,
                refused.getTransfers().get(0).getReason());
        assertEquals(gold, closed.dutch.getGold());
        assertTrue(closed.notices().isEmpty());
    }

    @Test
    void manualStopProcessingShowsItsResultButAddsNoReportRow() {
        // At a colony: the goods move and the result lists them.
        final World w = new World();
        final TradeRoute route = w.route("Coast", DELFT_STOP,
                List.of(GoodsType.ORE), List.of(GoodsType.HORSE));
        final TradeRouteStopResult colony = w.process(route, DELFT_STOP);
        assertEquals(60, colony.getTransfers().get(0).getAmount());
        assertEquals(50, colony.getTransfers().get(1).getAmount());
        assertEquals(60,
                w.delft.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertTrue(w.notices().isEmpty());

        // In Europe: the sale is paid as before, still without a report row.
        final World europe = new World();
        europe.putShipInEurope();
        final TradeRoute trade = europe.route("Rum Run", EUROPE_STOP,
                List.of(GoodsType.RUM), List.of());
        final int gold = europe.dutch.getGold();
        final int sale = europe.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.RUM, 100), false)
                .getTotal();
        final TradeRouteStopResult sold = europe.process(trade, EUROPE_STOP);
        assertEquals(sale, sold.getTransfers().get(0).getGoldChange());
        assertEquals(gold + sale, europe.dutch.getGold());
        assertTrue(europe.notices().isEmpty());
    }

    private static GameModelPo declared() {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch"))
                .forEach(player -> RebellionFixtures.declare(data.getModel(),
                        player));
        return data;
    }

    @Test
    void automaticArrivalReportsOnceAndAnotherUpdateAddsNothing() {
        final World w = new World();
        final TradeRoute route = w.route("Auto", DELFT_STOP,
                List.of(GoodsType.ORE), List.of(GoodsType.HORSE));
        w.model.assignTradeRoute(w.ship, route, DELFT_STOP);
        // Assigning moves nothing and reports nothing.
        assertTrue(w.notices().isEmpty());

        w.model.advanceTradeRouteAssignments(w.dutch);

        final List<TurnEventTradeRouteTransfer> notices = w.notices();
        assertEquals(2, notices.size());
        verifyNotice(notices.get(0), route, DELFT_STOP, true,
                Goods.of(GoodsType.ORE, 60), 0);
        verifyNotice(notices.get(1), route, DELFT_STOP, false,
                Goods.of(GoodsType.HORSE, 50), 0);
        w.model.advanceTradeRouteAssignments(w.dutch);
        assertEquals(2, w.notices().size());
    }

    @Test
    void savedNoticesSurviveContinueOnceAndStayReadableAfterRouteRemoval()
            throws Exception {
        final World w = new World();
        final TradeRoute route = w.route("Coast", DELFT_STOP,
                List.of(GoodsType.ORE), List.of(GoodsType.HORSE));
        w.model.assignTradeRoute(w.ship, route, DELFT_STOP);
        w.model.advanceTradeRouteAssignments(w.dutch);
        assertEquals(2, w.notices().size());
        final String settled = w.snapshot("settled.json");

        final World restored = new World(new GameModelPoDao()
                .loadFromFile(directory.resolve("settled.json").toFile()));

        // Loading neither repeats the transfer nor copies the notices.
        final List<TurnEventTradeRouteTransfer> notices = restored.notices();
        assertEquals(2, notices.size());
        verifyNotice(notices.get(0), route, DELFT_STOP, true,
                Goods.of(GoodsType.ORE, 60), 0);
        verifyNotice(notices.get(1), route, DELFT_STOP, false,
                Goods.of(GoodsType.HORSE, 50), 0);
        notices.forEach(TradeRouteTurnEventTest::verifyDelft);
        assertEquals(settled, restored.snapshot("restored.json"));
        final TradeRoute restoredRoute = restored.model
                .getTradeRoutes(restored.dutch).get(0);
        // A further automatic update of the restored turn moves no goods
        // again and adds no row.
        final Goods horse = restored.ship.getCargo().getSlotByIndex(0)
                .getGoods().orElseThrow();
        restored.model.advanceTradeRouteAssignments(restored.dutch);
        assertEquals(2, restored.notices().size());
        assertEquals(horse, restored.ship.getCargo().getSlotByIndex(0)
                .getGoods().orElseThrow());
        assertEquals(60, restored.delft.getWarehouse().getGoods(GoodsType.ORE)
                .getAmount());

        // The snapshot keeps the line readable after the route is gone.
        restored.model.deleteTradeRoute(restored.dutch, restoredRoute);
        assertEquals("Coast", restored.notices().get(0).getRouteName());

        // The notices end with the owner's turn, as every report does.
        restored.model.endTurn();
        assertTrue(restored.notices().isEmpty());
    }

    @Test
    void incompleteOrInconsistentSavedNoticeIsRejected() {
        final TurnEventPo other = new TurnEventPo();
        other.setType("TurnEventGoTo");
        assertNull(TurnEventTradeRouteTransfer.tryLoad(other));
        assertNotNull(TurnEventTradeRouteTransfer.tryLoad(colonyPo()));
        final List<Consumer<TurnEventPo>> omissions = List.of(
                po -> po.setTradeRouteId(null),
                po -> po.setTradeRouteName(null),
                po -> po.setTradeRouteCarrierId(null),
                po -> po.setTradeRouteCarrierType(null),
                po -> po.setTradeRouteStopIndex(null),
                po -> po.setTradeRouteUnload(null), po -> po.setGoods(null),
                po -> po.setTradeRouteGold(null), po -> po.setColonyName(null));
        for (final Consumer<TurnEventPo> omission : omissions) {
            final TurnEventPo po = colonyPo();
            omission.accept(po);
            assertThrows(IllegalArgumentException.class,
                    () -> TurnEventTradeRouteTransfer.tryLoad(po));
        }
        // A colony transfer has no price; a zero quantity is no transaction.
        final TurnEventPo paid = colonyPo();
        paid.setTradeRouteGold(5);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventTradeRouteTransfer.tryLoad(paid));
        final TurnEventPo empty = colonyPo();
        empty.setGoods(Goods.of(GoodsType.ORE, 0));
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventTradeRouteTransfer.tryLoad(empty));
        // A European sale cannot cost gold, nor a purchase earn it.
        final TurnEventPo sale = colonyPo();
        sale.setColonyId(null);
        sale.setColonyName(null);
        sale.setTradeRouteGold(-5);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventTradeRouteTransfer.tryLoad(sale));
        sale.setTradeRouteUnload(false);
        assertEquals(-5,
                TurnEventTradeRouteTransfer.tryLoad(sale).getGoldChange());
    }

    private static TurnEventPo colonyPo() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(TurnEventTradeRouteTransfer.class.getSimpleName());
        po.setPlayerName("Dutch");
        po.setTradeRouteId(0);
        po.setTradeRouteName("Coast");
        po.setTradeRouteCarrierId(SHIP_ID);
        po.setTradeRouteCarrierType(UnitType.GALLEON);
        po.setTradeRouteStopIndex(DELFT_STOP);
        po.setColonyId(DELFT);
        po.setColonyName("Delft");
        po.setTradeRouteUnload(true);
        po.setGoods(Goods.of(GoodsType.ORE, 60));
        po.setTradeRouteGold(0);
        return po;
    }
}
