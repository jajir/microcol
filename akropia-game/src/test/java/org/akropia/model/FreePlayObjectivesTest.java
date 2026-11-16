package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.PlayerPo;

class FreePlayObjectivesTest {
    private static void completeCongress(ModelPo data, String nation) {
        PlayerPo player = data.getGameManager().getPlayers().stream()
                .filter(p -> nation.equals(p.getName())).findFirst()
                .orElseThrow();
        player.setCongress(Congress.initial());
        player.getCongress().setActivated(true);
        player.getCongress().setGeneration(1);
        player.getCongress().setMembers(Arrays.asList(Founder.values()));
    }

    @Test
    void congressWinFreezesGoalScoreAndSupportsUnscoredContinuation() {
        ModelPo data = ScoreBoundaryTest.data(1300);
        data.setFreePlayGoals(List.of(FreePlayGoal.CONGRESS));
        completeCongress(data, "Dutch");
        Model model = ScoreBoundaryTest.make(data);
        model.startGame();
        assertEquals(GameOverEvaluator.REASON_OBJECTIVE_VICTORY,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        GameResult result = model.getFrozenResult().orElseThrow();
        assertEquals(List.of(FreePlayGoal.CONGRESS),
                result.getObjectives().getAchieved());
        assertEquals(1300, result.getObjectives().getTurn());
        assertTrue(model.canContinueAfterVictory());
        model.continueAfterVictory();
        model.getPlayerByName("Dutch").setGold(99999);
        Model restored = ScoreBoundaryTest.make(model.save());
        restored.startGame();
        assertTrue(restored.isGameRunning());
        assertEquals(result.getIdentity(),
                restored.getFrozenResult().orElseThrow().getIdentity());
        assertEquals(result.getScore().getTotal(),
                restored.getFrozenResult().orElseThrow().getScore().getTotal());
        HallOfFameEntry entry = HallOfFameEntry
                .load(model.getHallOfFameEntry().orElseThrow().save());
        assertEquals(result.getObjectives().getAchieved(),
                entry.getObjectives().getAchieved());
    }

    @Test
    void disabledCongressGoalDoesNotEndTheGame() {
        ModelPo data = ScoreBoundaryTest.data(0);
        completeCongress(data, "Dutch");
        Model model = ScoreBoundaryTest.make(data);
        model.startGame();
        assertTrue(model.getGameOverResult().isEmpty());
    }

    @Test
    void opponentCompletingEnabledGoalWinsTheRace() {
        ModelPo data = ScoreBoundaryTest.data(0);
        data.setFreePlayGoals(List.of(FreePlayGoal.CONGRESS));
        completeCongress(data, "France");
        Model model = ScoreBoundaryTest.make(data);
        model.startGame();
        assertEquals(GameOverEvaluator.REASON_OPPONENT_VICTORY,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        assertEquals("France", model.getFrozenResult().orElseThrow()
                .getObjectives().getWinner().orElseThrow());
        assertFalse(model.canContinueAfterVictory());
    }

    @Test
    void losingAllRivalColoniesCanCompleteMultipleGoalsOnce() {
        ModelPo data = ScoreBoundaryTest.data(0);
        data.setFreePlayGoals(
                List.of(FreePlayGoal.CONGRESS, FreePlayGoal.DOMINATION));
        completeCongress(data, "Dutch");
        data.getColonies().removeIf(c -> "France".equals(c.getOwnerName()));
        data.setEstablishedNations(List.of("France"));
        // Remove former colony workers together with the authored colony.
        data.getUnits().removeIf(u -> "France".equals(u.getOwnerId()));
        Model model = ScoreBoundaryTest.make(data);
        model.startGame();
        assertEquals(List.of(FreePlayGoal.CONGRESS, FreePlayGoal.DOMINATION),
                model.getFrozenResult().orElseThrow().getObjectives()
                        .getAchieved());
    }
}
