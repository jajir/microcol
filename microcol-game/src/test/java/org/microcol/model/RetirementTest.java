package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

import com.google.gson.Gson;

/**
 * Retirement of R-0194 in scored free play. A live retirement freezes one final
 * result at the score it previewed; an unscored continuation keeps its frozen
 * result unchanged. Stale and repeated confirmations change nothing, a canceled
 * preview changes nothing, and missions and the pending spring 1800 choice
 * offer no retirement. Turn numbers count from a 1490 start, as in
 * {@link ScoreBoundaryTest}.
 */
class RetirementTest {

    /**
     * Map whose human Dutch nation owns colonies from turn 0; the tests give it
     * the calendar they need.
     */
    private static final String COLONIAL_HUMAN = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH = "Dutch";

    /** Winter 1799: the round before spring 1800 freezes the score. */
    private static final int WINTER_1799 = 909;

    private static final int SPRING_1800 = 910;

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

    /** A running scored game that continued unscored, at the given turn. */
    private static Model continued(final int turns) {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        model.continueWithoutScoring(dutch(model));
        final ModelPo saved = model.save();
        saved.getCalendar().setNumberOfPlayedTurns(turns);
        return make(saved);
    }

    /** An unscored mission with the same deadline; no free-play rules apply. */
    private static Model mission(final int turns) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath(COLONIAL_HUMAN).getModel();
        data.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getCalendar().setNumberOfPlayedTurns(turns);
        return make(data);
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

    /** Ends the turns of other nations until the human nation is current. */
    private static void toHumanTurn(final Model model) {
        for (int i = 0; i < 60
                && model.getCurrentPlayer() != dutch(model); i++) {
            settle(model);
            model.endTurn();
        }
        assertEquals(dutch(model), model.getCurrentPlayer());
    }

    /** A running live game, one round before winter 1799, on the human turn. */
    private static Model liveOnHumanTurn() {
        final Model model = load(COLONIAL_HUMAN, WINTER_1799 - 1);
        model.startGame();
        toHumanTurn(model);
        return model;
    }

    // --- preview ------------------------------------------------------------

