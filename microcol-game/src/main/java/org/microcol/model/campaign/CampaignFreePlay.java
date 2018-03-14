package org.microcol.model.campaign;

public class CampaignFreePlay extends AbstractCampaign {

    public final static String FREE_PLAY = "freePlay";

    CampaignFreePlay() {
        super(FREE_PLAY);
        addMission(new MissionFreePlay());
    }

}
