package org.microcol.model.campaign;

final class FreePlay_0_missionFactory implements AbstractMissionFactory {

    @Override
    public Mission<?> make(final MissionCreationContext context) {

        final FreePlay_0_mission mission = new FreePlay_0_mission(context, new MissionGoalsEmpty());

        final FreePlay_0_modelListener listener = new FreePlay_0_modelListener(mission);

        return new MissionImpl<MissionGoalsEmpty>(FreePlay_missionNames.freePlay, listener,
                mission);
    }

}
