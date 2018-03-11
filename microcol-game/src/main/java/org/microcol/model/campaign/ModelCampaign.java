package org.microcol.model.campaign;

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
public class ModelCampaign {

    private final AbstractCampaign campaign;

    private final AbstractMission mission;

    private final Model model;

    ModelCampaign(final AbstractCampaign campaign, final AbstractMission mission,
            final Model model) {
        this.campaign = Preconditions.checkNotNull(campaign);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = Preconditions.checkNotNull(model);
    }

    AbstractMission getNextMission() {
        return null;
    }

    public ModelPo getModelPo() {
        final ModelPo out = model.save();
        out.setCampaign(new CampaignPo());
        out.getCampaign().setName(campaign.getName());
        out.getCampaign().setMission(mission.getName());
        return out;
    }
}
