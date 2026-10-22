package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceNativeMissionPo;
import org.microcol.model.store.UnitPo;

/**
 * The native mission foundation (R-0164): establishment and every refusal that
 * changes nothing, the expertise of ordinary, Jesuit and Brebeuf missionaries,
 * the exact owner and rival alarm effects, the clamps, the clock that counts
 * nonhostile owner updates once per fresh turn, the fifth-update boundary, and
 * the saved identity and associations.
 */
class NativeMissionTest {

    private static final int MISSIONARY_ID = 200;

    private static final int SECOND_MISSIONARY_ID = 201;

    private static final int MAX_ROUNDS = 10;

    /** Ordinary missionary: a plain colonist that holds the missionary role. */
    private static final Consumer<UnitPo> ORDINARY = unit -> unit
            .setMissionary(true);

    /**
     * Jesuit: a missionary specialist. Its saved colonist fields are present
     * but it has not been ordained, so the role flag stays false.
     */
    private static final Consumer<UnitPo> JESUIT = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    private static NativeRelationsScene missionaryScene(final UnitType type,
            final Consumer<UnitPo> fields) {
        return island(NativeNation.AZTEC).unit(DUTCH, type, at(1, 0), fields);
    }

    private static Model ordinaryScene() {
        return missionaryScene(UnitType.COLONIST, ORDINARY).toModel();
    }

    private static Unit missionary(final Model model) {
        return model.getUnitById(MISSIONARY_ID);
    }

    private static NativeMissionView termsOf(final Model model) {
        return model
                .getNativeMissionPreview(missionary(model), settlement(model))
                .orElseThrow();
    }

    private static void establish(final Model model) {
        model.establishNativeMission(missionary(model), settlement(model),
                termsOf(model));
    }

