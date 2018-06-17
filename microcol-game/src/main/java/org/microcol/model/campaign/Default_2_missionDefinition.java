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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

final class Default_2_missionDefinition extends AbstractModelListenerAdapter {
    /**
     * 
     */
    private final Default_2_mission mission;

    Default_2_missionDefinition(final Default_2_mission mission,
            final MissionCallBack missionCallBack) {
        super(missionCallBack);
        this.mission = Preconditions.checkNotNull(mission);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (Default_2_mission.GAME_OVER_REASON
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage("campaign.default.m2.done");
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
                missionCallBack.showMessage("campaign.default.m2.start",
                        "campaign.default.m2.foundColonies");
            });
        }
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        event.stopEventExecution();
        missionCallBack.showMessage("campaign.default.m1.cantDeclareIndependence");
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (!this.mission.getContext().isWasNumberOfColoniesTargetReached()) {
            if (this.mission.playerHaveMoreOrEqualColonies(event.getModel(),
                    Default_2_mission.TARGET_NUMBER_OF_COLONIES)) {
                missionCallBack.showMessage("campaign.default.m2.foundColonies.done",
                        "campaign.default.m2.get5000");
                this.mission.getContext().setWasNumberOfColoniesTargetReached(true);
            }
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        checkNumberOfGoldTarget(event.getModel());
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        if (!this.mission.getContext().isWasNumberOfMilitaryUnitsTargetReached()) {
            if (this.mission.getNumberOfMilitaryUnits(
                    event.getModel()) >= Default_2_mission.TARGET_NUMBER_OF_MILITARY_UNITS) {
                missionCallBack.showMessage("campaign.default.m2.done");
                this.mission.getContext().setWasNumberOfMilitaryUnitsTargetReached(true);
            }
        }
        checkNumberOfGoldTarget(event.getModel());
    }

    private void checkNumberOfGoldTarget(final Model model) {
        if (!this.mission.getContext().isWasNumberOfGoldTargetReached()) {
            final int golds = this.mission.getHumanPlayer(model).getGold();
            if (golds >= Default_2_mission.TARGET_NUMBER_OF_GOLD) {
                missionCallBack.showMessage("campaign.default.m2.get5000.done",
                        "campaign.default.m2.makeArmy");
                this.mission.getContext().setWasNumberOfGoldTargetReached(true);
            }
        }
    }
}