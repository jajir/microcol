package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;

/**
 * First mission. Find New World.
 */
public class Default_2_mission extends AbstractMission<Default_2_goals> {

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
        setMissionDefinition(
                new Default_2_missionDefinition(missionCallBack, model, new Default_2_goals()));
        model.addListener(getMissionDefinition());
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
