package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.ai.NativeRobotPlayer;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeTribalAngerPo;
import org.microcol.model.store.UnitActionSentryPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * Native braves raiding on real native turns (R-0156). Each scene is the island
 * with explicit parties. The robots take their turns on their own threads, so
 * the helpers wait for the natives to hand the round back to a European. Each
 * committed battle is one attack event; Dutch units are prepared again for
 * their own turn after the natives act, so a battle is witnessed by its event,
 * never by the Dutch unit's movement.
 * <p>
 * Knowledge is per nation: only units of the acting Aztec player extend what it
 * sees. A target beyond a brave's own sight is therefore made known by a
 * homeless Aztec lookout on a sentry order, which sees its neighbors but can
 * neither attack nor patrol, so lookouts never disturb the case under test.
 * </p>
 */
class NativeRaidTest {

    private static final long TIMEOUT_SECONDS = 10;

    /** Patrol radius of the raid AI, in Chebyshev squares. */
    private static final int PATROL_RADIUS = 2;

    private static final int AZTEC_HOME = 0;
    private static final int INCA_HOME = 1;

    /** Far corner of the island, where the Aztec capital stands by default. */
    private static final int FAR = -4;

    /**
     * Saved ID of the galleon in the ship test. It sits below the IDs the scene
     * gives colonists, so it would win a tie by ID if it were a legal target.
     */
    private static final int GALLEON_ID = 4990;

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

        /** Battles fought by the unit with this ID, whichever side won. */
        long attacksBy(final int attackerId) {
            return events.stream()
                    .filter(event -> event.getAttacker().getId() == attackerId)
                    .count();
        }

