package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.collect.Lists;

public class FreePlay_1_missionDefinition extends AbstractModelListenerAdapter {

    @SuppressWarnings("unused")
    private final FreePlay_1_mission mission;

    FreePlay_1_missionDefinition(final FreePlay_1_mission mission,
            final MissionCallBack missionCallBack) {
        super(missionCallBack);
        this.mission = mission;
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR);
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        missionCallBack.showMessage("dialogIndependenceWasDeclared.caption");
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {

    }
}
