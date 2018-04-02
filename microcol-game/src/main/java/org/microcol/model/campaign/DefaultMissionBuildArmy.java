package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.store.ModelPo;

import com.google.common.collect.Lists;

/**
 * First mission. Find New World.
 */
public class DefaultMissionBuildArmy extends AbstractMission {

    private final static String NAME = "buildArmy";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    public final static String GAME_OVER_REASON = "defaultMission2";

    private final static Integer TARGET_NUMBER_OF_COLONIES = 3;

    private final static Integer TARGET_NUMBER_OF_GOLD = 5000;

    private final static Integer TARGET_NUMBER_OF_MILITARY_UNITS = 15;

    private final static String KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED = "wasNumberOfColoniesTargetReached";

    private final static String KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED = "wasNumberOfGoldTargetReached";

    private final static String KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED = "wasNumberOfMilitaryUnitsTargetReached";

    private boolean wasNumberOfColoniesTargetReached = false;

    private boolean wasNumberOfGoldTargetReached = false;

    private boolean wasNumberOfMilitaryUnitsTargetReached = false;

    DefaultMissionBuildArmy() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ModelListenerAdapter() {

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
            }

            @Override
            public void colonyWasFounded(final ColonyWasFoundEvent event) {
                if (!wasNumberOfColoniesTargetReached) {
                    if (playerHaveMoreOrEqualColonies(event.getModel(),
                            TARGET_NUMBER_OF_COLONIES)) {
                        missionCallBack.showMessage("campaign.default.m2.foundColonies.done",
                                "campaign.default.m2.get5000");
                        wasNumberOfColoniesTargetReached = true;
                    }
                }
            }

            @Override
            public void goodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
                checkNumberOfGoldTarget(event.getModel());
            }

            @Override
            public void turnStarted(final TurnStartedEvent event) {
                if (!wasNumberOfMilitaryUnitsTargetReached) {
                    if (getNumberOfMilitaryUnits(
                            event.getModel()) >= TARGET_NUMBER_OF_MILITARY_UNITS) {
                        missionCallBack.showMessage("campaign.default.m2.done");
                        wasNumberOfMilitaryUnitsTargetReached = true;
                    }
                }
                checkNumberOfGoldTarget(event.getModel());
            }
            
            private void checkNumberOfGoldTarget(final Model model){
                if (!wasNumberOfGoldTargetReached) {
                    final int golds = getHumanPlayer(model).getGold();
                    if (golds >= TARGET_NUMBER_OF_GOLD) {
                        missionCallBack.showMessage("campaign.default.m2.get5000.done",
                                "campaign.default.m2.makeArmy");
                        wasNumberOfGoldTargetReached = true;
                    }
                }                
            }

        });
    }

    @Override
    public void initialize(ModelPo modelPo) {
        if (modelPo.getCampaign().getData() != null) {
            wasNumberOfColoniesTargetReached = Boolean.parseBoolean(
                    modelPo.getCampaign().getData().get(KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED));
            wasNumberOfGoldTargetReached = Boolean.parseBoolean(
                    modelPo.getCampaign().getData().get(KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED));
            wasNumberOfMilitaryUnitsTargetReached = Boolean.parseBoolean(modelPo.getCampaign()
                    .getData().get(KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED));
        }
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out = new HashMap<>();
        out.put(KEY_WAS_NUMBER_OF_COLONIES_TARGET_REACHED,
                Boolean.toString(wasNumberOfColoniesTargetReached));
        out.put(KEY_WAS_NUMBER_OF_GOLD_TARGET_REACHED,
                Boolean.toString(wasNumberOfGoldTargetReached));
        out.put(KEY_WAS_NUMBER_OF_MILITARY_UNITS_TARGET_REACHED,
                Boolean.toString(wasNumberOfMilitaryUnitsTargetReached));
        return out;
    }

    @Override
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    @SuppressWarnings("unused")
    private GameOverResult evaluateGameOver(final Model model) {
        if (wasNumberOfMilitaryUnitsTargetReached) {
            setFinished(true);
            flush();
            return new GameOverResult(null, null, GAME_OVER_REASON);
        } else {
            return null;
        }
    }

}
