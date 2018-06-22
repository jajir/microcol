package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
public class Default_campaign extends AbstractCampaign {

    public final static String NAME = "default";

    Default_campaign() {
        super(CampaignNames.defaultCampaign);
        addMission(new Default_1_mission());
        addMission(new Default_2_mission());
        addMission(new Default_3_mission());
    }
}
