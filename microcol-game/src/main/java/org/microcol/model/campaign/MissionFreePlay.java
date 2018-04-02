package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.collect.Lists;

/**
 * Free play mission definition. There are no limitations player can do
 * anything.
 */
public class MissionFreePlay extends AbstractMission {

    /**
     * Free play game map.
     */
    private final static String FREE_PLAY_MISSION_MAP = "/maps/free-play.json";

    MissionFreePlay() {
        super(CampaignFreePlay.FREE_PLAY, 0, FREE_PLAY_MISSION_MAP);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ExtendedModelListenerAdapter() {

            @Override
            protected List<Function<GameFinishedEvent, String>> prepareEvaluators() {
                return Lists.newArrayList((event) -> {
                    if (GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR
                            .equals(event.getGameOverResult().getGameOverReason())) {
                        missionCallBack.showMessage("dialogGameOver.timeIsUp");
                        missionCallBack.goToGameMenu();
                        return "ok";
                    }
                    return null;
                }, (event) -> {
                    if (GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES
                            .equals(event.getGameOverResult().getGameOverReason())) {
                        missionCallBack.showMessage("dialogGameOver.allColoniesAreLost");
                        missionCallBack.goToGameMenu();
                        return "ok";
                    }
                    return null;
                });
            }

            @Override
            public void independenceWasDeclared(final IndependenceWasDeclaredEvent event) {
                missionCallBack.showMessage("dialogIndependenceWasDeclared.caption");
            }

            @Override
            public void gameFinished(final GameFinishedEvent event) {

            }

        });
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }

}
