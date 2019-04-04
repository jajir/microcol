package org.microcol.model.campaign;

class Default_1_missionFactory implements AbstractMissionFactory {

    @Override
    public Mission<?> make(final MissionCreationContext context) {

        final Default_1_goals goals = new Default_1_goals(context.getGoalData());

        final Default_1_mission mission = new Default_1_mission(context, goals);

        final Default_1_modelListener listener = new Default_1_modelListener(mission);

        return new MissionImpl<Default_1_goals>(Default_missionNames.buildArmy, listener, mission);
    }

}
