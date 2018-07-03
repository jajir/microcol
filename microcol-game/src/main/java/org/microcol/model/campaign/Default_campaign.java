package org.microcol.model.campaign;

/**
 * Game default campaign. It's main game story.
 */
public class Default_campaign extends AbstractCampaign {

	Default_campaign() {
		super(CampaignNames.defaultCampaign);

		addMission(new DefaultCampaignMission(DefaultMissionNames.findNewWorld, 0,
				(context) -> new MissionImpl<Default_1_goals>(DefaultMissionNames.findNewWorld, 0,
						new Default_1_missionDefinition(context.getMissionCallBack(), context.getModel(),
								new Default_1_goals()),
						context.getModelPo(), context.getCampaignManager())));

		addMission(new DefaultCampaignMission(DefaultMissionNames.thrive, 1,
				(context) -> new MissionImpl<Default_2_goals>(
						DefaultMissionNames.thrive, 1, new Default_2_missionDefinition(context.getMissionCallBack(),
								context.getModel(), new Default_2_goals()),
						context.getModelPo(), context.getCampaignManager())));

		addMission(new DefaultCampaignMission(DefaultMissionNames.buildArmy, 2,
				(context) -> new MissionImpl<MissionGoalsEmpty>(
						DefaultMissionNames.buildArmy, 2, new Default_3_missionDefinition(context.getMissionCallBack(),
								context.getModel(), new MissionGoalsEmpty()),
						context.getModelPo(), context.getCampaignManager())));
	}
}
