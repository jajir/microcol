package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

import com.google.common.collect.Lists;

/**
 * First mission. Find New World.
 */
public class Default_1_mission extends AbstractMission<Default_1_missionContext> {

    private final static String MISSION_NAME = "findNewWorld";

    public final static String GAME_OVER_REASON = "defaultMission1";

    private final static String MISSION_MODEL_FILE = "/maps/default-" + MISSION_NAME + ".json";

    final static Integer TRAGET_AMOUNT_OF_CIGARS = 30;

    Default_1_mission() {
        super(MISSION_NAME, 0, MISSION_MODEL_FILE);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new Default_1_missionDefinition(this, model, missionCallBack));
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    @SuppressWarnings("unused")
    private GameOverResult evaluateGameOver(final Model model) {
        if (getContext().getCigarsWasSold() >= TRAGET_AMOUNT_OF_CIGARS) {
            setFinished(true);
            flush();
            return new GameOverResult(GAME_OVER_REASON);
        } else {
            return null;
        }
    }

    @Override
    protected Default_1_missionContext getNewContext() {
        return new Default_1_missionContext();
    }

}
