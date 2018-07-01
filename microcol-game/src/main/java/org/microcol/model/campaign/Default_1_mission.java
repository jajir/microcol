package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

/**
 * First mission. Find New World.
 */
public class Default_1_mission extends AbstractMission<Default_1_goals> {

    private final static String MISSION_NAME = "findNewWorld";

    public final static String GAME_OVER_REASON = "defaultMission1";

    private final static String MISSION_MODEL_FILE = "/maps/default-" + MISSION_NAME + ".json";

    private MissionDefinition<Default_1_goals> missionDefinition;

    Default_1_mission() {
        super(MISSION_NAME, 0, MISSION_MODEL_FILE);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        missionDefinition = new Default_1_missionDefinition(model, missionCallBack,
                new Default_1_goals());
        setMissionDefinition(missionDefinition);
        model.addListener(missionDefinition);
    }

    @Override
    public Default_1_goals getGoals() {
        return super.getGoals();
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

}
