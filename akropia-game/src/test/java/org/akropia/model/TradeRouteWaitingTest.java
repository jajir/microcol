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
import org.akropia.model.store.UnitPo;
import org.akropia.model.unit.UnitWithCargo;

/**
 * Route-wide stop mode and the fixed goods order (R-0421). Dutch's galleon 3
 * stands in the port of Delft with all six holds taken [ORE 60, OLIVE_OIL 100,
 * passenger, OLIVES 100, GRAPES 100, WOOL 100]; Delft holds ORE 0, HORSE 50 and
 * WEAPONS 50. A later owner turn is the next played turn followed by the
 * owner's preparation boundary.
 */
class TradeRouteWaitingTest {

    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F108-trade-route-cargo.akropia");

    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final int EUROPE_STOP = 0;
    private static final int DELFT_STOP = 1;
    private static final Location DELFT_AT = Location.of(28, 16);

    private static final class World {

        private final GameModelPo data;
        private final Model model;
        private final Player dutch;
        private final Colony delft;
        private final UnitWithCargo ship;

        World() {
            this(new GameModelPoDao().loadFromFile(SCENARIO));
        }

        World(final GameModelPo data) {
            this.data = data;
            model = Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            delft = model.getColonyById(DELFT).orElseThrow();
            dutch = delft.getOwner();
            ship = (UnitWithCargo) model.getAllUnits().stream()
                    .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                    .orElseThrow();
        }

        /** Sea route Home port, Delft with the given mode and cargo. */
        TradeRoute route(final TradeRouteMode mode, final TradeRouteStop europe,
                final TradeRouteStop colony) {
            final TradeRoute created = model.createTradeRoute(dutch, "Run",
                    TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                            TradeRouteStop.ofColony(DELFT)));
            return model.updateTradeRoute(dutch, created,
                    List.of(europe, colony), mode);
        }

        TradeRoute stored() {
            return model.getTradeRoutes(dutch).get(0);
        }

        static TradeRouteStop delftStop(final List<GoodsType> unload,
                final List<GoodsType> load) {
            return TradeRouteStop.ofColony(DELFT).withCargo(unload, load);
        }

        static TradeRouteStop europeStop(final List<GoodsType> unload,
                final List<GoodsType> load) {
            return TradeRouteStop.europe().withCargo(unload, load);
        }

        void assign(final TradeRoute route, final int stop) {
            model.assignTradeRoute(ship, route, stop);
        }

        void ownerUpdate() {
            model.advanceTradeRouteAssignments(dutch);
        }

        /** A later owner turn: the turn number advances, then preparation. */
        void laterTurn() {
            model.getCalendar().endRound();
            ownerUpdate();
        }

        int stock(final GoodsType type) {
            return delft.getWarehouse().getGoods(type).getAmount();
        }

        Optional<Goods> held(final int slot) {
            return ship.getCargo().getSlotByIndex(slot).getGoods();
        }

        TradeRouteAssignment assignment() {
            return model.getTradeRouteAssignment(ship).orElseThrow();
        }

        Optional<TradeRoutePause> pause() {
            return assignment().getPause();
        }

        void freeHold(final int slot) {
            final CargoSlot hold = ship.getCargo().getSlotByIndex(slot);
            hold.removeCargo(hold.getGoods().orElseThrow());
        }

        void fillWarehouse(final GoodsType type) {
            delft.getWarehouse().addGoods(
                    Goods.of(type, delft.getWarehouse().getFreeStorage(type)));
        }

