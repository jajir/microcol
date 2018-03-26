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
public class DefaultMissionFoundColony extends AbstractMission {

    private final static String NAME = "foundColony";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionFoundColony() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {

    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }

}
