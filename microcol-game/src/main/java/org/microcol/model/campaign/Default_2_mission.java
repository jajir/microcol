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
public class Default_2_mission extends AbstractMission<Default_2_missionContext> {

    private final static String NAME = "buildArmy";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    public final static String GAME_OVER_REASON = "defaultMission2";

    final static Integer TARGET_NUMBER_OF_COLONIES = 3;

    final static Integer TARGET_NUMBER_OF_GOLD = 5000;

    final static Integer TARGET_NUMBER_OF_MILITARY_UNITS = 15;

    Default_2_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new Default_2_missionDefinition(this, missionCallBack));
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    @SuppressWarnings("unused")
    private GameOverResult evaluateGameOver(final Model model) {
        if (getContext().isWasNumberOfMilitaryUnitsTargetReached()) {
            setFinished(true);
            flush();
            return new GameOverResult(GAME_OVER_REASON);
        } else {
            return null;
        }
    }

    @Override
    protected Default_2_missionContext getNewContext() {
        return new Default_2_missionContext();
    }

}
