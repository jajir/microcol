package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.collect.Lists;

/**
 * First mission. Thrive.
 */
public class Default_3_mission extends AbstractMission<Empty_missionContext> {

    private final static String NAME = "thrive";

    public final static String GAME_OVER_REASON = "defaultMission3";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    Default_3_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new Default_3_missionDefinition(this, missionCallBack));
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    private GameOverResult evaluateGameOver(final Model model) {
        if (getHumanPlayer(model).isDeclaredIndependence()) {
            final Player player = model.getPlayerByName("Dutch's King");
            if (getNumberOfMilitaryUnitsForPlayer(player) <= 0) {
                setFinished(true);
                flush();
                return new GameOverResult(GAME_OVER_REASON);
            }
        }
        return null;
    }

    @Override
    protected Empty_missionContext getNewContext() {
        return new Empty_missionContext();
    }

}
