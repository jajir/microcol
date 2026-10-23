package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

public final class FreePlay_0_mission
        extends AbstractMission<MissionGoalsEmpty> {

    FreePlay_0_mission(final MissionCreationContext context,
            final MissionGoalsEmpty goals) {
        super(context, goals);
    }

    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR,
                GameOverProcessors.REBELLION_LOST_PROCESSOR,
                GameOverProcessors.REBELLION_WON_PROCESSOR);
    }

    public void onIndependenceWasDeclared() {
        fireEvent(new EventShowMessages(
                Missions.independenceWasDeclared_caption));
    }

}
