package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.ai.NativeRobotPlayer;
import org.microcol.model.event.TurnFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * Native nations as real turn participants and braves as real units, loaded
 * through the ordinary save path onto a valid island. Every negative case
 * starts from a valid baseline and asserts the validation that rejects it, so a
 * rejection for an unrelated reason cannot pass.
 * <p>
 * Turn order is read on the test thread from the current player after each
 * transition and cross-checked with the synchronous turn-finished event.
 * Turn-started events are delivered on separate threads, so they are only used
 * where a real robot callback is awaited.
 * </p>
 */
class NativePlayerTurnTest {

    private static final int CALENDAR_START = 0;

    /** Prepared full foot movement: one point is three thirds. */
    private static final int FULL_FOOT_MOVEMENT_THIRDS = 3;

    /** Players whose turn finished, recorded on the ending thread. */
    private static final class Finished extends ModelListenerAdapter {

        private final List<String> names = new CopyOnWriteArrayList<>();

        @Override
        public void onTurnFinished(final TurnFinishedEvent event) {
            names.add(event.getPlayer().getName());
        }

        List<String> snapshot() {
            return List.copyOf(names);
        }
    }

    /**
     * Players that became current during one complete round, in order, and the
     * first player of the next round as the last element.
     */
    private static List<String> completeRound(final Model model) {
        final List<String> current = new ArrayList<>();
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        int guard = 0;
        while (model.getCalendar().getNumberOfPlayedTurns() == before) {
            model.endTurn();
            current.add(model.getCurrentPlayer().getName());
            assertTrue(++guard < 50, "The round never completed");
        }
        assertEquals(before + 1, model.getCalendar().getNumberOfPlayedTurns(),
                "The calendar must advance exactly once per round");
        return List.copyOf(current);
    }

    /**
     * Start a fresh game and play its first complete round.
     *
     * @return players in the order their turns were prepared during the round,
     *         without the first player of the next round
     */
    private static List<String> playRound(final Model model) {
        final Finished finished = new Finished();
        model.addListener(finished);
        model.startGame();
        final List<String> prepared = new ArrayList<>();
        prepared.add(model.getCurrentPlayer().getName());
        prepared.addAll(completeRound(model));
        final List<String> round = List
                .copyOf(prepared.subList(0, prepared.size() - 1));
        // Every prepared turn of the round also finished once, in order.
        assertEquals(round, finished.snapshot());
        return round;
    }

    /** European turn order of the stock world: the baseline without natives. */
    private static List<String> europeanOrder() {
        return playRound(NativeTurnScene.island().model());
    }

    /** Scene with INCA and ARAWAK settlements, each with one brave. */
    private static NativeTurnScene baseline() {
        return NativeTurnScene.island()
                .settlement(0, NativeNation.INCA, true, 0, -2)
                .settlement(1, NativeNation.ARAWAK, true, 2, -2)
                .nativePlayer(NativeNation.INCA)
                .nativePlayer(NativeNation.ARAWAK)
                .brave(NativeNation.INCA, 0, 0, 0)
                .brave(NativeNation.ARAWAK, 1, 2, 0);
    }

    /** Scene with every nation: one capital and one brave each. */
    private static NativeTurnScene allNations() {
        final NativeNation[] nations = NativeNation.values();
        NativeTurnScene scene = NativeTurnScene.island();
        for (int i = 0; i < nations.length; i++) {
            scene = scene.settlement(i, nations[i], true, i - 4, -2)
                    .nativePlayer(nations[i]).brave(nations[i], i, i - 4, 0);
        }
        return scene;
    }

    private static Player player(final Model model, final String name) {
        return model.getPlayers().stream()
                .filter(candidate -> candidate.getName().equals(name)).findAny()
                .orElseThrow();
    }

