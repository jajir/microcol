package org.microcol.model.campaign;

import org.microcol.model.Model;
import org.microcol.model.ModelListener;
import org.microcol.model.store.CampaignPo;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Encapsulate game model and campaign and missions.
 *
 * TODO review following documentations
 * <p>
 * All commands to model goes through mission. Mission decide if move of game
 * decision is legal. When player's move than mission can stop it by throwing
 * exception
 * </p>
 *
 */
public final class ModelMission {

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
    ModelMission(final Campaign campaign, final CampaignMission campaignMission,
            final Mission<?> mission, final Model model) {
        this.campaign = Preconditions.checkNotNull(campaign);
        this.campaignMission = Preconditions.checkNotNull(campaignMission);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = Preconditions.checkNotNull(model);
        /*
         * following doesn't allows different mission implementation and multiple
         * conditions.
         */
        // TODO try to refactore it
        model.addGameOverEvaluator(mission::evaluateGameOver);
    }

    /**
     * Return model persistent object.
     *
     * @return Return model persistent object
     */
    public ModelPo getModelPo() {
        final ModelPo out = model.save();
        out.setCampaign(new CampaignPo());
        out.getCampaign().setName(campaign.getName().toString());
        out.getCampaign().setMission(campaignMission.getName());
        out.getCampaign().setData(mission.saveToMap());
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

    /**
     * @return the mission
     */
    public Mission<?> getMission() {
        return mission;
    }

}
