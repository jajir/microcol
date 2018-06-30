package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
public class Default_campaign extends AbstractCampaign {

    public final static String NAME = "default";

    Default_campaign() {
        super(CampaignNames.defaultCampaign);
        addMission(new DefaultCampaignMission(DefaultMissionNames.findNewWorld.name(), 0,
                () -> new Default_1_mission()));
        addMission(new DefaultCampaignMission(DefaultMissionNames.thrive.name(), 1,
                () -> new Default_2_mission()));
        addMission(new DefaultCampaignMission(DefaultMissionNames.buildArmy.name(), 2,
                () -> new Default_3_mission()));
    }
}
