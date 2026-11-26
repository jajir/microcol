package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.akropia.model.game.GameModel;
import org.akropia.model.game.po.GameModelPoDao;
import org.akropia.model.store.ModelPo;

/** Free games have objectives and no year-based score or play boundary. */
class ScoreBoundaryTest {
    static ModelPo data(int turn) {
        ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel();
        data.getCalendar().setScoredPlay(true);
        data.getCalendar().setNumberOfPlayedTurns(turn);
        return data;
    }

    static Model make(ModelPo data) {
        return Model.make(data, GameModel.gameOverEvaluators());
    }

    static void round(Model model) {
        int before = model.getCalendar().getNumberOfPlayedTurns();
        for (int i = 0; i < 60 && model.getCalendar()
                .getNumberOfPlayedTurns() == before; i++) {
            Player human = model.getPlayerByName("Dutch");
            if (model.isAwaitingTaxDecision())
                human.acceptTaxDemand(human.getTaxDemand().orElseThrow());
            if (model.isAwaitingImmigrationChoice())
                human.chooseEarnedImmigrant(
                        human.getEarnedImmigrationOffers().get(0));
            model.endTurn();
        }
        assertEquals(before + 1, model.getCalendar().getNumberOfPlayedTurns());
    }

    @ParameterizedTest
    @ValueSource(ints = { 109, 909, 1109, 1309 })
    void formerDeadlinesDoNotEndOrFreezeFreePlay(int turn) {
        Model model = make(data(turn));
        model.startGame();
        round(model);
        assertTrue(model.isGameRunning());
        assertTrue(model.getFrozenResult().isEmpty());
        assertFalse(model.isAwaitingScoreChoice());
        Model restored = make(model.save());
        restored.startGame();
        assertTrue(restored.isGameRunning());
    }

    @Test
    void missionStillHasItsAuthoredDeadline() {
        Calendar mission = new Calendar(1, 2, 83, false);
        mission.endRound();
        assertTrue(mission.isFinished());
        Calendar free = new Calendar(1, 2, 83, true);
        free.endRound();
        free.endRound();
        assertFalse(free.isFinished());
    }

    @Test
    void freshNationWithoutColonyIsNotEliminated() {
        Model model = make(data(1400));
        model.startGame();
        assertFalse(model.getFreePlayObjectives()
                .isEliminated(model.getPlayerByName("Dutch")));
        assertTrue(model.getGameOverResult().isEmpty());
    }

    @Test
    void lossOfLastEstablishedColonyEndsPlayAtAnyDate() {
        ModelPo data = data(0);
        data.setEstablishedNations(List.of("Dutch"));
        Model model = make(data);
        model.startGame();
        assertEquals(GameOverEvaluator.REASON_NO_COLONIES,
                model.getGameOverResult().orElseThrow().getGameOverReason());
        Model restored = make(model.save());
        assertTrue(restored.getFreePlayObjectives()
                .isEliminated(restored.getPlayerByName("Dutch")));
        assertEquals(model.getFrozenResult().orElseThrow().getIdentity(),
                restored.getFrozenResult().orElseThrow().getIdentity());
    }

    @Test
    void objectiveSelectionSurvivesSaveAndReload() {
        ModelPo data = data(0);
        data.setFreePlayGoals(
                List.of(FreePlayGoal.CONGRESS, FreePlayGoal.DOMINATION));
        Model model = make(data);
        model.startGame();
        assertEquals(data.getFreePlayGoals(),
                make(model.save()).getFreePlayObjectives().getEnabled());
    }
}
