package org.microcol.model.campaign;

public class FreePlay_campaign extends AbstractCampaign {

    public final static String FREE_PLAY = "freePlay";

    FreePlay_campaign() {
        super(FREE_PLAY);
        addMission(new FreePlay_1_mission());
    }

}