    private static int updates(final Model model) {
        return settlement(model).getMissionUpdates();
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** The same game with the Aztec tribal anger toward the Dutch replaced. */
    private static Model withTribalAnger(final Model model,
            final double value) {
        final ModelPo saved = model.save();
        saved.getNativeTribalAnger().stream()
                .filter(entry -> entry.getNation() == NativeNation.AZTEC
                        && entry.getEuropean() == EuropeanNation.DUTCH)
                .findFirst().orElseThrow().setValue(value);
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Owner alarm after one mission update of a mission that starts at 50. */
    private static double alarmAfterOneUpdate(
            final NativeRelationsScene scene) {
        final Model model = scene.alarm(EuropeanNation.DUTCH, 50).toModel();
        establish(model);
        final Player dutch = model.getPlayerByName(DUTCH);
        final double anger = model.getNativeTribalAnger(dutch,
                NativeNation.AZTEC);
        model.advanceNativeMissions(dutch);
        assertEquals(anger,
                model.getNativeTribalAnger(dutch, NativeNation.AZTEC));
        return model.getNativeLocalAlarm(dutch, settlement(model));
    }

    /** Ends European turns until the owner's own next turn is prepared. */
    private static void ownerRound(final Model model, final Player owner) {
        int rounds = 0;
        do {
            model.endTurn();
            assertTrue(++rounds <= MAX_ROUNDS,
                    "The owner's next turn never came");
        } while (model.getCurrentPlayer() != owner);
    }

    @Test
    void ordinaryMissionaryEstablishesOnceAndLeavesTheMap() {
        final Model model = ordinaryScene();
        final Unit unit = missionary(model);
        final NativeSettlement site = settlement(model);
        final NativeMissionView terms = termsOf(model);
        assertEquals(EuropeanNation.DUTCH, terms.getOwner());
        assertFalse(terms.isEffectiveExpert());
        assertEquals(NativeHostility.CONTENT, terms.getHostility());
        assertEquals(0, terms.getCompletedUpdates());
        model.establishNativeMission(unit, site, terms);
        assertTrue(unit.isAtPlaceNativeMission());
        assertFalse(unit.isAtPlaceLocation());
        assertEquals(MISSIONARY_ID,
                site.getMissionaryId().orElseThrow().intValue());
        assertEquals(0, site.getMissionUpdates());
        assertEquals(1, model.getPlayerByName(DUTCH).getAllUnits().stream()
                .filter(owned -> owned == unit).count());
        assertTrue(model.getNativeMissionPreview(unit, site).isEmpty());
    }

    @Test
    void previewChangesNothingAndCancelLeavesEverythingUnchanged() {
        final Model model = ordinaryScene();
        termsOf(model);
        termsOf(model);
        assertTrue(missionary(model).isAtPlaceLocation());
        assertTrue(settlement(model).getMissionaryId().isEmpty());
        assertEquals(0, updates(model));
    }

    @Test
    void expertiseComesFromAJesuitOrABrebeufMemberAndNeverDoublesBoth() {
        assertFalse(termsOf(ordinaryScene()).isEffectiveExpert());
        assertTrue(termsOf(
                missionaryScene(UnitType.JESUIT_MISSIONARY, JESUIT).toModel())
                .isEffectiveExpert());
        assertTrue(termsOf(missionaryScene(UnitType.COLONIST, ORDINARY)
                .brebeufMember(DUTCH).toModel()).isEffectiveExpert());
        assertTrue(termsOf(missionaryScene(UnitType.JESUIT_MISSIONARY, JESUIT)
                .brebeufMember(DUTCH).toModel()).isEffectiveExpert());
    }

    @Test
    void theOwnerLosesTwoForOrdinaryAndFourForExpertWithoutDirectTribalChange() {
        assertEquals(48, alarmAfterOneUpdate(
                missionaryScene(UnitType.COLONIST, ORDINARY)), 1e-9);
        assertEquals(46,
                alarmAfterOneUpdate(
                        missionaryScene(UnitType.JESUIT_MISSIONARY, JESUIT)),
                1e-9);
        assertEquals(46,
                alarmAfterOneUpdate(missionaryScene(UnitType.COLONIST, ORDINARY)
                        .brebeufMember(DUTCH)),
                1e-9);
        assertEquals(46,
                alarmAfterOneUpdate(
                        missionaryScene(UnitType.JESUIT_MISSIONARY, JESUIT)
                                .brebeufMember(DUTCH)),
                1e-9);
    }

    /**
     * Saved local alarm of one nation at the settlement. Nations without a
     * player in the game have no Player to read, so the saved table is the
     * source for them.
     */
    private static double savedAlarm(final Model model,
            final EuropeanNation european) {
        return model.save().getNativeLocalAlarm().stream()
                .filter(entry -> entry.getSettlementId() == settlement(model)
                        .getId() && entry.getEuropean() == european)
                .findFirst().orElseThrow().getValue();
    }

    /**
     * The free-play game has two colonial players, Dutch and France. France is
     * the only real rival, and its raise is halved by its national factor.
     */
    @Test
    void aContactedRivalGainsOneHalvedAndTheOwnerGainsNothingExtra() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .contact(EuropeanNation.FRENCH, NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .alarm(EuropeanNation.DUTCH, 50)
                .alarm(EuropeanNation.FRENCH, 20).toModel();
        establish(model);
        final Player dutch = model.getPlayerByName(DUTCH);
        model.advanceNativeMissions(dutch);
        assertEquals(48, model.getNativeLocalAlarm(dutch, settlement(model)),
                1e-9);
        assertEquals(20.5, model.getNativeLocalAlarm(
                model.getPlayerByName(FRANCE), settlement(model)), 1e-9);
    }

    @Test
    void anUncontactedRivalPlayerGainsNothing() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .alarm(EuropeanNation.FRENCH, 20).toModel();
        establish(model);
        model.advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(20, model.getNativeLocalAlarm(
                model.getPlayerByName(FRANCE), settlement(model)), 1e-9);
    }

    /**
     * A contacted nation without a player of this game is not a rival: its
     * saved alarm stays as it was, and the owner still loses its own two.
     */
    @Test
    void aContactedNationWithoutAPlayerInTheGameGainsNothing() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .contact(EuropeanNation.ENGLISH, NativeNation.AZTEC)
                .contact(EuropeanNation.SPANISH, NativeNation.AZTEC)
                .alarm(EuropeanNation.DUTCH, 50)
                .alarm(EuropeanNation.ENGLISH, 20)
                .alarm(EuropeanNation.SPANISH, 30).toModel();
        establish(model);
        model.advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(48, model.getNativeLocalAlarm(model.getPlayerByName(DUTCH),
                settlement(model)), 1e-9);
        assertEquals(20, savedAlarm(model, EuropeanNation.ENGLISH), 1e-9);
        assertEquals(30, savedAlarm(model, EuropeanNation.SPANISH), 1e-9);
    }

