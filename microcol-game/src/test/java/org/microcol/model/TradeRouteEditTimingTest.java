package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Editing, canceling and deleting trade routes on the shared F110 scenario.
 * Dutch owns New Amsterdam (id 0, ORE 90, MUSKET 50) at (26,16), Delft (id 1,
 * ORE 0, MUSKET 50) at (28,16) and Haarlem (id 2, ORE 40, MUSKET 20) at
 * (24,16), each two squares from the next over grass. Free wagons 4 and 6 stand
 * in New Amsterdam, and a wagon moves one square a turn. The saved "Ore cart"
 * route unloads ORE and loads MUSKET in Delft and does the opposite in New
 * Amsterdam. Wagon 8 waits with a lost stop on "Lost cart", whose second stop
 * names colony 3, which was never founded. Galleon 7, with six holds [RUM 100,
 * COTTON 60, empty x4], is on the Atlantic with two turns left, assigned to
 * "Atlantic run". The Dutch player has 10000 gold and no tax is active.
 */
class TradeRouteEditTimingTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F110-trade-route-edit.microcol");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int HAARLEM = 2;
    private static final int LOST = 3;
    private static final int FIRST_WAGON = 4;
    private static final int SECOND_WAGON = 6;
    private static final int GALLEON = 7;
    private static final int LOST_WAGON = 8;
    private static final int ORE_CART_ID = 0;
    private static final int LOST_CART_ID = 1;
    private static final int ATLANTIC_RUN_ID = 2;
    private static final Location NEW_AMSTERDAM_AT = Location.of(26, 16);
    private static final Location DELFT_AT = Location.of(28, 16);
    private static final Location HAARLEM_AT = Location.of(24, 16);
    private static final Location LANE_AT = Location.of(49, 15);
    private static final String ORE_CART = "Ore cart";
    private static final String LOST_CART = "Lost cart";
    private static final String ATLANTIC_RUN = "Atlantic run";
    private static final List<GoodsType> TRACKED = List.of(GoodsType.ORE,
            GoodsType.MUSKET, GoodsType.HORSE, GoodsType.RUM, GoodsType.COTTON,
            GoodsType.TOOLS);
    /** Longest sequence of rounds any scenario below may need. */
    private static final int MAX_ROUNDS = 40;

    /** Loaded game and the handles every scenario needs. */
    private static final class World {

        private final GameModelPo data;
        private final Model model;
        private final Player dutch;
        private final Player king;

        World() {
            this(new GameModelPoDao().loadFromFile(SCENARIO));
        }

        World(final GameModelPo data) {
            this.data = data;
            model = Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            dutch = model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner();
            king = model.getPlayers().stream().filter(player -> player != dutch)
                    .findFirst().orElseThrow();
        }

        UnitWithCargo carrier(final int id) {
            return (UnitWithCargo) model.getAllUnits().stream()
                    .filter(unit -> unit.getId() == id).findFirst()
                    .orElseThrow();
        }

        TradeRoute route(final String name) {
            return model.getTradeRoutes(dutch).stream()
                    .filter(stored -> stored.getName().equals(name)).findFirst()
                    .orElseThrow();
        }

        int stock(final int colonyId, final GoodsType type) {
            return model.getColonyById(colonyId).orElseThrow().getWarehouse()
                    .getGoods(type).getAmount();
        }

        Optional<Goods> held(final int carrierId, final int slot) {
            return carrier(carrierId).getCargo().getSlotByIndex(slot)
                    .getGoods();
        }

        TradeRouteAssignment assignment(final int carrierId) {
            return model.getTradeRouteAssignment(carrier(carrierId))
                    .orElseThrow();
        }

        /** The owner's preparation boundary, without a real turn change. */
        void ownerUpdate() {
            model.advanceTradeRouteAssignments(dutch);
        }

        /**
         * Dutch and then the King end their turns; Dutch is current again. A
         * pending tax decision is answered through the player's own operation,
         * as the dialog does.
         */
        void playRound() {
            for (int i = 0; i < model.getPlayers().size(); i++) {
                model.endTurn();
                if (model.isAwaitingTaxDecision()) {
                    dutch.acceptTaxDemand(dutch.getTaxDemand().orElseThrow());
                }
            }
            assertSame(dutch, model.getCurrentPlayer());
            assertTrue(model.isGameRunning());
        }

        /** Restores a fresh model from this one's saved state. */
        World saveAndContinue() {
            data.setModel(model.save());
            return new World(data);
        }

        /** Total of one goods type in the warehouses and the three wagons. */
        int total(final GoodsType type) {
            int sum = 0;
            for (final Colony colony : model.getColonies(dutch)) {
                sum += colony.getWarehouse().getGoods(type).getAmount();
            }
            for (final int id : List.of(FIRST_WAGON, SECOND_WAGON,
                    LOST_WAGON)) {
                for (final CargoSlot slot : carrier(id).getCargo().getSlots()) {
                    sum += slot.getGoods()
                            .filter(goods -> goods.getType().equals(type))
                            .map(Goods::getAmount).orElse(0);
                }
            }
            return sum;
        }

        /** Where every unit stands and what the owner holds, nothing else. */
        String placementAndGoods() {
            final StringBuilder out = new StringBuilder();
            out.append("gold=").append(dutch.getGold()).append('\n');
            for (final Unit unit : sortedUnits()) {
                out.append(unit.getId()).append('@')
                        .append(unit.isAtPlaceLocation() ? unit.getLocation()
                                : unit.isAtHighSea() ? "sea" : "away")
                        .append(" move=").append(unit.getMovementThirds());
                if (unit.isAtHighSea()) {
                    out.append(" left=").append(((PlaceHighSea) unit.getPlace())
                            .getRemainigTurns());
                }
                if (unit instanceof UnitWithCargo) {
                    for (final CargoSlot slot : ((UnitWithCargo) unit)
                            .getCargo().getSlots()) {
                        out.append(' ')
                                .append(slot.getGoods()
                                        .map(goods -> goods.getType() + "x"
                                                + goods.getAmount())
                                        .orElse("-"));
                    }
                }
                out.append('\n');
            }
            for (final Colony colony : model.getColonies(dutch)) {
                out.append(colony.getName());
                for (final GoodsType type : TRACKED) {
                    out.append(' ').append(type).append('=').append(
                            colony.getWarehouse().getGoods(type).getAmount());
                }
                out.append('\n');
            }
            return out.toString();
        }

        /** Saved routes, assignments with accepted stops, and receipts. */
        String orders() {
            final StringBuilder out = new StringBuilder();
            for (final TradeRoute stored : model.getTradeRoutes(dutch)) {
                out.append(stored.getId()).append(':').append(stored.getName())
                        .append(':').append(stored.getKind());
                appendStops(out, stored.getStops());
                out.append('\n');
            }
            for (final Unit unit : sortedUnits()) {
                out.append(unit.getId());
                model.getTradeRouteAssignment(unit).ifPresent(assignment -> {
                    out.append(" route=").append(assignment.getRouteId())
                            .append(" stop=").append(assignment.getStopIndex())
                            .append(" pause=").append(assignment.getPause())
                            .append(" accepted=");
                    appendStops(out, assignment.getStops());
                });
                unit.getProcessedTradeRouteVisit()
                        .ifPresent(visit -> out.append(" visit=")
                                .append(visit.getRouteId()).append('/')
                                .append(visit.getStopIndex()).append('@')
                                .append(visit.getLocation()));
                out.append('\n');
            }
            return out.toString();
        }

        private List<Unit> sortedUnits() {
            return dutch.getAllUnits().stream()
                    .sorted(Comparator.comparingInt(Unit::getId))
                    .collect(java.util.stream.Collectors.toList());
        }

        private static void appendStops(final StringBuilder out,
                final List<TradeRouteStop> stops) {
            for (final TradeRouteStop stop : stops) {
                out.append(" [").append(stop).append(" unload=")
                        .append(stop.getUnloadGoods()).append(" load=")
                        .append(stop.getLoadGoods()).append(']');
            }
        }
    }

    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    /**
     * The first wagon sails a square ahead of the second. After three rounds
     * the first stands a square from Delft on its way back to New Amsterdam,
     * having taken MUSKET 50 in Delft, and the second stands in Delft having
     * found no MUSKET left, so its visit is processed and it has not left.
     */
    private static TradeRoute twoCarriersAtDifferentLegs(final World w) {
        final TradeRoute route = w.route(ORE_CART);
        w.model.assignTradeRoute(w.carrier(FIRST_WAGON), route, 0);
        w.playRound();
        w.model.assignTradeRoute(w.carrier(SECOND_WAGON), route, 0);
        w.playRound();
        w.playRound();
        return route;
    }

    private static void verifyTheTwoLegs(final World w) {
        final UnitWithCargo first = w.carrier(FIRST_WAGON);
        assertNotEquals(DELFT_AT, first.getLocation());
        assertNotEquals(NEW_AMSTERDAM_AT, first.getLocation());
        assertTrue(first.getLocation().isNeighbor(DELFT_AT));
        assertEquals(1, w.assignment(FIRST_WAGON).getStopIndex());
        assertEquals(Optional.empty(), first.getProcessedTradeRouteVisit());
        assertEquals(Optional.of(Goods.of(GoodsType.MUSKET, 50)),
                w.held(FIRST_WAGON, 0));
        final UnitWithCargo second = w.carrier(SECOND_WAGON);
        assertEquals(DELFT_AT, second.getLocation());
        assertEquals(1, w.assignment(SECOND_WAGON).getStopIndex());
        assertEquals(ORE_CART_ID, second.getProcessedTradeRouteVisit()
                .orElseThrow().getRouteId());
        assertEquals(Optional.empty(), w.held(SECOND_WAGON, 0));
        assertEquals(0, w.stock(DELFT, GoodsType.ORE));
        assertEquals(0, w.stock(DELFT, GoodsType.MUSKET));
        assertEquals(90, w.stock(NEW_AMSTERDAM, GoodsType.ORE));
        assertEquals(50, w.stock(NEW_AMSTERDAM, GoodsType.MUSKET));
    }

    @Test
    void identityReplacementKeepsIdNameOrderAndCargoOfTheStop() {
        final World w = new World();
        final TradeRoute oreCart = w.route(ORE_CART);
        final TradeRouteStop haarlem = TradeRouteStop.ofColony(HAARLEM);

        assertEquals(Optional.empty(), w.model
                .getTradeRouteStopEditProblem(w.dutch, oreCart, 1, haarlem));
        final TradeRoute edited = w.model.editTradeRouteStop(w.dutch, oreCart,
                1, haarlem);

        assertEquals(ORE_CART_ID, edited.getId());
        assertEquals(ORE_CART, edited.getName());
        assertEquals(TradeRouteKind.LAND, edited.getKind());
        assertEquals(NEW_AMSTERDAM, edited.getStartColonyId());
        assertEquals(2, edited.getStops().size());
        assertEquals(DELFT, edited.getStops().get(0).getColonyId());
        assertEquals(List.of(GoodsType.ORE),
                edited.getStops().get(0).getUnloadGoods());
        assertEquals(HAARLEM, edited.getStops().get(1).getColonyId());
        // The replaced stop keeps what it unloaded and loaded.
        assertEquals(List.of(GoodsType.MUSKET),
                edited.getStops().get(1).getUnloadGoods());
        assertEquals(List.of(GoodsType.ORE),
                edited.getStops().get(1).getLoadGoods());
        assertSame(edited, w.route(ORE_CART));
        // The reference taken before the edit is stale now.
        assertEquals(Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE), w.model
                .getTradeRouteStopEditProblem(w.dutch, oreCart, 1, haarlem));
    }

    @Test
    void twoCarriersOnDifferentLegsAdoptTheEditIndependentlyAtTheirBoundary() {
        final World w = new World();
        final TradeRoute route = twoCarriersAtDifferentLegs(w);
        verifyTheTwoLegs(w);
        final String placement = w.placementAndGoods();

        w.model.editTradeRouteStop(w.dutch, route, 1,
                TradeRouteStop.ofColony(HAARLEM));

        // The edit moved, paid and transferred nothing.
        assertEquals(placement, w.placementAndGoods());
        // The first wagon is on its leg and still heads for New Amsterdam,
        // while the second has not left Delft and already counts Haarlem.
        assertEquals(NEW_AMSTERDAM,
                w.model.getTradeRouteHeadingStop(w.carrier(FIRST_WAGON))
                        .orElseThrow().getColonyId());
        assertEquals(HAARLEM,
                w.model.getTradeRouteHeadingStop(w.carrier(SECOND_WAGON))
                        .orElseThrow().getColonyId());

        w.playRound();

        // The first wagon reached the stop it had accepted, once: MUSKET off,
        // ORE on, then it takes the latest plan for the next leg.
        assertEquals(NEW_AMSTERDAM_AT, w.carrier(FIRST_WAGON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 90)),
                w.held(FIRST_WAGON, 0));
        assertEquals(Optional.empty(), w.held(FIRST_WAGON, 1));
        assertEquals(100, w.stock(NEW_AMSTERDAM, GoodsType.MUSKET));
        assertEquals(0, w.stock(NEW_AMSTERDAM, GoodsType.ORE));
        assertEquals(0, w.assignment(FIRST_WAGON).getStopIndex());
        assertEquals(HAARLEM,
                w.assignment(FIRST_WAGON).getStops().get(1).getColonyId());
        // The second wagon left Delft for Haarlem; Delft was not repeated.
        assertTrue(w.carrier(SECOND_WAGON).getLocation().isNeighbor(DELFT_AT));
        assertTrue(
                w.carrier(SECOND_WAGON).getLocation().getX() < DELFT_AT.getX());
        assertEquals(1, w.assignment(SECOND_WAGON).getStopIndex());
        assertEquals(HAARLEM,
                w.assignment(SECOND_WAGON).getStops().get(1).getColonyId());
        assertEquals(0, w.stock(DELFT, GoodsType.ORE));
        assertEquals(0, w.stock(DELFT, GoodsType.MUSKET));

        // The second wagon reaches Haarlem and loads its ORE 40 once.
        int rounds = 0;
        while (!HAARLEM_AT.equals(w.carrier(SECOND_WAGON).getLocation())
                && rounds++ < MAX_ROUNDS) {
            w.playRound();
        }
        assertTrue(rounds < MAX_ROUNDS, "the second wagon never arrived");
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 40)),
                w.held(SECOND_WAGON, 0));
        assertEquals(0, w.stock(HAARLEM, GoodsType.ORE));
        assertEquals(20, w.stock(HAARLEM, GoodsType.MUSKET));
        assertEquals(0, w.assignment(SECOND_WAGON).getStopIndex());
        // Nothing was created or lost on the way.
        assertEquals(130, w.total(GoodsType.ORE));
        assertEquals(120, w.total(GoodsType.MUSKET));
        assertEquals(10000, w.dutch.getGold());
    }

    @Test
    void cargoEditReachesACarrierOnItsWayWhenItArrives() {
        final World w = new World();
        final TradeRoute route = w.route(ORE_CART);
        w.model.assignTradeRoute(w.carrier(FIRST_WAGON), route, 0);
        w.playRound();
        final String placement = w.placementAndGoods();

        // Delft is now to load TOOLS instead of MUSKET. TOOLS has no producer,
        // unlike HORSE, which breeds, so the amounts stay exact.
        w.model.configureTradeRouteStopCargo(w.dutch, route, 0,
                List.of(GoodsType.ORE), List.of(GoodsType.TOOLS));

        assertEquals(placement, w.placementAndGoods());
        w.playRound();

        assertEquals(DELFT_AT, w.carrier(FIRST_WAGON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.TOOLS, 50)),
                w.held(FIRST_WAGON, 0));
        assertEquals(0, w.stock(DELFT, GoodsType.TOOLS));
        assertEquals(50, w.stock(DELFT, GoodsType.MUSKET));
        assertEquals(10000, w.dutch.getGold());
    }

    @Test
    void lostStopRepairResumesWithinOneUpdateAtTheReplacement() {
        final World w = new World();
        final TradeRoute lostCart = w.route(LOST_CART);
        final Location start = w.carrier(LOST_WAGON).getLocation();
        final int movement = w.carrier(LOST_WAGON).getMovementThirds();
        assertEquals(NEW_AMSTERDAM_AT, start);
        assertEquals(Optional.of(TradeRoutePause.STOP_LOST),
                w.assignment(LOST_WAGON).getPause());

        // Updates while the stop is lost are finite and change nothing.
        w.ownerUpdate();
        w.ownerUpdate();
        assertEquals(Optional.of(TradeRoutePause.STOP_LOST),
                w.assignment(LOST_WAGON).getPause());
        assertEquals(1, w.assignment(LOST_WAGON).getStopIndex());
        assertEquals(start, w.carrier(LOST_WAGON).getLocation());
        assertEquals(movement, w.carrier(LOST_WAGON).getMovementThirds());

        // Repair: the lost colony's stop becomes Haarlem.
        assertEquals(Optional.empty(), w.model.getTradeRouteStopEditProblem(
                w.dutch, lostCart, 1, TradeRouteStop.ofColony(HAARLEM)));
        w.model.editTradeRouteStop(w.dutch, lostCart, 1,
                TradeRouteStop.ofColony(HAARLEM));
        // Nothing moved by the repair itself.
        assertEquals(start, w.carrier(LOST_WAGON).getLocation());
        assertEquals(movement, w.carrier(LOST_WAGON).getMovementThirds());

        w.ownerUpdate();

        // One ordinary step toward Haarlem, never a jump or another port.
        final Location stepped = w.carrier(LOST_WAGON).getLocation();
        assertTrue(stepped.isNeighbor(start));
        assertTrue(stepped.getX() < start.getX());
        assertEquals(Optional.empty(), w.assignment(LOST_WAGON).getPause());
        assertEquals(1, w.assignment(LOST_WAGON).getStopIndex());

        int rounds = 0;
        while (!HAARLEM_AT.equals(w.carrier(LOST_WAGON).getLocation())
                && rounds++ < MAX_ROUNDS) {
            w.playRound();
        }
        assertTrue(rounds < MAX_ROUNDS, "the wagon never reached Haarlem");
        final TradeRouteVisit visit = w.carrier(LOST_WAGON)
                .getProcessedTradeRouteVisit().orElseThrow();
        assertEquals(LOST_CART_ID, visit.getRouteId());
        assertEquals(1, visit.getStopIndex());
        assertEquals(0, w.assignment(LOST_WAGON).getStopIndex());
    }

    @Test
    void editBesideALostStopIsAllowedSoSeveralLostStopsCanBeMendedInTurn() {
        final World w = new World();
        final TradeRoute lostCart = w.route(LOST_CART);

        // The second stop is lost, so its leg cannot be judged; the first
        // stop may still be replaced by an owned colony reachable from the
        // start.
        assertEquals(Optional.empty(), w.model.getTradeRouteStopEditProblem(
                w.dutch, lostCart, 0, TradeRouteStop.ofColony(HAARLEM)));
        final TradeRoute edited = w.model.editTradeRouteStop(w.dutch, lostCart,
                0, TradeRouteStop.ofColony(HAARLEM));

        assertEquals(HAARLEM, edited.getStops().get(0).getColonyId());
        assertEquals(LOST, edited.getStops().get(1).getColonyId());
        // The lost stop itself still cannot be replaced by a lost colony.
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE),
                w.model.getTradeRouteStopEditProblem(w.dutch, edited, 1,
                        TradeRouteStop.ofColony(LOST)));
    }

    @Test
    void cancelingOneOfTwoCarriersLeavesTheOtherFollowingTheRoute() {
        final World w = new World();
        final TradeRoute route = twoCarriersAtDifferentLegs(w);
        final Location firstAt = w.carrier(FIRST_WAGON).getLocation();
        final String placement = w.placementAndGoods();

        assertEquals(Optional.empty(),
                w.model.getTradeRouteCancelProblem(w.carrier(FIRST_WAGON)));
        w.model.cancelTradeRouteAssignment(w.carrier(FIRST_WAGON));

        assertEquals(placement, w.placementAndGoods());
        assertEquals(Optional.empty(),
                w.model.getTradeRouteAssignment(w.carrier(FIRST_WAGON)));
        assertFalse(((AbstractUnit) w.carrier(FIRST_WAGON)).hasStandingOrder());
        assertEquals(List.of(w.carrier(SECOND_WAGON)),
                w.model.getTradeRouteAssignedCarriers(w.dutch, route));

        w.playRound();

        // The canceled wagon stays where it was; the other keeps going.
        assertEquals(firstAt, w.carrier(FIRST_WAGON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.MUSKET, 50)),
                w.held(FIRST_WAGON, 0));
        assertNotEquals(DELFT_AT, w.carrier(SECOND_WAGON).getLocation());
        assertEquals(1, w.assignment(SECOND_WAGON).getStopIndex());
    }

    @Test
    void canceledCrossingCompletesItsVoyageAndThenObeysTheOwner() {
        final World w = new World();
        final UnitWithCargo ship = w.carrier(GALLEON);
        assertTrue(ship.isAtHighSea());
        assertEquals(2, ((PlaceHighSea) ship.getPlace()).getRemainigTurns());
        final String placement = w.placementAndGoods();

        assertEquals(Optional.empty(),
                w.model.getTradeRouteCancelProblem(ship));
        w.model.cancelTradeRouteAssignment(ship);

        // Only the standing order went; the crossing is untouched.
        assertEquals(placement, w.placementAndGoods());
        assertEquals(Optional.empty(), w.model.getTradeRouteAssignment(ship));
        assertTrue(ship.isAtHighSea());
        assertEquals(2, ((PlaceHighSea) ship.getPlace()).getRemainigTurns());

        w.playRound();
        assertTrue(ship.isAtHighSea());
        assertEquals(1, ((PlaceHighSea) ship.getPlace()).getRemainigTurns());
        w.playRound();

        // It landed on the sea lane with its cargo and is a plain ship now.
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(LANE_AT, ship.getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.RUM, 100)),
                w.held(GALLEON, 0));
        assertEquals(Optional.of(Goods.of(GoodsType.COTTON, 60)),
                w.held(GALLEON, 1));
        assertEquals(10000, w.dutch.getGold());
        assertFalse(((AbstractUnit) ship).hasStandingOrder());
        final Location west = Location.of(LANE_AT.getX() - 1, LANE_AT.getY());
        w.model.moveUnit(ship, Path.of(List.of(west)));
        assertEquals(west, ship.getLocation());
    }

    @Test
    void deletingTheRouteDuringTheCrossingKeepsPlacementCountdownGoodsAndGold() {
        final World w = new World();
        final UnitWithCargo ship = w.carrier(GALLEON);
        final TradeRoute atlantic = w.route(ATLANTIC_RUN);
        final String placement = w.placementAndGoods();
        final TradeRoute lostCart = w.route(LOST_CART);

        assertEquals(Optional.empty(),
                w.model.getTradeRouteDeleteProblem(w.dutch, atlantic));
        w.model.deleteTradeRoute(w.dutch, atlantic);

        assertEquals(placement, w.placementAndGoods());
        assertEquals(Optional.empty(), w.model.getTradeRouteAssignment(ship));
        assertTrue(w.model.getTradeRoutes(w.dutch).stream()
                .noneMatch(stored -> stored.getId() == ATLANTIC_RUN_ID));
        // The other carrier's route is not touched.
        assertSame(lostCart, w.route(LOST_CART));
        assertEquals(LOST_CART_ID, w.assignment(LOST_WAGON).getRouteId());

        // Save and Continue bring back neither the route nor the order.
        final World restored = w.saveAndContinue();
        assertEquals(placement, restored.placementAndGoods());
        assertEquals(Optional.empty(), restored.model
                .getTradeRouteAssignment(restored.carrier(GALLEON)));
        assertTrue(restored.model.getTradeRoutes(restored.dutch).stream()
                .noneMatch(stored -> stored.getName().equals(ATLANTIC_RUN)));
        assertEquals(w.orders(), restored.orders());

        // The countdown goes on from where it was and ends in manual control.
        restored.playRound();
        assertEquals(1, ((PlaceHighSea) restored.carrier(GALLEON).getPlace())
                .getRemainigTurns());
        restored.playRound();
        assertEquals(LANE_AT, restored.carrier(GALLEON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.RUM, 100)),
                restored.held(GALLEON, 0));
        assertEquals(10000, restored.dutch.getGold());
    }

    @Test
    void deletingARouteWithTwoCarriersAndAReceiptReleasesThemWithoutReplay() {
        final World w = new World();
        final TradeRoute route = w.route(ORE_CART);
        w.model.assignTradeRoute(w.carrier(FIRST_WAGON), route, 0);
        w.playRound();
        w.model.assignTradeRoute(w.carrier(SECOND_WAGON), route, 0);
        w.playRound();
        // The first wagon stands in Delft with MUSKET 50 and a receipt for
        // the Delft stop; the second is on its way there.
        assertEquals(DELFT_AT, w.carrier(FIRST_WAGON).getLocation());
        assertTrue(w.carrier(FIRST_WAGON).getProcessedTradeRouteVisit()
                .isPresent());
        final String placement = w.placementAndGoods();

        w.model.deleteTradeRoute(w.dutch, route);

        assertEquals(placement, w.placementAndGoods());
        assertEquals(Optional.empty(),
                w.model.getTradeRouteAssignment(w.carrier(FIRST_WAGON)));
        assertEquals(Optional.empty(),
                w.model.getTradeRouteAssignment(w.carrier(SECOND_WAGON)));
        assertEquals(Optional.empty(),
                w.carrier(FIRST_WAGON).getProcessedTradeRouteVisit());
        assertEquals(Optional.of(TradeRouteStopProblem.ROUTE_UNAVAILABLE),
                w.model.getTradeRouteStopProblem(w.carrier(FIRST_WAGON), route,
                        0));

        final World restored = w.saveAndContinue();
        assertEquals(placement, restored.placementAndGoods());
        assertEquals(w.orders(), restored.orders());
        assertTrue(restored.model.getTradeRoutes(restored.dutch).stream()
                .noneMatch(stored -> stored.getName().equals(ORE_CART)));
        // The deleted route cannot be processed again, so nothing settled is
        // settled twice.
        assertEquals(Optional.of(TradeRouteStopProblem.ROUTE_UNAVAILABLE),
                restored.model.processTradeRouteStop(
                        restored.carrier(FIRST_WAGON), route, 0).getProblem());
        assertEquals(placement, restored.placementAndGoods());
        for (int i = 0; i < 3; i++) {
            restored.playRound();
        }
        assertEquals(DELFT_AT, restored.carrier(FIRST_WAGON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.MUSKET, 50)),
                restored.held(FIRST_WAGON, 0));
        assertEquals(0, restored.stock(DELFT, GoodsType.MUSKET));
        assertEquals(90, restored.stock(NEW_AMSTERDAM, GoodsType.ORE));
    }

    @Test
    void pendingEditAndBothLegsSurviveSaveAndContinueExactly() {
        final World w = new World();
        final TradeRoute route = twoCarriersAtDifferentLegs(w);
        w.model.editTradeRouteStop(w.dutch, route, 1,
                TradeRouteStop.ofColony(HAARLEM));
        final String placement = w.placementAndGoods();
        final String orders = w.orders();

        final World restored = w.saveAndContinue();

        // Loading moved nothing, settled nothing and forgot nothing.
        assertEquals(placement, restored.placementAndGoods());
        assertEquals(orders, restored.orders());
        assertEquals(NEW_AMSTERDAM,
                restored.model
                        .getTradeRouteHeadingStop(restored.carrier(FIRST_WAGON))
                        .orElseThrow().getColonyId());
        assertEquals(HAARLEM, restored.model
                .getTradeRouteHeadingStop(restored.carrier(SECOND_WAGON))
                .orElseThrow().getColonyId());

        restored.playRound();

        // The same outcome as without Save and Continue, transferred once.
        assertEquals(NEW_AMSTERDAM_AT,
                restored.carrier(FIRST_WAGON).getLocation());
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 90)),
                restored.held(FIRST_WAGON, 0));
        assertEquals(100, restored.stock(NEW_AMSTERDAM, GoodsType.MUSKET));
        assertEquals(0, restored.stock(NEW_AMSTERDAM, GoodsType.ORE));
        assertEquals(0, restored.stock(DELFT, GoodsType.MUSKET));
        assertEquals(HAARLEM, restored.assignment(SECOND_WAGON).getStops()
                .get(1).getColonyId());
        assertEquals(130, restored.total(GoodsType.ORE));
        assertEquals(120, restored.total(GoodsType.MUSKET));
    }

    @Test
    void staleAndWrongOwnerActionsThrowAndChangeNothing() {
        final World w = new World();
        final TradeRoute oreCart = w.route(ORE_CART);
        final TradeRoute edited = w.model.configureTradeRouteStopCargo(w.dutch,
                oreCart, 0, List.of(GoodsType.ORE), List.of());
        w.model.assignTradeRoute(w.carrier(FIRST_WAGON), edited, 0);
        final String placement = w.placementAndGoods();
        final String orders = w.orders();
        final TradeRouteStop haarlem = TradeRouteStop.ofColony(HAARLEM);
        final Unit kingsUnit = w.king.getAllUnits().get(0);

        // A reference taken before the cargo edit is stale.
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.dutch, oreCart, 1, haarlem));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.deleteTradeRoute(w.dutch, oreCart));
        // The King is not the owner or the current player.
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.king, edited, 1, haarlem));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.deleteTradeRoute(w.king, edited));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.cancelTradeRouteAssignment(kingsUnit));
        // A carrier without an order has nothing to cancel.
        assertThrows(IllegalArgumentException.class, () -> w.model
                .cancelTradeRouteAssignment(w.carrier(SECOND_WAGON)));
        // Malformed edits: out of range, lost colony, Europe by land, a stop
        // equal to its neighbour.
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.dutch, edited, 2, haarlem));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.dutch, edited, 1,
                        TradeRouteStop.ofColony(LOST)));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.dutch, edited, 1,
                        TradeRouteStop.europe()));
        assertThrows(IllegalArgumentException.class,
                () -> w.model.editTradeRouteStop(w.dutch, edited, 1,
                        TradeRouteStop.ofColony(DELFT)));

        assertEquals(placement, w.placementAndGoods());
        assertEquals(orders, w.orders());
        assertSame(edited, w.route(ORE_CART));
    }

    @Test
    void restoreRejectsBadAcceptedStopsInsteadOfRepairingThem() {
        assertRejected(po -> po.setTradeRouteAssignmentStops(
                List.of(po.getTradeRouteAssignmentStops().get(0))));
        assertRejected(
                po -> po.getTradeRouteAssignmentStops().get(1).setColonyId(99));
        assertRejected(po -> {
            po.getTradeRouteAssignmentStops().get(1).setEurope(true);
            po.getTradeRouteAssignmentStops().get(1).setColonyId(null);
        });
        assertRejected(po -> po.setTradeRouteAssignmentStops(null));
    }

    private static void assertRejected(final Consumer<UnitPo> tamper) {
        final World w = new World();
        w.model.assignTradeRoute(w.carrier(FIRST_WAGON), w.route(ORE_CART), 0);
        w.data.setModel(w.model.save());
        tamper.accept(unitPo(w.data.getModel(), FIRST_WAGON));
        assertThrows(RuntimeException.class, () -> new World(w.data));
    }
}
