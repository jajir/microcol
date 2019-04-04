package org.microcol.model.campaign;

public final class FreePlay_campaign extends AbstractCampaign {

    FreePlay_campaign() {
        super(CampaignNames.freePlay);

        addMission(new CampaignMission(FreePlay_missionNames.freePlay, 0));
    }

}
