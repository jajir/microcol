package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.PIONEER;
import static org.microcol.model.NativeRelationsScene.SCOUT;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Native course of an offered skill (R-0148): the admitted and refused pupils,
 * the single course per settlement, the stale offer and the current actor, the
 * exact fourth owner update with one grant, the safe waiting when no square is
 * free, the hostility cancel, the manual leave, and the saved stages. Owner
 * rounds run the whole European round, so no check calls the owner update
 * directly.
 */
class NativeTeachingTest {

    /**
     * Saved fields of an ordinary colonist, which the scene adds only for the
     * plain colonist type.
     */
    private static final Consumer<UnitPo> COLONIST_FIELDS = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    private static final int PUPIL_ID = 200;

    private static final int SECOND_PUPIL_ID = 201;

    /**
     * Foreign colonist that moves onto the pupil's square to block the exit.
     */
    private static final int BLOCKER_MOVER_ID = 208;

    private static final int MAX_ROUNDS = 10;

    private static Model teachingScene(final UnitType type,
            final Consumer<UnitPo> equipment) {
        return island(NativeNation.AZTEC).unit(DUTCH, type, at(1, 0), equipment)
                .toModel();
    }

    /**
     * The pupil with every other square beside the settlement held by a foreign
     * colonist, so that after the pupil leaves no legal square is free. The
     * mover stands one step further out and can take the pupil's square.
     */
    private static Model blockedScene() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(-1, -1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(0, -1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(1, -1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(-1, 0), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(-1, 1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(0, 1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(1, 1), UNARMED)
                .unit(FRANCE, UnitType.COLONIST, at(2, 0), UNARMED).toModel();
    }

    /**
     * Ends European turns until the owner's own next turn is prepared. One
     * round may pass other European turns first, so it is bounded.
     */
    private static void ownerRound(final Model model, final Player owner) {
        int rounds = 0;
        do {
            model.endTurn();
            assertTrue(++rounds <= MAX_ROUNDS,
                    "The owner's next turn never came");
        } while (model.getCurrentPlayer() != owner);
    }

    /** Count saved native teaching notices of one kind. */
    private static int countEvents(final Model model, final String kind) {
        int count = 0;
        for (final TurnEventPo event : model.save().getTurnEvents()) {
            if ("TurnEventNativeTeaching".equals(event.getType())
                    && kind.equals(event.getNativeTeachingKind())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Put the foreign mover on the pupil's old square to block the exit, or one
     * step further out to free it again.
     */
    private static void placeMoverOnExit(final Model model,
            final boolean blocking) {
        model.getUnitById(BLOCKER_MOVER_ID)
                .placeToLocation(blocking ? at(1, 0) : at(2, 0));
    }

    @Test
    void acceptedCourseTakesTheColonistOffTheMapWithoutTheSkill() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        assertTrue(colonist.isAtPlaceNativeCourse());
        assertFalse(colonist.isAtPlaceLocation());
        assertEquals(0, colonist.getActionPoints());
        assertEquals(UnitType.COLONIST, colonist.getType());
        assertEquals(PUPIL_ID, site.getTeachingPupilId().orElseThrow());
        final NativeTeachingCourse course = model.getNativeTeachingCourse(site)
                .orElseThrow();
        assertEquals(UnitType.EXPERT_FARMER, course.getSkill());
        assertEquals(0, course.getCompletedUpdates());
        assertEquals(NativeTeachingCourse.Stage.LEARNING, course.getStage());
        assertFalse(course.isAwaitingExit());
        assertTrue(
                model.getPlayerByName(DUTCH).getAllUnits().contains(colonist));
    }

    @Test
    void criminalAndExistingSpecialistCannotStart() {
        for (final UnitType type : new UnitType[] { UnitType.PETTY_CRIMINAL,
                UnitType.EXPERT_FARMER }) {
            final Model model = teachingScene(type, COLONIST_FIELDS);
            final Unit refused = model.getUnitById(PUPIL_ID);
            final NativeSettlement site = settlement(model);
            assertThrows(IllegalStateException.class,
                    () -> model.startNativeTeaching(refused, site,
                            site.getTeachableSkill()));
            assertTrue(refused.isAtPlaceLocation());
            assertTrue(site.getTeachingPupilId().isEmpty());
        }
    }

    @Test
    void staleOfferChangesNothing() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        assertThrows(IllegalStateException.class,
                () -> model.startNativeTeaching(colonist, site,
                        UnitType.EXPERT_LUMBERJACK));
        assertTrue(colonist.isAtPlaceLocation());
        assertTrue(site.getTeachingPupilId().isEmpty());
    }

    @Test
    void onlyTheCurrentActorMayStartAndNoForeignVisitorMay() {
        final Model outOfTurn = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .currentPlayer(FRANCE).toModel();
        final Unit colonist = outOfTurn.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(outOfTurn);
        assertThrows(IllegalStateException.class, () -> outOfTurn
                .startNativeTeaching(colonist, site, site.getTeachableSkill()));
        assertTrue(colonist.isAtPlaceLocation());
        assertTrue(site.getTeachingPupilId().isEmpty());
    }

    @Test
    void onlyOnePupilLearnsAtASettlementAtATime() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .unit(DUTCH, UnitType.COLONIST, at(0, 1), UNARMED).toModel();
        final Unit first = model.getUnitById(PUPIL_ID);
        final Unit second = model.getUnitById(SECOND_PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(first, site, site.getTeachableSkill());
        final NativeVisitChoice.Limit limit = model
                .getNativeVisitChoices(second, site).stream()
                .filter(choice -> choice
                        .getAction() == NativeVisitAction.TEACHING)
                .findAny().orElseThrow().getLimit();
        assertEquals(NativeVisitChoice.Limit.SETTLEMENT_OCCUPIED, limit);
        assertThrows(IllegalStateException.class, () -> model
                .startNativeTeaching(second, site, site.getTeachableSkill()));
        assertTrue(second.isAtPlaceLocation());
        assertEquals(PUPIL_ID, site.getTeachingPupilId().orElseThrow());
    }

    @Test
    void cancelingReleasesTheColonistWithoutTheSkillAndDiscardsProgress() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        model.cancelNativeTeaching(colonist);
        assertTrue(colonist.isAtPlaceLocation());
        assertTrue(site.getTeachingPupilId().isEmpty());
        assertTrue(site.getTeachingStage().isEmpty());
        assertEquals(UnitType.COLONIST, colonist.getType());
        assertTrue(site.getLocation().isNeighbor(colonist.getLocation()));
    }

    @Test
    void fourthOwnerUpdateGrantsTheSkillToTheSamePersonOnce() {
        final Model model = teachingScene(UnitType.COLONIST, PIONEER);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        final int tools = ((UnitFreeColonist) colonist).getTools();
        assertTrue(tools > 0);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        for (int update = 1; update < NativeTeachingCourse.REQUIRED_UPDATES; update++) {
            ownerRound(model, dutch);
            assertEquals(update, site.getTeachingUpdates());
            assertEquals(NativeTeachingCourse.Stage.LEARNING,
                    site.getTeachingStage().orElseThrow());
            assertTrue(colonist.isAtPlaceNativeCourse());
            assertEquals(UnitType.COLONIST, colonist.getType());
        }
        ownerRound(model, dutch);
        assertTrue(site.getTeachingPupilId().isEmpty());
        assertTrue(colonist.isAtPlaceLocation());
        assertEquals(PUPIL_ID, colonist.getId());
        assertEquals(UnitType.EXPERT_FARMER, colonist.getType());
        assertEquals(tools, ((UnitFreeColonist) colonist).getTools());
        assertEquals(1, countEvents(model, "COMPLETED"));
    }

    @Test
    void indenturedServantLearnsTheSkillDirectly() {
        final Model model = teachingScene(UnitType.INDENTURED_SERVANT,
                COLONIST_FIELDS);
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit servant = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(servant, site, site.getTeachableSkill());
        for (int update = 0; update < NativeTeachingCourse.REQUIRED_UPDATES; update++) {
            ownerRound(model, dutch);
        }
        assertEquals(UnitType.EXPERT_FARMER, servant.getType());
        assertTrue(servant.isAtPlaceLocation());
    }

    @Test
    void blockedCompletionGrantsOnceAndWaitsSafelyAcrossContinue() {
        final Model model = blockedScene();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        placeMoverOnExit(model, true);
        for (int update = 1; update < NativeTeachingCourse.REQUIRED_UPDATES; update++) {
            ownerRound(model, dutch);
        }
        // The fourth update grants the skill even though no square is free.
        assertFalse(model.hasNativeTeachingExit(colonist));
        ownerRound(model, dutch);
        assertEquals(NativeTeachingCourse.Stage.COMPLETED,
                site.getTeachingStage().orElseThrow());
        assertEquals(UnitType.EXPERT_FARMER, colonist.getType());
        assertTrue(colonist.isAtPlaceNativeCourse());
        assertEquals(1, countEvents(model, "COMPLETED"));

        // Continue restores the waiting colonist and the current report;
        // nothing
        // is granted or reported again by loading.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeSettlement restoredSite = settlement(restored);
        assertEquals(NativeTeachingCourse.Stage.COMPLETED,
                restoredSite.getTeachingStage().orElseThrow());
        assertTrue(restored.getUnitById(PUPIL_ID).isAtPlaceNativeCourse());
        assertEquals(UnitType.EXPERT_FARMER,
                restored.getUnitById(PUPIL_ID).getType());
        assertEquals(1, countEvents(restored, "COMPLETED"));

        // The report is the current turn's and ends with it. A later owner
        // round, still blocked, creates no new completion and keeps the person.
        ownerRound(restored, restored.getPlayerByName(DUTCH));
        assertEquals(NativeTeachingCourse.Stage.COMPLETED,
                settlement(restored).getTeachingStage().orElseThrow());
        assertTrue(restored.getUnitById(PUPIL_ID).isAtPlaceNativeCourse());
        assertEquals(UnitType.EXPERT_FARMER,
                restored.getUnitById(PUPIL_ID).getType());
        assertEquals(0, countEvents(restored, "COMPLETED"));
    }

    @Test
    void unblockingReleasesTheFinishedColonistWithoutRegranting() {
        final Model model = blockedScene();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        placeMoverOnExit(model, true);
        for (int update = 0; update < NativeTeachingCourse.REQUIRED_UPDATES; update++) {
            ownerRound(model, dutch);
        }
        assertTrue(colonist.isAtPlaceNativeCourse());
        // Exactly one completion report from the fourth update.
        assertEquals(1, countEvents(model, "COMPLETED"));
        placeMoverOnExit(model, false);
        ownerRound(model, dutch);
        assertTrue(colonist.isAtPlaceLocation());
        assertTrue(site.getTeachingPupilId().isEmpty());
        assertEquals(PUPIL_ID, colonist.getId());
        assertEquals(UnitType.EXPERT_FARMER, colonist.getType());
        // The earlier report ended with its turn; leaving adds no new one.
        assertEquals(0, countEvents(model, "COMPLETED"));
    }

    @Test
    void hostilityCancelsAtOnceEvenWhenTheExitIsBlockedAndNeverResumes() {
        final Model model = blockedScene();
        final Player dutch = model.getPlayerByName(DUTCH);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        placeMoverOnExit(model, true);
        ownerRound(model, dutch);
        assertEquals(1, site.getTeachingUpdates());
        model.addNativeLocalAlarm(site, dutch, 100);
        // The exit is still blocked, so the cancel is immediate but the
        // colonist
        // stays safely at the settlement.
        ownerRound(model, dutch);
        assertEquals(NativeTeachingCourse.Stage.CANCELED,
                site.getTeachingStage().orElseThrow());
        assertEquals(0, site.getTeachingUpdates());
        assertEquals(1, countEvents(model, "CANCELLED_HOSTILITY"));
        assertTrue(colonist.isAtPlaceNativeCourse());

        // Further rounds count nothing and grant nothing.
        ownerRound(model, dutch);
        ownerRound(model, dutch);
        assertEquals(0, site.getTeachingUpdates());
        assertEquals(UnitType.COLONIST, colonist.getType());
        assertTrue(colonist.isAtPlaceNativeCourse());

        // A free square lets the colonist leave with no skill.
        placeMoverOnExit(model, false);
        ownerRound(model, dutch);
        assertTrue(colonist.isAtPlaceLocation());
        assertTrue(site.getTeachingPupilId().isEmpty());
        assertEquals(UnitType.COLONIST, colonist.getType());
        assertEquals(0, countEvents(model, "COMPLETED"));
    }

    @Test
    void manualLeaveWithoutAFreeSquareIsRejectedAndChangesNothing() {
        final Model model = blockedScene();
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        placeMoverOnExit(model, true);
        assertFalse(model.hasNativeTeachingExit(colonist));
        assertThrows(IllegalStateException.class,
                () -> model.cancelNativeTeaching(colonist));
        assertTrue(colonist.isAtPlaceNativeCourse());
        assertEquals(NativeTeachingCourse.Stage.LEARNING,
                site.getTeachingStage().orElseThrow());
        assertEquals(0, site.getTeachingUpdates());
        assertEquals(UnitType.COLONIST, colonist.getType());
    }

    @Test
    void allEightNativeSkillsAreGrantedToTheSamePerson() {
        for (final UnitType skill : UnitType.NATIVE_TEACHABLE_SKILLS) {
            final Model built = teachingScene(UnitType.COLONIST, UNARMED);
            final ModelPo saved = built.save();
            saved.getNativeSettlements().get(0).setTeachableSkill(skill.name());
            final Model model = Model.make(saved,
                    MissionImpl.getPredefinedGameOverEvaluators());
            final Unit colonist = model.getUnitById(PUPIL_ID);
            final NativeSettlement site = settlement(model);
            assertEquals(skill, site.getTeachableSkill());
            model.startNativeTeaching(colonist, site, skill);
            ownerRound(model, model.getPlayerByName(DUTCH));
            ownerRound(model, model.getPlayerByName(DUTCH));
            ownerRound(model, model.getPlayerByName(DUTCH));
            ownerRound(model, model.getPlayerByName(DUTCH));
            assertTrue(
                    colonist.getType() == skill
                            || ((UnitFreeColonist) colonist).isVeteran(),
                    skill.name());
            assertEquals(PUPIL_ID, colonist.getId());
        }
    }

    @Test
    void queriesAndLoadsNeverRedrawTheOfferedSkill() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        final UnitType offered = site.getTeachableSkill();
        for (int query = 0; query < 3; query++) {
            model.getNativeVisitChoices(colonist, site);
            assertEquals(offered, site.getTeachableSkill());
        }
        model.startNativeTeaching(colonist, site, offered);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(offered, settlement(restored).getTeachableSkill());
        assertEquals(offered,
                restored.getNativeTeachingCourse(settlement(restored))
                        .orElseThrow().getSkill());
    }

    @Test
    void savedCourseWithoutItsPupilIsRejected() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        final ModelPo saved = model.save();
        saved.getNativeSettlements().get(0).setTeachingPupilId(999);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void savedStageMustMatchItsProgressAndItsPupil() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        final ModelPo saved = model.save();
        // A learner with four updates is incoherent: the grant would be lost.
        saved.getNativeSettlements().get(0).setTeachingUpdates(4);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
        // A finished course whose pupil still lacks the skill is incoherent.
        final ModelPo finished = model.save();
        finished.getNativeSettlements().get(0).setTeachingStage("COMPLETED");
        finished.getNativeSettlements().get(0).setTeachingUpdates(4);
        assertThrows(IllegalArgumentException.class, () -> Model.make(finished,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void courseSurvivesSaveAndContinueWithoutAdvancing() {
        final Model model = teachingScene(UnitType.COLONIST, UNARMED);
        final Unit colonist = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        model.startNativeTeaching(colonist, site, site.getTeachableSkill());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeSettlement restoredSite = settlement(restored);
        assertEquals(PUPIL_ID, restoredSite.getTeachingPupilId().orElseThrow());
        assertEquals(0, restoredSite.getTeachingUpdates());
        assertTrue(restored.getUnitById(PUPIL_ID).isAtPlaceNativeCourse());
        assertEquals(UnitType.EXPERT_FARMER,
                restored.getNativeTeachingCourse(restoredSite).orElseThrow()
                        .getSkill());
    }

    @Test
    void scoutOfAnotherPlayerCannotStartAtTheSettlement() {
        final Model model = island(NativeNation.AZTEC)
                .unit(FRANCE, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        final Unit foreign = model.getUnitById(PUPIL_ID);
        final NativeSettlement site = settlement(model);
        assertThrows(IllegalStateException.class, () -> model
                .startNativeTeaching(foreign, site, site.getTeachableSkill()));
        assertTrue(foreign.isAtPlaceLocation());
    }

}
