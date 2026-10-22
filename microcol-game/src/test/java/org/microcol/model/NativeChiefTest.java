package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeChiefOutcome.DEATH;
import static org.microcol.model.NativeChiefOutcome.GOLD;
import static org.microcol.model.NativeChiefOutcome.INFORMATION;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.SCOUT;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongPredicate;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.NativeChiefEncounterPo;
import org.microcol.model.store.UnitPo;

/**
 * The native chief encounter (R-0158): exact distributions for every mood and
 * both scouts, the seasoned halving, the gold cap, the one record per nation
 * and settlement, cancelling and stale terms that change nothing, the death
 * cleanup, and the saved record that persists. Random rolls come from the saved
 * random state; the seeds are searched so each test controls its first draw.
 */
class NativeChiefTest {

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    /** Seasoned scout: a free colonist of its own type. */
    private static final Consumer<UnitPo> SEASONED_FIELDS = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

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

    private static OptionalLong find(final LongPredicate wanted) {
        for (long seed = 0; seed < 5_000_000L; seed++) {
            if (wanted.test(seed)) {
                return OptionalLong.of(seed);
            }
        }
        return OptionalLong.empty();
    }

    /** Seed whose first draw gives the outcome for this mood and scout. */
    private static long seedFor(final NativeHostility hostility,
            final boolean seasoned, final NativeChiefOutcome outcome) {
        return find(seed -> NativeChiefRules.draw(hostility, seasoned,
                roll(seed, 1)) == outcome).orElseThrow();
    }

    /** Middle of one per-mille bucket, so the rolls never sit on a boundary. */
    private static double bucket(final int perMille) {
        return (perMille + 0.5) / NativeChiefRules.TOTAL;
    }

    /**
     * Distribution boundaries of one mood and role: information below its end,
     * gold up to the gold end, and death from there on.
     */
    private static void assertBoundaries(final NativeHostility hostility,
            final boolean seasoned, final int informationEnd,
            final int goldEnd) {
        final boolean gold = goldEnd > informationEnd;
        assertEquals(INFORMATION, NativeChiefRules.draw(hostility, seasoned,
                bucket(informationEnd - 1)));
        assertEquals(gold ? GOLD : DEATH, NativeChiefRules.draw(hostility,
                seasoned, bucket(informationEnd)));
        if (gold) {
            assertEquals(GOLD, NativeChiefRules.draw(hostility, seasoned,
                    bucket(goldEnd - 1)));
        }
        assertEquals(DEATH,
                NativeChiefRules.draw(hostility, seasoned, bucket(goldEnd)));
        assertEquals(DEATH, NativeChiefRules.draw(hostility, seasoned,
                bucket(NativeChiefRules.TOTAL - 1)));
    }

