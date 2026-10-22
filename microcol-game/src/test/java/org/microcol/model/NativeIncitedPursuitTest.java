package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeTurnScene.at;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.ai.NativeRobotPlayer;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.NativeIncitementPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitActionSentryPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * The paid pursuit of a native tribe (R-0166) on real native turns. A brave
 * goes for the nearest known unit of the selected European power by the cost of
 * the shared legal route, at any distance, walking only what its one movement
 * point pays each turn, and attacks only once it stands beside the target with
 * movement left. Equal costs go by stable unit ID. A hidden unit, or one with
 * no legal route, is never chosen and the ordinary raid follows; a distant
 * target that merely cannot be reached this turn does not hand the brave back
 * to it. An order ends after its last native turn, or when the tribe's anger
 * toward the target falls below 75.
 * <p>
 * The helpers mirror {@link NativeRaidTest}: the natives act on their own
 * threads and each round is waited for before the outcome is read. A brave
 * crosses one grassland square per turn, so a target three squares away needs
 * one step in the first round, a second step in the next, and the attack in the
 * round after that. Which of several equal routes the finder takes is not part
 * of the rule, so positions are checked by their distance to the target.
 * </p>
 */
class NativeIncitedPursuitTest {

    private static final long TIMEOUT_SECONDS = 10;

    private static final int AZTEC_HOME = 0;

    /** Far corner of the island, where the Aztec capital stands. */
    private static final int FAR = -4;

    /** Saved IDs of French colonists; the lower one is listed first. */
    private static final int FRENCH_LOW = 7010;

    private static final int FRENCH_HIGH = 7020;

    private static final String FRANCE = "France";

    /** Battles committed in one model, recorded on the thread that fights. */
    private static final class Attacks extends ModelListenerAdapter {

        private final List<UnitAttackedEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void onUnitAttacked(final UnitAttackedEvent event) {
            events.add(event);
        }

        int count() {
            return events.size();
        }

        long attacksOn(final int defenderId) {
            return events.stream()
                    .filter(event -> event.getDefender().getId() == defenderId)
                    .count();
        }
    }

    /**
     * Island with an Aztec capital in the far corner, hostile to the Dutch.
     * Each test chooses its own anger toward France.
     */
    private static NativeTurnScene scene() {
        return NativeTurnScene.island()
                .settlement(AZTEC_HOME, NativeNation.AZTEC, true, FAR, FAR)
                .nativePlayer(NativeNation.AZTEC)
                .anger(NativeNation.AZTEC, EuropeanNation.DUTCH, 80);
    }

    /** Scene whose Aztecs are hostile to France and pursue it for 20 turns. */
    private static NativeTurnScene incitedScene() {
        return incited(scene(), EuropeanNation.FRENCH, 20)
                .anger(NativeNation.AZTEC, EuropeanNation.FRENCH, 80);
    }

    /** Saved order of the Aztecs, with the given target and native turns. */
    private static NativeTurnScene incited(final NativeTurnScene scene,
            final EuropeanNation target, final int turns) {
        final NativeIncitementPo order = new NativeIncitementPo();
        order.setNation(NativeNation.AZTEC);
        order.setTarget(target);
        order.setRemainingTurns(turns);
        final List<NativeIncitementPo> orders = new ArrayList<>();
        orders.add(order);
        scene.po().setNativeIncitements(orders);
        return scene;
    }

    /** Dutch colonist with full movement; returns its saved ID. */
    private static int dutchColonist(final NativeTurnScene scene, final int dx,
            final int dy) {
        scene.dutchColonist(dx, dy);
        final List<UnitPo> units = scene.po().getUnits();
        final UnitPo colonist = units.get(units.size() - 1);
        colonist.setMovementThirds(3);
        return colonist.getId();
    }

    private static int frenchColonist(final NativeTurnScene scene, final int id,
            final int dx, final int dy) {
        scene.rawUnit(id, UnitType.COLONIST, FRANCE, null, null, at(dx, dy));
        return id;
    }

    /**
     * Adds a homeless Aztec lookout on a sentry order: it makes the neighbors
     * of its square known to the Aztecs, but can neither attack nor move, so it
     * never disturbs the case under test even though it shares the order.
     */
    private static void lookout(final NativeTurnScene scene, final int dx,
            final int dy) {
        scene.brave(NativeNation.AZTEC, null, dx, dy);
        final List<UnitPo> units = scene.po().getUnits();
        units.get(units.size() - 1).setAction(new UnitActionSentryPo());
    }

    private static Attacks watch(final Model model) {
        final Attacks attacks = new Attacks();
        model.addListener(attacks);
        return attacks;
    }