    @Test
    void livePreviewShowsTheCurrentScoreAndChangesNothing() {
        final Model model = liveOnHumanTurn();
        final String before = snapshot(model);
        final RetirementQuote quote = model.getRetirementQuote().orElseThrow();
        assertFalse(quote.isContinued());
        assertTrue(quote.getIdentity().isEmpty());
        assertEquals(DUTCH, quote.getPlayerName());
        assertEquals(dutch(model).getResultScore().getTotal(),
                quote.getScore().getTotal());
        assertEquals(dutch(model).getBaseScore().getSubtotal(),
                quote.getBaseScore().getSubtotal());
        // Reading the preview and cancelling it leave the game as it was.
        assertEquals(before, snapshot(model));
        assertTrue(model.isGameRunning());
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void retirementIsOfferedOnlyOnTheHumanTurn() {
        final Model model = load(COLONIAL_HUMAN, WINTER_1799 - 1);
        model.startGame();
        if (model.getCurrentPlayer() != dutch(model)) {
            assertTrue(model.getRetirementQuote().isEmpty());
        }
        toHumanTurn(model);
        assertTrue(model.getRetirementQuote().isPresent());
    }

    // --- confirmation -------------------------------------------------------

    @Test
    void confirmedLiveRetirementFreezesOneFinalResultAndEndsThePlay() {
        final Model model = liveOnHumanTurn();
        final RetirementQuote quote = model.getRetirementQuote().orElseThrow();
        model.retire(quote);
        assertTrue(model.isGameFinished());
        assertFalse(model.isGameRunning());
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.FINAL, frozen.getStatus());
        assertEquals(DUTCH, frozen.getPlayerName());
        assertEquals(quote.getScore().getTotal(), frozen.getScore().getTotal());
        assertEquals(quote.getBaseScore().getSubtotal(),
                frozen.getBaseScore().getSubtotal());
        assertThrows(IllegalStateException.class, model::endTurn);
        // The retired result roundtrips with its identity, components and end.
        final Model reloaded = reload(model);
        final GameResult after = reloaded.getFrozenResult().orElseThrow();
        assertEquals(frozen.getIdentity(), after.getIdentity());
        assertEquals(GameResult.Status.FINAL, after.getStatus());
        assertEquals(frozen.getScore().getTotal(), after.getScore().getTotal());
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                reloaded.getGameOverResult().orElseThrow().getGameOverReason());
        assertTrue(reloaded.getRetirementQuote().isEmpty());
    }

    @Test
    void staleLivePreviewIsRejectedWithoutChangeAndAFreshPreviewRetires() {
        final Model model = liveOnHumanTurn();
        final RetirementQuote stale = model.getRetirementQuote().orElseThrow();
        dutch(model).setGold(dutch(model).getGold() + 5000);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> model.retire(stale));
        assertEquals(before, snapshot(model));
        assertTrue(model.isGameRunning());
        assertTrue(model.getFrozenResult().isEmpty());
        // The player previews again; the new preview is the one confirmed.
        final RetirementQuote fresh = model.getRetirementQuote().orElseThrow();
        assertNotEquals(stale.getBaseScore().getGold(),
                fresh.getBaseScore().getGold());
        model.retire(fresh);
        assertEquals(fresh.getScore().getTotal(),
                model.getFrozenResult().orElseThrow().getScore().getTotal());
    }

    @Test
    void repeatedConfirmationIsRejectedBeforeAnyChange() {
        final Model model = liveOnHumanTurn();
        final RetirementQuote quote = model.getRetirementQuote().orElseThrow();
        model.retire(quote);
        final String after = snapshot(model);
        assertThrows(IllegalStateException.class, () -> model.retire(quote));
        assertEquals(after, snapshot(model));
        assertTrue(model.getRetirementQuote().isEmpty());
    }

    // --- exit without retiring ----------------------------------------------

    @Test
    void exitLeavesTheLiveGameOpenAndRecordsNoResult() {
        final Model model = liveOnHumanTurn();
        final ModelPo saved = model.save();
        model.stop();
        assertTrue(model.getFrozenResult().isEmpty());
        assertTrue(model.getGameOverResult().isEmpty());
        assertFalse(model.isGameFinished());
        // The saved position reopens as the same running game, still offering
        // its retirement and without any frozen result.
        final Model reopened = make(saved);
        reopened.startGame();
        assertTrue(reopened.isGameRunning());
        assertTrue(reopened.getFrozenResult().isEmpty());
        assertTrue(reopened.getRetirementQuote().isPresent());
    }

    // --- unscored continuation --------------------------------------------

    @Test
    void unscoredRetirementKeepsTheFrozenResultAndAddsNoSecondOne() {
        final Model model = continued(SPRING_1800 + 40);
        model.startGame();
        toHumanTurn(model);
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        final String frozenBefore = frozenJson(model);
        final RetirementQuote quote = model.getRetirementQuote().orElseThrow();
        assertTrue(quote.isContinued());
        assertEquals(frozen.getIdentity(), quote.getIdentity().orElseThrow());
        assertEquals(frozen.getScore().getTotal(), quote.getScore().getTotal());
        model.retire(quote);
        assertTrue(model.isGameFinished());
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        // The frozen identity and components stay exactly as they were.
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                model.getFrozenResult().orElseThrow().getStatus());
        assertEquals(frozenBefore, frozenJson(model));
        // A reload keeps the same result and the same end.
        final Model reloaded = reload(model);
        assertEquals(frozenBefore, frozenJson(reloaded));
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                reloaded.getGameOverResult().orElseThrow().getGameOverReason());
        assertTrue(reloaded.getRetirementQuote().isEmpty());
    }

    // --- refusals ----------------------------------------------------------

    @Test
    void pendingSpring1800ChoiceOffersNoRetirementAndRefusesOne() {
        final Model model = load(COLONIAL_HUMAN, SPRING_1800 - 1);
        model.startGame();
        finishRound(model);
        assertTrue(model.isAwaitingScoreChoice());
        assertTrue(model.getRetirementQuote().isEmpty());
        final RetirementQuote preview = RetirementQuote.live(dutch(model));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> model.retire(preview));
        assertEquals(before, snapshot(model));
        assertTrue(model.isAwaitingScoreChoice());
        assertEquals(GameResult.Status.AWAITING_CONTINUATION,
                model.getFrozenResult().orElseThrow().getStatus());
    }

    @Test
    void missionDeadlineNeverOffersOrConfirmsRetirement() {
        final Model model = mission(SPRING_1800 - 2);
        model.startGame();
        toHumanTurn(model);
        assertFalse(model.getCalendar().isScoredPlay());
        assertTrue(model.getRetirementQuote().isEmpty());
        final RetirementQuote preview = RetirementQuote.live(dutch(model));
        assertThrows(IllegalStateException.class, () -> model.retire(preview));
        assertTrue(model.getFrozenResult().isEmpty());
    }

    /** A running scored game of a declared rebellion, on the human turn. */
    private static Model declaredOnHumanTurn() {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        data.getModel().getCalendar().setNumberOfPlayedTurns(SPRING_1800 - 2);
        data.getModel().getCalendar()
                .setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        data.getModel().getCalendar().setScoredPlay(true);
        final Model model = make(data.getModel());
        model.startGame();
        toHumanTurn(model);
        return model;
    }

    @Test
    void openDeclaredRebellionRetiresWithoutIndependenceBonus() {
        final Model model = declaredOnHumanTurn();
        assertTrue(dutch(model).isDeclaredIndependence());
        final String before = snapshot(model);
        final RetirementQuote quote = model.getRetirementQuote().orElseThrow();
        assertEquals(before, snapshot(model));
        assertEquals(0, quote.getScore().getOrderBonusPercent());
        assertEquals(0L, quote.getScore().getEarlyPoints());
        assertEquals(dutch(model).getResultScore().getTotal(),
                quote.getScore().getTotal());
        model.retire(quote);
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        final GameResult frozen = model.getFrozenResult().orElseThrow();
        assertEquals(GameResult.Status.FINAL, frozen.getStatus());
        assertTrue(frozen.getVictoryOrder().isEmpty());
        assertTrue(frozen.getDeclarationYear().isPresent());
        assertEquals(quote.getScore().getTotal(), frozen.getScore().getTotal());
        // The strict reload keeps the identity, the total and the end.
        final Model reloaded = reload(model);
        final GameResult after = reloaded.getFrozenResult().orElseThrow();
        assertEquals(frozen.getIdentity(), after.getIdentity());
        assertEquals(GameResult.Status.FINAL, after.getStatus());
        assertEquals(frozen.getScore().getTotal(), after.getScore().getTotal());
        assertEquals(GameOverEvaluator.REASON_RETIRED,
                reloaded.getGameOverResult().orElseThrow().getGameOverReason());
        assertTrue(reloaded.getRetirementQuote().isEmpty());
    }

    @Test
    void retiredOpenRebellionRejectsALossReasonOnLoad() {
        final Model model = declaredOnHumanTurn();
        model.retire(model.getRetirementQuote().orElseThrow());
        final ModelPo saved = model.save();
        saved.getGameManager().setGameOverReason(
                GameOverEvaluator.REASON_REBELLION_NO_COLONIES);
        assertThrows(IllegalArgumentException.class, () -> make(saved));
    }

    @Test
    void declaredRetirementIsNotOfferedAfterTheRebellionEnded() {
        final Model model = declaredOnHumanTurn();
        final RetirementQuote preview = model.getRetirementQuote()
                .orElseThrow();
        dutch(model).markRebellionLost(
                GameOverEvaluator.REASON_REBELLION_NO_COLONIES);
        assertTrue(model.getRetirementQuote().isEmpty());
        assertThrows(IllegalStateException.class, () -> model.retire(preview));
        assertTrue(model.getFrozenResult().isEmpty());
    }

    @Test
    void savedRetirementReasonWithoutAScoredResultIsRejected() {
        final ModelPo saved = scored(COLONIAL_HUMAN, WINTER_1799 - 1);
        saved.getGameManager().setGameFinished(true);
        saved.getGameManager()
                .setGameOverReason(GameOverEvaluator.REASON_RETIRED);
        assertThrows(IllegalArgumentException.class, () -> make(saved));
    }
}
