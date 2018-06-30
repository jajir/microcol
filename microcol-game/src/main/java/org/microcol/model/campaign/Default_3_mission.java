package org.microcol.model.campaign;

import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.Player;

/**
 * First mission. Thrive.
 */
public class Default_3_mission extends AbstractMission<MissionGoalsEmpty, Empty_missionContext> {

    private final static String NAME = "thrive";

    public final static String GAME_OVER_REASON = "defaultMission3";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    Default_3_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD, new MissionGoalsEmpty());
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new Default_3_missionDefinition(missionCallBack, model));
    }

    @Override
    protected GameOverResult evaluateGameOver(final Model model) {
        //TODO move it to selarate rule
        if (getHumanPlayer(model).isDeclaredIndependence()) {
            final Player player = model.getPlayerByName("Dutch's King");
            if (getNumberOfMilitaryUnitsForPlayer(player) <= 0) {
                setFinished(true);
                return new GameOverResult(GAME_OVER_REASON);
            }
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

    @Override
    protected Empty_missionContext getNewContext() {
        return new Empty_missionContext();
    }

}