    private static List<NativeRobotPlayer> robots(final Model model) {
        return model.getPlayers().stream().filter(Player::isNative)
                .map(player -> new NativeRobotPlayer(model, player, () -> {
                })).collect(Collectors.toList());
    }

    /** Waits until the natives have ended their turns and a European is up. */
    private static void awaitEuropeanTurn(final Model model)
            throws InterruptedException {
        final long deadline = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (model.getCurrentPlayer().isNative()) {
            assertTrue(System.nanoTime() < deadline,
                    "The native turn never ended");
            Thread.sleep(5);
        }
    }

    private static void startRound(final Model model)
            throws InterruptedException {
        model.startGame();
        awaitEuropeanTurn(model);
    }

    /** Ends European turns until the calendar advances and the natives act. */
    private static void completeRound(final Model model)
            throws InterruptedException {
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        int guard = 0;
        while (model.getCalendar().getNumberOfPlayedTurns() == before) {
            model.endTurn();
            awaitEuropeanTurn(model);
            assertTrue(++guard < 50, "The round never completed");
        }
    }

    /** The one Aztec brave that has a home, which is never a lookout. */
    private static UnitNativeBrave homedAztec(final Model model) {
        return model.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave)
                .map(unit -> (UnitNativeBrave) unit)
                .filter(brave -> brave.getHomeSettlementId().isPresent())
                .findAny().orElseThrow();
    }

    /** ID of the native brave standing on a square. */
    private static int braveIdAt(final Model model, final Location where) {
        return model.getUnitsAt(where).stream()
                .filter(unit -> unit instanceof UnitNativeBrave).findAny()
                .orElseThrow().getId();
    }

    /** Chebyshev distance of a unit to an island offset. */
    private static int distance(final Model model, final int unitId,
            final int dx, final int dy) {
        return model.getUnitById(unitId).getLocation()
                .getDistanceChebyshev(at(dx, dy));
    }

    /** Chebyshev distance of the homed brave to an island offset. */
    private static int distance(final Model model, final int dx, final int dy) {
        return distance(model, homedAztec(model).getId(), dx, dy);
    }

    /** The first battle of the test is won by the attacker. */
    private static void attackerWins(final NativeTurnScene scene) {
        scene.po().setCombatRandomState(CombatDraws.attackerWins());
    }

    // ---- pursuit beyond the radius and over several turns ------------------

    /**
     * The French colonist is three squares away, beyond the raid radius, and
     * known through a lookout; a Dutch colonist stands beside the brave. The
     * paid target wins every round: the brave takes one step in round one
     * without touching the Dutch, a second in round two that ends beside the
     * French with no movement left to attack, and attacks in round three.
     */
    @Test
    void knownDistantTargetOfThePaidPowerIsWalkedToOverTurnsAndAttacked()
            throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 3, -1);
        final int french = frenchColonist(scene, FRENCH_LOW, 3, 0);
        final int dutch = dutchColonist(scene, 0, 1);
        attackerWins(scene);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count(),
                    "The first step cannot reach, and does not fall back to the "
                            + "adjacent ordinary target");
            assertEquals(2, distance(model, 3, 0),
                    "One affordable step toward the paid target");

            completeRound(model);
            assertEquals(0, attacks.count(),
                    "Standing beside the target after moving leaves no "
                            + "movement to attack");
            assertEquals(1, distance(model, 3, 0));

            completeRound(model);
            assertEquals(1, attacks.attacksOn(french));
            assertEquals(0, attacks.attacksOn(dutch));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * Both French colonists are two route steps away. The lower ID stands east
     * and the higher ID west; a coordinate order would prefer the west, so only
     * the stable ID explains the eastward step.
     */
    @Test
    void equalRouteCostTargetsAreTakenByStableUnitId() throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 3, -1);
        lookout(scene, -3, -1);
        frenchColonist(scene, FRENCH_LOW, 3, 0);
        frenchColonist(scene, FRENCH_HIGH, -3, 0);
        final Model model = scene.model();
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(2, distance(model, 3, 0),
                    "The lower ID is approached");
            assertEquals(4, distance(model, -3, 0));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * The lower ID stands two route steps away to the north, the higher ID is
     * one step from a square beside it to the south, so the route cost, not the
     * ID, decides: the brave steps south.
     */
    @Test
    void nearerRouteBeatsTheLowerUnitId() throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 1, -3);
        lookout(scene, -1, 3);
        frenchColonist(scene, FRENCH_LOW, 0, -3);
        frenchColonist(scene, FRENCH_HIGH, 0, 2);
        final Model model = scene.model();
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, distance(model, 0, 2),
                    "The brave steps beside the cheaper higher ID");
            assertTrue(
                    homedAztec(model).getLocation().getY() > at(0, 0).getY());
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * The straight-line nearest French colonist, with the lower ID, stands
     * behind a sea wall that leaves only a route around its south end, three
     * steps long. The other is straight-line farther but two steps away to the
     * north. The route cost chooses the north; the straight distance would
     * choose the east, and every route to the first starts southward. The
     * lookout at (2, 2) explores the southern detour, so the first target keeps
     * a known route and is beaten by cost, not left out.
     */
    @Test
    void routeCostNotStraightDistanceChoosesTheTarget() throws Exception {
        final NativeTurnScene scene = incitedScene()
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0).sea(1, -4)
                .sea(1, -3).sea(1, -2).sea(1, -1).sea(1, 0).sea(1, 1);
        lookout(scene, 3, 0);
        lookout(scene, -1, -3);
        lookout(scene, 2, 2);
        frenchColonist(scene, FRENCH_LOW, 2, 0);
        frenchColonist(scene, FRENCH_HIGH, 0, -3);
        final Model model = scene.model();
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertTrue(homedAztec(model).getLocation().getY() < at(0, 0).getY(),
                    "The brave goes north to the cheaper target, not south "
                            + "around the wall");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- exclusions and the ordinary fallback ------------------------------

    /**
     * The only French colonist is in no sight of the Aztecs. The order selects
     * nothing, so the ordinary raid attacks the Dutch colonist beside the
     * brave.
     */
    @Test
    void hiddenPaidTargetIsIgnoredAndTheOrdinaryRaidFollows() throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        final int french = frenchColonist(scene, FRENCH_LOW, 4, 4);
        final int dutch = dutchColonist(scene, 0, 1);
        attackerWins(scene);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.attacksOn(dutch),
                    "The fallback is the ordinary raid");
            assertEquals(0, attacks.attacksOn(french));
            assertEquals(4, distance(model, 4, 4),
                    "The brave did not walk toward the hidden unit");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * The known French colonist stands on a two-square islet that no legal
     * route reaches. It is not a paid target, so the ordinary raid attacks the
     * Dutch colonist beside the brave.
     */
    @Test
    void unreachablePaidTargetFallsBackToTheOrdinaryRaid() throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        isolateWestIslet(scene);
        lookout(scene, -3, 0);
        final int french = frenchColonist(scene, FRENCH_LOW, -2, 0);
        final int dutch = dutchColonist(scene, 0, 1);
        attackerWins(scene);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.attacksOn(dutch));
            assertEquals(0, attacks.attacksOn(french));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * An unreachable French colonist, with the lower ID and nearer in a
     * straight line, does not hide a reachable farther one: the brave walks
     * east.
     */
    @Test
    void unreachablePaidTargetDoesNotHideAReachableFartherOne()
            throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        isolateWestIslet(scene);
        lookout(scene, -3, 0);
        lookout(scene, 3, -1);
        frenchColonist(scene, FRENCH_LOW, -2, 0);
        frenchColonist(scene, FRENCH_HIGH, 3, 0);
        final Model model = scene.model();
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(2, distance(model, 3, 0));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * A homeless brave of the tribe obeys the order too, within its one
     * movement point: it takes a single step toward the known target. Without
     * an order it would stand still, as in the ordinary raid tests.
     */
    @Test
    void homelessBravePursuesTheKnownPaidTargetWithinItsMovement()
            throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                null, 0, 0);
        lookout(scene, 3, -1);
        frenchColonist(scene, FRENCH_LOW, 3, 0);
        final Model model = scene.model();
        final int walker = braveIdAt(model, at(0, 0));
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(2, distance(model, walker, 3, 0));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- lifetime and de-escalation ----------------------------------------

    /**
     * The last native turn of an order still acts: the brave steps toward the
     * target, and the order is gone once that turn has ended.
     */
    @Test
    void lastNativeTurnStillPursuesThenTheOrderEnds() throws Exception {
        final NativeTurnScene scene = incited(scene(), EuropeanNation.FRENCH, 1)
                .anger(NativeNation.AZTEC, EuropeanNation.FRENCH, 80)
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0);
        lookout(scene, 3, -1);
        frenchColonist(scene, FRENCH_LOW, 3, 0);
        final Model model = scene.model();
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(2, distance(model, 3, 0));
            assertTrue(model.getNativeIncitementOrder(NativeNation.AZTEC)
                    .isEmpty());
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * Anger of 60 toward the target is below 75, so the order ends at the start
     * of the native turn and no French unit is pursued.
     */
    @Test
    void lowAngerEndsTheOrderBeforeTheBraveActs() throws Exception {
        final NativeTurnScene scene = incited(scene(), EuropeanNation.FRENCH,
                20).anger(NativeNation.AZTEC, EuropeanNation.FRENCH, 60)
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0);
        lookout(scene, 3, -1);
        final int french = frenchColonist(scene, FRENCH_LOW, 3, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.attacksOn(french));
            assertTrue(distance(model, 3, 0) > 2,
                    "The brave does not walk toward the former target");
            assertTrue(model.getNativeIncitementOrder(NativeNation.AZTEC)
                    .isEmpty());
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * Sea around the two western squares (-3, 0) and (-2, 0), which no route
     * from the middle of the island reaches.
     */
    // ---- known terrain only ------------------------------------------------

    /**
     * The French colonist at (3, 0) is in sight of the lookout at (4, 0), but
     * nothing of the Aztecs has explored the column x = 2 between the brave and
     * the lookout. No known route exists, so the order selects nothing and the
     * ordinary raid attacks the Dutch colonist beside the brave; a route
     * through the unexplored column would have sent the brave east instead.
     */
    @Test
    void visibleTargetBehindUnexploredTerrainFallsBackToTheOrdinaryRaid()
            throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 4, 0);
        final int french = frenchColonist(scene, FRENCH_LOW, 3, 0);
        final int dutch = dutchColonist(scene, 0, 1);
        attackerWins(scene);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.attacksOn(dutch),
                    "Without a known route the ordinary raid follows");
            assertEquals(0, attacks.attacksOn(french));
            assertEquals(3, distance(model, 3, 0),
                    "The brave did not walk into unexplored terrain");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * The same position, but the Aztecs explored (2, 0) earlier: it is saved as
     * explored though no unit sees it now. That remembered square completes a
     * known route, so the brave pursues the French colonist and leaves the
     * ordinary Dutch target alone.
     */
    @Test
    void exploredButNotCurrentlyVisibleTerrainIsAKnownRoute() throws Exception {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 4, 0);
        frenchColonist(scene, FRENCH_LOW, 3, 0);
        final int dutch = dutchColonist(scene, 0, 1);
        explored(scene, at(2, 0));
        final Model model = scene.model();
        assertFalse(aztecs(model).isCurrentlyVisible(at(2, 0)));
        assertTrue(aztecs(model).isVisible(at(2, 0)));
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.attacksOn(dutch));
            assertEquals(2, distance(model, 3, 0),
                    "One step along the remembered route");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    /**
     * Beside (3, 0) the shortest route runs through (2, -1) or (2, 0), which no
     * Aztec has explored. The known route goes around them over (1, -1), (2,
     * -2) and (3, -1), explored by the brave and the two lookouts. The hidden
     * shortcut does not hide that longer known route, and the known route never
     * steps on an unexplored square.
     */
    @Test
    void knownRouteGoesAroundAnUnexploredShortcut() {
        final NativeTurnScene scene = incitedScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, 4, -2);
        lookout(scene, 1, -3);
        final Model model = scene.model();
        final UnitNativeBrave brave = homedAztec(model);
        assertEquals(2, brave.getPath(at(3, 0), true).orElseThrow().size(),
                "The shortcut is the shortest route on the whole map");
        final List<Location> known = brave.getKnownPath(at(3, 0), true)
                .orElseThrow();
        assertEquals(List.of(at(1, -1), at(2, -2), at(3, -1)), known);
        known.forEach(square -> assertTrue(aztecs(model).isVisible(square),
                () -> "Unexplored square on the route: " + square));
    }

    private static Player aztecs(final Model model) {
        return model.getPlayerByName(NativeNation.AZTEC.name());
    }

    /** Squares the Aztecs explored earlier, saved without a unit in sight. */
    private static void explored(final NativeTurnScene scene,
            final Location... squares) {
        final PlayerPo aztecs = scene.po().getGameManager().getPlayers()
                .stream().filter(player -> NativeNation.AZTEC.name()
                        .equals(player.getName()))
                .findFirst().orElseThrow();
        aztecs.getVisible().setVisibility(Set.of(squares),
                scene.po().getMap().getMaxX(), scene.po().getMap().getMaxY());
    }

    private static void isolateWestIslet(final NativeTurnScene scene) {
        scene.sea(-4, -1).sea(-4, 0).sea(-4, 1).sea(-3, -1).sea(-3, 1)
                .sea(-2, -1).sea(-2, 1).sea(-1, -1).sea(-1, 0).sea(-1, 1);
    }

}
