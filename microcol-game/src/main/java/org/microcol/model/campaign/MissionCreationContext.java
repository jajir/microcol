package org.microcol.model.campaign;

import java.util.Map;

import org.microcol.model.Model;
import org.microcol.model.campaign.po.GameModelPo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Holds parameters needed for creation of mission. It simplify parameters
 * passing.
 */
final class MissionCreationContext {

    private final EventBus eventBus;

    private final Model model;

    private final GameModelPo gameModelPo;

    private final CampaignManager campaignManager;

    private final CampaignMission campaignMission;

    MissionCreationContext(final EventBus eventBus, final Model model,
            final GameModelPo gameModelPo, final CampaignManager campaignManager,
            final CampaignMission campaignMission) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.model = Preconditions.checkNotNull(model);
        this.gameModelPo = Preconditions.checkNotNull(gameModelPo);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.campaignMission = Preconditions.checkNotNull(campaignMission);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Model getModel() {
        return model;
    }

    public GameModelPo getModelPo() {
        return gameModelPo;
    }

    public CampaignManager getCampaignManager() {
        return campaignManager;
    }

    public CampaignMission getCampaignMission() {
        return campaignMission;
    }

    public final Map<String, String> getGoalData() {
        return gameModelPo.getGoalData();
    }

}
