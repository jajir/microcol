package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.ModelListener;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Abstract mission. Contain some basic functionality.
 */
public final class MissionImpl<G extends MissionGoals> implements Mission<G> {

    public final static String GAME_OVER_REASON_ALL_GOALS_ARE_DONE = "gameOverReasonAllGoalsAreDone";

    private final MissionName missionName;

    private final ModelListener modelListener;

    private final AbstractMission<G> mission;

    MissionImpl(final MissionName missionName, ModelListener modelListener,
            final AbstractMission<G> mission) {
        this.missionName = Preconditions.checkNotNull(missionName);
        this.modelListener = Preconditions.checkNotNull(modelListener);
        this.mission = Preconditions.checkNotNull(mission);
        mission.getModel().addListener(getModelListener());
    }

    @Override
    public G getGoals() {
        return mission.getGoals();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("missionName", missionName).toString();
    }

    /**
     * Get list of functions that evaluate game over conditions. When one
     * function return nun-null return than game is over and returned object is
     * passed in game over event to front-end.
     *
     * @return list of game over evaluators
     */
    public static List<Function<Model, GameOverResult>> getPredefinedGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }

    @Override
    public Map<String, String> saveGoals() {
        final Map<String, String> out = new HashMap<String, String>();
        mission.save(out);
        return out;
    }

    /**
     * @return the missionDefinition
     */
    public ModelListener getModelListener() {
        return modelListener;
    }

}