    /** A settlement whose natives have the given anger toward the Dutch. */
    private static Model scoutScene(final long seed, final double anger,
            final boolean seasoned) {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH,
                        seasoned ? UnitType.SEASONED_SCOUT : UnitType.COLONIST,
                        at(1, 0), seasoned ? SEASONED_FIELDS : SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, anger);
        scene.toPo().setCombatRandomState(seed);
        return scene.toModel();
    }

    private static Model contentScene(final long seed, final boolean seasoned) {
        return scoutScene(seed, 10, seasoned);
    }

    private static Unit scout(final Model model) {
        return model.getUnitById(200);
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState().longValue();
    }

    private static NativeChiefPreview previewOf(final Model model) {
        return model.getNativeChiefPreview(scout(model), settlement(model))
                .orElseThrow();
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void contentOrdinaryScoutDistributionIsExact() {
        assertBoundaries(NativeHostility.CONTENT, false, 700, 950);
    }

    @Test
    void contentSeasonedScoutDistributionIsExact() {
        assertBoundaries(NativeHostility.CONTENT, true, 725, 975);
    }

    @Test
    void uneasyDistributionsAreExact() {
        assertBoundaries(NativeHostility.UNEASY, false, 750, 900);
        assertBoundaries(NativeHostility.UNEASY, true, 800, 950);
    }

    @Test
    void angryDistributionsAreExact() {
        assertBoundaries(NativeHostility.ANGRY, false, 600, 700);
        assertBoundaries(NativeHostility.ANGRY, true, 750, 850);
    }

    @Test
    void hostileDistributionsHaveNoGoldAndExactDeath() {
        assertBoundaries(NativeHostility.HOSTILE, false, 400, 400);
        assertBoundaries(NativeHostility.HOSTILE, true, 700, 700);
    }

    @Test
    void seasonedScoutHalvesTheDeathChanceExactlyAndGivesTheDifferenceToInformation() {
        assertEquals(50,
                NativeChiefRules.deathPerMille(NativeHostility.CONTENT, false));
        assertEquals(25,
                NativeChiefRules.deathPerMille(NativeHostility.CONTENT, true));
        assertEquals(725, NativeChiefRules
                .informationPerMille(NativeHostility.CONTENT, true));
        assertEquals(300,
                NativeChiefRules.deathPerMille(NativeHostility.ANGRY, false));
        assertEquals(150,
                NativeChiefRules.deathPerMille(NativeHostility.ANGRY, true));
        assertEquals(750, NativeChiefRules
                .informationPerMille(NativeHostility.ANGRY, true));
        assertEquals(600,
                NativeChiefRules.deathPerMille(NativeHostility.HOSTILE, false));
    }

    @Test
    void rollOutsideTheUnitIntervalIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> NativeChiefRules
                .draw(NativeHostility.CONTENT, false, 1.0));
        assertThrows(IllegalArgumentException.class, () -> NativeChiefRules
                .draw(NativeHostility.CONTENT, false, -0.1));
    }

    @Test
    void goldIsCappedByTheTreasuryAndByTheCapitalLimit() {
        assertEquals(100, NativeChiefRules.goldAmount(1000, false));
        assertEquals(200, NativeChiefRules.goldAmount(1000, true));
        assertEquals(50, NativeChiefRules.goldAmount(50, true));
        assertEquals(100, NativeChiefRules.goldAmount(150, false));
        assertEquals(0, NativeChiefRules.goldAmount(0, false));
    }

    @Test
    void previewStatesMoodRoleAndChancesWithoutDrawing() {
        final Model ordinary = scoutScene(7, 30, false);
        final long before = randomState(ordinary);
        final NativeChiefPreview preview = previewOf(ordinary);
        assertEquals(NativeHostility.UNEASY, preview.getHostility());
        assertFalse(preview.isSeasoned());
        assertFalse(preview.isRepeat());
        assertEquals(750, preview.getInformationPerMille());
        assertEquals(150, preview.getGoldPerMille());
        assertEquals(100, preview.getDeathPerMille());
        assertEquals(before, randomState(ordinary));
        assertTrue(settlement(ordinary).findChiefEncounter(EuropeanNation.DUTCH)
                .isEmpty());
        assertTrue(ordinary.isUnitInGame(scout(ordinary)));
    }

    @Test
    void everyMoodGivesItsOwnChancesToBothScouts() {
        assertEquals(725,
                previewOf(scoutScene(1, 10, true)).getInformationPerMille());
        assertEquals(25, previewOf(scoutScene(1, 10, true)).getDeathPerMille());
        assertEquals(800,
                previewOf(scoutScene(1, 30, true)).getInformationPerMille());
        assertEquals(750,
                previewOf(scoutScene(1, 60, true)).getInformationPerMille());
        assertEquals(700,
                previewOf(scoutScene(1, 80, true)).getInformationPerMille());
        assertEquals(0, previewOf(scoutScene(1, 80, false)).getGoldPerMille());
        assertEquals(NativeHostility.HOSTILE,
                previewOf(scoutScene(1, 80, false)).getHostility());
    }

    @Test
    void cancelledPreviewChangesNothingIncludingRandomStateAndRecord() {
        final Model model = contentScene(11, false);
        final long before = randomState(model);
        final int treasury = settlement(model).getTreasury();
        final int gold = model.getPlayerByName(DUTCH).getGold();
        previewOf(model);
        previewOf(model);
        assertEquals(before, randomState(model));
        assertEquals(treasury, settlement(model).getTreasury());
        assertEquals(gold, model.getPlayerByName(DUTCH).getGold());
        assertTrue(settlement(model).findChiefEncounter(EuropeanNation.DUTCH)
                .isEmpty());
        assertTrue(model.isUnitInGame(scout(model)));
    }

    @Test
    void firstInformationDisclosesOnlyThisSettlementAndDrawsOnce() {
        final long seed = seedFor(NativeHostility.CONTENT, false, INFORMATION);
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10);
        final Map<Location, TerrainType> terrain = scene.toPo().getMap()
                .getTerrainMap();
        terrain.put(at(-3, 0), TerrainType.MOUNTAIN);
        scene.toPo().getMap().setTerrainType(terrain);
        scene.toPo().setCombatRandomState(seed);
        final Model model = scene.toModel();
        final NativeChiefResult result = model.visitNativeChief(scout(model),
                settlement(model), previewOf(model));
        assertEquals(INFORMATION, result.getOutcome());
        assertFalse(result.isRepeat());
        assertEquals(0, result.getGold());
        final NativeChiefResult.Information info = result.getInformation()
                .orElseThrow();
        assertEquals(settlement(model).getOfferedGoods(),
                info.getOfferedGoods());
        assertEquals(settlement(model).getWantedGoods(), info.getWantedGoods());
        assertEquals(settlement(model).getTeachableSkill(),
                info.getTeachableSkill());
        // The land within two squares is explored; the mountain three squares
        // away stays unknown (see the map tests below).
        assertTrue(model.getPlayerByName(DUTCH).isVisible(at(-2, 0)));
        assertFalse(model.getPlayerByName(DUTCH).isVisible(at(-3, 0)));
        assertEquals(INFORMATION, settlement(model)
                .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
        assertEquals(seed + GOLDEN, randomState(model));
    }

    @Test
    void goldMovesOnlyUpToTheCapAndNothingIsCreated() {
        final long seed = seedFor(NativeHostility.CONTENT, false, GOLD);
        final Model model = contentScene(seed, false);
        final NativeSettlement site = settlement(model);
        final int treasury = site.getTreasury();
        final int gold = model.getPlayerByName(DUTCH).getGold();
        final int paid = Math.min(site.isCapital() ? 200 : 100, treasury);
        final NativeChiefResult result = model.visitNativeChief(scout(model),
                site, previewOf(model));
        assertEquals(GOLD, result.getOutcome());
        assertEquals(paid, result.getGold());
        assertEquals(treasury - paid, site.getTreasury());
        assertEquals(gold + paid, model.getPlayerByName(DUTCH).getGold());
        assertTrue(result.getInformation().isEmpty());
    }

    @Test
    void emptyTreasuryPaysNothingButStillRecordsTheOutcome() {
        final long seed = seedFor(NativeHostility.CONTENT, false, GOLD);
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .treasuryOfFirst(0);
        scene.toPo().setCombatRandomState(seed);
        final Model model = scene.toModel();
        final int gold = model.getPlayerByName(DUTCH).getGold();
        final NativeChiefResult result = model.visitNativeChief(scout(model),
                settlement(model), previewOf(model));
        assertEquals(GOLD, result.getOutcome());
        assertEquals(0, result.getGold());
        assertEquals(0, settlement(model).getTreasury());
        assertEquals(gold, model.getPlayerByName(DUTCH).getGold());
        assertEquals(GOLD, settlement(model)
                .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
    }

    @Test
    void goldThatWouldOverflowThePurseIsRefusedBeforeTheDraw() {
        final long seed = seedFor(NativeHostility.CONTENT, false, GOLD);
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .gold(DUTCH, Integer.MAX_VALUE);
        scene.toPo().setCombatRandomState(seed);
        final Model model = scene.toModel();
        final NativeChiefPreview preview = previewOf(model);
        final long before = randomState(model);
        final int treasury = settlement(model).getTreasury();
        assertThrows(IllegalStateException.class, () -> model
                .visitNativeChief(scout(model), settlement(model), preview));
        assertEquals(before, randomState(model));
        assertEquals(treasury, settlement(model).getTreasury());
        assertTrue(settlement(model).findChiefEncounter(EuropeanNation.DUTCH)
                .isEmpty());
    }

    @Test
    void deathRemovesTheScoutAndRecordsTheDeath() {
        final long seed = seedFor(NativeHostility.CONTENT, false, DEATH);
        final Model model = contentScene(seed, false);
        final Unit victim = scout(model);
        final int treasury = settlement(model).getTreasury();
        final NativeChiefResult result = model.visitNativeChief(victim,
                settlement(model), previewOf(model));
        assertEquals(DEATH, result.getOutcome());
        assertEquals(0, result.getGold());
        assertFalse(model.isUnitInGame(victim));
        assertEquals(treasury, settlement(model).getTreasury());
        assertEquals(DEATH, settlement(model)
                .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
        assertEquals(seed + GOLDEN, randomState(model));
    }

    @Test
    void aRepeatVisitRepeatsTheInformationWithoutADrawOrPayment() {
        final long seed = seedFor(NativeHostility.CONTENT, false, GOLD);
        final Model model = contentScene(seed, false);
        final NativeSettlement site = settlement(model);
        model.visitNativeChief(scout(model), site, previewOf(model));
        final long afterFirst = randomState(model);
        final int treasury = site.getTreasury();
        final int gold = model.getPlayerByName(DUTCH).getGold();

        // The same scout meets the chief again: a repeat without risk.
        final NativeChiefPreview repeat = previewOf(model);
        assertTrue(repeat.isRepeat());
        assertEquals(0, repeat.getGoldPerMille());
        assertEquals(0, repeat.getDeathPerMille());
        final NativeChiefResult again = model.visitNativeChief(scout(model),
                site, repeat);
        assertTrue(again.isRepeat());
        assertEquals(INFORMATION, again.getOutcome());
        assertEquals(0, again.getGold());
        assertTrue(again.getInformation().isPresent());
        assertEquals(afterFirst, randomState(model));
        assertEquals(treasury, site.getTreasury());
        assertEquals(gold, model.getPlayerByName(DUTCH).getGold());
        assertEquals(GOLD,
                site.findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
    }

    @Test
    void aRecordedNationIsRepeatedAfterReloadingTheSave() {
        final long seed = seedFor(NativeHostility.CONTENT, false, GOLD);
        final Model model = contentScene(seed, false);
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        final Model reloaded = reload(model);
        assertTrue(previewOf(reloaded).isRepeat());
        assertEquals(GOLD, settlement(reloaded)
                .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
    }

    @Test
    void aSavedRecordOfTheDutchDoesNotStopAFrenchScoutFromDrawing() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(FRANCE, UnitType.COLONIST, at(1, 0), SCOUT)
                .currentPlayer(FRANCE);
        final NativeChiefEncounterPo dutch = new NativeChiefEncounterPo();
        dutch.setEuropean(EuropeanNation.DUTCH);
        dutch.setOutcome(INFORMATION);
        scene.toPo().getNativeSettlements().get(0)
                .setChiefEncounters(List.of(dutch));
        final Model model = scene.toModel();
        final NativeChiefPreview preview = previewOf(model);
        assertFalse(preview.isRepeat());
        final long before = randomState(model);
        final NativeChiefResult result = model.visitNativeChief(scout(model),
                settlement(model), preview);
        assertFalse(result.isRepeat());
        assertEquals(before + GOLDEN, randomState(model));
        assertTrue(settlement(model).findChiefEncounter(EuropeanNation.FRENCH)
                .isPresent());
    }

    @Test
    void informationAddsExactlyTheUnexploredSquaresWithinTwoOfTheSettlement() {
        final Model model = contentScene(
                seedFor(NativeHostility.CONTENT, false, INFORMATION), false);
        final Set<Location> before = explored(model);
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        final Set<Location> after = explored(model);
        assertTrue(after.containsAll(before));
        // The scout's own sight already explored part of the area, so the
        // information must add something, and nothing outside two squares.
        assertFalse(before.containsAll(after));
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                final Location square = at(dx, dy);
                if (at(0, 0).getDistanceChebyshev(square) <= 2) {
                    assertTrue(after.contains(square), "Inside " + square);
                } else {
                    assertEquals(before.contains(square),
                            after.contains(square), "Outside " + square);
                }
            }
        }
    }

    @Test
    void aRepeatVisitAfterGoldOrDeathIsTheFirstRevealOfTheLandAndDrawsNothing() {
        for (final NativeChiefOutcome first : List.of(GOLD, DEATH)) {
            final NativeRelationsScene scene = island(NativeNation.AZTEC)
                    .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                    .unit(DUTCH, UnitType.SEASONED_SCOUT, at(1, -1),
                            SEASONED_FIELDS)
                    .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10);
            scene.toPo().setCombatRandomState(
                    seedFor(NativeHostility.CONTENT, false, first));
            final Model model = scene.toModel();
            model.visitNativeChief(scout(model), settlement(model),
                    previewOf(model));
            final Player dutch = model.getPlayerByName(DUTCH);
            final Set<Location> before = explored(model);
            assertFalse(dutch.isVisible(at(-2, 0)), "After " + first);
            final long state = randomState(model);
            final int gold = dutch.getGold();
            final int treasury = settlement(model).getTreasury();

            // Another scout of the same nation repeats the information only.
            final Unit other = model.getUnitById(201);
            final NativeChiefPreview repeat = model
                    .getNativeChiefPreview(other, settlement(model))
                    .orElseThrow();
            assertTrue(repeat.isRepeat());
            assertTrue(repeat.isSeasoned());
            final NativeChiefResult result = model.visitNativeChief(other,
                    settlement(model), repeat);
            assertTrue(result.isRepeat());
            assertEquals(INFORMATION, result.getOutcome());
            assertEquals(0, result.getGold());
            assertEquals(state, randomState(model));
            assertEquals(gold, dutch.getGold());
            assertEquals(treasury, settlement(model).getTreasury());
            assertEquals(first, settlement(model)
                    .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
            assertTrue(model.isUnitInGame(other));
            assertTrue(dutch.isVisible(at(-2, 0)), "Repeat after " + first);
            assertFalse(dutch.isVisible(at(-3, 0)));
            assertTrue(explored(model).containsAll(before));
        }
    }

    @Test
    void goldBelowTheCapitalCapPaysTheWholeTreasuryOnly() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .treasuryOfFirst(150);
        scene.toPo().setCombatRandomState(
                seedFor(NativeHostility.CONTENT, false, GOLD));
        final Model model = scene.toModel();
        final int gold = model.getPlayerByName(DUTCH).getGold();
        final NativeChiefResult result = model.visitNativeChief(scout(model),
                settlement(model), previewOf(model));
        assertEquals(150, result.getGold());
        assertEquals(0, settlement(model).getTreasury());
        assertEquals(gold + 150, model.getPlayerByName(DUTCH).getGold());
    }

    /** Explored squares of the island around the settlement, as a set. */
    private static Set<Location> explored(final Model model) {
        final Player dutch = model.getPlayerByName(DUTCH);
        final Set<Location> out = new HashSet<>();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                if (dutch.isVisible(at(dx, dy))) {
                    out.add(at(dx, dy));
                }
            }
        }
        return out;
    }

    @Test
    void staleTermsAreRefusedBeforeAnyDraw() {
        final Model model = contentScene(5, false);
        final long before = randomState(model);
        final NativeChiefPreview stale = NativeChiefPreview
                .of(NativeHostility.HOSTILE, false, false);
        assertThrows(IllegalStateException.class, () -> model
                .visitNativeChief(scout(model), settlement(model), stale));
        assertEquals(before, randomState(model));
        assertTrue(model.isUnitInGame(scout(model)));
        assertTrue(settlement(model).findChiefEncounter(EuropeanNation.DUTCH)
                .isEmpty());
    }

    @Test
    void anUnarmedColonistAndAForeignScoutGetNoChief() {
        final Model colonist = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED).toModel();
        assertTrue(colonist
                .getNativeChiefPreview(scout(colonist), settlement(colonist))
                .isEmpty());
        final Model foreign = island(NativeNation.AZTEC)
                .unit(FRANCE, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        assertTrue(foreign
                .getNativeChiefPreview(scout(foreign), settlement(foreign))
                .isEmpty());
    }

    @Test
    void aMalformedChiefRecordIsRejectedWhenTheModelIsBuilt() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT);
        final NativeChiefEncounterPo broken = new NativeChiefEncounterPo();
        broken.setEuropean(EuropeanNation.DUTCH);
        scene.toPo().getNativeSettlements().get(0)
                .setChiefEncounters(List.of(broken));
        assertThrows(IllegalArgumentException.class, scene::toModel);
    }

    /**
     * The ordinary scout already explores the radius-one ring and the squares
     * within its own two-square sight. The information adds only the unexplored
     * squares within two of the settlement, and radius three stays unknown.
     */
    @Test
    void informationMarksOnlyTheUnexploredLandWithinTwoSquares() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10);
        scene.toPo().setCombatRandomState(
                seedFor(NativeHostility.CONTENT, false, INFORMATION));
        final Model model = scene.toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        assertTrue(dutch.isVisible(at(-1, 0)));
        assertFalse(dutch.isVisible(at(-2, 0)));
        assertFalse(dutch.isVisible(at(-2, -2)));
        assertFalse(dutch.isVisible(at(-3, 0)));
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        assertTrue(dutch.isVisible(at(-1, 0)));
        assertTrue(dutch.isVisible(at(-2, 0)));
        assertTrue(dutch.isVisible(at(-2, -2)));
        assertFalse(dutch.isVisible(at(-3, 0)));
        assertFalse(dutch.isVisible(at(-4, 0)));
    }

    /**
     * Explored land is not present sight: a foreign unit on a newly explored
     * square is still unseen, and nothing else becomes visible.
     */
    @Test
    void informationGrantsNoPresentSightOfForeignUnits() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .unit(FRANCE, UnitType.COLONIST, at(-2, 0), UNARMED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10);
        scene.toPo().setCombatRandomState(
                seedFor(NativeHostility.CONTENT, false, INFORMATION));
        final Model model = scene.toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        assertFalse(dutch.isCurrentlyVisible(at(-2, 0)));
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        assertTrue(dutch.isVisible(at(-2, 0)));
        assertFalse(dutch.isCurrentlyVisible(at(-2, 0)));
        assertTrue(dutch.getObservedUnitsAt(at(-2, 0)).isEmpty());
    }

    /** Preview and cancel reveal no land; gold and death reveal none either. */
    @Test
    void previewCancelGoldAndDeathRevealNoLand() {
        final Model cancelled = contentScene(
                seedFor(NativeHostility.CONTENT, false, INFORMATION), false);
        previewOf(cancelled);
        assertFalse(cancelled.getPlayerByName(DUTCH).isVisible(at(-2, 0)));
        for (final NativeChiefOutcome outcome : List.of(GOLD, DEATH)) {
            final Model model = contentScene(
                    seedFor(NativeHostility.CONTENT, false, outcome), false);
            model.visitNativeChief(scout(model), settlement(model),
                    previewOf(model));
            assertEquals(outcome, settlement(model)
                    .findChiefEncounter(EuropeanNation.DUTCH).orElseThrow());
            assertFalse(model.getPlayerByName(DUTCH).isVisible(at(-2, 0)));
        }
    }

    /**
     * A repeat visit reveals the same land again, with no draw, gold or
     * treasury change, and no new unexplored square.
     */
    @Test
    void repeatVisitRevealsNoNewLandAndDrawsNothing() {
        final Model model = contentScene(
                seedFor(NativeHostility.CONTENT, false, INFORMATION), false);
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        final Player dutch = model.getPlayerByName(DUTCH);
        final long before = randomState(model);
        final int gold = dutch.getGold();
        final int treasury = settlement(model).getTreasury();
        final NativeChiefResult again = model.visitNativeChief(scout(model),
                settlement(model), previewOf(model));
        assertTrue(again.isRepeat());
        assertEquals(before, randomState(model));
        assertEquals(gold, dutch.getGold());
        assertEquals(treasury, settlement(model).getTreasury());
        assertTrue(dutch.isVisible(at(-2, 0)));
        assertFalse(dutch.isVisible(at(-3, 0)));
    }

    /** Save and Continue keep the explored land of the information. */
    @Test
    void revealedLandSurvivesReloadingTheSave() {
        final Model model = contentScene(
                seedFor(NativeHostility.CONTENT, false, INFORMATION), false);
        model.visitNativeChief(scout(model), settlement(model),
                previewOf(model));
        final Model reloaded = reload(model);
        assertTrue(reloaded.getPlayerByName(DUTCH).isVisible(at(-2, 0)));
        assertFalse(reloaded.getPlayerByName(DUTCH).isVisible(at(-3, 0)));
    }

}
