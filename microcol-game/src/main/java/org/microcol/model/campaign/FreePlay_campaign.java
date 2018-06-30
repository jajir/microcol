package org.microcol.model.campaign;

public class FreePlay_campaign extends AbstractCampaign {

    public final static String FREE_PLAY = "freePlay";

    FreePlay_campaign() {
        super(CampaignNames.freePlay);
        // FIXME in string is stored enum. Store enum and convert it to String.
        addMission(new DefaultCampaignMission(FreePlayMissionNames.freePlay.name(), 0,
                () -> new FreePlay_1_mission()));
    }

}
