package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceNativeMissionPo;
import org.microcol.model.store.UnitPo;

/**
 * The rival-mission contest (R-0165): the exact strengths and chances, the
 * numeric hostility, expertise counted once on both sides, the clamps, the
 * refusals that change nothing, the single saved draw, the exact removal and
 * clock of each outcome, and Save and Continue without a replay.
 *
 * Strength is 1 (ordinary) or 2 (expert) times 1 + (100 - hostility) / 100, so
 * at hostility 0 an ordinary missionary has 2 and an expert 4, and at hostility
 * 100 they have 1 and 2.
 */
class NativeMissionContestTest {

    /** Dutch visitor: the first unit of each scene. */
    private static final int VISITOR_ID = 200;

    /** Resident of the mission: the second unit of each scene. */
    private static final int RESIDENT_ID = 201;

    /** Clock of the resident's mission before the contest. */
    private static final int RESIDENT_UPDATES = 3;

    /** Step of the saved random stream, as the model advances it. */
    private static final long DRAW_STEP = 0x9E3779B97F4A7C15L;

    private static final double EPSILON = 1e-9;

    private static final Consumer<UnitPo> ORDINARY = unit -> unit
            .setMissionary(true);

    /** Jesuit: a missionary specialist whose role flag stays false. */
    private static final Consumer<UnitPo> JESUIT = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    /**
     * Dutch visitor beside the settlement, and the resident of the mission held
     * by the given owner, stationed at the settlement as the saved loader reads
     * it. Hostility starts at zero on both sides.
     */
    private static NativeRelationsScene missionScene(final String holder,
            final UnitType visitorType, final Consumer<UnitPo> visitorFields,
            final UnitType residentType,
            final Consumer<UnitPo> residentFields) {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, visitorType, at(1, 0), visitorFields)
                .unit(holder, residentType, at(-1, 0), residentFields);
        final ModelPo po = scene.toPo();
        final UnitPo resident = po.getUnits().stream()
                .filter(unit -> unit.getId() == RESIDENT_ID).findFirst()
                .orElseThrow();
        resident.setPlaceMap(null);
        final PlaceNativeMissionPo place = new PlaceNativeMissionPo();
        place.setSettlementId(0);
        resident.setPlaceNativeMission(place);
        po.getNativeSettlements().get(0).setMissionaryId(RESIDENT_ID);
        po.getNativeSettlements().get(0).setMissionUpdates(RESIDENT_UPDATES);
        return scene;
    }

    /** Ordinary Dutch visitor against an ordinary French resident. */
    private static NativeRelationsScene rivalScene() {
        return missionScene(FRANCE, UnitType.COLONIST, ORDINARY,
                UnitType.COLONIST, ORDINARY);
    }

    /**
     * Set the numeric tribal anger and local alarm of both sides. Each side
     * reads the larger of its two numbers as its hostility.
     */
    private static NativeRelationsScene hostility(
            final NativeRelationsScene scene, final double dutchTribal,
            final double dutchAlarm, final double franceTribal,
            final double franceAlarm) {
        return scene
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, dutchTribal)
                .alarm(EuropeanNation.DUTCH, dutchAlarm)
                .tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, franceTribal)
                .alarm(EuropeanNation.FRENCH, franceAlarm);
    }

    private static Unit visitor(final Model model) {
        return model.getUnitById(VISITOR_ID);
    }

    private static NativeMissionContestView termsOf(final Model model) {
        return model.getNativeMissionContestPreview(visitor(model),
                settlement(model)).orElseThrow();
    }

    private static NativeVisitChoice.Limit limitOfMission(final Model model) {
        return model.getNativeVisitChoices(visitor(model), settlement(model))
                .stream()
                .filter(choice -> choice
                        .getAction() == NativeVisitAction.MISSION)
                .findFirst().orElseThrow().getLimit();
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Smallest saved state whose first draw gives the outcome at this chance.
     * The first draw of the contest is that draw, since nothing else draws
     * before the confirmation.
     */
    private static long seedFor(final NativeMissionContestOutcome outcome,
            final double chance) {
        for (long seed = 1;; seed++) {
            if (NativeContestRules.outcome(chance,
                    Model.combatRandomAfter(seed)) == outcome) {
                return seed;
            }
        }
    }

    /** The scene's saved randomness set before the model is built. */
    private static Model withSeed(final NativeRelationsScene scene,
            final long seed) {
        scene.toPo().setCombatRandomState(seed);
        return scene.toModel();
    }

    @Test
    void equalStrengthsGiveAnEvenChance() {
        final Model model = hostility(rivalScene(), 50, 0, 50, 0).toModel();
        final NativeMissionContestView terms = termsOf(model);
        // Both sides have strength 1 * 1.5.
        assertEquals(0.5, terms.getChallengerChance(), EPSILON);
        assertEquals(VISITOR_ID, terms.getVisitorId());
        assertEquals(RESIDENT_ID, terms.getResidentId());
        assertEquals(settlement(model).getId(), terms.getSettlementId());
        assertEquals(EuropeanNation.FRENCH, terms.getResidentOwner());
        assertEquals(FRANCE, terms.getResidentOwnerName());
        assertFalse(terms.isVisitorExpert());
        assertFalse(terms.isResidentExpert());
    }

    /**
     * An expert at zero hostility has strength 2 * 2 = 4, an ordinary one at
     * full hostility 1 * 1 = 1, so the challenger's chance is exactly 4 / 5.
     */
    @Test
    void unequalExpertAndHostilityGiveTheProportionOfTheSums() {
        final Model model = hostility(
                missionScene(FRANCE, UnitType.JESUIT_MISSIONARY, JESUIT,
                        UnitType.COLONIST, ORDINARY),
                0, 0, 100, 0).toModel();
        final NativeMissionContestView terms = termsOf(model);
        assertTrue(terms.isVisitorExpert());
        assertFalse(terms.isResidentExpert());
        assertEquals(0.8, terms.getChallengerChance(), EPSILON);
    }

    /**
     * The hostility of a side is the larger of tribal anger and local alarm, so
     * the same larger number gives the same chance whichever of the two it is.
     */
    @Test
    void theNumericHostilityIsTheLargerOfTribalAngerAndLocalAlarm() {
        final Model tribal = hostility(rivalScene(), 60, 10, 0, 0).toModel();
        final Model alarm = hostility(rivalScene(), 10, 60, 0, 0).toModel();
        assertEquals(60, termsOf(tribal).getVisitorHostility(), EPSILON);
        assertEquals(60, termsOf(alarm).getVisitorHostility(), EPSILON);
        assertEquals(termsOf(tribal).getChallengerChance(),
                termsOf(alarm).getChallengerChance(), EPSILON);
        // Visitor strength 1.4 against resident strength 2.
        assertEquals(1.4 / 3.4, termsOf(tribal).getChallengerChance(), EPSILON);
    }

    /**
     * Two numbers inside the same angry band still count exactly: one more
     * point of hostility lowers the challenger's strength.
     */
    @Test
    void theExactNumbersCountInsideTheSameMood() {
        final Model fiftyFive = hostility(rivalScene(), 55, 0, 0, 0).toModel();
        final Model seventy = hostility(rivalScene(), 70, 0, 0, 0).toModel();
        assertEquals(NativeHostility.ANGRY, fiftyFive.getNativeHostility(
                fiftyFive.getPlayerByName(DUTCH), settlement(fiftyFive)));
        assertEquals(NativeHostility.ANGRY, seventy.getNativeHostility(
                seventy.getPlayerByName(DUTCH), settlement(seventy)));
        assertEquals(1.45 / 3.45, termsOf(fiftyFive).getChallengerChance(),
                EPSILON);
        assertEquals(1.30 / 3.30, termsOf(seventy).getChallengerChance(),
                EPSILON);
    }

    /**
     * Brebeuf makes an ordinary missionary expert once; a Jesuit with Brebeuf
     * is not doubled. The rule applies to the resident's side too.
     */
    @Test
    void brebeufCountsOnceOnEitherSideAndNeverDoublesAJesuit() {
        final Model ordinaryBrebeuf = hostility(rivalScene(), 0, 0, 0, 0)
                .brebeufMember(DUTCH).toModel();
        assertTrue(termsOf(ordinaryBrebeuf).isVisitorExpert());
        // Expert 4 against ordinary 2.
        assertEquals(2.0 / 3.0, termsOf(ordinaryBrebeuf).getChallengerChance(),
                EPSILON);

        final Model jesuitBrebeuf = missionScene(FRANCE,
                UnitType.JESUIT_MISSIONARY, JESUIT, UnitType.COLONIST, ORDINARY)
                .brebeufMember(DUTCH).toModel();
        // Already expert: still 4 against 2, not 8 against 2.
        assertEquals(2.0 / 3.0, termsOf(jesuitBrebeuf).getChallengerChance(),
                EPSILON);

        final Model residentBrebeuf = hostility(rivalScene(), 0, 0, 0, 0)
                .brebeufMember(FRANCE).toModel();
        assertTrue(termsOf(residentBrebeuf).isResidentExpert());
        // Ordinary 2 against expert 4.
        assertEquals(1.0 / 3.0, termsOf(residentBrebeuf).getChallengerChance(),
                EPSILON);
    }

    /**
     * Real inputs keep the strengths between 1 and 4, so the chance stays
     * between 20 and 80 percent. The 10 and 90 caps are reachable only through
     * the rule itself.
     */
    @Test
    void theChanceIsClampedToTenAndNinetyPercentAndRealInputsStayInside() {
        assertEquals(0.1, NativeContestRules.challengerChance(1, 9), EPSILON);
        assertEquals(0.9, NativeContestRules.challengerChance(9, 1), EPSILON);
        assertEquals(0.5, NativeContestRules.challengerChance(1, 1), EPSILON);
        assertEquals(0.2, NativeContestRules.challengerChance(1, 4), EPSILON);
        assertEquals(0.8, NativeContestRules.challengerChance(4, 1), EPSILON);
        assertEquals(1.0, NativeContestRules.strength(false, 100), EPSILON);
        assertEquals(4.0, NativeContestRules.strength(true, 0), EPSILON);
    }

    @Test
    void previewChangesNothingAndCancelLeavesEverythingUnchanged() {
        final Model model = hostility(rivalScene(), 50, 0, 50, 0).toModel();
        final long before = randomState(model);
        termsOf(model);
        termsOf(model);
        assertEquals(before, randomState(model));
        assertTrue(visitor(model).isAtPlaceLocation());
        assertEquals(RESIDENT_ID,
                settlement(model).getMissionaryId().orElseThrow().intValue());
        assertEquals(RESIDENT_UPDATES, settlement(model).getMissionUpdates());
    }

    @Test
    void anOrdinaryColonistAnAbsentMissionAndAnOwnMissionOfferNoContest() {
        final Model plain = missionScene(FRANCE, UnitType.COLONIST, UNARMED,
                UnitType.COLONIST, ORDINARY).toModel();
        assertTrue(plain.getNativeMissionContestPreview(visitor(plain),
                settlement(plain)).isEmpty());

        final Model absent = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), ORDINARY).toModel();
        assertTrue(absent.getNativeMissionContestPreview(visitor(absent),
                settlement(absent)).isEmpty());

        // A mission of the visitor's own nation is refused, never contested.
        final Model own = missionScene(DUTCH, UnitType.COLONIST, ORDINARY,
                UnitType.COLONIST, ORDINARY).toModel();
        assertTrue(own
                .getNativeMissionContestPreview(visitor(own), settlement(own))
                .isEmpty());
        assertEquals(NativeVisitChoice.Limit.MISSION_OCCUPIED,
                limitOfMission(own));
    }

    /**
     * A rival mission is offered as its own contest; the establishment of
     * R-0164 stays refused for an occupied settlement.
     */
    @Test
    void aRivalMissionIsOfferedAsAContestNotAsAnEstablishment() {
        final Model model = rivalScene().toModel();
        assertEquals(NativeVisitChoice.Limit.CONTEST_AVAILABLE,
                limitOfMission(model));
        assertTrue(
                model.getNativeMissionPreview(visitor(model), settlement(model))
                        .isEmpty());
        final NativeMissionView establishment = new NativeMissionView(
                EuropeanNation.DUTCH, DUTCH, false, NativeHostility.CONTENT, 0);
        assertThrows(IllegalStateException.class,
                () -> model.establishNativeMission(visitor(model),
                        settlement(model), establishment));
        assertEquals(RESIDENT_ID,
                settlement(model).getMissionaryId().orElseThrow().intValue());
    }

    /**
     * A moved visitor and a missionary on the other nation's turn are refused
     * with nothing changed.
     */
    @Test
    void movedAndTurnRefusalsChangeNothing() {
        final Model moved = hostility(rivalScene(), 50, 0, 50, 0).toModel();
        final NativeMissionContestView terms = termsOf(moved);
        visitor(moved).placeToLocation(at(2, 0));
        assertTrue(moved.getNativeMissionContestPreview(visitor(moved),
                settlement(moved)).isEmpty());
        final long before = randomState(moved);
        assertThrows(IllegalStateException.class,
                () -> moved.contestNativeMission(visitor(moved),
                        settlement(moved), terms));
        assertEquals(at(2, 0), visitor(moved).getLocation());
        assertEquals(before, randomState(moved));
        assertEquals(RESIDENT_ID,
                settlement(moved).getMissionaryId().orElseThrow().intValue());

        final Model turnOfFrance = hostility(rivalScene(), 50, 0, 50, 0)
                .currentPlayer(FRANCE).toModel();
        assertTrue(turnOfFrance.getNativeMissionContestPreview(
                visitor(turnOfFrance), settlement(turnOfFrance)).isEmpty());
    }

    /**
     * A quote taken before the mission changed is refused, even when five
     * updates bring the visible clock back to the same value. Nothing is drawn
     * and nothing moves.
     */
    @Test
    void aStaleQuoteIsRefusedEvenWhenItsValuesMatchAgain() {
        final Model model = hostility(rivalScene(), 50, 0, 50, 0).toModel();
        final NativeMissionContestView shown = termsOf(model);
        final NativeSettlement site = settlement(model);
        final long before = randomState(model);
        for (int update = 0; update < NativeMissionView.REQUIRED_UPDATES; update++) {
            site.addMissionUpdate();
        }
        // The clock is back at its old count: only the mission revision
        // differs.
        assertEquals(RESIDENT_UPDATES, site.getMissionUpdates());
        assertThrows(IllegalStateException.class,
                () -> model.contestNativeMission(visitor(model), site, shown));
        assertEquals(before, randomState(model));
        assertTrue(visitor(model).isAtPlaceLocation());
        assertEquals(RESIDENT_ID,
                site.getMissionaryId().orElseThrow().intValue());
    }

    /**
     * A success removes only the resident, moves the visitor into the mission
     * with its clock at zero, draws once and leaves the tribe's anger alone.
     */
    @Test
    void aSuccessRemovesOnlyTheResidentAndTakesTheMissionWithAClockAtZero() {
        final long seed = seedFor(NativeMissionContestOutcome.SUCCESS, 0.5);
        final Model model = withSeed(hostility(rivalScene(), 50, 0, 50, 0),
                seed);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Player france = model.getPlayerByName(FRANCE);
        final double anger = model.getNativeTribalAnger(dutch,
                NativeNation.AZTEC);
        final Unit visitor = visitor(model);
        final Unit resident = model.getUnitById(RESIDENT_ID);
        final NativeSettlement site = settlement(model);

        final NativeMissionContestOutcome outcome = model
                .contestNativeMission(visitor, site, termsOf(model));

        assertEquals(NativeMissionContestOutcome.SUCCESS, outcome);
        assertEquals(seed + DRAW_STEP, randomState(model));
        assertTrue(model.tryGetUnitById(RESIDENT_ID).isEmpty());
        assertFalse(france.getAllUnits().contains(resident));
        assertTrue(visitor.isAtPlaceNativeMission());
        assertTrue(dutch.getAllUnits().contains(visitor));
        assertEquals(VISITOR_ID,
                site.getMissionaryId().orElseThrow().intValue());
        assertEquals(0, site.getMissionUpdates());
        assertEquals(anger,
                model.getNativeTribalAnger(dutch, NativeNation.AZTEC), EPSILON);
        assertEquals(EuropeanNation.DUTCH, model
                .getNativeMissionView(dutch, site).orElseThrow().getOwner());
    }

    /**
     * A failure removes only the visitor, draws once, and keeps the resident
     * mission with its clock.
     */
    @Test
    void aFailureRemovesOnlyTheVisitorAndKeepsTheResidentMissionAndClock() {
        final long seed = seedFor(NativeMissionContestOutcome.FAILURE, 0.5);
        final Model model = withSeed(hostility(rivalScene(), 50, 0, 50, 0),
                seed);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit visitor = visitor(model);
        final NativeSettlement site = settlement(model);

        final NativeMissionContestOutcome outcome = model
                .contestNativeMission(visitor, site, termsOf(model));

        assertEquals(NativeMissionContestOutcome.FAILURE, outcome);
        assertEquals(seed + DRAW_STEP, randomState(model));
        assertTrue(model.tryGetUnitById(VISITOR_ID).isEmpty());
        assertFalse(dutch.getAllUnits().contains(visitor));
        assertTrue(model.getUnitById(RESIDENT_ID).isAtPlaceNativeMission());
        assertEquals(RESIDENT_ID,
                site.getMissionaryId().orElseThrow().intValue());
        assertEquals(RESIDENT_UPDATES, site.getMissionUpdates());
    }

    /**
     * Each outcome is saved in the units, so Save and Continue do not draw
     * again and do not bring back the removed missionary.
     */
    @Test
    void theConfirmedOutcomeIsNeverReplayedAfterSaveAndContinue() {
        final Model won = withSeed(hostility(rivalScene(), 50, 0, 50, 0),
                seedFor(NativeMissionContestOutcome.SUCCESS, 0.5));
        won.contestNativeMission(visitor(won), settlement(won), termsOf(won));
        final Model restoredWon = reload(won);
        final long state = randomState(restoredWon);
        assertTrue(restoredWon.getNativeMissionContestPreview(
                restoredWon.getUnitById(VISITOR_ID), settlement(restoredWon))
                .isEmpty());
        assertTrue(restoredWon.tryGetUnitById(RESIDENT_ID).isEmpty());
        assertEquals(state, randomState(restoredWon));
        assertEquals(VISITOR_ID, settlement(restoredWon).getMissionaryId()
                .orElseThrow().intValue());

        final Model lost = withSeed(hostility(rivalScene(), 50, 0, 50, 0),
                seedFor(NativeMissionContestOutcome.FAILURE, 0.5));
        lost.contestNativeMission(visitor(lost), settlement(lost),
                termsOf(lost));
        final Model restoredLost = reload(lost);
        assertTrue(restoredLost.tryGetUnitById(VISITOR_ID).isEmpty());
        assertEquals(RESIDENT_UPDATES,
                settlement(restoredLost).getMissionUpdates());
        assertEquals(randomState(lost), randomState(restoredLost));
    }

}
