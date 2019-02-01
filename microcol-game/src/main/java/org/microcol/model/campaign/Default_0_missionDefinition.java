package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.ai.Continent;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
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
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;

import com.google.common.collect.Lists;

final class Default_0_missionDefinition extends MissionDefinition<Default_0_goals> {

    final static Integer TRAGET_AMOUNT_OF_CIGARS = 30;

    Default_0_missionDefinition(final MissionCallBack missionCallBack, final Model model,
            final Default_0_goals goals) {
        super(missionCallBack, model, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage(Missions.default_m0_gameOver);
                            callBackContext.goToGameMenu();
                        });
                        return "ok";
                    }
                    return null;
                });
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        /*
         * Disable declaring of independence.
         */
        event.stopEventExecution();
        missionCallBack.showMessage(Missions.default_m0_cantDeclareIndependence);
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(event.getModel())) {
            missionCallBack.showMessage(Missions.default_m0_start);
        }
    }

    @Override
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        if (!isPlayerHaveAnyColony(event.getModel())
                && isAnyLocationAtHighSeas(event.getModel(), event.getPath())) {
            event.stopEventExecution();
            missionCallBack.showMessage(Missions.default_m0_cantMoveToHighSeas);
        }
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (isFirstTurn(event.getModel()) && event.getUnit().getActionPoints() == 0) {
            missionCallBack.showMessage(Missions.default_m0_pressNextTurn);
        } else {
            if (!goals.getGoalFindNewWorld().isFinished()) {
                final ContinentTool ct = new ContinentTool();
                final Continents continents = ct.findContinents(event.getModel(),
                        getModel().getCurrentPlayer());
                final Continent c = continents.getForLocation(Location.of(19, 15)).orElseThrow(
                        () -> new MicroColException("Continent is not at expected location"));
                if (event.getUnit().isAtPlaceLocation()
                        && c.getDistance(event.getUnit().getLocation()) < 4) {
                    missionCallBack.showMessage(Missions.default_m0_continentInSight);
                    goals.getGoalFindNewWorld().setFinished(true);
                }
            }
        }
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        if (isFirstTurn(getModel())) {
            final Player human = getHumanPlayer(getModel());
            final Unit ship = findFirstShip(getModel(), human);
            if (ship.getActionPoints() == ship.getSpeed()) {
                missionCallBack.showMessage(Missions.default_m0_moveUnitBeforeEndTurn);
            }
        }
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        if (goals.getGoalFoundColony().isFinished()) {
            if (!goals.getGoalProduceCigars().isFinished()) {
                // verify cigars producing
                final Player human = getHumanPlayer(getModel());
                if (human.getPlayerStatistics().getGoodsStatistics()
                        .getGoodsAmount(GoodType.CIGARS) >= TRAGET_AMOUNT_OF_CIGARS) {
                    missionCallBack.showMessage(Missions.default_m0_sellCigarsInEuropePort);
                    goals.getGoalProduceCigars().setFinished(true);
                }

            }
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        if (event.getGoodsAmount().getGoodType() == GoodType.CIGARS) {
            goals.getGoalSellCigars().setWasSold(
                    goals.getGoalSellCigars().getWasSold() + event.getGoodsAmount().getAmount());
        }
        if (goals.getGoalSellCigars().getWasSold() >= TRAGET_AMOUNT_OF_CIGARS) {
            goals.getGoalSellCigars().setFinished(true);
            missionCallBack.showMessage(Missions.default_m0_cigarsWasSold);
        }
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            goals.getGoalFoundColony().setFinished(true);
            missionCallBack.showMessage(Missions.default_m0_firstColonyWasFounded,
                    Missions.default_m0_produce100cigars);
        }
    }

    private boolean isAnyLocationAtHighSeas(final Model model, final Path path) {
        return path.getLocations().stream()
                .filter(loc -> model.getMap().getTerrainTypeAt(loc).equals(TerrainType.HIGH_SEA))
                .findAny().isPresent();
    }

    private Unit findFirstShip(final Model model, final Player player) {
        return model.getAllUnits().stream().filter(u -> u.getOwner().equals(player))
                .filter(u -> u.getType().isShip()).findAny()
                .orElseThrow(() -> new MicroColException("human player doesn't have any ship."));
    }

    private boolean isPlayerHaveAnyColony(final Model model) {
        return !model.getColonies(getHumanPlayer(model)).isEmpty();
    }
}