    private static List<UnitNativeBrave> braves(final Model model) {
        return model.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave)
                .map(unit -> (UnitNativeBrave) unit)
                .collect(Collectors.toList());
    }

    private static UnitNativeBrave braveOf(final Model model,
            final NativeNation nation) {
        return braves(model).stream().filter(
                brave -> brave.getOwner().getName().equals(nation.name()))
                .findAny().orElseThrow();
    }

    private static void assertRejectedBecause(final NativeTurnScene scene,
            final String fragment) {
        final IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class, scene::model);
        assertTrue(failure.getMessage().contains(fragment),
                "Rejected for another reason: " + failure.getMessage());
    }

    // ---- identities -------------------------------------------------------

    @Test
    void validBaselineLoads() {
        final Model model = assertDoesNotThrow(() -> baseline().model());
        assertEquals(2, braves(model).size());
    }

    @Test
    void allEightNationsKeepTheirOwnBraveAndHome() {
        final NativeNation[] nations = NativeNation.values();
        final Model model = allNations().model();
        assertEquals(8, braves(model).size());
        for (int i = 0; i < nations.length; i++) {
            final UnitNativeBrave brave = braveOf(model, nations[i]);
            assertEquals(nations[i], brave.getNation());
            assertEquals(nations[i],
                    ((PlayerNative) brave.getOwner()).getNativeNation());
            assertEquals(Optional.of(i), brave.getHomeSettlementId());
            assertEquals(NativeTurnScene.at(i - 4, 0), brave.getLocation());
            final NativeSettlement home = model.getNativeSettlements().stream()
                    .filter(settlement -> settlement.getId() == brave
                            .getHomeSettlementId().orElseThrow())
                    .findAny().orElseThrow();
            assertEquals(nations[i], home.getNation());
        }
    }

    @Test
    void allEightIdentitiesSurviveSaveAndLoad() {
        final Model model = allNations().model();
        // Only a started game has a save boundary.
        model.startGame();
        final Model restored = NativeTurnScene.make(model.save());
        for (final NativeNation nation : NativeNation.values()) {
            final UnitNativeBrave before = braveOf(model, nation);
            final UnitNativeBrave after = braveOf(restored, nation);
            assertEquals(before.getId(), after.getId());
            assertEquals(nation, after.getNation());
            assertEquals(before.getHomeSettlementId(),
                    after.getHomeSettlementId());
            assertEquals(before.getLocation(), after.getLocation());
        }
        assertEquals(8, braves(restored).size());
    }

    @Test
    void explicitlyHomelessSurvivorKeepsItsNation() {
        final Model model = baseline().brave(NativeNation.INCA, null, 1, 1)
                .model();
        final UnitNativeBrave survivor = braves(model).stream()
                .filter(brave -> brave.getHomeSettlementId().isEmpty())
                .findAny().orElseThrow();
        assertEquals("INCA", survivor.getOwner().getName());
        assertEquals(NativeNation.INCA, survivor.getNation());
        assertTrue(survivor.getOwner().isNative());
        // The homeless state is saved explicitly and restored, not guessed.
        model.startGame();
        final Model restored = NativeTurnScene.make(model.save());
        final UnitNativeBrave again = (UnitNativeBrave) restored
                .getUnitById(survivor.getId());
        assertTrue(again.getHomeSettlementId().isEmpty());
        assertEquals(NativeNation.INCA, again.getNation());
    }

    @Test
    void homeOfAnotherNationIsRejectedNotReassigned() {
        assertRejectedBecause(baseline().rawBrave("INCA", 1, false, 1, 1),
                "belongs to another nation");
    }

    @Test
    void unknownHomeIsRejected() {
        assertRejectedBecause(baseline().rawBrave("INCA", 42, false, 1, 1),
                "home is unknown");
    }

    @Test
    void malformedHomeStateIsRejected() {
        final String message = "Invalid saved native brave home";
        // Homeless flag together with a home id.
        assertRejectedBecause(baseline().rawBrave("INCA", 0, true, 1, 1),
                message);
        // Not homeless but no home id.
        assertRejectedBecause(baseline().rawBrave("INCA", null, false, 1, 1),
                message);
        // Missing flag entirely.
        assertRejectedBecause(baseline().rawBrave("INCA", 0, null, 1, 1),
                message);
        // A European unit carrying brave home fields.
        final NativeTurnScene colonist = baseline().dutchColonist(1, 1);
        final List<UnitPo> saved = colonist.po().getUnits();
        saved.get(saved.size() - 1).setNativeHomeless(true);
        assertRejectedBecause(colonist, message);
    }

    @Test
    void braveOfAEuropeanOwnerAndForeignUnitOfANativeOwnerAreRejected() {
        final String message = "Invalid saved native unit owner";
        assertRejectedBecause(baseline().rawBrave("Dutch", 0, false, 1, 1),
                message);
        assertRejectedBecause(baseline().rawUnit(7000, UnitType.COLONIST,
                "INCA", null, null, NativeTurnScene.at(1, 1)), message);
    }

    @Test
    void braveMustStandOnMapLand() {
        // The corner is made ocean in this scene; the baseline stays valid.
        assertRejectedBecause(
                baseline().sea(4, 4).rawBrave("INCA", 0, false, 4, 4),
                "map land only");
    }

    @Test
    void unitIdsStayGloballyUnique() {
        // The first scene unit already uses id 5000.
        assertRejectedBecause(
                baseline().rawUnit(5000, UnitType.COLONIST, "Dutch", null, null,
                        NativeTurnScene.at(3, 3)),
                "duplicate saved unit identity");
    }

    @Test
    void malformedNativeParticipantsAreRejected() {
        final NativeTurnScene king = baseline();
        lastPlayer(king.po()).setWhosKingThisPlayerIs("Dutch");
        assertRejectedBecause(king, "Native players have no European state");

        final NativeTurnScene human = baseline();
        lastPlayer(human.po()).setComputer(false);
        assertRejectedBecause(human, "Native players are computer players");

        final NativeTurnScene renamed = baseline();
        lastPlayer(renamed.po()).setName("Dutch Natives");
        assertRejectedBecause(renamed, "Native player name must be its nation");
    }

    private static PlayerPo lastPlayer(final ModelPo po) {
        final List<PlayerPo> players = po.getGameManager().getPlayers();
        return players.get(players.size() - 1);
    }

    // ---- turn order -------------------------------------------------------

    @Test
    void nativesTakeTurnsFirstInNationOrderWhateverTheSavedOrder() {
        final List<String> european = europeanOrder();
        final List<NativeNation> shuffled = Arrays.asList(NativeNation.TUPI,
                NativeNation.ARAWAK, NativeNation.SIOUX, NativeNation.INCA);
        final List<String> expected = new ArrayList<>(
                List.of("ARAWAK", "INCA", "TUPI", "SIOUX"));
        expected.addAll(european);
        for (final boolean firstInFile : new boolean[] { false, true }) {
            NativeTurnScene scene = NativeTurnScene.island();
            for (int i = 0; i < shuffled.size(); i++) {
                final NativeNation nation = shuffled.get(i);
                scene = scene.settlement(i, nation, true, i * 2 - 4, -2)
                        .brave(nation, i, i * 2 - 4, 0);
                scene = firstInFile ? scene.nativePlayerFirst(nation)
                        : scene.nativePlayer(nation);
            }
            assertEquals(expected, playRound(scene.model()),
                    "First-in-file placement " + firstInFile);
        }
    }

    @Test
    void absentNativesTakeNoTurnAndNoNativeRoundIsUnchanged() {
        final List<String> european = europeanOrder();
        assertTrue(european.stream()
                .noneMatch(name -> Arrays.stream(NativeNation.values())
                        .anyMatch(nation -> nation.name().equals(name))));
        // A settlement without its native participant: nobody acts for it.
        final Model withoutPlayers = NativeTurnScene.island()
                .settlement(0, NativeNation.INCA, true, 0, -2).model();
        assertTrue(withoutPlayers.getPlayers().stream()
                .noneMatch(Player::isNative));
        assertEquals(european, playRound(withoutPlayers));
        // Only the represented nation acts.
        final List<String> onlyInca = playRound(NativeTurnScene.island()
                .settlement(0, NativeNation.INCA, true, 0, -2)
                .nativePlayer(NativeNation.INCA)
                .brave(NativeNation.INCA, 0, 0, 0).model());
        final List<String> expected = new ArrayList<>(List.of("INCA"));
        expected.addAll(european);
        assertEquals(expected, onlyInca);
    }

    @Test
    void everyNativeNationActsOncePerRoundAndTheCalendarAdvancesOnce() {
        final NativeNation[] nations = NativeNation.values();
        final Model model = allNations().model();
        final List<String> round = playRound(model);
        final List<String> expected = Arrays.stream(nations)
                .map(NativeNation::name).collect(Collectors.toList());
        expected.addAll(europeanOrder());
        assertEquals(expected, round,
                "Each nation exactly once, natives first in nation order");
        assertEquals(CALENDAR_START + 1,
                model.getCalendar().getNumberOfPlayedTurns());
        // Each native preparation reset its saved exhausted brave.
        for (final NativeNation nation : nations) {
            assertEquals(FULL_FOOT_MOVEMENT_THIRDS,
                    braveOf(model, nation).getMovementThirds(), nation.name());
        }
    }

    @Test
    void emptyNativeTurnEndsSafelyIntoTheNextParticipant() {
        // A native nation with no unit at all still takes a turn and ends.
        final Model model = NativeTurnScene.island()
                .settlement(0, NativeNation.APACHE, true, 0, -2)
                .nativePlayer(NativeNation.APACHE).model();
        model.startGame();
        assertEquals("APACHE", model.getCurrentPlayer().getName());
        assertTrue(model.getCurrentPlayer().getAllUnits().isEmpty());
        model.endTurn();
        assertFalse(model.getCurrentPlayer().isNative());
        assertTrue(model.isGameRunning());
    }

    @Test
    void nativeRobotTakesItsCallbackAndEndsItsTurnWithoutRaiding()
            throws Exception {
        final Model model = baseline().model();
        final Finished finished = new Finished();
        final CountDownLatch europeanTurn = new CountDownLatch(1);
        model.addListener(finished);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (!event.getPlayer().isNative()) {
                    europeanTurn.countDown();
                }
            }
        });
        final List<NativeRobotPlayer> robots = model.getPlayers().stream()
                .filter(Player::isNative)
                .map(nation -> new NativeRobotPlayer(model, nation, () -> {
                })).collect(Collectors.toList());
        try {
            model.startGame();
            // Each robot receives its own real callback and ends its turn;
            // no test code ends a native turn.
            assertTrue(europeanTurn.await(10, TimeUnit.SECONDS),
                    "The native robots never handed over the round");
            assertEquals(List.of("ARAWAK", "INCA"), finished.snapshot());
            assertEquals(europeanOrder().get(0),
                    model.getCurrentPlayer().getName());
            // No raid: calm braves take only the shared home patrol, so each
            // stays within two squares of its own home (R-0156).
            assertTrue(braveOf(model, NativeNation.ARAWAK).getLocation()
                    .getDistanceChebyshev(NativeTurnScene.at(2, -2)) <= 2);
            assertTrue(braveOf(model, NativeNation.INCA).getLocation()
                    .getDistanceChebyshev(NativeTurnScene.at(0, -2)) <= 2);
        } finally {
            robots.forEach(NativeRobotPlayer::stop);
        }
    }

    // ---- preparation and save boundaries ----------------------------------

    @Test
    void nativePreparationRunsOncePerRoundAndNotAgainAfterContinue() {
        final Model model = baseline().model();
        model.startGame();
        assertEquals("ARAWAK", model.getCurrentPlayer().getName());
        final UnitNativeBrave arawak = braveOf(model, NativeNation.ARAWAK);
        assertEquals(FULL_FOOT_MOVEMENT_THIRDS, arawak.getMovementThirds(),
                "The native turn prepares its braves");
        model.skipUnitTurn(arawak);
        assertEquals(0, arawak.getMovementThirds());

        // Save at the native boundary and Continue: nothing is prepared again.
        final Model restored = NativeTurnScene.make(model.save());
        restored.startGame();
        assertEquals("ARAWAK", restored.getCurrentPlayer().getName());
        final UnitNativeBrave again = braveOf(restored, NativeNation.ARAWAK);
        assertEquals(arawak.getId(), again.getId());
        assertEquals(0, again.getMovementThirds(),
                "Continue must not prepare the native turn twice");
        assertEquals(2, braves(restored).size(), "No brave may be duplicated");
        assertEquals(CALENDAR_START,
                restored.getCalendar().getNumberOfPlayedTurns());

        // The next round prepares it once more, and only once.
        final List<String> rest = completeRound(restored);
        assertEquals("ARAWAK", rest.get(rest.size() - 1));
        assertEquals(1,
                rest.stream().filter(name -> name.equals("INCA")).count());
        assertEquals(FULL_FOOT_MOVEMENT_THIRDS,
                braveOf(restored, NativeNation.ARAWAK).getMovementThirds());
    }

    @Test
    void saveAfterNativeTurnsKeepsEuropeanTurnsAndCalendarOnce() {
        final List<String> european = europeanOrder();
        final Model model = baseline().model();
        model.startGame();
        model.endTurn();
        model.endTurn();
        assertEquals(european.get(0), model.getCurrentPlayer().getName());
        final int turn = model.getCalendar().getNumberOfPlayedTurns();

        final Model restored = NativeTurnScene.make(model.save());
        final Finished finished = new Finished();
        restored.addListener(finished);
        restored.startGame();
        assertEquals(european.get(0), restored.getCurrentPlayer().getName());
        assertEquals(turn, restored.getCalendar().getNumberOfPlayedTurns());
        final List<String> rest = completeRound(restored);

        // The restored round finishes every European turn exactly once, no
        // native repeats, and the next round starts with ARAWAK.
        assertEquals(european, finished.snapshot());
        final List<String> expected = new ArrayList<>(
                european.subList(1, european.size()));
        expected.add("ARAWAK");
        assertEquals(expected, rest);
        assertEquals(2, braves(restored).size());
    }

    // ---- isolation from European rules ------------------------------------

    @Test
    void nativeParticipantHasNoEuropeanPhasesOrRelations() {
        final Model model = baseline().model();
        final Player inca = player(model, "INCA");
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        assertTrue(inca.isNative());
        assertTrue(inca.isComputer());
        assertFalse(inca.isHuman());
        assertFalse(inca.isKing());
        assertFalse(inca.isColonialNation());
        assertEquals(NativeNation.INCA,
                ((PlayerNative) inca).getNativeNation());
        assertTrue(inca.getCongressMembers().isEmpty());
        assertFalse(inca.canTradeInEurope());
        assertTrue(inca.getColonies().isEmpty());
        assertFalse(model.areHostile(inca, dutch));
        assertFalse(model.areHostile(dutch, inca));
        assertFalse(model.areAllied(inca, dutch));
        assertFalse(model.isAttackRestrained(dutch, inca));
        assertTrue(model.getPendingPeaceOffer(dutch, inca).isEmpty());
        assertTrue(model.getPendingTributeDemand(dutch, inca).isEmpty());
        assertTrue(model.getPendingAllianceOffer(dutch, inca).isEmpty());
        // The European table is untouched and has no native row.
        assertDoesNotThrow(() -> model.getEuropeanRelation(dutch, france));
        assertThrows(IllegalArgumentException.class,
                () -> model.getEuropeanRelation(dutch, inca));
        assertTrue(dutch.isColonialNation());
        assertTrue(france.isColonialNation());
        assertFalse(player(model, "Dutch's King").isColonialNation());
    }

    @Test
    void braveNeverFoundsAEuropeanColony() {
        final NativeTurnScene scene = baseline().dutchColonist(-3, 2);
        final List<UnitPo> saved = scene.po().getUnits();
        final int colonistId = saved.get(saved.size() - 1).getId();
        final Model model = scene.model();
        model.startGame();
        final Location site = NativeTurnScene.at(-3, 3);
        // The same empty site is legal for a Dutch colonist.
        assertTrue(model.canFoundColonyAt(model.getUnitById(colonistId), site));
        final UnitNativeBrave brave = braveOf(model, NativeNation.ARAWAK);
        assertEquals("ARAWAK", model.getCurrentPlayer().getName());
        assertFalse(model.canFoundColonyAt(brave, site));
        assertFalse(model.canUnitBuildColony(brave));
        assertTrue(model.getColonies().stream()
                .noneMatch(colony -> colony.getOwner().isNative()));
    }

    @Test
    void braveUsesSharedFootMovement() {
        // The square below the INCA brave is ocean: foot units cannot enter.
        final Model model = baseline().sea(0, 1).model();
        model.startGame();
        assertEquals("ARAWAK", model.getCurrentPlayer().getName());
        model.endTurn();
        assertEquals("INCA", model.getCurrentPlayer().getName());
        final UnitNativeBrave brave = braveOf(model, NativeNation.INCA);
        // Shared ordinary foot movement; strength is the agreed role value and
        // R-0173 lets a brave attack (the legal battle is tested separately).
        assertEquals(UnitType.COLONIST.getSpeed(), brave.getSpeed());
        assertEquals(1.0, brave.getMilitaryStrenght());
        assertTrue(brave.canAttack());
        final Location from = brave.getLocation();
        assertEquals(FULL_FOOT_MOVEMENT_THIRDS, brave.getMovementThirds());
        final Location sea = NativeTurnScene.at(0, 1);
        assertFalse(brave.isPossibleToMoveAt(sea), "A brave never enters sea");
        assertFalse(brave.getAvailableLocations().contains(sea));
        final Location next = Location.of(from.getX() - 1, from.getY());
        assertTrue(brave.isPossibleToMoveAt(next));
        assertTrue(brave.getAvailableLocations().contains(next));
        model.moveUnit(brave, Path.of(List.of(next)));
        assertEquals(next, brave.getLocation());
        assertTrue(brave.getMovementThirds() < FULL_FOOT_MOVEMENT_THIRDS,
                "The shared step cost was spent");
    }

    @Test
    void europeanUnitCannotEnterButMayAttackABraveSquare() {
        // A Dutch musketeer beside the INCA brave.
        final NativeTurnScene scene = baseline().dutchColonist(1, 0);
        final List<UnitPo> saved = scene.po().getUnits();
        final UnitPo armed = saved.get(saved.size() - 1);
        armed.setHoldingGuns(true);
        final Model model = scene.model();
        model.startGame();
        int guard = 0;
        while (!model.getCurrentPlayer().getName().equals("Dutch")) {
            model.endTurn();
            assertTrue(++guard < 20, "The Dutch turn never came");
        }
        final Unit musketeer = model.getUnitById(armed.getId());
        final Location braveSquare = braveOf(model, NativeNation.INCA)
                .getLocation();
        assertTrue(musketeer.canAttack(), "The scene needs an armed unit");
        assertTrue(musketeer.getMovementThirds() > 0,
                "The scene needs a unit that could act");
        assertTrue(musketeer.getLocation().isNeighbor(braveSquare));
        // Ordinary movement still treats the foreign occupant as a blocker.
        assertFalse(musketeer.isPossibleToMoveAt(braveSquare),
                "A foreign unit blocks the square");
        // R-0173: the passive hostility query stays closed (no AI attack), but
        // an explicit decision opens the shared battle without any declaration,
        // and merely preparing it changes neither the unit nor the saved state.
        final int movesBefore = musketeer.getMovementThirds();
        final long randomBefore = model.save().getCombatRandomState();
        final int outcomesBefore = model.save().getNativeRelationOutcomeIds()
                .size();
        assertFalse(musketeer.isPossibleToAttackAt(braveSquare));
        assertTrue(musketeer.prepareAttack(braveSquare, false).isEmpty());
        final AttackOrder order = musketeer.prepareAttack(braveSquare, true)
                .orElseThrow();
        assertFalse(order.isDeclarationRequired());
        assertEquals(movesBefore, musketeer.getMovementThirds());
        assertEquals(randomBefore, model.save().getCombatRandomState());
        assertEquals(outcomesBefore,
                model.save().getNativeRelationOutcomeIds().size());
        assertEquals(braveSquare, order.getBattlefield());
    }

}