    @Test
    void theFrenchNationalFactorHalvesTheRivalRaise() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .contact(EuropeanNation.FRENCH, NativeNation.AZTEC)
                .alarm(EuropeanNation.FRENCH, 20).toModel();
        establish(model);
        model.advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(20.5, model.getNativeLocalAlarm(
                model.getPlayerByName(FRANCE), settlement(model)), 1e-9);
    }

    @Test
    void theAlarmStaysInsideZeroToOneHundred() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .contact(EuropeanNation.FRENCH, NativeNation.AZTEC)
                .alarm(EuropeanNation.DUTCH, 1)
                .alarm(EuropeanNation.FRENCH, 100).toModel();
        establish(model);
        model.advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(0, model.getNativeLocalAlarm(model.getPlayerByName(DUTCH),
                settlement(model)), 0);
        assertEquals(100, model.getNativeLocalAlarm(
                model.getPlayerByName(FRANCE), settlement(model)), 0);
    }

    /**
     * Only the hostile band (75 or more) pauses the clock; the angry band (50
     * up to 75) counts. The attitude is read before the update's own reduction,
     * so the boundary is the entry value, never the reduced one.
     */
    @Test
    void theHostileBandPausesTheClockAtExactlySeventyFiveAndAngryCounts() {
        final Model justBelow = missionaryScene(UnitType.COLONIST, ORDINARY)
                .alarm(EuropeanNation.DUTCH, 74.999).toModel();
        establish(justBelow);
        justBelow.advanceNativeMissions(justBelow.getPlayerByName(DUTCH));
        assertEquals(1, updates(justBelow));

        final Model angry = missionaryScene(UnitType.COLONIST, ORDINARY)
                .alarm(EuropeanNation.DUTCH, 60).toModel();
        establish(angry);
        angry.advanceNativeMissions(angry.getPlayerByName(DUTCH));
        assertEquals(1, updates(angry));

        final Model atBoundary = missionaryScene(UnitType.COLONIST, ORDINARY)
                .alarm(EuropeanNation.DUTCH, 75).toModel();
        establish(atBoundary);
        atBoundary.advanceNativeMissions(atBoundary.getPlayerByName(DUTCH));
        assertEquals(0, updates(atBoundary));
        // The reduction brought the alarm to 73, but the entry was hostile.
        assertEquals(73,
                atBoundary.getNativeLocalAlarm(
                        atBoundary.getPlayerByName(DUTCH),
                        settlement(atBoundary)),
                1e-9);
    }

    /** The attitude is the larger of tribal anger and local alarm. */
    @Test
    void tribalAngerAloneCanPauseTheClock() {
        final Model model = withTribalAnger(ordinaryScene(), 75);
        establish(model);
        model.advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(0, updates(model));
    }

    @Test
    void theClockAdvancesOncePerFreshOwnerTurnAndContinueDoesNotAdvanceIt() {
        final Model model = ordinaryScene();
        establish(model);
        final Player dutch = model.getPlayerByName(DUTCH);
        assertEquals(0, updates(model));
        ownerRound(model, dutch);
        assertEquals(1, updates(model));
        // Opening the settlement reads the mission and advances nothing.
        assertTrue(model.getNativeMissionView(dutch, settlement(model))
                .isPresent());
        assertEquals(1, updates(model));
        final Model restored = reload(model);
        assertEquals(1, updates(restored));
        assertEquals(1, updates(restored));
    }

    @Test
    void hostileUpdatesPauseTheClockAndTheFifthNonhostileUpdateFiresOnce() {
        Model model = ordinaryScene();
        establish(model);
        for (int update = 0; update < 3; update++) {
            assertTrue(model.advanceNativeMissions(model.getPlayerByName(DUTCH))
                    .isEmpty());
        }
        assertEquals(3, updates(model));

        // A hostile entry counts nothing and loses no progress.
        model = withTribalAnger(model, 90);
        assertTrue(model.advanceNativeMissions(model.getPlayerByName(DUTCH))
                .isEmpty());
        assertEquals(3, updates(model));

        model = withTribalAnger(model, 0);
        assertTrue(model.advanceNativeMissions(model.getPlayerByName(DUTCH))
                .isEmpty());
        assertEquals(4, updates(model));
        final List<NativeSettlement> fifth = model
                .advanceNativeMissions(model.getPlayerByName(DUTCH));
        assertEquals(1, fifth.size());
        assertSame(settlement(model), fifth.get(0));
        assertEquals(0, updates(model));
        // Nothing is banked: the next cycle starts empty.
        assertTrue(model.advanceNativeMissions(model.getPlayerByName(DUTCH))
                .isEmpty());
        assertEquals(1, updates(model));
    }

    @Test
    void refusedMissionsChangeNeitherTheMissionaryNorTheSettlement() {
        final Model unarmed = missionaryScene(UnitType.COLONIST, UNARMED)
                .toModel();
        assertTrue(unarmed.getNativeMissionPreview(missionary(unarmed),
                settlement(unarmed)).isEmpty());

        final Model moved = ordinaryScene();
        final NativeMissionView terms = termsOf(moved);
        missionary(moved).placeToLocation(at(2, 0));
        assertTrue(moved
                .getNativeMissionPreview(missionary(moved), settlement(moved))
                .isEmpty());
        assertThrows(IllegalStateException.class,
                () -> moved.establishNativeMission(missionary(moved),
                        settlement(moved), terms));
        assertEquals(at(2, 0), missionary(moved).getLocation());
        assertTrue(settlement(moved).getMissionaryId().isEmpty());

        final Model stale = ordinaryScene();
        final NativeMissionView current = termsOf(stale);
        final NativeMissionView changed = new NativeMissionView(
                current.getOwner(), current.getOwnerName(), true,
                current.getHostility(), 0);
        assertThrows(IllegalStateException.class,
                () -> stale.establishNativeMission(missionary(stale),
                        settlement(stale), changed));
        assertTrue(missionary(stale).isAtPlaceLocation());
        assertTrue(settlement(stale).getMissionaryId().isEmpty());

        final Model turnOfFrance = missionaryScene(UnitType.COLONIST, ORDINARY)
                .currentPlayer(FRANCE).toModel();
        assertTrue(turnOfFrance.getNativeMissionPreview(
                missionary(turnOfFrance), settlement(turnOfFrance)).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> turnOfFrance.establishNativeMission(
                        missionary(turnOfFrance), settlement(turnOfFrance),
                        changed));
        assertTrue(missionary(turnOfFrance).isAtPlaceLocation());
    }

    @Test
    void anOccupiedSettlementNeverReplacesItsMissionOrItsMissionary() {
        final Model model = missionaryScene(UnitType.COLONIST, ORDINARY)
                .unit(DUTCH, UnitType.COLONIST, at(1, -1), ORDINARY).toModel();
        final NativeMissionView firstTerms = termsOf(model);
        establish(model);
        final Unit second = model.getUnitById(SECOND_MISSIONARY_ID);
        final NativeSettlement site = settlement(model);
        assertEquals(NativeVisitChoice.Limit.MISSION_OCCUPIED,
                model.getNativeVisitChoices(second, site).stream()
                        .filter(choice -> choice
                                .getAction() == NativeVisitAction.MISSION)
                        .findFirst().orElseThrow().getLimit());
        assertTrue(model.getNativeMissionPreview(second, site).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> model.establishNativeMission(second, site, firstTerms));
        assertTrue(second.isAtPlaceLocation());
        assertEquals(MISSIONARY_ID,
                site.getMissionaryId().orElseThrow().intValue());
        assertEquals(0, updates(model));
    }

    @Test
    void theStationedMissionaryKeepsItsIdentityAcrossSaveAndNeverActsTwice() {
        final Model model = ordinaryScene();
        establish(model);
        final Model restored = reload(model);
        final Unit unit = restored.getUnitById(MISSIONARY_ID);
        final NativeSettlement site = settlement(restored);
        assertTrue(unit.isAtPlaceNativeMission());
        assertEquals(MISSIONARY_ID,
                site.getMissionaryId().orElseThrow().intValue());
        assertEquals(1, restored.getPlayerByName(DUTCH).getAllUnits().stream()
                .filter(owned -> owned == unit).count());
        assertTrue(restored.getNativeVisitChoices(unit, site).isEmpty());
        final NativeMissionView view = restored
                .getNativeMissionView(restored.getPlayerByName(DUTCH), site)
                .orElseThrow();
        assertEquals(EuropeanNation.DUTCH, view.getOwner());
        assertFalse(view.isEffectiveExpert());
        assertEquals(0, view.getCompletedUpdates());
    }

    @Test
    void aMalformedMissionAssociationIsRejectedWhenTheModelIsBuilt() {
        final ModelPo missingUnit = ordinaryScene().save();
        missingUnit.getNativeSettlements().get(0).setMissionaryId(999);
        missingUnit.getNativeSettlements().get(0).setMissionUpdates(0);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(missingUnit,
                        MissionImpl.getPredefinedGameOverEvaluators()));

        // A held mission whose missionary is not stationed there is refused.
        final ModelPo notStationed = ordinaryScene().save();
        notStationed.getNativeSettlements().get(0)
                .setMissionaryId(MISSIONARY_ID);
        notStationed.getNativeSettlements().get(0).setMissionUpdates(0);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(notStationed,
                        MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo outOfRange = ordinaryScene().save();
        outOfRange.getNativeSettlements().get(0).setMissionaryId(MISSIONARY_ID);
        outOfRange.getNativeSettlements().get(0).setMissionUpdates(5);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(outOfRange,
                        MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo progressWithoutMissionary = ordinaryScene().save();
        progressWithoutMissionary.getNativeSettlements().get(0)
                .setMissionUpdates(2);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(progressWithoutMissionary,
                        MissionImpl.getPredefinedGameOverEvaluators()));

        final NativeRelationsScene twin = missionaryScene(UnitType.COLONIST,
                ORDINARY).ordinaryWithCapitalAt(at(-4, 4));
        final ModelPo duplicate = twin.toPo();
        duplicate.getNativeSettlements().get(0).setMissionaryId(MISSIONARY_ID);
        duplicate.getNativeSettlements().get(0).setMissionUpdates(0);
        duplicate.getNativeSettlements().get(1).setMissionaryId(MISSIONARY_ID);
        duplicate.getNativeSettlements().get(1).setMissionUpdates(0);
        assertThrows(IllegalArgumentException.class, () -> Model.make(duplicate,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void aStationedMissionaryWithoutItsSettlementIsRejectedWhenTheModelIsBuilt() {
        final NativeRelationsScene stationed = missionaryScene(
                UnitType.COLONIST, ORDINARY);
        final UnitPo unit = stationed.toPo().getUnits().stream()
                .filter(owned -> owned.getId() == MISSIONARY_ID).findFirst()
                .orElseThrow();
        unit.setPlaceMap(null);
        final PlaceNativeMissionPo place = new PlaceNativeMissionPo();
        place.setSettlementId(0);
        unit.setPlaceNativeMission(place);
        assertThrows(IllegalArgumentException.class, stationed::toModel);
    }

}
