package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.microcol.ai.Continent;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.store.ModelPo;

import com.google.common.collect.Lists;

/**
 * First mission. Find New World.
 */
public class DefaultMissionFindNewWold extends AbstractMission {

    private final static String NAME = "findNewWorld";

    public final static String GAME_OVER_REASON = "defaultMission1";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    private final static String MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN = "wasContinentOnSightMessageWasShown";

    private final static String MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN = "wasSellCigarsMessageShown";

    private final static String MAP_KEY_CIGARS_WAS_SOLD = "cigarsWasSold";

    private final static Integer TRAGET_AMOUNT_OF_CIGARS = 10;

    private Integer cigarsWasSold = 0;

    private boolean wasContinentOnSightMessageShown = false;

    private boolean wasMessageSellCigarsShown = false;

    DefaultMissionFindNewWold() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ModelListenerAdapter() {

            @Override
            public void gameStarted(final GameStartedEvent event) {
                if (isFirstTurn(event.getModel())) {
                    missionCallBack.addCallWhenReady(model -> {
                        missionCallBack.showMessage("campaign.default.start");
                    });
                }
            }

            @Override
            public void unitMoveStarted(final UnitMoveStartedEvent event) {
                if (!isPlayerHaveAnyColony(event.getModel())
                        && isAnyLocationAtHighSeas(event.getModel(), event.getPath())) {
                    event.stopEventExecution();
                    missionCallBack.showMessage("campaign.default.cantMoveToHighSeas");
                }
            }

            @Override
            public void unitMoveFinished(final UnitMoveFinishedEvent event) {
                if (isFirstTurn(event.getModel()) && event.getUnit().getAvailableMoves() == 0) {
                    missionCallBack.showMessage("campaign.default.pressNextTurn");
                } else {
                    if (!wasContinentOnSightMessageShown) {
                        final ContinentTool ct = new ContinentTool();
                        final Continents continents = ct.findContinents(event.getModel(),
                                model.getCurrentPlayer());
                        final Continent c = continents.getForLocation(Location.of(9, 15))
                                .orElseThrow(() -> new MicroColException(
                                        "Continent is not at expected location"));
                        if (event.getUnit().isAtPlaceLocation()
                                && c.getDistance(event.getUnit().getLocation()) < 4) {
                            missionCallBack.showMessage("campaign.default.continentInSight");
                            wasContinentOnSightMessageShown = true;
                        }
                    }
                }
            }

            @Override
            public void beforeEndTurn(final BeforeEndTurnEvent event) {
                if (isFirstTurn(event.getModel())) {
                    final Player human = getHumanPlayer(event.getModel());
                    final Unit ship = findFirstShip(event.getModel(), human);
                    if (ship.getAvailableMoves() == ship.getType().getSpeed()) {
                        missionCallBack.showMessage("campaign.default.moveUnitBeforeEndTurn");
                    }
                }
            }

            @Override
            public void turnStarted(final TurnStartedEvent event) {
                if (isPlayerHaveAnyColony(event.getModel())) {
                    final Player human = getHumanPlayer(event.getModel());
                    if (human.getPlayerStatistics().getGoodsStatistics()
                            .getGoodsAmount(GoodType.CIGARS) >= TRAGET_AMOUNT_OF_CIGARS) {
                        if (!wasMessageSellCigarsShown) {
                            missionCallBack.showMessage("campaign.default.sellCigarsInEuropePort");
                            wasMessageSellCigarsShown = true;
                        }
                    }
                }
            }

            @Override
            public void goodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
                if (event.getGoodsAmount().getGoodType() == GoodType.CIGARS) {
                    cigarsWasSold += event.getGoodsAmount().getAmount();
                }
                if (cigarsWasSold >= TRAGET_AMOUNT_OF_CIGARS) {
                    missionCallBack.showMessage("campaign.default.cigarsWasSold");
                }
            }

            @Override
            public void colonyWasFounded(final ColonyWasFoundEvent event) {
                if (isPlayerHaveAnyColony(event.getModel())) {
                    missionCallBack.showMessage("campaign.default.firstColonyWasFounded",
                            "campaign.default.produce100cigars");
                }
            }

            private boolean isAnyLocationAtHighSeas(final Model model, final Path path) {
                return path.getLocations().stream().filter(
                        loc -> model.getMap().getTerrainTypeAt(loc).equals(TerrainType.HIGH_SEA))
                        .findAny().isPresent();
            }

            private Unit findFirstShip(final Model model, final Player player) {
                return model.getAllUnits().stream().filter(u -> u.getOwner().equals(player))
                        .filter(u -> u.getType().isShip()).findAny().orElseThrow(
                                () -> new MicroColException("human player doesn't have any ship."));
            }

            private boolean isPlayerHaveAnyColony(final Model model) {
                return !model.getColonies(getHumanPlayer(model)).isEmpty();
            }

        });
    }

    @Override
    public void initialize(ModelPo modelPo) {
        if (modelPo.getCampaign().getData() != null) {
            wasContinentOnSightMessageShown = Boolean.parseBoolean(modelPo.getCampaign().getData()
                    .get(MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN));
            wasMessageSellCigarsShown = Boolean.parseBoolean(
                    modelPo.getCampaign().getData().get(MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN));
            cigarsWasSold = get(modelPo.getCampaign().getData(), MAP_KEY_CIGARS_WAS_SOLD);
        }
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out = new HashMap<>();
        out.put(MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_SHOWN,
                Boolean.toString(wasContinentOnSightMessageShown));
        out.put(MAP_KEY_WAS_SELL_CIGARS_MESSAGE_SHOWN, Boolean.toString(wasMessageSellCigarsShown));
        out.put(MAP_KEY_CIGARS_WAS_SOLD, Integer.toString(cigarsWasSold));
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
        if (cigarsWasSold >= TRAGET_AMOUNT_OF_CIGARS) {
            setFinished(true);
            flush();
            return new GameOverResult(null, null, GAME_OVER_REASON);
        } else {
            return null;
        }
    }

}