        World saveAndContinue() {
            data.setModel(model.save());
            return new World(data);
        }
    }

    @Test
    void touchAndGoTransfersWhatIsAvailableAndContinues() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.TOUCH_AND_GO,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        // Fifty horses fill half of the freed hold; the carrier does not wait.
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
        assertEquals(Optional.empty(), w.pause());
    }

    @Test
    void waitingForStockKeepsTheCarrierAndTopsUpThePartialHoldLater() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(0, w.stock(GoodsType.HORSE));
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(DELFT_STOP, w.assignment().getStopIndex());
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());
        assertEquals(DELFT_AT, w.ship.getLocation());

        // A repeated preparation in the same turn transfers nothing, even
        // though new stock arrived meanwhile.
        w.delft.getWarehouse().addGoods(Goods.of(GoodsType.HORSE, 80));
        w.ownerUpdate();
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(80, w.stock(GoodsType.HORSE));
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());

        // A later turn tops the partial hold up from the actual stock; the
        // ignored goods and the passenger keep their holds, so the carrier
        // continues without needing them filled.
        w.laterTurn();
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 100)), w.held(0));
        assertEquals(30, w.stock(GoodsType.HORSE));
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(Optional.of(Goods.of(GoodsType.OLIVE_OIL, 100)),
                w.held(1));
        assertTrue(w.ship.getCargo().getSlotByIndex(2).getUnit().isPresent());
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
        assertEquals(Optional.empty(), w.pause());
    }

    @Test
    void firstAvailableGoodsFillsEveryFreeHoldWithoutQuotas() {
        final World w = new World();
        w.freeHold(1);
        w.delft.getWarehouse().addGoods(Goods.of(GoodsType.GRAIN, 300));
        final int grain = w.stock(GoodsType.GRAIN);
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.WEAPONS, GoodsType.GRAIN)));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        // Grain precedes the weapon in the goods order and is abundant, so it
        // takes both free holds; nothing is split between the goods.
        assertEquals(Optional.of(Goods.of(GoodsType.GRAIN, 100)), w.held(0));
        assertEquals(Optional.of(Goods.of(GoodsType.GRAIN, 100)), w.held(1));
        assertEquals(grain - 200, w.stock(GoodsType.GRAIN));
        assertEquals(50, w.stock(GoodsType.WEAPONS));
        assertEquals(Optional.empty(), w.pause());
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
    }

    @Test
    void scarceFirstGoodsLeavesTheRestOfTheCapacityToLaterGoods() {
        final World w = new World();
        w.freeHold(1);
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE, GoodsType.WEAPONS)));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        // Fifty horses, then fifty weapons in the next free hold; both holds
        // still have room and Delft has no more, so the carrier waits.
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(Optional.of(Goods.of(GoodsType.WEAPONS, 50)), w.held(1));
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());
    }

    @Test
    void fullWarehouseKeepsTheCarrierAndLoadingWaitsForTheUnloading() {
        final World w = new World();
        w.freeHold(1);
        w.fillWarehouse(GoodsType.ORE);
        final int full = w.stock(GoodsType.ORE);
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        // The ore cannot go anywhere, and the free hold is not filled while
        // the designated unloading is unfinished.
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 60)), w.held(0));
        assertTrue(w.held(1).isEmpty());
        assertEquals(50, w.stock(GoodsType.HORSE));
        assertEquals(Optional.of(TradeRoutePause.WAITING_UNLOAD), w.pause());

        // Room appears; a later turn unloads, then loads the horses, and the
        // carrier still waits for the rest of its free capacity.
        w.delft.getWarehouse().removeGoods(Goods.of(GoodsType.ORE, 100));
        w.laterTurn();
        assertEquals(full - 40, w.stock(GoodsType.ORE));
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertTrue(w.held(1).isEmpty());
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());
    }

    @Test
    void unloadOnlyAndNoInstructionStopsDoNotWaitForFullHolds() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(),
                World.delftStop(List.of(GoodsType.ORE), List.of()));
        w.assign(route, DELFT_STOP);

        w.ownerUpdate();

        assertTrue(w.held(0).isEmpty());
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(Optional.empty(), w.pause());
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());

        final World blank = new World();
        final TradeRoute plain = blank.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), TradeRouteStop.ofColony(DELFT));
        blank.assign(plain, DELFT_STOP);
        blank.ownerUpdate();
        assertEquals(Optional.empty(), blank.pause());
        assertEquals(EUROPE_STOP, blank.assignment().getStopIndex());
    }

    @Test
    void switchingToTouchAndGoReleasesAWaitingCarrierWithoutReplay() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());

        w.model.updateTradeRoute(w.dutch, w.stored(), w.stored().getStops(),
                TradeRouteMode.TOUCH_AND_GO);
        // The edit itself moves and transfers nothing.
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());
        assertEquals(DELFT_AT, w.ship.getLocation());

        w.laterTurn();

        // Nothing new to load; the ore unloaded earlier is not unloaded twice.
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(Optional.empty(), w.pause());
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
    }

    @Test
    void removingAWaitingCarrierKeepsItsPlaceAndCargo() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();

        w.model.cancelTradeRouteAssignment(w.ship);

        assertTrue(w.model.getTradeRouteAssignment(w.ship).isEmpty());
        assertEquals(DELFT_AT, w.ship.getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(Optional.of(Goods.of(GoodsType.OLIVE_OIL, 100)),
                w.held(1));
    }

    @Test
    void editOfTheWaitingStopReachesTheCarrierOnlyAtItsNextStop() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();

        // The player now wants weapons at Delft instead of horses.
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(TradeRouteStop.europe(),
                        World.delftStop(List.of(GoodsType.ORE),
                                List.of(GoodsType.WEAPONS))),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        assertEquals(List.of(GoodsType.HORSE),
                w.assignment().getStops().get(DELFT_STOP).getLoadGoods());

        w.delft.getWarehouse().addGoods(Goods.of(GoodsType.HORSE, 50));
        w.laterTurn();

        // The current visit keeps its horse instruction and completes with
        // it; the weapons stay for the next visit.
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 100)), w.held(0));
        assertEquals(50, w.stock(GoodsType.WEAPONS));
        assertEquals(Optional.empty(), w.pause());
        assertEquals(List.of(GoodsType.WEAPONS),
                w.assignment().getStops().get(DELFT_STOP).getLoadGoods());
    }

    @Test
    void homePortSellsBeforeBuyingAndWaitsForFundsAcrossTurns() {
        final World w = new World();
        w.model.getEurope().getPort().placeShipToPort(w.ship);
        // Five holds will be free once the olive oil is sold.
        for (final int hold : List.of(0, 3, 4, 5)) {
            w.freeHold(hold);
        }
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                World.europeStop(List.of(GoodsType.OLIVE_OIL),
                        List.of(GoodsType.WEAPONS)),
                TradeRouteStop.ofColony(DELFT));
        // Only the olive oil sale pays for the weapons.
        w.dutch.setGold(0);
        w.assign(route, EUROPE_STOP);

        w.ownerUpdate();

        // Selling came first: no olive oil is left on board.
        assertTrue(w.held(1).isEmpty()
                || w.held(1).get().getType() == GoodsType.WEAPONS);
        final int bought = weapons(w);
        assertTrue(bought > 0 && bought < 500, "bought " + bought);
        assertTrue(w.dutch.getGold() < w.dutch
                .quoteEuropeTrade(Goods.of(GoodsType.WEAPONS, 1), true)
                .getTotal());
        assertEquals(Optional.of(TradeRoutePause.WAITING_FUNDS), w.pause());

        // More money in the same turn buys nothing and the sale is not
        // repeated.
        w.dutch.setGold(100000);
        w.ownerUpdate();
        assertEquals(bought, weapons(w));
        assertEquals(100000, w.dutch.getGold());

        // Save and Continue keep the mode, the waiting visit and its turn.
        final World restored = w.saveAndContinue();
        assertEquals(TradeRouteMode.WAIT_FOR_COMPLETION,
                restored.stored().getMode());
        assertEquals(Optional.of(TradeRoutePause.WAITING_FUNDS),
                restored.pause());
        assertEquals(
                w.ship.getProcessedTradeRouteVisit().orElseThrow().getTurn(),
                restored.ship.getProcessedTradeRouteVisit().orElseThrow()
                        .getTurn());
        restored.ownerUpdate();
        assertEquals(bought, weapons(restored));
        assertEquals(100000, restored.dutch.getGold());

        // A later turn buys the rest of the free capacity and departs.
        restored.laterTurn();
        assertEquals(500, weapons(restored));
        assertTrue(restored.dutch.getGold() < 100000);
        assertEquals(Optional.empty(), restored.pause());
        assertEquals(DELFT_STOP, restored.assignment().getStopIndex());
    }

    private static int weapons(final World w) {
        return w.ship.getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType() == GoodsType.WEAPONS)
                .mapToInt(Goods::getAmount).sum();
    }

    @Test
    void routeUpdateIsValidatedBeforeAnyChange() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.TOUCH_AND_GO,
                TradeRouteStop.europe(), TradeRouteStop.ofColony(DELFT));
        assertEquals(Optional.of(TradeRouteProblem.STOP_COUNT),
                w.model.getTradeRouteUpdateProblem(w.dutch, route, List.of(),
                        TradeRouteMode.TOUCH_AND_GO));
        assertEquals(Optional.of(TradeRouteProblem.SAME_STOP),
                w.model.getTradeRouteUpdateProblem(w.dutch, route,
                        List.of(TradeRouteStop.europe(),
                                TradeRouteStop.europe()),
                        TradeRouteMode.TOUCH_AND_GO));
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE),
                w.model.getTradeRouteUpdateProblem(w.dutch, route,
                        List.of(TradeRouteStop.europe(),
                                TradeRouteStop.ofColony(99)),
                        TradeRouteMode.TOUCH_AND_GO));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.updateTradeRoute(w.dutch, route, List.of(),
                        TradeRouteMode.WAIT_FOR_COMPLETION));
        assertSame(route, w.stored());
        assertEquals(TradeRouteMode.TOUCH_AND_GO, w.stored().getMode());

        // Reordering is a legal edit; the old reference is stale afterwards.
        final TradeRoute reordered = w.model.updateTradeRoute(w.dutch, route,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.europe()),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        assertEquals(route.getId(), reordered.getId());
        assertEquals(List.of(TradeRouteStop.ofColony(DELFT),
                TradeRouteStop.europe()), reordered.getStops());
        assertEquals(Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE),
                w.model.getTradeRouteUpdateProblem(w.dutch, route,
                        route.getStops(), TradeRouteMode.TOUCH_AND_GO));
        // The editor offers only destinations not chosen yet.
        assertEquals(List.of(), w.model.getTradeRouteAddableStops(w.dutch,
                reordered, reordered.getStops()));
    }

    @Test
    void stopCountEditsRemapTheCarrierAtItsBoundary() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.TOUCH_AND_GO,
                TradeRouteStop.europe(),
                World.delftStop(List.of(GoodsType.ORE), List.of()));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();
        // The ship has left Delft for Home port, its next stop.
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());

        // Home port is removed while the ship is on its way: it keeps the leg
        // it accepted, and the single remaining stop is Delft.
        final TradeRoute single = w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(TradeRouteStop.ofColony(DELFT)),
                TradeRouteMode.TOUCH_AND_GO);
        assertEquals(1, single.getStops().size());
        assertEquals(2, w.assignment().getStops().size());
        assertTrue(w.model.getTradeRouteHeadingStop(w.ship).orElseThrow()
                .isEurope()
                || w.ship.getProcessedTradeRouteVisit().isPresent());
        // Saving and restoring keeps the longer accepted copy.
        final World restored = w.saveAndContinue();
        assertEquals(2, restored.assignment().getStops().size());
    }

    @Test
    void shortenedWaitingRouteCanContinueAfterItsVisitMovesToASmallerPlan() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(TradeRouteStop.ofColony(DELFT)),
                TradeRouteMode.TOUCH_AND_GO);
        w.laterTurn();
        final World restored = w.saveAndContinue();
        assertEquals(DELFT_AT, restored.ship.getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)),
                restored.held(0));
        assertEquals(60, restored.stock(GoodsType.ORE));
    }

    @Test
    void cancelledWaitingCarrierCanContinueAfterRouteWasShortened() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(TradeRouteStop.ofColony(DELFT)),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        w.model.cancelTradeRouteAssignment(w.ship);
        final World restored = w.saveAndContinue();
        assertTrue(restored.model.getTradeRouteAssignment(restored.ship)
                .isEmpty());
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)),
                restored.held(0));
        assertEquals(60, restored.stock(GoodsType.ORE));
    }

    @TempDir
    Path directory;

    /** A waiting galleon at Delft: ore unloaded, fifty horses loaded. */
    private static World waitingAtDelft() {
        final World w = new World();
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)));
        w.assign(route, DELFT_STOP);
        w.ownerUpdate();
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD), w.pause());
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        return w;
    }

    @Test
    void reorderingTheStopsOfAWaitingCarrierKeepsAValidReceipt() {
        final World w = waitingAtDelft();
        // Delft moves from the second to the first place.
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE)), TradeRouteStop.europe()),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        final World restored = w.saveAndContinue();
        assertEquals(Optional.of(TradeRoutePause.WAITING_LOAD),
                restored.pause());
        assertEquals(Optional.of(DELFT_AT), restored.ship
                .getProcessedTradeRouteVisit().orElseThrow().getLocation());
        // The same turn repeats nothing after Continue.
        restored.delft.getWarehouse().addGoods(Goods.of(GoodsType.HORSE, 50));
        restored.ownerUpdate();
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)),
                restored.held(0));
        assertEquals(60, restored.stock(GoodsType.ORE));
    }

    @Test
    void reEditAfterCancellingAShortenedRouteRepeatsNothingAtTheSamePlace() {
        final World w = waitingAtDelft();
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.HORSE))),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        w.model.cancelTradeRouteAssignment(w.ship);
        // The player puts Home port back and sends the galleon again from
        // where it stands; more horses arrived and the ore is gone from the
        // warehouse, so a replay would be visible.
        w.model.updateTradeRoute(w.dutch, w.stored(),
                List.of(TradeRouteStop.europe(),
                        World.delftStop(List.of(GoodsType.ORE),
                                List.of(GoodsType.HORSE))),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        w.delft.getWarehouse().addGoods(Goods.of(GoodsType.HORSE, 50));
        w.assign(w.stored(), DELFT_STOP);
        w.ownerUpdate();

        // The receipt of the visit stayed: Delft is passed, not processed.
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertEquals(50, w.stock(GoodsType.HORSE));
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
        final World restored = w.saveAndContinue();
        assertEquals(EUROPE_STOP, restored.assignment().getStopIndex());
    }

    @Test
    void reassigningAWaitingCarrierToAnotherRouteRepeatsNothing() {
        final World w = waitingAtDelft();
        final TradeRoute other = w.model.createTradeRoute(w.dutch, "Other",
                TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
        w.model.updateTradeRoute(w.dutch, other,
                List.of(TradeRouteStop.europe(),
                        World.delftStop(List.of(),
                                List.of(GoodsType.HORSE, GoodsType.WEAPONS))),
                TradeRouteMode.WAIT_FOR_COMPLETION);
        final TradeRoute stored = w.model.getTradeRoutes(w.dutch).get(1);
        w.model.changeTradeRoute(w.ship, stored, w.assignment());
        assertEquals(stored.getId(), w.assignment().getRouteId());

        w.ownerUpdate();
        w.laterTurn();

        // No second load in this visit: the weapons stay in Delft and the
        // carrier is not held by the other route's instructions.
        assertEquals(50, w.stock(GoodsType.WEAPONS));
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertTrue(w.pause().isEmpty() || !w.pause().get().isWaiting());
    }

    @Test
    void savedVisitTurnIsRequiredNonNegativeAndNotInTheFuture() {
        final World w = waitingAtDelft();
        final int played = w.model.getCalendar().getNumberOfPlayedTurns();
        w.data.setModel(w.model.save());
        final org.akropia.model.campaign.po.GameModelPo saved = w.data;
        for (final Integer bad : new Integer[] { null, -1, played + 1 }) {
            final org.akropia.model.campaign.po.GameModelPo copy = new GameModelPoDao()
                    .loadFromFile(write(saved, "bad.json").toFile());
            ship(copy).setTradeRouteVisitTurn(bad);
            assertThrows(IllegalArgumentException.class, () -> new World(copy),
                    "turn " + bad);
        }
        // The current turn is accepted.
        final org.akropia.model.campaign.po.GameModelPo good = new GameModelPoDao()
                .loadFromFile(write(saved, "good.json").toFile());
        assertEquals(played, ship(good).getTradeRouteVisitTurn());
        new World(good);
    }

    @Test
    void restoringAWaitingCarrierChangesNoGameState() throws Exception {
        final World w = waitingAtDelft();
        w.data.setModel(w.model.save());
        final Path first = write(w.data, "first.json");
        final World restored = new World(
                new GameModelPoDao().loadFromFile(first.toFile()));
        restored.data.setModel(restored.model.save());
        final Path second = write(restored.data, "second.json");
        assertEquals(Files.readString(first), Files.readString(second));
    }

    private Path write(final org.akropia.model.campaign.po.GameModelPo data,
            final String name) {
        final Path file = directory.resolve(name);
        new GameModelPoDao().saveToFile(file.toString(), data);
        return file;
    }

    private static UnitPo ship(
            final org.akropia.model.campaign.po.GameModelPo data) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
    }

    /**
     * The F238 fixture used by the home-port UI scenario: the galleon is docked
     * at the home port and the treasury is empty.
     */
    @Test
    void homePortFixtureWaitsForFundsUntilAManualSalePaysForTheRest() {
        final World w = new World(new GameModelPoDao().loadFromFile(new File(
                "../akropia-test/src/test/scenarios/F238-trade-route-home-port.akropia")));
        assertTrue(w.ship.isAtEuropePort());
        assertEquals(0, w.dutch.getGold());
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                World.europeStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.WEAPONS)),
                TradeRouteStop.ofColony(DELFT));
        w.assign(route, EUROPE_STOP);

        w.ownerUpdate();

        // The ore is sold first; its price buys only part of the hold.
        final int bought = weapons(w);
        assertTrue(bought > 0 && bought < 100, "bought " + bought);
        assertEquals(Optional.of(Goods.of(GoodsType.WEAPONS, bought)),
                w.held(0));
        assertEquals(Optional.of(TradeRoutePause.WAITING_FUNDS), w.pause());
        final int gold = w.dutch.getGold();

        // The player sells the olive oil by hand; the next turn completes the
        // purchase and the galleon leaves for Delft.
        final CargoSlot oil = w.ship.getCargo().getSlotByIndex(1);
        final Goods goods = oil.getGoods().orElseThrow();
        w.model.sellGoods(oil, goods, w.dutch.quoteEuropeTrade(goods, false));
        assertTrue(w.dutch.getGold() > gold);
        w.laterTurn();
        assertEquals(Optional.of(Goods.of(GoodsType.WEAPONS, 100)), w.held(0));
        assertTrue(w.held(1).isEmpty()
                || w.held(1).get().getType() == GoodsType.WEAPONS);
        assertEquals(Optional.empty(), w.pause());
        assertEquals(DELFT_STOP, w.assignment().getStopIndex());
    }

    /**
     * The F239 fixture used by the full-warehouse UI scenario, played with real
     * turn changes so colony production is included: Delft's ore store is full,
     * the galleon has one free hold and an empty merchantman 6 waits in the
     * port.
     */
    @Test
    void fullWarehouseFixtureWaitsUntilAPlayerMovesOreAway() {
        final World w = new World(new GameModelPoDao().loadFromFile(new File(
                "../akropia-test/src/test/scenarios/F239-trade-route-full-warehouse.akropia")));
        assertEquals(0, w.delft.getWarehouse().getFreeStorage(GoodsType.ORE));
        final TradeRoute route = w.route(TradeRouteMode.WAIT_FOR_COMPLETION,
                TradeRouteStop.europe(), World.delftStop(List.of(GoodsType.ORE),
                        List.of(GoodsType.TOOLS, GoodsType.WEAPONS)));
        w.assign(route, DELFT_STOP);

        playRound(w);

        // The ore cannot be unloaded, so nothing is loaded either.
        assertEquals(DELFT_AT, w.ship.getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 60)), w.held(0));
        assertTrue(w.held(3).isEmpty());
        assertEquals(100, w.stock(GoodsType.ORE));
        assertEquals(100, w.stock(GoodsType.TOOLS));
        assertEquals(100, w.stock(GoodsType.WEAPONS));
        assertEquals(Optional.of(TradeRoutePause.WAITING_UNLOAD), w.pause());

        // The player loads all the ore into the spare merchantman.
        final UnitWithCargo spare = (UnitWithCargo) w.model.getAllUnits()
                .stream().filter(unit -> unit.getId() == 6).findFirst()
                .orElseThrow();
        spare.getCargo().getSlotByIndex(0).storeFromColonyWarehouse(
                Goods.of(GoodsType.ORE, 100), w.delft);
        assertEquals(0, w.stock(GoodsType.ORE));

        playRound(w);

        // The ore is unloaded, then tools and weapons fill both free holds
        // in the goods order, and the galleon leaves for the home port.
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(Optional.of(Goods.of(GoodsType.TOOLS, 100)), w.held(0));
        assertEquals(Optional.of(Goods.of(GoodsType.WEAPONS, 100)), w.held(3));
        assertEquals(0, w.stock(GoodsType.TOOLS));
        assertEquals(0, w.stock(GoodsType.WEAPONS));
        assertEquals(Optional.empty(), w.pause());
        assertEquals(EUROPE_STOP, w.assignment().getStopIndex());
        assertNotEquals(DELFT_AT,
                w.ship.isAtPlaceLocation() ? w.ship.getLocation() : null);
    }

    /** Every player ends a turn; Dutch is current again. */
    private static void playRound(final World w) {
        for (int i = 0; i < w.model.getPlayers().size(); i++) {
            w.model.endTurn();
            if (w.model.isAwaitingTaxDecision()) {
                w.dutch.acceptTaxDemand(w.dutch.getTaxDemand().orElseThrow());
            }
        }
        assertSame(w.dutch, w.model.getCurrentPlayer());
    }
}
