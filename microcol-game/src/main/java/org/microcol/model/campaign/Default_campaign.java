package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
public class Default_campaign extends AbstractCampaign {

    Default_campaign() {
	super(CampaignNames.defaultCampaign);

	addMission(new DefaultCampaignMission(DefaultMissionNames.findNewWorld, 0,
		(context) -> new MissionImpl<Default_0_goals>(DefaultMissionNames.findNewWorld,
			new Default_0_missionDefinition(context.getMissionCallBack(),
				context.getModel(), new Default_0_goals()),
			context.getModelPo(), context.getCampaignManager())));

	addMission(new DefaultCampaignMission(DefaultMissionNames.buildArmy, 1,
		(context) -> new MissionImpl<Default_1_goals>(DefaultMissionNames.buildArmy,
			new Default_1_missionDefinition(context.getMissionCallBack(),
				context.getModel(), new Default_1_goals()),
			context.getModelPo(), context.getCampaignManager())));

	addMission(new DefaultCampaignMission(DefaultMissionNames.thrive, 2,
		(context) -> new MissionImpl<Default_2_goals>(DefaultMissionNames.thrive,
			new Default_2_missionDefinition(context.getMissionCallBack(),
				context.getModel(), new Default_2_goals()),
			context.getModelPo(), context.getCampaignManager())));
    }
}
