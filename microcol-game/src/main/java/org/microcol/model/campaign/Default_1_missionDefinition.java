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

    Default_1_missionDefinition(final Default_1_mission mission, final Model model,
            MissionCallBack missionCallBack) {
        super(missionCallBack);
        this.mission = Preconditions.checkNotNull(mission);
        this.model = model;
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
        event.stopEventExecution();
        missionCallBack.showMessage("campaign.default.m1.cantDeclareIndependence");
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        if (this.mission.isFirstTurn(event.getModel())) {
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
        if (this.mission.isFirstTurn(event.getModel())
                && event.getUnit().getAvailableMoves() == 0) {
            missionCallBack.showMessage("campaign.default.pressNextTurn");
        } else {
            if (!this.mission.getContext().isWasContinentOnSightMessageShown()) {
                final ContinentTool ct = new ContinentTool();
                final Continents continents = ct.findContinents(event.getModel(),
                        model.getCurrentPlayer());
                final Continent c = continents.getForLocation(Location.of(9, 15)).orElseThrow(
                        () -> new MicroColException("Continent is not at expected location"));
                if (event.getUnit().isAtPlaceLocation()
                        && c.getDistance(event.getUnit().getLocation()) < 4) {
                    missionCallBack.showMessage("campaign.default.continentInSight");
                    this.mission.getContext().setWasContinentOnSightMessageShown(true);
                }
            }
        }
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        if (this.mission.isFirstTurn(event.getModel())) {
            final Player human = this.mission.getHumanPlayer(event.getModel());
            final Unit ship = findFirstShip(event.getModel(), human);
            if (ship.getAvailableMoves() == ship.getType().getSpeed()) {
                missionCallBack.showMessage("campaign.default.moveUnitBeforeEndTurn");
            }
        }
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            final Player human = this.mission.getHumanPlayer(event.getModel());
            if (human.getPlayerStatistics().getGoodsStatistics()
                    .getGoodsAmount(GoodType.CIGARS) >= Default_1_mission.TRAGET_AMOUNT_OF_CIGARS) {
                if (!this.mission.getContext().isWasMessageSellCigarsShown()) {
                    missionCallBack.showMessage("campaign.default.sellCigarsInEuropePort");
                    this.mission.getContext().setWasMessageSellCigarsShown(true);
                }
            }
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        if (event.getGoodsAmount().getGoodType() == GoodType.CIGARS) {
            this.mission.getContext().setCigarsWasSold(this.mission.getContext().getCigarsWasSold()
                    + event.getGoodsAmount().getAmount());
        }
        if (this.mission.getContext()
                .getCigarsWasSold() >= Default_1_mission.TRAGET_AMOUNT_OF_CIGARS) {
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
        return !model.getColonies(this.mission.getHumanPlayer(model)).isEmpty();
    }
}