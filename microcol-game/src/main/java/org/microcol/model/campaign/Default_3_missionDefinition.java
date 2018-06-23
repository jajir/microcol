package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

final class Default_3_missionDefinition extends AbstractModelListenerAdapter {

    /**
     * 
     */
    private final Default_3_mission mission;

    Default_3_missionDefinition(final Default_3_mission mission,
            final MissionCallBack missionCallBack) {
        super(missionCallBack);
        this.mission = Preconditions.checkNotNull(mission);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (Default_3_mission.GAME_OVER_REASON
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage("campaign.default.m3.done1",
                                    "campaign.default.m3.done2");
                            callBackContext.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (this.mission.isFirstTurn(event.getModel())) {
            missionCallBack.addCallWhenReady(model -> {
                missionCallBack.showMessage("campaign.default.m3.start",
                        "campaign.default.m3.declareIndependence");
            });
        }
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        missionCallBack.showMessage("campaign.default.m3.declareIndependence.done",
                "campaign.default.m3.portIsClosed");
    }
}