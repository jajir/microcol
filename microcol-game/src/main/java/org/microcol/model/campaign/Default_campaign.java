package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
final class Default_campaign extends AbstractCampaign {

    Default_campaign() {
        super(CampaignNames.defaultCampaign);

        addMission(new CampaignMission(Default_missionNames.findNewWorld, 0));

        addMission(new CampaignMission(Default_missionNames.buildArmy, 1));

        addMission(new CampaignMission(Default_missionNames.thrive, 2));
    }
}
