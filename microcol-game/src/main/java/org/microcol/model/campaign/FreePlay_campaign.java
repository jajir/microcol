package org.microcol.model.campaign;

public class FreePlay_campaign extends AbstractCampaign {

    public final static String FREE_PLAY = "freePlay";

    FreePlay_campaign() {
        super(CampaignNames.freePlay);
        addMission(new FreePlay_1_mission());
    }

}
