package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.Lists;

/**
 * First mission. Thrive.
 */
public class DefaultMissionThrive extends AbstractMission {

    private final static String NAME = "thrive";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionThrive() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ModelListenerAdapter() {

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

            @Override
            public void turnStarted(final TurnStartedEvent event) {
                final Model model = event.getModel();
                if (getHumanPlayer(model).isDeclaredIndependence()) {
                    final Player player = model.getPlayerByName("Dutch's King");
                    if (getNumberOfMilitaryUnitsForPlayer(player) <= 0) {
                        missionCallBack.showMessage("campaign.default.m3.done1",
                                "campaign.default.m3.done2");
                    }
                }
            }

        });
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }

}
