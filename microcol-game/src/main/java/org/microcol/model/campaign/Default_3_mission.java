package org.microcol.model.campaign;

import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.Player;

/**
 * First mission. Thrive.
 */
public class Default_3_mission extends AbstractMission<MissionGoalsEmpty> {

    private final static String NAME = "thrive";

    public final static String GAME_OVER_REASON = "defaultMission3";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    Default_3_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        setMissionDefinition(
                new Default_3_missionDefinition(missionCallBack, model, new MissionGoalsEmpty()));
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

    protected Player getHumanPlayer(final Model model) {
        return model.getPlayers().stream().filter(p -> p.isHuman()).findAny()
                .orElseThrow(() -> new MicroColException("There is no human player."));
    }

    protected int getNumberOfMilitaryUnitsForPlayer(final Player player) {
        return (int) player.getAllUnits().stream().filter(unit -> unit.getType().canAttack())
                .count();
    }

    @SuppressWarnings("unchecked")
    @Override
    CampaignNames getCampaignKey() {
        return CampaignNames.defaultCampaign;
    }

}
