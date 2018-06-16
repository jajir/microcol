package org.microcol.model.campaign;

import java.util.List;
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
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;

import com.google.common.collect.Lists;

/**
 * First mission. Find New World.
 */
public class Default_1_mission extends AbstractMission<Default_1_missionContext> {

    private final static String MISSION_NAME = "findNewWorld";

    public final static String GAME_OVER_REASON = "defaultMission1";

    private final static String MISSION_MODEL_FILE = "/maps/default-" + MISSION_NAME + ".json";

    private final static Integer TRAGET_AMOUNT_OF_CIGARS = 30;

    Default_1_mission() {
        super(MISSION_NAME, 0, MISSION_MODEL_FILE);
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
                            context.showMessage("campaign.default.gameOver");
                            context.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
            }

            @Override
            public void beforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
                event.stopEventExecution();
                missionCallBack.showMessage("campaign.default.m1.cantDeclareIndependence");
            }

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
                    if (!getContext().isWasContinentOnSightMessageShown()) {
                        final ContinentTool ct = new ContinentTool();
                        final Continents continents = ct.findContinents(event.getModel(),
                                model.getCurrentPlayer());
                        final Continent c = continents.getForLocation(Location.of(9, 15))
                                .orElseThrow(() -> new MicroColException(
                                        "Continent is not at expected location"));
                        if (event.getUnit().isAtPlaceLocation()
                                && c.getDistance(event.getUnit().getLocation()) < 4) {
                            missionCallBack.showMessage("campaign.default.continentInSight");
                            getContext().setWasContinentOnSightMessageShown(true);
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
                        if (!getContext().isWasMessageSellCigarsShown()) {
                            missionCallBack.showMessage("campaign.default.sellCigarsInEuropePort");
                            getContext().setWasMessageSellCigarsShown(true);
                        }
                    }
                }
            }

            @Override
            public void goodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
                if (event.getGoodsAmount().getGoodType() == GoodType.CIGARS) {
                    getContext().setCigarsWasSold(
                            getContext().getCigarsWasSold() + event.getGoodsAmount().getAmount());
                }
                if (getContext().getCigarsWasSold() >= TRAGET_AMOUNT_OF_CIGARS) {
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
    public List<Function<Model, GameOverResult>> getGameOverEvaluators() {
        return Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES,
                this::evaluateGameOver);
    }

    @SuppressWarnings("unused")
    private GameOverResult evaluateGameOver(final Model model) {
        if (getContext().getCigarsWasSold() >= TRAGET_AMOUNT_OF_CIGARS) {
            setFinished(true);
            flush();
            return new GameOverResult(GAME_OVER_REASON);
        } else {
            return null;
        }
    }

}
