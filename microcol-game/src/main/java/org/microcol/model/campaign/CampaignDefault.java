package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
public class CampaignDefault extends AbstractCampaign {

    public final static String NAME = "default";

    CampaignDefault() {
        super(NAME);
        addMission(new DefaultMissionFindNewWold());
        addMission(new DefaultMissionFoundColony());
        addMission(new DefaultMissionThrive());
    }
}
