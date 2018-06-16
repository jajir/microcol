package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.Lists;

/**
 * First mission. Find New World.
 */
public class Default_2_mission extends AbstractMission<Default_2_missionContext> {

    private final static String NAME = "buildArmy";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    public final static String GAME_OVER_REASON = "defaultMission2";

    private final static Integer TARGET_NUMBER_OF_COLONIES = 3;

    private final static Integer TARGET_NUMBER_OF_GOLD = 5000;

    private final static Integer TARGET_NUMBER_OF_MILITARY_UNITS = 15;

    Default_2_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new AbstractModelListenerAdapter() {

            @Override
            protected List<Function<GameFinishedEvent, String>> prepareEvaluators() {
                return Lists.newArrayList((event) -> {
                    if (GameOverEvaluator.REASON_TIME_IS_UP
                            .equals(event.getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(context -> {
                            context.showMessage("dialogGameOver.timeIsUp");
                            context.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                }, (event) -> {
                    if (GameOverEvaluator.REASON_NO_COLONIES
                            .equals(event.getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(context -> {
                            context.showMessage("dialogGameOver.allColoniesAreLost");
                            context.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                }, (event) -> {
                    if (GAME_OVER_REASON.equals(event.getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(context -> {
                            context.showMessage("campaign.default.m2.done");
                            context.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
            }

            @Override
            public void gameStarted(final GameStartedEvent event) {
                if (isFirstTurn(event.getModel())) {
                    missionCallBack.addCallWhenReady(model -> {
                        missionCallBack.showMessage("campaign.default.m2.start",
                                "campaign.default.m2.foundColonies");
                    });
                }
            }

            @Override
            public void beforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
                event.stopEventExecution();
                missionCallBack.showMessage("campaign.default.m1.cantDeclareIndependence");
            }

            @Override
            public void colonyWasFounded(final ColonyWasFoundEvent event) {
                if (!getContext().isWasNumberOfColoniesTargetReached()) {
                    if (playerHaveMoreOrEqualColonies(event.getModel(),
                            TARGET_NUMBER_OF_COLONIES)) {
                        missionCallBack.showMessage("campaign.default.m2.foundColonies.done",
                                "campaign.default.m2.get5000");
                        getContext().setWasNumberOfColoniesTargetReached(true);
                    }
                }
            }

            @Override
            public void goodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
                checkNumberOfGoldTarget(event.getModel());
            }

            @Override
            public void turnStarted(final TurnStartedEvent event) {
                if (!getContext().isWasNumberOfMilitaryUnitsTargetReached()) {
                    if (getNumberOfMilitaryUnits(
                            event.getModel()) >= TARGET_NUMBER_OF_MILITARY_UNITS) {
                        missionCallBack.showMessage("campaign.default.m2.done");
                        getContext().setWasNumberOfMilitaryUnitsTargetReached(true);
                    }
                }
                checkNumberOfGoldTarget(event.getModel());
            }

            private void checkNumberOfGoldTarget(final Model model) {
                if (!getContext().isWasNumberOfGoldTargetReached()) {
                    final int golds = getHumanPlayer(model).getGold();
                    if (golds >= TARGET_NUMBER_OF_GOLD) {
                        missionCallBack.showMessage("campaign.default.m2.get5000.done",
                                "campaign.default.m2.makeArmy");
                        getContext().setWasNumberOfGoldTargetReached(true);
                    }
                }
            }

        });
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    @SuppressWarnings("unused")
    private GameOverResult evaluateGameOver(final Model model) {
        if (getContext().isWasNumberOfMilitaryUnitsTargetReached()) {
            setFinished(true);
            flush();
            return new GameOverResult(GAME_OVER_REASON);
        } else {
            return null;
        }
    }

}
