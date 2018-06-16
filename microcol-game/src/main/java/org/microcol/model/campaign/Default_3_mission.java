package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.collect.Lists;

/**
 * First mission. Thrive.
 */
public class Default_3_mission extends AbstractMission<Empty_missionContext> {

    private final static String NAME = "thrive";

    public final static String GAME_OVER_REASON = "defaultMission3";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    Default_3_mission() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ExtendedModelListenerAdapter() {

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
                            context.showMessage("campaign.default.m3.done1",
                                    "campaign.default.m3.done2");
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
                        missionCallBack.showMessage("campaign.default.m3.start",
                                "campaign.default.m3.declareIndependence");
                    });
                }
            }

            @Override
            public void independenceWasDeclared(final IndependenceWasDeclaredEvent event) {
                missionCallBack.showMessage("campaign.default.m3.declareIndependence.done",
                        "campaign.default.m3.portIsClosed");
            }

        });
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    private GameOverResult evaluateGameOver(final Model model) {
        if (getHumanPlayer(model).isDeclaredIndependence()) {
            final Player player = model.getPlayerByName("Dutch's King");
            if (getNumberOfMilitaryUnitsForPlayer(player) <= 0) {
                setFinished(true);
                flush();
                return new GameOverResult(GAME_OVER_REASON);
            }
        }
        return null;
    }

}
