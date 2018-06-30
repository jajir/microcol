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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

final class Default_1_missionDefinition extends AbstractModelListenerAdapter {
    /**
     * 
     */
    private final Default_1_mission mission;

    private final Model model;

    private final Default_1_goals goals;

    Default_1_missionDefinition(final Default_1_mission mission, final Model model,
            final MissionCallBack missionCallBack, final Default_1_goals goals) {
        super(missionCallBack);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = Preconditions.checkNotNull(model);
        this.goals = Preconditions.checkNotNull(goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (Default_1_mission.GAME_OVER_REASON
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        missionCallBack.executeOnFrontEnd(callBackContext -> {
                            callBackContext.showMessage("campaign.default.gameOver");
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
        missionCallBack.showMessage("campaign.default.m1.cantDeclareIndependence");
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (mission.isFirstTurn(event.getModel())) {
            missionCallBack.addCallWhenReady(model -> {
                missionCallBack.showMessage("campaign.default.start");
            });
        }
    }

    @Override
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        if (!isPlayerHaveAnyColony(event.getModel())
                && isAnyLocationAtHighSeas(event.getModel(), event.getPath())) {
            event.stopEventExecution();
            missionCallBack.showMessage("campaign.default.cantMoveToHighSeas");
        }
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (mission.isFirstTurn(event.getModel()) && event.getUnit().getAvailableMoves() == 0) {
            missionCallBack.showMessage("campaign.default.pressNextTurn");
        } else {
            if (!goals.getGoalFindNewWorld().isFinished()) {
                final ContinentTool ct = new ContinentTool();
                final Continents continents = ct.findContinents(event.getModel(),
                        model.getCurrentPlayer());
                final Continent c = continents.getForLocation(Location.of(9, 15)).orElseThrow(
                        () -> new MicroColException("Continent is not at expected location"));
                if (event.getUnit().isAtPlaceLocation()
                        && c.getDistance(event.getUnit().getLocation()) < 4) {
                    missionCallBack.showMessage("campaign.default.continentInSight");
                    goals.getGoalFindNewWorld().setFinished(true);
                }
            }
        }
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        if (mission.isFirstTurn(event.getModel())) {
            final Player human = mission.getHumanPlayer(event.getModel());
            final Unit ship = findFirstShip(event.getModel(), human);
            if (ship.getAvailableMoves() == ship.getType().getSpeed()) {
                missionCallBack.showMessage("campaign.default.moveUnitBeforeEndTurn");
            }
        }
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            final Player human = mission.getHumanPlayer(event.getModel());
            if (human.getPlayerStatistics().getGoodsStatistics()
                    .getGoodsAmount(GoodType.CIGARS) >= Default_1_mission.TRAGET_AMOUNT_OF_CIGARS) {
                if (!mission.getContext().isWasMessageSellCigarsShown()) {
                    missionCallBack.showMessage("campaign.default.sellCigarsInEuropePort");
                    mission.getContext().setWasMessageSellCigarsShown(true);
                }
            }
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        if (event.getGoodsAmount().getGoodType() == GoodType.CIGARS) {
            mission.getContext().setCigarsWasSold(
                    mission.getContext().getCigarsWasSold() + event.getGoodsAmount().getAmount());
        }
        if (mission.getContext().getCigarsWasSold() >= Default_1_mission.TRAGET_AMOUNT_OF_CIGARS) {
            missionCallBack.showMessage("campaign.default.cigarsWasSold");
        }
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            missionCallBack.showMessage("campaign.default.firstColonyWasFounded",
                    "campaign.default.produce100cigars");
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
        return !model.getColonies(mission.getHumanPlayer(model)).isEmpty();
    }
}