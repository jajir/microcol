package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Owner-turn navigation of assigned carriers on the shared F108 scenario.
 * Dutch's galleon 3 (movement 18 thirds, six holds [ORE 60, RUM 100, passenger,
 * SUGAR 100, TOBACCO 100, COTTON 100]) stands in the port of Delft at (28,16);
 * the Dutch player's other colony New Amsterdam (id 0, ORE 90) lies inland at
 * (24,16). The sea lane to Europe is the east edge of the 50 by 30 map, and
 * Dutch's King is the only other player. Delft's warehouse holds CORN 100,
 * HORSE 50, MUSKET 50, TOOLS 50 and no ORE, RUM, SUGAR or TOBACCO.
 * <p>
 * Where exact amounts matter the owner update is driven directly, so no colony
 * production lies between the assignment and the check. Movement, voyages and
 * turn isolation use real {@link Model#endTurn()} rounds.
 * </p>
 */
class TradeRouteNavigationTest {

    private static final File SCENARIO = new File(
            "../microcol-test/src/test/scenarios/F108-trade-route-cargo.microcol");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final int WAGON_ID = 4;
    private static final Location DELFT_AT = Location.of(28, 16);
    private static final Location NEW_AMSTERDAM_AT = Location.of(24, 16);
    /** Longest sequence of rounds any scenario below may need. */
    private static final int MAX_ROUNDS = 120;

    /** Loaded game and the handles every scenario needs. */
    private static final class World {

        private final GameModelPo data;
        private final Model model;
        private final Player dutch;
        private final Colony delft;
        private final UnitWithCargo ship;
        private final Map<GoodsType, Integer> sold = new HashMap<>();
        private final List<Location> shipSteps = new ArrayList<>();
        private int departures;
        private int routes;

        World() {
            this(po -> {
            });
        }

        World(final Consumer<ModelPo> prepare) {
            this(prepared(prepare));
        }

        World(final GameModelPo data) {
            this.data = data;
            model = Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            delft = model.getColonyById(DELFT).orElseThrow();
            dutch = delft.getOwner();
            ship = (UnitWithCargo) unitOf(model, SHIP_ID);
            listen();
        }

        private static GameModelPo prepared(final Consumer<ModelPo> prepare) {
            final GameModelPo data = new GameModelPoDao()
                    .loadFromFile(SCENARIO);
            prepare.accept(data.getModel());
            return data;
        }

        /** Counts what really happened in this model, per event. */
        private void listen() {
            model.addListener(new ModelListenerAdapter() {
                @Override
                public void onGoodsWasSoldInEurope(
                        final GoodsWasSoldInEuropeEvent event) {
                    sold.merge(event.getGoods().getType(), 1, Integer::sum);
                }

                @Override
                public void onUnitMovedStepFinished(
                        final UnitMovedStepFinishedEvent event) {
                    if (event.getUnit() == ship) {
                        shipSteps.add(event.getEnd());
                    }
                }

                @Override
                public void onUnitMovedToHighSeas(
                        final UnitMovedToHighSeasEvent event) {
                    if (event.getUnit() == ship) {
                        departures++;
                    }
                }
            });
        }

        /** Sea route Europe, Delft with the given cargo at the two stops. */
        TradeRoute seaRoute(final List<GoodsType> europeUnload,
                final List<GoodsType> delftUnload,
                final List<GoodsType> delftLoad) {
            routes++;
            TradeRoute route = model.createTradeRoute(dutch, "Route " + routes,
                    TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                            TradeRouteStop.ofColony(DELFT)));
            route = model.configureTradeRouteStopCargo(dutch, route, 0,
                    europeUnload, List.of());
            return model.configureTradeRouteStopCargo(dutch, route, 1,
                    delftUnload, delftLoad);
        }

        int stock(final GoodsType type) {
            return delft.getWarehouse().getGoods(type).getAmount();
        }

        Optional<Goods> held(final int slot) {
            return ship.getCargo().getSlotByIndex(slot).getGoods();
        }

        int stopIndex() {
            return stopIndex(ship);
        }

        int stopIndex(final Unit carrier) {
            return model.getTradeRouteAssignment(carrier).orElseThrow()
                    .getStopIndex();
        }

        Optional<TradeRoutePause> pause() {
            return model.getTradeRouteAssignment(ship).orElseThrow().getPause();
        }

        int soldCount(final GoodsType type) {
            return sold.getOrDefault(type, 0);
        }

        /** The owner's preparation boundary, without a real turn change. */
        void ownerUpdate() {
            model.advanceTradeRouteAssignments(dutch);
        }

        /**
         * Dutch and then the King end their turns; Dutch is current again. On
         * Conquistador the Crown demands a tax every 14 Dutch updates, which
         * pauses Dutch's preparation until the player answers. The demand is
         * accepted through the player's own operation, which finishes the
         * preparation and continues the standing orders, as the dialog does.
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
    }

    private static Unit unitOf(final Model model, final int id) {
        return model.getAllUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static UnitPo savedUnit(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    /**
     * Turns colonist 4, who stands in New Amsterdam, into an empty wagon. A
     * wagon has no colonist work history, missionary role or equipment, and the
     * strict load rejects those fields on any other unit.
     */
    private static void colonistToWagon(final ModelPo po) {
        final UnitPo wagon = savedUnit(po, WAGON_ID);
        wagon.setType(UnitType.WAGON);
        wagon.setTools(0);
        wagon.setHoldingGuns(false);
        wagon.setMounted(false);
        wagon.setWorkUpdates(null);
        wagon.setWorkSkill(null);
        wagon.setMissionary(null);
    }

    @Test
    void firstUpdateProcessesTheFirstStopOnceThenSailsOnItsMovement() {
        final World w = new World();
        final TradeRoute route = w.seaRoute(List.of(), List.of(GoodsType.ORE),
                List.of(GoodsType.HORSE));
        assertEquals(0, w.stock(GoodsType.ORE));
        assertEquals(50, w.stock(GoodsType.HORSE));
        final int movement = w.ship.getMovementThirds();
        w.model.assignTradeRoute(w.ship, route, 1);
        // Assigning moves and transfers nothing.
        assertEquals(DELFT_AT, w.ship.getLocation());
        assertEquals(movement, w.ship.getMovementThirds());
        assertEquals(0, w.stock(GoodsType.ORE));

        w.ownerUpdate();

        // Delft was processed once, as R-0140 does: ore off, horse on.
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(0, w.stock(GoodsType.HORSE));
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        // The next ordered stop is Europe and the ship paid real movement.
        assertEquals(0, w.stopIndex());
        assertEquals(Optional.empty(), w.pause());
        assertTrue(w.ship.isAtPlaceLocation());
        assertTrue(w.ship.getLocation().getX() > DELFT_AT.getX());
        assertTrue(w.ship.getMovementThirds() < movement);
        assertFalse(w.shipSteps.isEmpty());

        // A second update of the same turn transfers nothing again.
        final Location sailed = w.ship.getLocation();
        w.ownerUpdate();
        assertEquals(60, w.stock(GoodsType.ORE));
        assertEquals(0, w.stock(GoodsType.HORSE));
        assertEquals(Optional.of(Goods.of(GoodsType.HORSE, 50)), w.held(0));
        assertTrue(w.ship.getLocation().getX() >= sailed.getX());
    }

    @Test
    void assignedShipDoesNotMoveOnTheOtherPlayersTurnButSailsOnItsOwn() {
        final World w = new World();
        w.model.assignTradeRoute(w.ship,
                w.seaRoute(List.of(), List.of(), List.of()), 0);
        final Location before = w.ship.getLocation();

        // Dutch ends its turn; the King's preparation must not move the ship.
        w.model.endTurn();
        assertNotSame(w.dutch, w.model.getCurrentPlayer());
        assertEquals(before, w.ship.getLocation());
        w.model.advanceTradeRouteAssignments(w.model.getCurrentPlayer());
        assertEquals(before, w.ship.getLocation());
        assertEquals(0, w.stopIndex());

        // Back to Dutch: only now does the owner update advance it.
        w.model.endTurn();
        assertSame(w.dutch, w.model.getCurrentPlayer());
        assertTrue(w.ship.getLocation().getX() > before.getX());
    }

    @Test
    void twoCompleteCircuitsSellEachGoodsOnceAndDepartFromTheSeaLane() {
        final World w = new World();
        // Circuit one empties three holds in Europe and fills them at Delft;
        // circuit two sells what Delft gave. Nothing is processed twice.
        TradeRoute route = w.model.createTradeRoute(w.dutch, "Circuits",
                TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 0,
                List.of(GoodsType.RUM, GoodsType.SUGAR, GoodsType.TOBACCO,
                        GoodsType.CORN, GoodsType.HORSE, GoodsType.MUSKET),
                List.of());
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 1,
                List.of(),
                List.of(GoodsType.CORN, GoodsType.HORSE, GoodsType.MUSKET));
        final int gold = w.dutch.getGold();
        w.model.assignTradeRoute(w.ship, route, 0);

        int rounds = 0;
        while (w.soldCount(GoodsType.HORSE) == 0 && rounds++ < MAX_ROUNDS) {
            w.playRound();
        }
        assertTrue(rounds < MAX_ROUNDS, "second Europe visit never happened");

        // First Europe visit sold the three full holds exactly once each.
        assertEquals(1, w.soldCount(GoodsType.RUM));
        assertEquals(1, w.soldCount(GoodsType.SUGAR));
        assertEquals(1, w.soldCount(GoodsType.TOBACCO));
        // Delft was reached once between the visits and filled the holds that
        // the first visit freed; the second visit sold them once.
        assertEquals(1, w.soldCount(GoodsType.HORSE));
        assertEquals(1, w.soldCount(GoodsType.MUSKET));
        assertEquals(1, w.shipSteps.stream().filter(DELFT_AT::equals).count());
        // Both outward legs were real departures, and the ship holds neither
        // the sold goods nor anything it never carried.
        assertEquals(2, w.departures);
        assertTrue(w.dutch.getGold() > gold);
        assertEquals(Optional.empty(), w.held(1));
        assertEquals(Optional.empty(), w.held(3));
        assertEquals(Optional.empty(), w.held(4));
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 60)), w.held(0));
        assertEquals(Optional.of(Goods.of(GoodsType.COTTON, 100)), w.held(5));
        assertEquals(Optional.empty(), w.pause());
        // The recorded departure square is a square of the east sea lane, from
        // which the ship returned: departing needs a step to the next column.
        assertEquals(49,
                ((Ship) w.ship).getDepartureLocation().orElseThrow().getX());
    }

    @Test
    void singleStopInEuropeProcessesOnceAndStaysFinite() {
        final World w = new World();
        TradeRoute route = w.model.createTradeRoute(w.dutch, "Europe only",
                TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe()));
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 0,
                List.of(GoodsType.RUM), List.of());
        w.model.getEurope().getPort().placeShipToPort(w.ship);
        assertTrue(w.ship.isAtEuropePort());
        final int gold = w.dutch.getGold();
        w.model.assignTradeRoute(w.ship, route, 0);

        w.ownerUpdate();

        // The visit is processed once and the single stop is next again.
        assertEquals(1, w.soldCount(GoodsType.RUM));
        assertEquals(Optional.empty(), w.held(1));
        assertEquals(0, w.stopIndex());
        assertEquals(Optional.empty(), w.pause());
        assertTrue(w.ship.isAtEuropePort());
        assertTrue(w.ship.getProcessedTradeRouteVisit().isPresent());
        final int afterFirst = w.dutch.getGold();
        assertTrue(afterFirst > gold);

        // More updates, including one after Save and Continue, sell nothing.
        w.ownerUpdate();
        w.ownerUpdate();
        assertEquals(1, w.soldCount(GoodsType.RUM));
        assertEquals(afterFirst, w.dutch.getGold());
        final World restored = w.saveAndContinue();
        assertTrue(restored.ship.isAtEuropePort());
        assertTrue(restored.ship.getProcessedTradeRouteVisit().isPresent());
        assertEquals(afterFirst, restored.dutch.getGold());
        restored.ownerUpdate();
        assertEquals(0, restored.soldCount(GoodsType.RUM));
        assertEquals(afterFirst, restored.dutch.getGold());
        assertEquals(0, restored.stopIndex());
    }

    @Test
    void coincidentWrapStopsAreNotProcessedTwiceInOneVisit() {
        final World w = new World();
        // The last stop and the first stop are both Europe.
        TradeRoute route = w.model.createTradeRoute(w.dutch, "Wrap",
                TradeRouteKind.SEA, DELFT,
                List.of(TradeRouteStop.europe(), TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.europe()));
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 0,
                List.of(GoodsType.SUGAR), List.of());
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 2,
                List.of(GoodsType.RUM), List.of());
        w.model.getEurope().getPort().placeShipToPort(w.ship);
        w.model.assignTradeRoute(w.ship, route, 2);

        w.ownerUpdate();

        // The last stop was processed; the coincident first stop is the same
        // visit and is passed, and the ship starts for Delft.
        assertEquals(1, w.soldCount(GoodsType.RUM));
        assertEquals(0, w.soldCount(GoodsType.SUGAR));
        assertEquals(1, w.stopIndex());
        assertEquals(Optional.empty(), w.pause());
        assertTrue(w.ship.isAtHighSea());
    }

    @Test
    void seizedShipInClosedPortPausesInsteadOfLeavingForAColony() {
        final World w = new World();
        final TradeRoute route = w.model.createTradeRoute(w.dutch, "Closed",
                TradeRouteKind.SEA, DELFT,
                List.of(TradeRouteStop.europe(), TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.europe()));
        w.model.getEurope().getPort().placeShipToPort(w.ship);
        w.model.assignTradeRoute(w.ship, route, 1);
        // Saved after a declaration made while the ship waited in the port: the
        // Crown holds the ship there (R-0187).
        w.data.setModel(w.model.save());
        w.data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName()))
                .forEach(player -> RebellionFixtures.declare(w.data.getModel(),
                        player));
        ClosedEuropeFixtures.seizeHomeEurope(w.data.getModel());
        final World closed = new World(w.data);
        assertTrue(closed.ship.isCrownSeized());

        closed.ownerUpdate();

        assertEquals(Optional.of(TradeRoutePause.EUROPE_CLOSED),
                closed.pause());
        assertTrue(closed.ship.isAtEuropePort());
        assertTrue(closed.ship.isCrownSeized());
        assertEquals(0, closed.departures);
    }

    @Test
    void emptyStopAndFullHoldAreNoOpsThatDoNotStopTheTurn() {
        final World w = new World();
        // Nothing is configured, so the visit moves nothing and the ship goes
        // on to the next stop in the same update.
        final TradeRoute route = w.seaRoute(List.of(), List.of(), List.of());
        w.model.assignTradeRoute(w.ship, route, 1);
        w.ownerUpdate();
        assertEquals(Optional.of(Goods.of(GoodsType.ORE, 60)), w.held(0));
        assertEquals(0, w.stock(GoodsType.ORE));
        assertEquals(0, w.stopIndex());
        assertTrue(w.ship.getLocation().getX() > DELFT_AT.getX());
    }

    @Test
    void fullHoldsLoadNothingAndTheShipStillLeaves() {
        final World w = new World();
        // All six holds are taken, so the load is a reported no-op.
        final TradeRoute route = w.seaRoute(List.of(), List.of(),
                List.of(GoodsType.HORSE));
        w.model.assignTradeRoute(w.ship, route, 1);
        w.ownerUpdate();
        assertEquals(50, w.stock(GoodsType.HORSE));
        assertEquals(0, w.stopIndex());
        assertTrue(w.ship.getLocation().getX() > DELFT_AT.getX());
    }

    @Test
    void persistedPauseResumesWhenTheStopIsReachableAgain() {
        final World w = new World();
        final TradeRoute route = w.seaRoute(List.of(), List.of(), List.of());
        w.model.assignTradeRoute(w.ship, route, 0);
        w.model.getTradeRouteAssignment(w.ship).orElseThrow()
                .setPause(TradeRoutePause.STOP_UNREACHABLE);

        // Save and Continue keep the reason and move nothing.
        final World restored = w.saveAndContinue();
        assertEquals(Optional.of(TradeRoutePause.STOP_UNREACHABLE),
                restored.pause());
        assertEquals(DELFT_AT, restored.ship.getLocation());

        // The path exists, so the next owner update clears the reason and goes.
        restored.ownerUpdate();
        assertEquals(Optional.empty(), restored.pause());
        assertTrue(restored.ship.getLocation().getX() > DELFT_AT.getX());
    }

    @Test
    void midLegSaveAndContinueKeepsEverythingAndMovesNothing() {
        final World w = new World();
        final TradeRoute route = w.seaRoute(List.of(GoodsType.RUM), List.of(),
                List.of());
        w.model.assignTradeRoute(w.ship, route, 0);
        w.playRound();
        final Location mid = w.ship.getLocation();
        final int movement = w.ship.getMovementThirds();
        final int gold = w.dutch.getGold();
        assertTrue(mid.getX() > DELFT_AT.getX());

        final World restored = w.saveAndContinue();

        assertEquals(mid, restored.ship.getLocation());
        assertEquals(movement, restored.ship.getMovementThirds());
        assertEquals(0, restored.stopIndex());
        assertEquals(gold, restored.dutch.getGold());
        assertEquals(Optional.of(Goods.of(GoodsType.RUM, 100)),
                restored.held(1));
        assertEquals(0, restored.soldCount(GoodsType.RUM));
        assertTrue(restored.shipSteps.isEmpty());
    }

    @Test
    void savedAssignmentNamingAStopBeyondTheRouteIsRejected() {
        final World w = new World();
        final TradeRoute route = w.seaRoute(List.of(), List.of(), List.of());
        w.model.assignTradeRoute(w.ship, route, 0);
        w.data.setModel(w.model.save());
        savedUnit(w.data.getModel(), SHIP_ID)
                .setTradeRouteAssignmentStopIndex(5);

        assertThrows(RuntimeException.class, () -> new World(w.data));
    }

    @Test
    void savedAssignmentWithoutItsStopIndexIsRejected() {
        final World w = new World();
        w.model.assignTradeRoute(w.ship,
                w.seaRoute(List.of(), List.of(), List.of()), 0);
        w.data.setModel(w.model.save());
        savedUnit(w.data.getModel(), SHIP_ID)
                .setTradeRouteAssignmentStopIndex(null);

        assertThrows(RuntimeException.class, () -> new World(w.data));
    }

    @Test
    void goToAndAssignmentExcludeEachOther() {
        final World w = new World(po -> {
        });
        final TradeRoute route = w.seaRoute(List.of(), List.of(), List.of());
        final Location target = Location.of(DELFT_AT.getX() + 3,
                DELFT_AT.getY());
        w.model.assignTradeRoute(w.ship, route, 0);

        assertFalse(w.model.canIssueGoTo(w.ship, target));
        assertThrows(IllegalStateException.class,
                () -> w.model.issueGoTo(w.ship, target));
        assertEquals(DELFT_AT, w.ship.getLocation());
        assertTrue(w.model.getGoToDestination(w.ship).isEmpty());
    }

    @Test
    void wagonFollowsALandRouteAndUnloadsAtTheOtherColony() {
        final World w = new World(TradeRouteNavigationTest::colonistToWagon);
        final UnitWithCargo wagon = (UnitWithCargo) unitOf(w.model, WAGON_ID);
        final Colony newAmsterdam = w.model.getColonyById(NEW_AMSTERDAM)
                .orElseThrow();
        assertEquals(NEW_AMSTERDAM_AT, wagon.getLocation());
        TradeRoute route = w.model.createTradeRoute(w.dutch, "Ore cart",
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 0,
                List.of(GoodsType.ORE), List.of());
        route = w.model.configureTradeRouteStopCargo(w.dutch, route, 1,
                List.of(), List.of(GoodsType.ORE));
        final int ore = newAmsterdam.getWarehouse().getGoods(GoodsType.ORE)
                .getAmount();
        assertEquals(90, ore);

        // A ship cannot take a land route.
        assertEquals(Optional.of(TradeRouteAssignmentProblem.NOT_CARRIER),
                w.model.getTradeRouteAssignmentProblem(w.ship, route, 1));
        w.model.assignTradeRoute(wagon, route, 1);
        w.ownerUpdate();

        // Standing in New Amsterdam, the wagon loads once and heads for Delft
        // on its own movement allowance.
        assertEquals(0, newAmsterdam.getWarehouse().getGoods(GoodsType.ORE)
                .getAmount());
        assertEquals(0, w.stopIndex(wagon));
        assertTrue(wagon.getMovementThirds() < 3);

        int rounds = 0;
        while (w.stock(GoodsType.ORE) == 0 && rounds++ < MAX_ROUNDS) {
            w.playRound();
        }
        assertTrue(rounds < MAX_ROUNDS, "the wagon never reached Delft");
        assertEquals(ore, w.stock(GoodsType.ORE));
        assertEquals(1, w.stopIndex(wagon));
    }

    @Test
    void lostStopPausesWithItsReasonWithoutMovingOrSubstitutingAStop() {
        final World w = new World(TradeRouteNavigationTest::colonistToWagon);
        final UnitWithCargo wagon = (UnitWithCargo) unitOf(w.model, WAGON_ID);
        final TradeRoute route = w.model.createTradeRoute(w.dutch, "Lost cart",
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        w.model.assignTradeRoute(wagon, route, 0);
        final int movement = wagon.getMovementThirds();

        // Delft is lost before the owner update: the same removal the game
        // applies to a destroyed colony.
        w.model.destroyColony(w.delft);
        w.ownerUpdate();

        // The wagon waits with the reason, keeps Delft as its stop and does
        // not head for New Amsterdam or anywhere else instead.
        final TradeRouteAssignment assignment = w.model
                .getTradeRouteAssignment(wagon).orElseThrow();
        assertEquals(Optional.of(TradeRoutePause.STOP_LOST),
                assignment.getPause());
        assertEquals(0, assignment.getStopIndex());
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(NEW_AMSTERDAM_AT, wagon.getLocation());
        assertEquals(movement, wagon.getMovementThirds());

        // Further updates stay finite and change nothing.
        w.ownerUpdate();
        assertEquals(NEW_AMSTERDAM_AT, wagon.getLocation());
        assertEquals(0, w.stopIndex(wagon));
    }

    /**
     * Delft is taken by a real defense battle (R-0219). The colony still exists
     * with the same identity, but it no longer belongs to the route owner, so
     * the former owner's stored route keeps naming it while every route
     * operation treats it as lost: the assigned wagon waits with the reason, a
     * visit is refused, a new route and the stop choice exclude it, and Save
     * and Continue keep the pause. The owner check does not depend on who
     * captured the colony; the Crown is the only rival in this scenario.
     */
    @Test
    void capturedStopIsLostForTheFormerOwnerRouteWithoutMovingTheWagon() {
        final World w = new World(TradeRouteNavigationTest::colonistToWagon);
        final UnitWithCargo wagon = (UnitWithCargo) unitOf(w.model, WAGON_ID);
        final TradeRoute route = w.model.createTradeRoute(w.dutch,
                "Captured cart", TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        w.model.assignTradeRoute(wagon, route, 0);
        final int movement = wagon.getMovementThirds();

        final Player king = w.model.getPlayerByName("Dutch's King");
        final Unit force = w.model.addUnitOutSideColony(w.delft);
        force.takeOver(king);
        ColonyBattles.capture(w.model, w.delft, force);
        assertSame(king, w.delft.getOwner());
        assertEquals(DELFT, w.delft.getId());

        // The stored route still names Delft, but nothing may use it.
        assertEquals(List.of(DELFT, NEW_AMSTERDAM),
                w.model.getTradeRoutes(w.dutch).get(0).getStops().stream()
                        .map(TradeRouteStop::getColonyId)
                        .collect(java.util.stream.Collectors.toList()));
        assertFalse(w.model
                .getTradeRouteStopOptions(w.dutch, TradeRouteKind.LAND,
                        NEW_AMSTERDAM, List.of())
                .contains(TradeRouteStop.ofColony(DELFT)));
        assertEquals(Optional.of(TradeRouteProblem.STOP_UNAVAILABLE),
                w.model.getTradeRouteProblem(w.dutch, "Again",
                        TradeRouteKind.LAND, NEW_AMSTERDAM,
                        List.of(TradeRouteStop.ofColony(DELFT))));
        assertEquals(Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE),
                w.model.processTradeRouteStop(wagon, route, 0).getProblem());

        // The owner update pauses the wagon with the reason and moves nothing.
        w.ownerUpdate();
        final TradeRouteAssignment assignment = w.model
                .getTradeRouteAssignment(wagon).orElseThrow();
        assertEquals(Optional.of(TradeRoutePause.STOP_LOST),
                assignment.getPause());
        assertEquals(0, assignment.getStopIndex());
        assertEquals(NEW_AMSTERDAM_AT, wagon.getLocation());
        assertEquals(movement, wagon.getMovementThirds());

        // Save and Continue keep the lost stop and the pause. The restored
        // world's own handle names Delft's current owner, so the Dutch update
        // is driven for the Dutch by name.
        final World restored = w.saveAndContinue();
        final Unit restoredWagon = unitOf(restored.model, WAGON_ID);
        assertSame(restored.model.getPlayerByName("Dutch's King"),
                restored.delft.getOwner());
        restored.model.advanceTradeRouteAssignments(
                restored.model.getPlayerByName("Dutch"));
        assertEquals(Optional.of(TradeRoutePause.STOP_LOST),
                restored.model.getTradeRouteAssignment(restoredWagon)
                        .orElseThrow().getPause());
        assertEquals(NEW_AMSTERDAM_AT, restoredWagon.getLocation());
    }

    @Test
    void damagedShipShowsItsReasonAndKeepsItsAssignmentThroughSaveAndContinue() {
        final World w = new World();
        final TradeRoute route = w.seaRoute(List.of(), List.of(), List.of());
        w.model.assignTradeRoute(w.ship, route, 0);
        w.model.damageShip((Ship) w.ship);
        assertTrue(((Ship) w.ship).isDamaged());

        w.ownerUpdate();
        assertEquals(Optional.of(TradeRoutePause.CARRIER_DAMAGED), w.pause());

        final World restored = w.saveAndContinue();
        assertTrue(((Ship) restored.ship).isDamaged());
        assertEquals(Optional.of(TradeRoutePause.CARRIER_DAMAGED),
                restored.pause());
        assertEquals(0, restored.stopIndex());
    }
}
