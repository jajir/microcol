package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.Lists;

final class Default_1_missionDefinition extends MissionDefinition<Default_1_goals> {

    final static Integer TARGET_NUMBER_OF_COLONIES = 3;

    final static Integer TARGET_NUMBER_OF_GOLD = 5000;

    final static Integer TARGET_NUMBER_OF_MILITARY_UNITS = 15;

    Default_1_missionDefinition(final MissionCallBack missionCallBack, final Model model,
            final Default_1_goals goals) {
        super(missionCallBack, model, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage(Missions.default_m1_done);
                            callBackContext.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(event.getModel())) {
            missionCallBack.showMessage(Missions.default_m1_start,
                    Missions.default_m1_foundColonies);
        }
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        event.stopEventExecution();
        missionCallBack.showMessage(Missions.default_m0_cantDeclareIndependence);
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (!goals.getGoalNumberOfColonies().isFinished()) {
            if (playerHaveMoreOrEqualColonies(event.getModel(), TARGET_NUMBER_OF_COLONIES)) {
                missionCallBack.showMessage(Missions.default_m1_foundColonies_done,
                        Missions.default_m1_get5000);
                goals.getGoalNumberOfColonies().setFinished(true);
            }
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        checkNumberOfGoldTarget(event.getModel());
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        if (!goals.getGoalMilitaryPower().isFinished()) {
            if (getNumberOfMilitaryUnits(event.getModel()) >= TARGET_NUMBER_OF_MILITARY_UNITS) {
                missionCallBack.showMessage(Missions.default_m1_makeArmy_done);
                goals.getGoalMilitaryPower().setFinished(true);
            }
        }
        checkNumberOfGoldTarget(event.getModel());
    }

    private void checkNumberOfGoldTarget(final Model model) {
        if (!goals.getGoalAmountOfGold().isFinished()) {
            final int golds = getHumanPlayer(model).getGold();
            if (golds >= TARGET_NUMBER_OF_GOLD) {
                missionCallBack.showMessage(Missions.default_m1_get5000_done,
                        Missions.default_m1_makeArmy);
                goals.getGoalAmountOfGold().setFinished(true);
            }
        }
    }
}