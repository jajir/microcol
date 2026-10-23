package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.LostCityRumorRules.Outcome;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventGoTo;
import org.microcol.model.turnevent.TurnEventLostCityRumor;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Lost-city rumors through real scout movement on a deliberately built free
 * play save: a nine by nine island with an Aztec capital, a Dutch scout, and a
 * saved random state that selects the result. The expected rolls come from an
 * independent copy of the persisted generator, so a seed that yields a wanted
 * result is found without running the code under test.
 */
class LostCityRumorTest {

    private static final double EXACT = 1e-9;

    /** Increment of the persisted generator after each draw. */
    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private static final int SCOUT_ID = 200;

    /** The marker square; the Aztec capital is two squares east of it. */
    private static final Location RUMOR = at(-2, 0);

    /** Second marker square, one step from {@link #RUMOR}. */
    private static final Location SECOND_RUMOR = at(-3, -1);

    /** Land square next to {@link #RUMOR}; the usual start of the scout. */
    private static final Location START = at(-3, 0);

    /** Two squares west of {@link #RUMOR}; a path start before the marker. */
    private static final Location FAR = at(-4, 0);

    /** Square east of {@link #RUMOR}, the step after the marker. */
    private static final Location BEYOND = at(-1, 0);

    /** Land square next to {@link #START} that holds no marker. */
    private static final Location QUIET = at(-3, 1);

    /** Land square next to {@link #RUMOR} and to the lake square. */
    private static final Location LANDING_START = at(-2, 1);

    private static final Consumer<UnitPo> ORDINARY_SCOUT = unit -> {
        unit.setMounted(true);
        unit.setMovementThirds(15);
    };

