package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

final class Default_2_missionDefinition extends MissionDefinition<Default_2_goals> {

    Default_2_missionDefinition(final MissionCallBack missionCallBack, final Model model,
            final Default_2_goals goals) {
        super(missionCallBack, model, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage("campaign.default.m2.done1",
                                    "campaign.default.m2.done2");
                            callBackContext.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        Player p = event.getModel().getPlayerByName("Dutch's King");
        /**
         * When King is conquered than sent this property in his data to true.
         */
        String s = (String) p.getExtraData().get("kingWasConquered");
        // TODO this is ugly. Redesign relation between AI - king and mission
        if (!Strings.isNullOrEmpty(s)) {
            if (Boolean.parseBoolean(s)) {
                goals.getGoalConquerRaf().setFinished(true);
            }
        }
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(getModel())) {
            missionCallBack.showMessage("campaign.default.m2.start",
                    "campaign.default.m2.declareIndependence");
        }
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        if (!getGoals().getGoalDeclareIndependence().isFinished()) {
            missionCallBack.showMessage("campaign.default.m2.declareIndependence.done",
                    "campaign.default.m2.portIsClosed");
            getGoals().getGoalDeclareIndependence().setFinished(true);
        }
    }
}