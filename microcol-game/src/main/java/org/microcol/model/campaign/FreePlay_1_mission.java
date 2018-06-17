package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

import com.google.common.collect.Lists;

/**
 * Free play mission definition. There are no limitations player can do
 * anything.
 */
public class FreePlay_1_mission extends AbstractMission<Empty_missionContext> {

    /**
     * Free play game map.
     */
    private final static String FREE_PLAY_MISSION_MAP = "/maps/free-play.json";

    FreePlay_1_mission() {
        super(FreePlay_campaign.FREE_PLAY, 0, FREE_PLAY_MISSION_MAP);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new FreePlay_1_missionDefinition(this, missionCallBack));
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }

    @Override
    protected Empty_missionContext getNewContext() {
        return new Empty_missionContext();
    }

}
