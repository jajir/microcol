package org.microcol.model.campaign;

class Default_0_missionFactory implements AbstractMissionFactory {

    @Override
    public Mission<?> make(final MissionCreationContext context) {
        final Default_0_goals missionGoals = new Default_0_goals(context.getGoalData());

        final Default_0_mission mission = new Default_0_mission(context, missionGoals);

        final Default_0_modelListener modelListener = new Default_0_modelListener(mission);

        return new MissionImpl<Default_0_goals>(Default_missionNames.findNewWorld, modelListener,
                mission);
    }

}
