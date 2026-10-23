package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

import com.google.gson.Gson;

/**
 * Free-play boundaries of R-0193: the zero-colony year rule, the frozen score
 * at spring 1800, the unscored continuation, the end at spring 1850 and the
 * single final result of an ordinary colony loss. Turn numbers count from a
 * 1490 start: spring 1600 is turn 110, spring 1800 turn 910 and spring 1850
 * turn 1110. The scored policy is an explicit calendar field; a mission
 * deadline in the same year is a different game.
 */
class ScoreBoundaryTest {

    /** Free play with no colony of the human nation: only the year rule. */
    private static final String EMPTY_HUMAN = "/maps/free-play.microcol";

    /**
     * Map whose human Dutch nation owns colonies from turn 0; the tests give it
     * the calendar they need.
     */
    private static final String COLONIAL_HUMAN = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH = "Dutch";

    private static final int SPRING_1600 = 110;

    private static final int SPRING_1800 = 910;

    private static final int SPRING_1850 = 1110;

    /** A saved game with the scored free-play calendar at the given turn. */
    private static ModelPo scored(final String map, final int turns) {
        final ModelPo data = new GameModelPoDao().loadFromClassPath(map)
                .getModel();
        data.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getCalendar().setScoredPlay(true);
        data.getCalendar().setNumberOfPlayedTurns(turns);
        return data;
    }