    private static final Consumer<UnitPo> SEASONED = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
        unit.setMovementThirds(3);
    };

    private static final Consumer<UnitPo> FOOT_SOLDIER = unit -> {
        unit.setHoldingGuns(true);
        unit.setMovementThirds(3);
    };

    private static final Consumer<UnitPo> FOOT = unit -> unit
            .setMovementThirds(3);

    // ---------------------------------------------------------------------
    // The rule itself
    // ---------------------------------------------------------------------

    @Test
    void ordinaryBoundariesAreLowerInclusive() {
        assertEquals(Outcome.EMPTY, LostCityRumorRules.draw(false, 0.0));
        assertEquals(Outcome.EMPTY, LostCityRumorRules.draw(false, 0.39999));
        assertEquals(Outcome.GOLD, LostCityRumorRules.draw(false, 0.40));
        assertEquals(Outcome.GOLD, LostCityRumorRules.draw(false, 0.64999));
        assertEquals(Outcome.RECRUIT, LostCityRumorRules.draw(false, 0.65));
        assertEquals(Outcome.RECRUIT, LostCityRumorRules.draw(false, 0.79999));
        assertEquals(Outcome.EXPLORER_LOST,
                LostCityRumorRules.draw(false, 0.80));
        assertEquals(Outcome.EXPLORER_LOST,
                LostCityRumorRules.draw(false, 0.89999));
        assertEquals(Outcome.BURIAL, LostCityRumorRules.draw(false, 0.90));
        assertEquals(Outcome.BURIAL, LostCityRumorRules.draw(false, 0.99999));
    }

    @Test
    void seasonedBoundariesAreLowerInclusive() {
        assertEquals(Outcome.EMPTY, LostCityRumorRules.draw(true, 0.0));
        assertEquals(Outcome.EMPTY, LostCityRumorRules.draw(true, 0.29999));
        assertEquals(Outcome.GOLD, LostCityRumorRules.draw(true, 0.30));
        assertEquals(Outcome.GOLD, LostCityRumorRules.draw(true, 0.64999));
        assertEquals(Outcome.RECRUIT, LostCityRumorRules.draw(true, 0.65));
        assertEquals(Outcome.RECRUIT, LostCityRumorRules.draw(true, 0.89999));
        assertEquals(Outcome.EXPLORER_LOST,
                LostCityRumorRules.draw(true, 0.90));
        assertEquals(Outcome.EXPLORER_LOST,
                LostCityRumorRules.draw(true, 0.94999));
        assertEquals(Outcome.BURIAL, LostCityRumorRules.draw(true, 0.95));
        assertEquals(Outcome.BURIAL, LostCityRumorRules.draw(true, 0.99999));
    }

    @Test
    void rollOutsideUnitIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> LostCityRumorRules.draw(false, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> LostCityRumorRules.draw(true, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> LostCityRumorRules.gold(1.0));
    }

    @Test
    void distributionsMatchTheAgreedFrequencies() {
        final int steps = 100_000;
        final Map<Outcome, Integer> ordinary = new EnumMap<>(Outcome.class);
        final Map<Outcome, Integer> seasoned = new EnumMap<>(Outcome.class);
        for (int step = 0; step < steps; step++) {
            final double roll = (step + 0.5) / steps;
            ordinary.merge(LostCityRumorRules.draw(false, roll), 1,
                    Integer::sum);
            seasoned.merge(LostCityRumorRules.draw(true, roll), 1,
                    Integer::sum);
        }
        assertEquals(40_000, ordinary.get(Outcome.EMPTY));
        assertEquals(25_000, ordinary.get(Outcome.GOLD));
        assertEquals(15_000, ordinary.get(Outcome.RECRUIT));
        assertEquals(10_000, ordinary.get(Outcome.EXPLORER_LOST));
        assertEquals(10_000, ordinary.get(Outcome.BURIAL));
        assertEquals(30_000, seasoned.get(Outcome.EMPTY));
        assertEquals(35_000, seasoned.get(Outcome.GOLD));
        assertEquals(25_000, seasoned.get(Outcome.RECRUIT));
        assertEquals(5_000, seasoned.get(Outcome.EXPLORER_LOST));
        assertEquals(5_000, seasoned.get(Outcome.BURIAL));
    }

    @Test
    void goldCoversExactlyTwoHundredThroughFiveHundredInclusive() {
        assertEquals(200, LostCityRumorRules.gold(0.0));
        assertEquals(201, LostCityRumorRules.gold(1.0 / 301 + 1e-9));
        assertEquals(499, LostCityRumorRules.gold(299.0 / 301 + 1e-9));
        assertEquals(500, LostCityRumorRules.gold(300.0 / 301 + 1e-9));
        assertEquals(500, LostCityRumorRules.gold(0.9999999999));
    }

    @Test
    void deSotoTurnsBothBadResultsIntoEmptyOnly() {
        assertEquals(Outcome.EMPTY,
                LostCityRumorRules.withDeSoto(Outcome.EXPLORER_LOST, true));
        assertEquals(Outcome.EMPTY,
                LostCityRumorRules.withDeSoto(Outcome.BURIAL, true));
        assertEquals(Outcome.GOLD,
                LostCityRumorRules.withDeSoto(Outcome.GOLD, true));
        assertEquals(Outcome.RECRUIT,
                LostCityRumorRules.withDeSoto(Outcome.RECRUIT, true));
        assertEquals(Outcome.EXPLORER_LOST,
                LostCityRumorRules.withDeSoto(Outcome.EXPLORER_LOST, false));
        assertEquals(Outcome.BURIAL,
                LostCityRumorRules.withDeSoto(Outcome.BURIAL, false));
    }

    // ---------------------------------------------------------------------
    // Real scout movement
    // ---------------------------------------------------------------------

    @Test
    void theIndependentGeneratorMatchesTheSavedRandomState() {
        final long seed = 123_456_789L;
        final Model model = ordinaryScoutScene(seed).toModel();
        assertEquals(roll(seed, 1), model.nextCombatRandom(), 0.0);
        assertEquals(roll(seed, 2), model.nextCombatRandom(), 0.0);
        assertEquals(roll(seed, 3), model.nextCombatRandom(), 0.0);
    }

    @Test
    void everyOutcomeOfBothScoutsIsAppliedThroughARealStep() {
        for (final boolean seasoned : new boolean[] { false, true }) {
            for (final Outcome expected : Outcome.values()) {
                verifyRealStep(seasoned, expected);
            }
        }
    }

    /**
     * A Seasoned Scout bought in Europe takes the seasoned draw once placed on
     * the square before the marker; the seed draws gold only for that scout.
     */
    @Test
    void purchasedSeasonedScoutDrawsTheSpecialistRumorAndKeepsItsIdentity() {
        final long seed = seedFor(true, Outcome.GOLD);
        final Model model = scene(seed, RUMOR)
                .gold(DUTCH, UnitType.SEASONED_SCOUT.getEuropePrice())
                .toModel();
        final Player dutch = dutch(model);
        dutch.buy(UnitType.SEASONED_SCOUT);
        final UnitFreeColonist scout = (UnitFreeColonist) dutch.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.SEASONED_SCOUT)
                .findFirst().orElseThrow();
        scout.placeToLocation(START);
        scout.setActionPoints(scout.getSpeed());
        final int gold = dutch.getGold();

        step(model, scout, RUMOR);

        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
        final List<TurnEventLostCityRumor> notices = notices(model);
        assertEquals(1, notices.size());
        final TurnEventLostCityRumor notice = notices.get(0);
        assertEquals(Outcome.GOLD, notice.getOutcome());
        assertEquals(scout.getId(), notice.getExplorerId());
        final int amount = 200 + (int) (roll(seed, 2) * 301);
        assertEquals(amount, notice.getGold());
        assertEquals(gold + amount, dutch.getGold());
        assertSame(UnitType.SEASONED_SCOUT, scout.getType());
    }

    private void verifyRealStep(final boolean seasoned,
            final Outcome expected) {
        final long seed = seedFor(seasoned, expected);
        final String label = (seasoned ? "seasoned " : "ordinary ") + expected
                + " seed " + seed;
        final Model model = scene(seed, RUMOR).unit(DUTCH,
                seasoned ? UnitType.SEASONED_SCOUT : UnitType.COLONIST, START,
                seasoned ? SEASONED : ORDINARY_SCOUT).toModel();
        final Player dutch = dutch(model);
        final int gold = dutch.getGold();
        final int units = model.getAllUnits().size();

        step(model, scout(model), RUMOR);

        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR), label);
        final List<TurnEventLostCityRumor> notices = notices(model);
        assertEquals(1, notices.size(), label);
        final TurnEventLostCityRumor notice = notices.get(0);
        assertEquals(expected, notice.getOutcome(), label);
        assertEquals(SCOUT_ID, notice.getExplorerId(), label);
        assertFalse(notice.isDeSotoAverted(), label);
        assertEquals(afterDraws(seed, expected == Outcome.GOLD ? 2 : 1),
                randomState(model), label);
        assertEquals(0.0, model.getNativeTribalAnger(dutch, NativeNation.AZTEC)
                - (expected == Outcome.BURIAL ? 30 : 0), EXACT, label);
        switch (expected) {
        case GOLD:
            final int amount = 200 + (int) (roll(seed, 2) * 301);
            assertEquals(amount, notice.getGold(), label);
            assertEquals(gold + amount, dutch.getGold(), label);
            assertEquals(units, model.getAllUnits().size(), label);
            assertEquals(RUMOR, scout(model).getLocation(), label);
            break;
        case RECRUIT:
            assertEquals(0, notice.getGold(), label);
            assertEquals(gold, dutch.getGold(), label);
            assertEquals(units + 1, model.getAllUnits().size(), label);
            assertEquals(2, unitsAt(model, RUMOR), label);
            assertEquals(RUMOR, scout(model).getLocation(), label);
            final Unit recruit = model.getAllUnits().stream()
                    .filter(unit -> unit.getId() != SCOUT_ID
                            && unit.isAtPlaceLocation()
                            && unit.getLocation().equals(RUMOR))
                    .findFirst().orElseThrow();
            assertEquals(UnitType.COLONIST, recruit.getType(), label);
            assertSame(dutch, recruit.getOwner(), label);
            assertFalse(((UnitFreeColonist) recruit).isScout(), label);
            assertTrue(recruit.getId() > SCOUT_ID, label);
            break;
        case EXPLORER_LOST:
            assertEquals(gold, dutch.getGold(), label);
            assertEquals(units - 1, model.getAllUnits().size(), label);
            assertTrue(model.getAllUnits().stream()
                    .noneMatch(unit -> unit.getId() == SCOUT_ID), label);
            assertEquals(0, unitsAt(model, RUMOR), label);
            break;
        case BURIAL:
            assertEquals(gold, dutch.getGold(), label);
            assertEquals(units, model.getAllUnits().size(), label);
            assertEquals(List.of(1L),
                    model.save().getNativeRelationOutcomeIds(), label);
            assertEquals(0.0,
                    model.getNativeLocalAlarm(dutch, settlement(model)), EXACT,
                    label);
            assertEquals(0.0,
                    model.getNativeTribalAnger(model.getPlayerByName(FRANCE),
                            NativeNation.AZTEC),
                    EXACT, label);
            break;
        default:
            assertEquals(gold, dutch.getGold(), label);
            assertEquals(units, model.getAllUnits().size(), label);
            assertEquals(RUMOR, scout(model).getLocation(), label);
            assertTrue(model.save().getNativeRelationOutcomeIds().isEmpty(),
                    label);
            break;
        }
    }

    @Test
    void goldEndpointsAreAwardedThroughTheModel() {
        for (final boolean seasoned : new boolean[] { false, true }) {
            for (final int wanted : new int[] { 200, 500 }) {
                final long seed = find(candidate -> LostCityRumorRules
                        .draw(seasoned, roll(candidate, 1)) == Outcome.GOLD
                        && LostCityRumorRules
                                .gold(roll(candidate, 2)) == wanted)
                        .orElseThrow();
                final Model model = scene(seed, RUMOR).unit(DUTCH,
                        seasoned ? UnitType.SEASONED_SCOUT : UnitType.COLONIST,
                        START, seasoned ? SEASONED : ORDINARY_SCOUT).toModel();
                final int gold = dutch(model).getGold();
                step(model, scout(model), RUMOR);
                assertEquals(wanted, notices(model).get(0).getGold());
                assertEquals(gold + wanted, dutch(model).getGold());
            }
        }
    }

    @Test
    void aResolvedRumorNeverPaysAgainOnRevisitOrAtTheEndOfTheTurn() {
        final Model model = ordinaryScoutScene(seedFor(false, Outcome.GOLD))
                .toModel();
        final Unit scout = scout(model);
        step(model, scout, RUMOR);
        final int gold = dutch(model).getGold();
        final long random = randomState(model);

        step(model, scout, BEYOND);
        step(model, scout, RUMOR);
        assertEquals(gold, dutch(model).getGold());
        assertEquals(random, randomState(model));
        assertEquals(1, notices(model).size());

        endRound(model);
        assertTrue(notices(model).isEmpty());
        assertEquals(gold, dutch(model).getGold());
        final long afterRound = randomState(model);
        step(model, scout, BEYOND);
        step(model, scout, RUMOR);
        assertEquals(gold, dutch(model).getGold());
        assertEquals(afterRound, randomState(model));
        assertTrue(notices(model).isEmpty());
        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
    }

    @Test
    void savedGameKeepsMarkerResultAndNoticeAndReplaysNothing() {
        final Model model = ordinaryScoutScene(seedFor(false, Outcome.GOLD))
                .toModel();
        step(model, scout(model), RUMOR);
        final TurnEventLostCityRumor notice = notices(model).get(0);
        final int gold = dutch(model).getGold();
        final long random = randomState(model);

        final Model restored = reload(model);
        assertFalse(restored.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(gold, dutch(restored).getGold());
        assertEquals(random, randomState(restored));
        assertEquals(1, notices(restored).size());
        assertEquals(notice.getGold(), notices(restored).get(0).getGold());
        assertEquals(Outcome.GOLD, notices(restored).get(0).getOutcome());

        step(restored, scout(restored), BEYOND);
        step(restored, scout(restored), RUMOR);
        endRound(restored);
        assertEquals(gold, dutch(restored).getGold());
        assertTrue(notices(restored).isEmpty());
    }

    @Test
    void aGameSavedBeforeTheEntryResolvesTheSameResultAfterContinue() {
        for (final Outcome outcome : Outcome.values()) {
            final long seed = seedFor(false, outcome);
            final Model direct = ordinaryScoutScene(seed).toModel();
            final Model continued = reload(ordinaryScoutScene(seed).toModel());
            assertTrue(continued.getMap().hasLostCityRumorAt(RUMOR), "" + seed);
            assertEquals(seed, randomState(continued));
            step(direct, scout(direct), RUMOR);
            step(continued, scout(continued), RUMOR);
            assertEquals(outcome, notices(continued).get(0).getOutcome());
            assertEquals(notices(direct).get(0).getGold(),
                    notices(continued).get(0).getGold());
            assertEquals(randomState(direct), randomState(continued));
            assertEquals(dutch(direct).getGold(), dutch(continued).getGold());
            assertEquals(direct.getAllUnits().size(),
                    continued.getAllUnits().size());
        }
    }

    @Test
    void standingOnAMarkerIsNotAnEntryAndLoadingOrTurnsNeverResolveIt() {
        final Model model = scene(seedFor(false, Outcome.GOLD), RUMOR)
                .unit(DUTCH, UnitType.COLONIST, RUMOR, ORDINARY_SCOUT)
                .toModel();
        final long random = randomState(model);
        final int gold = dutch(model).getGold();
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));

        final Model restored = reload(model);
        assertTrue(restored.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(random, randomState(restored));
        endRound(restored);
        endRound(restored);
        assertTrue(restored.getMap().hasLostCityRumorAt(RUMOR));
        assertTrue(notices(restored).isEmpty());
        assertEquals(gold, dutch(restored).getGold());

        // Leaving and entering again is a legal entry and resolves it once.
        step(restored, scout(restored), BEYOND);
        step(restored, scout(restored), RUMOR);
        assertFalse(restored.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(1, notices(restored).size());
    }

    // ---------------------------------------------------------------------
    // Illegal and stale entries change nothing
    // ---------------------------------------------------------------------

    @Test
    void aSoldierStepOntoTheMarkerLeavesMarkerRandomStateAndTreasury() {
        final long seed = seedFor(false, Outcome.GOLD);
        final Model model = scene(seed, RUMOR)
                .unit(DUTCH, UnitType.COLONIST, START, FOOT_SOLDIER).toModel();
        final int gold = dutch(model).getGold();
        step(model, scout(model), RUMOR);
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(seed, randomState(model));
        assertEquals(gold, dutch(model).getGold());
        assertTrue(notices(model).isEmpty());
        assertEquals(RUMOR, scout(model).getLocation());
    }

    @Test
    void anUnarmedUnmountedColonistIsNoScout() {
        final long seed = seedFor(false, Outcome.GOLD);
        final Model model = scene(seed, RUMOR)
                .unit(DUTCH, UnitType.COLONIST, START, FOOT).toModel();
        step(model, scout(model), RUMOR);
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(seed, randomState(model));
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void aStepOntoASquareWithoutMarkerLeavesTheOtherMarkerAndTheRandomState() {
        final long seed = seedFor(false, Outcome.GOLD);
        final Model model = ordinaryScoutScene(seed).toModel();
        step(model, scout(model), QUIET);
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(seed, randomState(model));
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void staleEntriesLeaveTheWholeSavedStateUnchanged() {
        final Model model = ordinaryScoutScene(seedFor(false, Outcome.GOLD))
                .toModel();
        final Unit scout = scout(model);
        final String before = snapshot(model);

        // The scout does not stand on the marker square it is reported for.
        model.resolveLostCityRumor(scout, RUMOR);
        assertEquals(before, snapshot(model));

        // The scout stands on its own square, but it holds no marker.
        model.resolveLostCityRumor(scout, START);
        assertEquals(before, snapshot(model));
    }

    @Test
    void aUnitThatLeftTheGameResolvesNothingEvenOnAMarkerSquare() {
        final Model model = scene(seedFor(false, Outcome.GOLD), RUMOR)
                .unit(DUTCH, UnitType.COLONIST, RUMOR, ORDINARY_SCOUT)
                .toModel();
        final Unit scout = scout(model);
        model.destroyUnit(scout);
        assertFalse(model.isUnitInGame(scout));
        final String before = snapshot(model);

        // The removed unit still reports the square and the marker is there.
        assertTrue(scout.isAtPlaceLocation());
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
        model.resolveLostCityRumor(scout, RUMOR);

        assertEquals(before, snapshot(model));
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
    }

    @Test
    void aGoldResultThatCannotBePaidIsRefusedWholeAndCanBeRetried() {
        final long seed = seedFor(false, Outcome.GOLD);
        final Model model = ordinaryScoutScene(seed)
                .gold(DUTCH, Integer.MAX_VALUE - 100).toModel();
        final Unit scout = scout(model);

        step(model, scout, RUMOR);
        assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(seed, randomState(model));
        assertEquals(Integer.MAX_VALUE - 100, dutch(model).getGold());
        assertTrue(notices(model).isEmpty());
        assertEquals(RUMOR, scout.getLocation());

        // With room in the treasury the same entry gives the same result.
        dutch(model).setGold(1000);
        step(model, scout, BEYOND);
        step(model, scout, RUMOR);
        final int amount = 200 + (int) (roll(seed, 2) * 301);
        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(1000 + amount, dutch(model).getGold());
        assertEquals(Outcome.GOLD, notices(model).get(0).getOutcome());
    }

    @Test
    void aResultThatNeedsNoGoldIsNotBlockedByAFullTreasury() {
        final long seed = seedFor(false, Outcome.EMPTY);
        final Model model = ordinaryScoutScene(seed)
                .gold(DUTCH, Integer.MAX_VALUE).toModel();
        step(model, scout(model), RUMOR);
        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(Outcome.EMPTY, notices(model).get(0).getOutcome());
        assertEquals(Integer.MAX_VALUE, dutch(model).getGold());
    }

    @Test
    void aRecruitWithoutAFreeUnitIdIsRefusedButOtherResultsAreNot() {
        final long recruit = seedFor(false, Outcome.RECRUIT);
        final Model refused = exhaustedIds(ordinaryScoutScene(recruit))
                .toModel();
        final int units = refused.getAllUnits().size();
        step(refused, scout(refused), RUMOR);
        assertTrue(refused.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(recruit, randomState(refused));
        assertTrue(notices(refused).isEmpty());
        assertEquals(units, refused.getAllUnits().size());
        assertEquals(RUMOR, scout(refused).getLocation());

        final long gold = seedFor(false, Outcome.GOLD);
        final Model paid = exhaustedIds(ordinaryScoutScene(gold)).toModel();
        step(paid, scout(paid), RUMOR);
        assertFalse(paid.getMap().hasLostCityRumorAt(RUMOR));
        assertEquals(Outcome.GOLD, notices(paid).get(0).getOutcome());
    }

    // ---------------------------------------------------------------------
    // Multi-step movement, landing and automated go-to orders
    // ---------------------------------------------------------------------

    @Test
    void aPathContinuesAfterASurvivedRumorAndStopsWhenTheExplorerIsLost() {
        for (final Outcome outcome : Outcome.values()) {
            final long seed = seedFor(false, outcome);
            final Model model = scene(seed, RUMOR)
                    .unit(DUTCH, UnitType.COLONIST, FAR, ORDINARY_SCOUT)
                    .toModel();
            final Recorder recorder = record(model);
            step(model, scout(model), START, RUMOR, BEYOND);
            if (outcome == Outcome.EXPLORER_LOST) {
                assertEquals(List.of(START, RUMOR), recorder.ends, "" + seed);
                assertFalse(model.isUnitInGame(recorder.unit));
            } else {
                assertEquals(List.of(START, RUMOR, BEYOND), recorder.ends,
                        "" + outcome);
                assertEquals(BEYOND, scout(model).getLocation());
            }
            assertEquals(1, recorder.finished, "" + outcome);
            assertEquals(1, notices(model).size(), "" + outcome);
        }
    }

    @Test
    void aWalkAsFarAsPossibleNeverMovesALostExplorerAgain() {
        final long seed = seedFor(false, Outcome.EXPLORER_LOST);
        final Model model = scene(seed, RUMOR)
                .unit(DUTCH, UnitType.COLONIST, FAR, ORDINARY_SCOUT).toModel();
        final Recorder recorder = record(model);
        model.moveUnitAsFarAsPossible(scout(model),
                Path.of(List.of(START, RUMOR, BEYOND)));
        assertEquals(List.of(START, RUMOR), recorder.ends);
        assertEquals(1, recorder.finished);
        assertFalse(model.isUnitInGame(recorder.unit));
        assertEquals(1, notices(model).size());
    }

    @Test
    void aScoutLandingFromAShipResolvesTheRumorOnTheLandingSquare() {
        for (final Outcome outcome : new Outcome[] { Outcome.GOLD,
                Outcome.EXPLORER_LOST }) {
            final long seed = seedFor(false, outcome);
            final Model model = scene(seed, RUMOR)
                    .unit(DUTCH, UnitType.GALLEON, SEA_ONE, FOOT).unit(DUTCH,
                            UnitType.COLONIST, LANDING_START, ORDINARY_SCOUT)
                    .toModel();
            final UnitWithCargo ship = (UnitWithCargo) model.getUnitById(200);
            final Unit scout = model.getUnitById(201);
            final int gold = dutch(model).getGold();

            scout.embarkFromLocation(
                    ship.getCargo().getEmptyCargoSlot().orElseThrow());
            assertTrue(scout.isAtCargoSlot());
            assertTrue(model.getMap().hasLostCityRumorAt(RUMOR));
            assertEquals(seed, randomState(model));
            assertTrue(notices(model).isEmpty());

            scout.disembarkToLocation(RUMOR);
            assertFalse(model.getMap().hasLostCityRumorAt(RUMOR), "" + outcome);
            assertEquals(outcome, notices(model).get(0).getOutcome());
            if (outcome == Outcome.GOLD) {
                assertTrue(model.isUnitInGame(scout));
                assertEquals(RUMOR, scout.getLocation());
                assertEquals(gold + notices(model).get(0).getGold(),
                        dutch(model).getGold());
            } else {
                assertFalse(model.isUnitInGame(scout));
                assertTrue(model.isUnitInGame(ship));
            }
        }
    }

    @Test
    void aGoToOrderEndingOnTheMarkerResolvesItAndALostExplorerKeepsNoOrder() {
        for (final Outcome outcome : new Outcome[] { Outcome.EXPLORER_LOST,
                Outcome.EMPTY, Outcome.GOLD }) {
            final long seed = seedFor(false, outcome);
            final Model model = scene(seed, RUMOR)
                    .unit(DUTCH, UnitType.COLONIST, FAR, ORDINARY_SCOUT)
                    .toModel();
            final Unit scout = scout(model);

            model.issueGoTo(scout, RUMOR);

            assertFalse(model.getMap().hasLostCityRumorAt(RUMOR), "" + outcome);
            assertEquals(1, notices(model).size(), "" + outcome);
            assertTrue(
                    model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                            .noneMatch(TurnEventGoTo.class::isInstance),
                    "" + outcome);
            if (outcome == Outcome.EXPLORER_LOST) {
                assertFalse(model.isUnitInGame(scout));
            } else {
                assertEquals(RUMOR, scout.getLocation());
                assertFalse(model.hasGoToOrder(scout));
            }
            // The next round starts without touching a lost or finished order.
            endRound(model);
            assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
        }
    }

    // ---------------------------------------------------------------------
    // Native burial
    // ---------------------------------------------------------------------

    @Test
    void burialAngersTheNationOfTheNearestSettlementWhateverTheListOrder() {
        final long seed = seedFor(false, Outcome.BURIAL);
        // The Inca capital is one square from the marker; the Aztec capital
        // that is listed first is two squares away.
        final Model model = withSettlement(ordinaryScoutScene(seed), 1,
                NativeNation.INCA, true, QUIET).toModel();
        final Player dutch = dutch(model);

        step(model, scout(model), RUMOR);

        assertEquals(30.0, model.getNativeTribalAnger(dutch, NativeNation.INCA),
                EXACT);
        assertEquals(0.0, model.getNativeTribalAnger(dutch, NativeNation.AZTEC),
                EXACT);
        assertEquals(0.0, model.getNativeLocalAlarm(dutch,
                model.getNativeSettlements().get(1)), EXACT);
        assertEquals(afterDraws(seed, 1), randomState(model));
        assertEquals(List.of(1L), model.save().getNativeRelationOutcomeIds());
        assertEquals(NativeNation.INCA, notices(model).get(0).getNation());
    }

    @Test
    void equallyNearSettlementsAreChosenUniformlyAmongSettlementsNotNations() {
        // Two Aztec and one Inca settlement, all two squares from the marker,
        // in the list order Aztec, Inca, Aztec.
        final List<Long> seeds = new ArrayList<>();
        for (long seed = 0; seeds.size() < 60; seed++) {
            if (LostCityRumorRules.draw(false,
                    roll(seed, 1)) == Outcome.BURIAL) {
                seeds.add(seed);
            }
        }
        final int[] chosen = new int[3];
        for (final long seed : seeds) {
            final NativeRelationsScene scene = ordinaryScoutScene(seed);
            withSettlement(scene, 1, NativeNation.INCA, true, FAR);
            withSettlement(scene, 2, NativeNation.AZTEC, false, at(-2, -2));
            final Model model = scene.toModel();
            final Player dutch = dutch(model);

            step(model, scout(model), RUMOR);

            final int index = (int) (roll(seed, 2) * 3);
            chosen[index]++;
            final NativeNation expected = index == 1 ? NativeNation.INCA
                    : NativeNation.AZTEC;
            final NativeNation other = index == 1 ? NativeNation.AZTEC
                    : NativeNation.INCA;
            assertEquals(30.0, model.getNativeTribalAnger(dutch, expected),
                    EXACT, "seed " + seed);
            assertEquals(0.0, model.getNativeTribalAnger(dutch, other), EXACT,
                    "seed " + seed);
            assertEquals(afterDraws(seed, 2), randomState(model),
                    "seed " + seed);
            assertEquals(expected, notices(model).get(0).getNation());
            // The choice is saved once: neither Continue nor a turn redraws it.
            final Model restored = reload(model);
            assertEquals(30.0, restored.getNativeTribalAnger(
                    restored.getPlayerByName(DUTCH), expected), EXACT);
            assertEquals(randomState(model), randomState(restored));
        }
        assertTrue(chosen[0] > 0 && chosen[1] > 0 && chosen[2] > 0,
                "every tied settlement is reachable: "
                        + List.of(chosen[0], chosen[1], chosen[2]));
    }

    @Test
    void burialWithoutAnyNativeSettlementAngersNobodyAndDrawsNoTie() {
        final long seed = seedFor(false, Outcome.BURIAL);
        final NativeRelationsScene scene = ordinaryScoutScene(seed);
        scene.toPo().setNativeSettlements(new ArrayList<>());
        scene.toPo().setNativeLocalAlarm(
                NativeRelations.freshLocalAlarm(new ArrayList<>()));
        final Model model = scene.toModel();

        step(model, scout(model), RUMOR);

        for (final NativeNation nation : NativeNation.values()) {
            assertEquals(0.0, model.getNativeTribalAnger(dutch(model), nation),
                    EXACT);
        }
        final TurnEventLostCityRumor notice = notices(model).get(0);
        assertEquals(Outcome.BURIAL, notice.getOutcome());
        assertNull(notice.getNation());
        assertTrue(model.save().getNativeRelationOutcomeIds().isEmpty());
        assertEquals(afterDraws(seed, 1), randomState(model));
        assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
    }

    @Test
    void everyBurialGetsItsOwnOutcomeIdBesideIdsOfOtherProducers() {
        final long seed = find(candidate -> LostCityRumorRules.draw(false,
                roll(candidate, 1)) == Outcome.BURIAL
                && LostCityRumorRules.draw(false,
                        roll(candidate, 2)) == Outcome.BURIAL)
                .orElseThrow();
        final NativeRelationsScene scene = scene(seed, RUMOR, SECOND_RUMOR)
                .unit(DUTCH, UnitType.COLONIST, START, ORDINARY_SCOUT);
        scene.toPo().setNativeRelationOutcomeIds(List.of(1L, 7L));
        final Model model = scene.toModel();

        step(model, scout(model), RUMOR, SECOND_RUMOR);

        assertEquals(List.of(1L, 7L, 8L, 9L),
                model.save().getNativeRelationOutcomeIds());
        assertEquals(60.0,
                model.getNativeTribalAnger(dutch(model), NativeNation.AZTEC),
                EXACT);
        assertEquals(2, notices(model).size());
        // Continue and a later turn apply neither burial again.
        final Model restored = reload(model);
        endRound(restored);
        assertEquals(List.of(1L, 7L, 8L, 9L),
                restored.save().getNativeRelationOutcomeIds());
    }

    @Test
    void deSotoTurnsBothBadResultsIntoEmptyAndLeavesTheGoodOnes() {
        for (final Outcome drawn : new Outcome[] { Outcome.EXPLORER_LOST,
                Outcome.BURIAL }) {
            final long seed = seedFor(false, drawn);
            final Model model = withDeSoto(ordinaryScoutScene(seed)).toModel();
            final int units = model.getAllUnits().size();
            step(model, scout(model), RUMOR);
            final TurnEventLostCityRumor notice = notices(model).get(0);
            assertEquals(Outcome.EMPTY, notice.getOutcome(), "" + drawn);
            assertTrue(notice.isDeSotoAverted(), "" + drawn);
            assertTrue(model.isUnitInGame(scout(model)), "" + drawn);
            assertEquals(units, model.getAllUnits().size(), "" + drawn);
            assertEquals(0.0, model.getNativeTribalAnger(dutch(model),
                    NativeNation.AZTEC), EXACT, "" + drawn);
            assertTrue(model.save().getNativeRelationOutcomeIds().isEmpty());
            assertFalse(model.getMap().hasLostCityRumorAt(RUMOR));
            assertEquals(afterDraws(seed, 1), randomState(model));
        }
        for (final Outcome kept : new Outcome[] { Outcome.GOLD, Outcome.RECRUIT,
                Outcome.EMPTY }) {
            final Model model = withDeSoto(
                    ordinaryScoutScene(seedFor(false, kept))).toModel();
            step(model, scout(model), RUMOR);
            assertEquals(kept, notices(model).get(0).getOutcome());
            assertFalse(notices(model).get(0).isDeSotoAverted(), "" + kept);
        }
    }

    @Test
    void deSotoAlsoGuardsTheSeasonedScout() {
        final long seed = seedFor(true, Outcome.EXPLORER_LOST);
        final Model model = withDeSoto(scene(seed, RUMOR).unit(DUTCH,
                UnitType.SEASONED_SCOUT, START, SEASONED)).toModel();
        step(model, scout(model), RUMOR);
        assertEquals(Outcome.EMPTY, notices(model).get(0).getOutcome());
        assertTrue(model.isUnitInGame(scout(model)));
    }

    // ---------------------------------------------------------------------
    // Fixture and oracle
    // ---------------------------------------------------------------------

    /** The persisted generator, copied so seeds are chosen independently. */
    private static double roll(final long seed, final int draw) {
        long state = seed;
        long z = 0;
        for (int index = 0; index < draw; index++) {
            state += GOLDEN;
            z = state;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z ^= z >>> 31;
        }
        return (z >>> 11) * 0x1.0p-53;
    }

    private static long afterDraws(final long seed, final int draws) {
        return seed + draws * GOLDEN;
    }

    private static OptionalLong find(final LongPredicate wanted) {
        for (long seed = 0; seed < 5_000_000L; seed++) {
            if (wanted.test(seed)) {
                return OptionalLong.of(seed);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * Seed whose first draw gives the outcome for this kind of scout. A seed
     * where the other kind of scout would draw a different outcome is
     * preferred, so a model that used the wrong table fails the test.
     */
    private static long seedFor(final boolean seasoned, final Outcome outcome) {
        final OptionalLong distinct = find(seed -> {
            final double first = roll(seed, 1);
            return LostCityRumorRules.draw(seasoned, first) == outcome
                    && LostCityRumorRules.draw(!seasoned, first) != outcome;
        });
        return distinct.orElseGet(() -> find(seed -> LostCityRumorRules
                .draw(seasoned, roll(seed, 1)) == outcome).orElseThrow());
    }

    private static NativeRelationsScene scene(final long seed,
            final Location... rumors) {
        final NativeRelationsScene scene = island(NativeNation.AZTEC);
        scene.toPo().setCombatRandomState(seed);
        scene.toPo().getMap().setRumors(Set.of(rumors));
        return scene;
    }

    private static NativeRelationsScene ordinaryScoutScene(final long seed) {
        return scene(seed, RUMOR).unit(DUTCH, UnitType.COLONIST, START,
                ORDINARY_SCOUT);
    }

    /**
     * A saved Dutch unit with the highest legal ID leaves no ID to allocate.
     */
    private static NativeRelationsScene exhaustedIds(
            final NativeRelationsScene scene) {
        scene.unit(DUTCH, UnitType.COLONIST, at(4, 4), FOOT);
        final List<UnitPo> units = scene.toPo().getUnits();
        units.get(units.size() - 1).setId(Integer.MAX_VALUE - 1);
        return scene;
    }

    private static NativeRelationsScene withSettlement(
            final NativeRelationsScene scene, final int id,
            final NativeNation nation, final boolean capital,
            final Location at) {
        final NativeSettlementPo settlement = new NativeSettlementPo();
        settlement.setId(id);
        settlement.setNation(nation);
        settlement.setSettlementClass(nation.getSettlementClass());
        settlement.setCapital(capital);
        settlement.setTreasury(capital ? 2000 : 1000);
        settlement.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        settlement.setOfferedGoods(List.of());
        settlement.setWantedGoods(List.of());
        settlement.setTrade(List.of());
        settlement.setLocation(at);
        final List<NativeSettlementPo> all = new ArrayList<>(
                scene.toPo().getNativeSettlements());
        all.add(settlement);
        scene.toPo().setNativeSettlements(all);
        scene.toPo().setNativeLocalAlarm(NativeRelations.freshLocalAlarm(all));
        return scene;
    }

    private static NativeRelationsScene withDeSoto(
            final NativeRelationsScene scene) {
        final CongressPo congress = scene.toPo().getGameManager().getPlayers()
                .stream().filter(player -> DUTCH.equals(player.getName()))
                .findFirst().orElseThrow().getCongress();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(List.of(Founder.HERNANDO_DE_SOTO));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(null);
        congress.setGeneration(2);
        congress.setJonesDeliveryPending(false);
        return scene;
    }

    private static Unit scout(final Model model) {
        return model.getUnitById(SCOUT_ID);
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    private static void step(final Model model, final Unit unit,
            final Location... steps) {
        model.moveUnit(unit, Path.of(List.of(steps)));
    }

    private static List<TurnEventLostCityRumor> notices(final Model model) {
        return model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                .filter(TurnEventLostCityRumor.class::isInstance)
                .map(TurnEventLostCityRumor.class::cast)
                .collect(Collectors.toList());
    }

    private static long unitsAt(final Model model, final Location at) {
        return model.getAllUnits().stream()
                .filter(unit -> unit.isAtPlaceLocation()
                        && unit.getLocation().equals(at))
                .count();
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState().longValue();
    }

    private static String snapshot(final Model model) {
        return new com.google.gson.Gson().toJson(model.save());
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Ends turns until the Dutch player is current again: one full round. */
    private static void endRound(final Model model) {
        final Player dutch = dutch(model);
        do {
            model.endTurn();
        } while (model.getCurrentPlayer() != dutch);
    }

    private static Recorder record(final Model model) {
        final Recorder recorder = new Recorder(scout(model));
        model.addListener(recorder);
        return recorder;
    }

    /** Counts the committed steps and finished moves of one unit. */
    private static final class Recorder extends ModelListenerAdapter {
        private final Unit unit;
        private final List<Location> ends = new ArrayList<>();
        private int finished;

        private Recorder(final Unit unit) {
            this.unit = unit;
        }

        @Override
        public void onUnitMovedStepFinished(
                final UnitMovedStepFinishedEvent event) {
            if (event.getUnit() == unit) {
                ends.add(event.getEnd());
            }
        }

        @Override
        public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
            if (event.getUnit() == unit) {
                finished++;
            }
        }
    }

}
