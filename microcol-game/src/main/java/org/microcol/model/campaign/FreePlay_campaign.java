package org.microcol.model.campaign;

public class FreePlay_campaign extends AbstractCampaign {

	public final static String FREE_PLAY = "freePlay";

	FreePlay_campaign() {
		super(CampaignNames.freePlay);
		addMission(new DefaultCampaignMission(FreePlayMissionNames.freePlay, 0,
				(context) -> new MissionImpl<MissionGoalsEmpty>(
						FreePlayMissionNames.freePlay, 0, new FreePlay_1_missionDefinition(context.getMissionCallBack(),
								context.getModel(), new MissionGoalsEmpty()),
						context.getModelPo(), context.getCampaignManager())));
	}

}