    private static Model make(final ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Model load(final String map, final int turns) {
        return make(scored(map, turns));
    }

    private static Model reload(final Model model) {
        return make(model.save());
    }

    /** A declared rebellion of the royal scene, in the free-play calendar. */
    private static Model declaredRebellion(final int turns) {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        data.getModel().getCalendar().setNumberOfPlayedTurns(turns);
        data.getModel().getCalendar()
                .setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getModel().getCalendar().setScoredPlay(true);
        return make(data.getModel());
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    /** The saved form of the frozen result, component by component. */
    private static String frozenJson(final Model model) {
        return new Gson().toJson(model.save().getGameManager().getGameResult());
    }

    /** Answer the choices that a legal round may legitimately pause for. */
    private static void settle(final Model model) {
        final Player human = dutch(model);
        if (model.isAwaitingTaxDecision()) {
            human.acceptTaxDemand(human.getTaxDemand().orElseThrow());
        }
        if (model.isAwaitingImmigrationChoice()) {
            human.chooseEarnedImmigrant(
                    human.getEarnedImmigrationOffers().get(0));
        }
    }

    /**
     * Ends the round that is running, through every actor of its turn order.
     */
    private static void finishRound(final Model model) {
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        for (int i = 0; i < 60 && model.getCalendar()
                .getNumberOfPlayedTurns() == before; i++) {
            settle(model);
            model.endTurn();
        }
        assertEquals(before + 1, model.getCalendar().getNumberOfPlayedTurns());
    }

    /** The saved state of a game that continued unscored, then was edited. */
    private static ModelPo continuedAt(final int turns,
            final Consumer<ModelPo> edit) {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        model.continueWithoutScoring(dutch(model));
        final ModelPo saved = model.save();
        saved.getCalendar().setNumberOfPlayedTurns(turns);
        edit.accept(saved);
        return saved;
    }

    // --- the zero-colony year rule ----------------------------------------

    @Test
    void zeroColoniesDoNotEndPlayBeforeSpring1600() {
        final Model model = load(EMPTY_HUMAN, SPRING_1600 - 2);
        model.startGame();
        finishRound(model);
        assertEquals(SPRING_1600 - 1,
                model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(model.getGameOverResult().isEmpty());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void zeroColoniesEndPlayAtSpring1600WithOneFinalResult() {
        final Model model = load(EMPTY_HUMAN, SPRING_1600 - 1);
        model.startGame();
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_NO_COLONIES,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertFalse(model.isGameRunning());
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.FINAL, frozen.getStatus());
        // The ordinary loss roundtrips with the same identity and components.
        final Model reloaded = reload(model);
        final GameResult after = reloaded.getFrozenResult().orElseThrow();
        assertEquals(frozen.getIdentity(), after.getIdentity());
        assertEquals(GameResult.Status.FINAL, after.getStatus());
        assertEquals(frozen.getBaseScore().getSubtotal(),
                after.getBaseScore().getSubtotal());
        assertEquals(frozen.getScore().getTotal(), after.getScore().getTotal());
        assertEquals(GameOverEvaluator.REASON_NO_COLONIES,
                reloaded.getGameOverResult().orElseThrow().getGameOverReason());
    }

    @Test
    void zeroColoniesAtSpring1800EndPlayInsteadOfTheScoreChoice() {
        final Model model = load(EMPTY_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_NO_COLONIES,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertFalse(model.isAwaitingScoreChoice());
        assertEquals(GameResult.Status.FINAL,
                model.getFrozenResult().orElseThrow().getStatus());
    }

    // --- the policy is explicit, not a deadline ---------------------------

    @Test
    void missionWithTheSameDeadlineRunsNoFreePlayRules() {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath(COLONIAL_HUMAN).getModel();
        data.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getCalendar().setNumberOfPlayedTurns(SPRING_1800 - 1);
        final Model model = make(data);
        assertFalse(model.getCalendar().isScoredPlay());
        model.startGame();
        finishRound(model);
        settle(model);
        assertTrue(model.isGameRunning());
        assertFalse(model.isAwaitingScoreChoice());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void missionDeadlineEndsWithItsOwnReasonAndNoResult() {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath(COLONIAL_HUMAN).getModel();
        data.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getCalendar().setNumberOfPlayedTurns(SPRING_1850 - 1);
        final Model model = make(data);
        model.startGame();
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_TIME_IS_UP,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void scoredCalendarMustCarryTheFreePlayEndYear() {
        final ModelPo data = scored(COLONIAL_HUMAN, 0);
        data.getCalendar().setEndYear(1900);
        assertThrows(IllegalArgumentException.class, () -> make(data));
    }

    // --- the spring 1800 boundary -----------------------------------------

    @Test
    void winter1799HasNoScoreBoundary() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 2);
        model.startGame();
        finishRound(model);
        assertEquals(SPRING_1800 - 1,
                model.getCalendar().getNumberOfPlayedTurns());
        assertFalse(model.isAwaitingScoreChoice());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void springOf1800FreezesTheScoreOnceAndWaitsForTheChoice() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        assertEquals(SPRING_1800, model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(model.isAwaitingScoreChoice());
        assertFalse(model.isGameRunning());
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.AWAITING_CONTINUATION,
                frozen.getStatus());
        assertEquals(DUTCH, frozen.getPlayerName());
        assertEquals(dutch(model).getResultScore().getTotal(),
                frozen.getScore().getTotal());
        assertEquals(dutch(model).getBaseScore().getSubtotal(),
                frozen.getBaseScore().getSubtotal());
    }

    @Test
    void awaitingChoiceRejectsGameplayAndLeavesTheWorldUnchanged() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, model::endTurn);
        assertThrows(IllegalStateException.class,
                () -> dutch(model).declareIndependence());
        final Player other = model.getPlayers().stream()
                .filter(player -> player != dutch(model)).findFirst()
                .orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> model.continueWithoutScoring(other));
        assertTrue(model.isAwaitingScoreChoice());
        // Saving is read-only and keeps the pause.
        assertEquals(before, snapshot(model));
    }

    @Test
    void continuingWithoutScoringKeepsTheFrozenResultAndStartsOneRound() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        final String identity = frozen.getIdentity();
        final long total = frozen.getScore().getTotal();
        model.continueWithoutScoring(dutch(model));
        settle(model);
        assertTrue(model.isGameRunning());
        assertFalse(model.isAwaitingScoreChoice());
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                model.getFrozenResult().orElseThrow().getStatus());
        // A second answer is stale and changes nothing.
        final String afterAnswer = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.continueWithoutScoring(dutch(model)));
        assertEquals(afterAnswer, snapshot(model));
        finishRound(model);
        final GameResult after = model.getFrozenResult().orElseThrow();
        assertEquals(identity, after.getIdentity());
        assertEquals(total, after.getScore().getTotal());
        assertEquals(SPRING_1800 + 1,
                model.getCalendar().getNumberOfPlayedTurns());
    }

    @Test
    void laterUnscoredChangesNeverReplaceTheFrozenComponents() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        final long gold = frozen.getBaseScore().getGold();
        model.continueWithoutScoring(dutch(model));
        dutch(model).setGold(dutch(model).getGold() + 5000);
        assertNotEquals(gold, dutch(model).getBaseScore().getGold());
        final GameResult after = model.getFrozenResult().orElseThrow();
        assertEquals(gold, after.getBaseScore().getGold());
        assertEquals(frozen.getScore().getTotal(), after.getScore().getTotal());
    }

    // --- reload and strict validation -------------------------------------

    @Test
    void reloadAtTheBoundaryShowsTheSameChoiceAndResult() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        final GameResult before = model.getFrozenResult().orElseThrow();
        final Model reloaded = reload(model);
        reloaded.startGame();
        assertTrue(reloaded.isAwaitingScoreChoice());
        final GameResult after = reloaded.getFrozenResult().orElseThrow();
        assertEquals(before.getIdentity(), after.getIdentity());
        assertEquals(GameResult.Status.AWAITING_CONTINUATION,
                after.getStatus());
        assertEquals(before.getScore().getTotal(), after.getScore().getTotal());
        assertEquals(before.getScore().getBellsAfterTarget(),
                after.getScore().getBellsAfterTarget());
        // Loading and starting wrote nothing: the frozen result is unchanged.
        assertEquals(frozenJson(model), frozenJson(reloaded));
    }

    @Test
    void reloadAfterContinuationKeepsTheSameIdentityAndComponents() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        final GameResult before = model.getFrozenResult().orElseThrow();
        model.continueWithoutScoring(dutch(model));
        finishRound(model);
        final Model reloaded = reload(model);
        reloaded.startGame();
        final GameResult after = reloaded.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.CONTINUED_UNSCORED, after.getStatus());
        assertEquals(before.getIdentity(), after.getIdentity());
        assertEquals(before.getBaseScore().getSubtotal(),
                after.getBaseScore().getSubtotal());
        assertEquals(before.getScore().getTotal(), after.getScore().getTotal());
    }

    @Test
    void savedGamePastSpring1800WithoutItsFrozenResultIsRejected() {
        final ModelPo saved = continuedAt(SPRING_1800 + 1,
                po -> po.getGameManager().setGameResult(null));
        assertThrows(IllegalArgumentException.class, () -> make(saved));
    }

    @Test
    void finishedScoredGameWithoutItsFinalResultIsRejected() {
        final Model model = load(EMPTY_HUMAN, SPRING_1600 - 1);
        model.startGame();
        finishRound(model);
        final ModelPo saved = model.save();
        saved.getGameManager().setGameResult(null);
        assertThrows(IllegalArgumentException.class, () -> make(saved));
    }

    @Test
    void impossibleResultHistoriesAreRejectedNotGuessed() {
        final Model ended = load(EMPTY_HUMAN, SPRING_1600 - 1);
        ended.startGame();
        finishRound(ended);
        // A final result of a game that is not finished.
        final ModelPo live = ended.save();
        live.getGameManager().setGameFinished(false);
        live.getGameManager().setGameOverReason(null);
        assertThrows(IllegalArgumentException.class, () -> make(live));
        // A final result under a reason that did not produce it.
        final ModelPo wrongReason = ended.save();
        wrongReason.getGameManager()
                .setGameOverReason(GameOverEvaluator.REASON_FREE_PLAY_ENDED);
        assertThrows(IllegalArgumentException.class, () -> make(wrongReason));
        // A tampered total and a missing identity.
        final ModelPo tampered = ended.save();
        tampered.getGameManager().getGameResult().setTotal(
                tampered.getGameManager().getGameResult().getTotal() + 1);
        assertThrows(IllegalArgumentException.class, () -> make(tampered));
        final ModelPo nameless = ended.save();
        nameless.getGameManager().getGameResult().setIdentity(null);
        assertThrows(IllegalArgumentException.class, () -> make(nameless));
        // A frozen result in an unscored calendar.
        final ModelPo unscored = continuedAt(SPRING_1800 + 1,
                po -> po.getCalendar().setScoredPlay(false));
        assertThrows(IllegalArgumentException.class, () -> make(unscored));
        // A pending choice of a game that is already finished.
        final ModelPo choice = continuedAt(SPRING_1800 + 1, po -> {
            po.getGameManager().getGameResult()
                    .setStatus(GameResult.Status.AWAITING_CONTINUATION.name());
            po.getGameManager().setGameFinished(true);
            po.getGameManager()
                    .setGameOverReason(GameOverEvaluator.REASON_NO_COLONIES);
        });
        assertThrows(IllegalArgumentException.class, () -> make(choice));
    }

    // --- the unscored loss and spring 1850 --------------------------------

    @Test
    void colonyLossAfterTheContinuationKeepsTheEarlierResult() {
        final Model model = make(continuedAt(SPRING_1800 + 40, po -> {
        }));
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        model.startGame();
        // Every resident steps out onto its colony's square; the last one to
        // leave removes the colony, the only path that destroys one.
        for (final Colony colony : new ArrayList<>(
                model.getColonies(dutch(model)))) {
            new ArrayList<>(colony.getUnitsInsideColony()).forEach(
                    worker -> worker.placeToLocation(colony.getLocation()));
            model.destroyColony(colony);
        }
        assertTrue(model.getColonies(dutch(model)).isEmpty());
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_NO_COLONIES,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        final GameResult after = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.CONTINUED_UNSCORED, after.getStatus());
        assertEquals(frozen.getIdentity(), after.getIdentity());
        assertEquals(frozen.getScore().getTotal(), after.getScore().getTotal());
        // The unscored end roundtrips with the earlier result.
        final GameResult reloaded = reload(model).getFrozenResult()
                .orElseThrow();
        assertEquals(frozen.getIdentity(), reloaded.getIdentity());
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                reloaded.getStatus());
    }

    @Test
    void winter1849IsStillOpenAndSpring1850EndsAllFreePlay() {
        // An authored valid unscored state two rounds before spring 1850.
        final Model model = make(continuedAt(SPRING_1850 - 2, po -> {
        }));
        final String identity = model.getFrozenResult().orElseThrow()
                .getIdentity();
        final long total = model.getFrozenResult().orElseThrow().getScore()
                .getTotal();
        model.startGame();
        finishRound(model);
        assertEquals(SPRING_1850 - 1,
                model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(model.getGameOverResult().isEmpty());
        settle(model);
        assertTrue(model.isGameRunning());
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_FREE_PLAY_ENDED,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertFalse(model.isGameRunning());
        assertThrows(IllegalStateException.class, model::endTurn);
        final GameResult after = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.CONTINUED_UNSCORED, after.getStatus());
        assertEquals(identity, after.getIdentity());
        assertEquals(total, after.getScore().getTotal());
        // The ended game reloads with the same terminal result.
        final Model reloaded = reload(model);
        assertEquals(identity,
                reloaded.getFrozenResult().orElseThrow().getIdentity());
        assertEquals(GameOverEvaluator.REASON_FREE_PLAY_ENDED,
                reloaded.getGameOverResult().orElseThrow().getGameOverReason());
    }

    // --- a declared rebellion ----------------------------------------------

    @Test
    void declaredRebellionStaysScoredPastSpring1800() {
        final Model model = declaredRebellion(SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        assertTrue(model.isGameRunning());
        assertFalse(model.isAwaitingScoreChoice());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void unresolvedRebellionIsLostAndFrozenAtSpring1850() {
        final Model model = declaredRebellion(SPRING_1850 - 1);
        model.startGame();
        finishRound(model);
        assertEquals(GameOverEvaluator.REASON_REBELLION_UNRESOLVED,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertEquals(GameOverEvaluator.REASON_REBELLION_UNRESOLVED,
                dutch(model).getRebellionLossReason().orElseThrow());
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.FINAL, frozen.getStatus());
        assertEquals(dutch(model).getResultScore().getTotal(),
                frozen.getScore().getTotal());
        // The rebellion's final result roundtrips.
        final GameResult reloaded = reload(model).getFrozenResult()
                .orElseThrow();
        assertEquals(frozen.getIdentity(), reloaded.getIdentity());
    }
}
