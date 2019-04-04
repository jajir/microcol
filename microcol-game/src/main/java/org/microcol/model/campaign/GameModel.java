package org.microcol.model.campaign;

import org.microcol.model.Model;
import org.microcol.model.ModelListener;
import org.microcol.model.campaign.po.GameModelPo;

import com.google.common.base.Preconditions;

/**
 * Encapsulate whole game model. It put together:
 * <ul>
 * <li>model - main model with units, world map, ...</li>
 * <li>campaign</li>
 * <li>mission state - what was done by player</li>
 * </ul>
 *
 * <p>
 * All commands to model goes through mission. Mission decide if move of game
 * decision is valid. When player move than mission can stop it by throwing
 * exception
 * </p>
 *
 */
public final class GameModel {

    private final Campaign campaign;

    private final CampaignMission campaignMission;

    private final Mission<?> mission;

    private final Model model;

    /**
     * Default constructor.
     *
     * @param campaign
     *            required campaign
     * @param campaignMission
     *            required campaign mission
     * @param mission
     *            required mission
     * @param model
     *            required game model
     */
    GameModel(final Campaign campaign, final CampaignMission campaignMission,
            final Mission<?> mission, final Model model) {
        this.campaign = Preconditions.checkNotNull(campaign);
        this.campaignMission = Preconditions.checkNotNull(campaignMission);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = Preconditions.checkNotNull(model);
    }

    /**
     * Return model persistent object.
     *
     * @return Return model persistent object
     */
    public GameModelPo getModelPo() {
        final GameModelPo out = new GameModelPo();
        out.setModel(model.save());
        out.setCampaignName(campaign.getName().toString());
        out.setMissionName(campaignMission.getName());
        out.setData(mission.saveGoals());
        return out;
    }

    /**
     * @return return actual game model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Allows to add model listener to react on game model events.
     *
     * @param listener
     *            required model event listener
     */
    public void addListener(final ModelListener listener) {
        model.addListener(listener);
    }

    /**
     * Allows to start mission and game model.
     */
    public void startGame() {
        model.startGame();
    }

    /**
     * Allows to stop mission and game model.
     */
    public void stop() {
        model.stop();
    }

    public MissionGoals getMissionGoals() {
        return mission.getGoals();
    }

}