        /** Battles in which the unit with this ID was the defender. */
        long attacksOn(final int defenderId) {
            return events.stream()
                    .filter(event -> event.getDefender().getId() == defenderId)
                    .count();
        }
    }

    /**
     * Island with an Aztec capital at the given offset and an Inca capital far
     * away. The Aztec tribal anger toward the Dutch is set as given; the Inca
     * stay content.
     */
    private static NativeTurnScene scene(final int capitalDx,
            final int capitalDy, final double aztecAnger) {
        return NativeTurnScene.island()
                .settlement(AZTEC_HOME, NativeNation.AZTEC, true, capitalDx,
                        capitalDy)
                .settlement(INCA_HOME, NativeNation.INCA, true, 4, -4)
                .nativePlayer(NativeNation.AZTEC)
                .nativePlayer(NativeNation.INCA)
                .anger(NativeNation.AZTEC, EuropeanNation.DUTCH, aztecAnger);
    }

    /** Scene with the Aztec capital in the far corner and hostile Aztecs. */
    private static NativeTurnScene hostileScene() {
        return scene(FAR, FAR, 80);
    }

    /** Adds a Dutch colonist with full movement and returns its ID. */
    private static int placeColonist(final NativeTurnScene scene, final int dx,
            final int dy) {
        scene.dutchColonist(dx, dy);
        final List<UnitPo> units = scene.po().getUnits();
        final UnitPo colonist = units.get(units.size() - 1);
        colonist.setMovementThirds(3);
        return colonist.getId();
    }

    /** Adds a Dutch galleon on a sea square and returns its ID. */
    private static int placeGalleon(final NativeTurnScene scene, final int dx,
            final int dy) {
        scene.rawUnit(GALLEON_ID, UnitType.GALLEON, "Dutch", null, null,
                at(dx, dy));
        return GALLEON_ID;
    }

    /**
     * Adds a homeless Aztec brave on a sentry order. It extends what the Aztecs
     * see by its neighbors, but a standing order forbids its attack and a
     * homeless brave never patrols.
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

    /** Starts the game and waits for the first native round to finish. */
    private static void startRound(final Model model)
            throws InterruptedException {
        model.startGame();
        awaitEuropeanTurn(model);
    }

    /**
     * Ends European turns until the calendar advances and the natives of the
     * next round have acted.
     */
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

    private static boolean exists(final Model model, final int id) {
        return model.getAllUnits().stream()
                .anyMatch(unit -> unit.getId() == id);
    }

    /** ID of the native brave standing on a square. */
    private static int braveIdAt(final Model model, final Location at) {
        return model.getUnitsAt(at).stream()
                .filter(unit -> unit instanceof UnitNativeBrave).findAny()
                .orElseThrow().getId();
    }

    private static UnitNativeBrave braveOf(final Model model,
            final NativeNation nation) {
        return model.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave)
                .map(unit -> (UnitNativeBrave) unit)
                .filter(brave -> brave.getNation() == nation).findAny()
                .orElseThrow();
    }

    /** The one Aztec brave that has a home, which is never a lookout. */
    private static UnitNativeBrave homedAztec(final Model model) {
        return model.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave)
                .map(unit -> (UnitNativeBrave) unit)
                .filter(brave -> brave.getHomeSettlementId().isPresent())
                .filter(brave -> brave.getNation() == NativeNation.AZTEC)
                .findAny().orElseThrow();
    }

    private static Player player(final Model model, final String name) {
        return model.getPlayers().stream()
                .filter(candidate -> candidate.getName().equals(name)).findAny()
                .orElseThrow();
    }

    private static Location at(final int dx, final int dy) {
        return NativeTurnScene.at(dx, dy);
    }

    private static int distanceToHome(final UnitNativeBrave brave,
            final Location home) {
        return brave.getLocation().getDistanceChebyshev(home);
    }

    // ---- nearest, ties and visibility -----------------------------------

    @Test
    void hostileBraveAttacksTheNearestKnownEnemyAndLeavesTheFarOneAlone()
            throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        // The far colonist is known through the lookout beside it, and has the
        // lower ID, so only the distance can prefer the near one.
        lookout(scene, 3, -2);
        final int far = placeColonist(scene, 2, -2);
        final int near = placeColonist(scene, 1, 0);
        assertTrue(far < near);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.count(), "The hostile brave attacks once");
            assertEquals(1, attacks.attacksOn(near),
                    "The nearer enemy is attacked");
            assertEquals(0, attacks.attacksOn(far),
                    "The farther known enemy is left alone");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void equallyNearEnemiesAreChosenByStableUnitId() throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        final int first = placeColonist(scene, 1, 0);
        final int second = placeColonist(scene, 0, 1);
        assertTrue(first < second);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.count());
            assertEquals(1, attacks.attacksOn(first),
                    "The lower ID is attacked");
            assertEquals(0, attacks.attacksOn(second));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void hiddenEnemyBeyondPresentSightIsNotSelected() throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        // Two squares away, outside the brave's own sight and no other Aztec
        // unit sees it.
        final int hidden = placeColonist(scene, 2, 2);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count(),
                    "A hidden enemy is never attacked");
            assertEquals(0, attacks.attacksOn(hidden));
            assertTrue(distanceToHome(homedAztec(model), at(FAR, FAR)) < 4,
                    "With no target the brave returns toward home");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void knownEnemyBeyondTheRaidRadiusIsNotSelected() throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        // Three squares away but known through the lookout beside it.
        lookout(scene, 3, -1);
        final int beyond = placeColonist(scene, 3, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count());
            assertEquals(0, attacks.attacksOn(beyond));
            assertTrue(distanceToHome(homedAztec(model), at(FAR, FAR)) < 4,
                    "A target beyond the radius leaves the home patrol");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void untargetableGalleonDoesNotDistractFromALegalColonist()
            throws Exception {
        // The galleon is known through the lookout and has the lower ID, but
        // no brave may attack a ship, so the brave approaches the colonist.
        final NativeTurnScene scene = hostileScene()
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0).sea(2, 0);
        lookout(scene, 1, 1);
        final int galleon = placeGalleon(scene, 2, 0);
        final int colonist = placeColonist(scene, 0, 2);
        assertTrue(galleon < colonist);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final int approacher = braveIdAt(model, at(0, 0));
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count(), "The approach ends the movement");
            assertEquals(0, attacks.attacksOn(galleon),
                    "No native brave attacks the galleon");
            assertTrue(
                    model.getUnitById(approacher).getLocation()
                            .isNeighbor(at(0, 2)),
                    "The brave steps beside the legal colonist");
            assertTrue(exists(model, colonist));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- hostility and de-escalation ------------------------------------

    @Test
    void calmBraveNeverRaidsAndStillReturnsHome() throws Exception {
        final NativeTurnScene scene = scene(FAR, FAR, 10)
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0);
        lookout(scene, 3, -2);
        placeColonist(scene, 2, -2);
        placeColonist(scene, 1, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count(), "A content brave never attacks");
            assertTrue(distanceToHome(homedAztec(model), at(FAR, FAR)) < 4,
                    "A calm brave takes the shared home patrol, not a raid");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void uneasyAndAngryBravesNeverAttack() throws Exception {
        for (final double anger : new double[] { 30, 74.9 }) {
            final NativeTurnScene scene = scene(FAR, FAR, anger)
                    .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0);
            placeColonist(scene, 1, 0);
            final Model model = scene.model();
            final Attacks attacks = watch(model);
            final List<NativeRobotPlayer> robots = robots(model);
            try {
                startRound(model);
                assertEquals(0, attacks.count(),
                        "No attack at tribal anger " + anger);
            } finally {
                robots.forEach(NativeRobotPlayer::stop);
            }
        }
    }

    @Test
    void braveStandsDownOnceRelationsImproveAndContinueReplaysNothing()
            throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        final int first = placeColonist(scene, 1, 0);
        final int second = placeColonist(scene, 0, 1);
        // The first battle is decided by the saved draws: the brave wins, so it
        // survives for the later rounds.
        scene.po().setCombatRandomState(CombatDraws.attackerWins());
        final Model firstModel = scene.model();
        final Attacks firstAttacks = watch(firstModel);
        final List<NativeRobotPlayer> firstRobots = robots(firstModel);
        try {
            startRound(firstModel);
            assertEquals(1, firstAttacks.count(),
                    "The hostile round attacks once");
            assertEquals(1, firstAttacks.attacksOn(first));
            assertEquals(0, firstAttacks.attacksOn(second));
        } finally {
            firstRobots.forEach(NativeRobotPlayer::stop);
        }

        // Peace improves while the game is saved at a European turn. Continue
        // must not replay the attack, and the next native turn stands down.
        final ModelPo saved = firstModel.save();
        for (final NativeTribalAngerPo entry : saved.getNativeTribalAnger()) {
            if (entry.getNation() == NativeNation.AZTEC
                    && entry.getEuropean() == EuropeanNation.DUTCH) {
                entry.setValue(10.0);
            }
        }
        final Model restored = NativeTurnScene.make(saved);
        final Attacks restoredAttacks = watch(restored);
        final List<NativeRobotPlayer> restoredRobots = robots(restored);
        try {
            restored.startGame();
            assertEquals(0, restoredAttacks.count(),
                    "Continue replays no attack");
            // The winner stands where the battle left it, outside the radius of
            // its home, so the calm patrol must bring it closer.
            final Location before = homedAztec(restored).getLocation();
            final Location home = at(FAR, FAR);
            assertTrue(before.getDistanceChebyshev(home) > PATROL_RADIUS);
            completeRound(restored);
            assertEquals(0, restoredAttacks.count(),
                    "The calmer brave does not attack in the next round");
            assertTrue(exists(restored, second));
            assertTrue(
                    distanceToHome(homedAztec(restored), home) < before
                            .getDistanceChebyshev(home),
                    "The calmer brave makes progress toward home");
        } finally {
            restoredRobots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- movement, exhaustion and blocked targets -----------------------

    @Test
    void blockedNearerEnemyIsSkippedAndReachableFarEnemyApproached()
            throws Exception {
        // The west colonist and its lookout stand on a two-square islet that
        // no path from the brave reaches. Both colonists are known through
        // lookouts and are equally near, so the lower ID (west) is tried first.
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        lookout(scene, -3, 0);
        lookout(scene, 1, 2);
        final int blocked = placeColonist(scene, -2, 0);
        final int reachable = placeColonist(scene, 0, 2);
        assertTrue(blocked < reachable);
        scene.sea(-4, -1).sea(-4, 0).sea(-4, 1).sea(-3, -1).sea(-3, 1)
                .sea(-2, -1).sea(-2, 1).sea(-1, -1).sea(-1, 0).sea(-1, 1);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final int approacher = braveIdAt(model, at(0, 0));
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count(),
                    "The approach spends the movement; lookouts cannot attack");
            assertEquals(at(0, 1), model.getUnitById(approacher).getLocation(),
                    "The brave steps to the first reachable square beside "
                            + "the reachable colonist");

            // The next native turn starts with full movement and attacks the
            // adjacent colonist, never the one it cannot reach.
            completeRound(model);
            assertEquals(1, attacks.attacksBy(approacher));
            assertEquals(1, attacks.attacksOn(reachable));
            assertEquals(0, attacks.attacksOn(blocked));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void noLegalStepEndsTheTurnWithoutMovingOrAttacking() throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        scene.sea(-1, -1).sea(0, -1).sea(1, -1).sea(-1, 0).sea(1, 0).sea(-1, 1)
                .sea(0, 1).sea(1, 1);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count());
            assertEquals(at(0, 0), homedAztec(model).getLocation());
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- home patrol ----------------------------------------------------

    @Test
    void patrolStaysWithinTheRadiusOfHomeInCoordinateOrder() throws Exception {
        // Home two squares north, so the brave starts on the patrol radius.
        final NativeTurnScene scene = scene(0, -2, 80).brave(NativeNation.AZTEC,
                AZTEC_HOME, 0, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            // The first reachable neighbor by ascending y, then x.
            assertEquals(at(-1, -1), homedAztec(model).getLocation());
            for (int round = 0; round < 3; round++) {
                completeRound(model);
                assertTrue(
                        distanceToHome(homedAztec(model),
                                at(0, -2)) <= PATROL_RADIUS,
                        "The patrol stays within the radius of home");
            }
            assertEquals(0, attacks.count());
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void homelessHostileBraveWithoutTargetEndsItsTurn() throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                null, 0, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(0, attacks.count());
            assertEquals(at(0, 0),
                    braveOf(model, NativeNation.AZTEC).getLocation(),
                    "A homeless brave without a target does not patrol");
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    @Test
    void homelessHostileBraveRaidsByTheTribalAngerOfItsNation()
            throws Exception {
        final NativeTurnScene scene = hostileScene().brave(NativeNation.AZTEC,
                null, 0, 0);
        final int colonist = placeColonist(scene, 1, 0);
        final Model model = scene.model();
        final Attacks attacks = watch(model);
        final List<NativeRobotPlayer> robots = robots(model);
        try {
            startRound(model);
            assertEquals(1, attacks.count());
            assertEquals(1, attacks.attacksOn(colonist));
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- attitude read by the model -------------------------------------

    @Test
    void homelessBraveReadsOnlyTheTribalAngerOfItsNation() {
        final Model model = scene(FAR, FAR, 30)
                .brave(NativeNation.AZTEC, null, 0, 0).model();
        final UnitNativeBrave brave = braveOf(model, NativeNation.AZTEC);
        assertTrue(model.getNativeBraveHome(brave).isEmpty());
        assertEquals(NativeHostility.UNEASY,
                model.getNativeBraveAttitude(brave, player(model, "Dutch")));
    }

    @Test
    void homedBraveReadsItsHomeSettlementAttitude() {
        final Model model = scene(FAR, FAR, 80)
                .brave(NativeNation.AZTEC, AZTEC_HOME, 0, 0).model();
        final UnitNativeBrave brave = braveOf(model, NativeNation.AZTEC);
        assertEquals(at(FAR, FAR),
                model.getNativeBraveHome(brave).orElseThrow().getLocation());
        assertEquals(NativeHostility.HOSTILE,
                model.getNativeBraveAttitude(brave, player(model, "Dutch")));
        // Toward a nation with no anger the same brave is content.
        assertEquals(NativeHostility.CONTENT,
                model.getNativeBraveAttitude(brave, player(model, "France")));
    }

}
