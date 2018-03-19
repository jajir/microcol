package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
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
public class ModelMission {

    private final Campaign campaign;

    private final Mission mission;

    private final Model model;

    ModelMission(final Campaign campaign, final Mission mission, final Model model) {
        this.campaign = Preconditions.checkNotNull(campaign);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = Preconditions.checkNotNull(model);
    }

    Mission getNextMission() {
        return null;
    }

    public ModelPo getModelPo() {
        final ModelPo out = model.save();
        out.setCampaign(new CampaignPo());
        out.getCampaign().setName(campaign.getName());
        out.getCampaign().setMission(mission.getName());
        return out;
    }

    /**
     * TODO this should be replace by direct calls. Direct calls allows to call
     * mission to verify move.
     * 
     * @return
     */
    @Deprecated
    public Model getModel() {
        return model;
    }

    public void startGame(final MissionCallBack missionCallBack) {
        mission.startMission(model, missionCallBack);
        model.startGame();
    }
}