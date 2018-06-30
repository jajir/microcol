package org.microcol.model.campaign;

import java.util.HashMap;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

/**
 * First mission. Find New World.
 */
public class Default_1_mission extends AbstractMission<Default_1_goals, Default_1_missionContext> {

    private final static String MISSION_NAME = "findNewWorld";

    public final static String GAME_OVER_REASON = "defaultMission1";

    private final static String MISSION_MODEL_FILE = "/maps/default-" + MISSION_NAME + ".json";

    final static Integer TRAGET_AMOUNT_OF_CIGARS = 30;

    Default_1_mission() {
        super(MISSION_NAME, 0, MISSION_MODEL_FILE, new Default_1_goals());
    }

    @Override
    public void initialize(final ModelPo modelPo) {
        super.initialize(modelPo);
        if (modelPo.getCampaign().getData() == null) {
            getGoals().initialize(new HashMap<>());
        } else {
            getGoals().initialize(modelPo.getCampaign().getData());
        }
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(
                new Default_1_missionDefinition(this, model, missionCallBack, getGoals()));
    }

    @Override
    protected GameOverResult evaluateGameOver(final Model model) {
        if (getGoals().isAllGoalsDone()) {
            setFinished(true);
            return new GameOverResult(GAME_OVER_REASON);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    CampaignNames getCampaignKey() {
        return CampaignNames.defaultCampaign;
    }

    @Override
    protected Default_1_missionContext getNewContext() {
        return new Default_1_missionContext();
    }

}
