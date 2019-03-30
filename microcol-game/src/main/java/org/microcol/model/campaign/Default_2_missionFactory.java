package org.microcol.model.campaign;

final class Default_2_missionFactory implements AbstractMissionFactory {

    @Override
    public Mission<?> make(final MissionCreationContext context) {

        final Default_2_goals goals = new Default_2_goals(context.getGoalData());

        final Default_2_mission mission = new Default_2_mission(context, goals);

        final Default_2_modelListener listener = new Default_2_modelListener(mission);

        return new MissionImpl<Default_2_goals>(Default_missionNames.thrive, listener, mission);
    }

}
