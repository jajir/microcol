package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.Model;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.Lists;

final class Default_1_mission extends AbstractMission<Default_1_goals> {

    final static Integer TARGET_NUMBER_OF_COLONIES = 3;

    final static Integer TARGET_NUMBER_OF_GOLD = 5000;

    final static Integer TARGET_NUMBER_OF_MILITARY_UNITS = 15;

    Default_1_mission(final MissionCreationContext context, final Default_1_goals goals) {
        super(context, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        fireEvent(new EventFinishMission(Missions.default_m1_done));
                        return "ok";
                    }
                    return null;
                });
    }

    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(event.getModel())) {
            fireEvent(new EventShowMessages(Missions.default_m1_start,
                    Missions.default_m1_foundColonies));
        }
    }

    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        event.stopEventExecution();
        fireEvent(new EventShowMessages(Missions.default_m0_cantDeclareIndependence));
    }

    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (!getGoals().getGoalNumberOfColonies().isFinished()) {
            if (playerHaveMoreOrEqualColonies(event.getModel(), TARGET_NUMBER_OF_COLONIES)) {
                fireEvent(new EventShowMessages(Missions.default_m1_foundColonies_done,
                        Missions.default_m1_get5000));
                getGoals().getGoalNumberOfColonies().setFinished(true);
            }
        }
    }

    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        checkNumberOfGoldTarget(event.getModel());
    }

    public void onTurnStarted(final TurnStartedEvent event) {
        super.onTurnStarted();
        if (!getGoals().getGoalMilitaryPower().isFinished()) {
            if (getNumberOfMilitaryUnits(event.getModel()) >= TARGET_NUMBER_OF_MILITARY_UNITS) {
                fireEvent(new EventShowMessages(Missions.default_m1_makeArmy_done));
                getGoals().getGoalMilitaryPower().setFinished(true);
            }
        }
        checkNumberOfGoldTarget(event.getModel());
    }

    private void checkNumberOfGoldTarget(final Model model) {
        if (!getGoals().getGoalAmountOfGold().isFinished()) {
            final int golds = getHumanPlayer(model).getGold();
            if (golds >= TARGET_NUMBER_OF_GOLD) {
                fireEvent(new EventShowMessages(Missions.default_m1_get5000_done,
                        Missions.default_m1_makeArmy));
                getGoals().getGoalAmountOfGold().setFinished(true);
            }
        }
    }
}