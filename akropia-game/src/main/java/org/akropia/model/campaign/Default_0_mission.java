package org.akropia.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.akropia.model.Continent;
import org.akropia.model.ContinentTool;
import org.akropia.model.Continents;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.AkropiaException;
import org.akropia.model.Model;
import org.akropia.model.Path;
import org.akropia.model.Player;
import org.akropia.model.TerrainType;
import org.akropia.model.Unit;
import org.akropia.model.event.BeforeDeclaringIndependenceEvent;
import org.akropia.model.event.ColonyWasFoundEvent;
import org.akropia.model.event.GameStartedEvent;
import org.akropia.model.event.GoodsWasSoldInEuropeEvent;
import org.akropia.model.event.UnitMoveFinishedEvent;
import org.akropia.model.event.UnitMoveStartedEvent;

import com.google.common.collect.Lists;

final class Default_0_mission extends AbstractMission<Default_0_goals> {

    private final static Integer TRAGET_AMOUNT_OF_WINE = 30;

    Default_0_mission(final MissionCreationContext context,
            final Default_0_goals goals) {
        super(context, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR,
                GameOverProcessors.REBELLION_LOST_PROCESSOR,
                GameOverProcessors.REBELLION_WON_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult()
                                    .getGameOverReason())) {
                        fireEvent(EventFinishMission
                                .victory(Missions.default_m0_gameOver));
                        return "ok";
                    }
                    return null;
                });
    }

    public void onBeforeDeclaringIndependence(
            final BeforeDeclaringIndependenceEvent event) {
        /*
         * Disable declaring of independence.
         */
        event.stopEventExecution();
        fireEvent(new EventShowDialogWithMan(
                Missions.default_m0_cantDeclareIndependence));
    }

    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(event.getModel())) {
            fireEvent(new EventShowDialogWithMan(Missions.default_m0_start));
        }
    }

    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        if (!isPlayerHaveAnyColony(event.getModel())
                && isPathAtHighSeas(event.getModel(), event.getPath())) {
            event.stopEventExecution();
            fireEvent(new EventShowDialogWithMan(
                    Missions.default_m0_cantMoveToHighSeas));
        }
    }

    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (isFirstTurn(event.getModel())
                && event.getUnit().getActionPoints() == 0) {
            fireEvent(new EventShowDialogWithMan(
                    Missions.default_m0_pressNextTurn));
        } else {
            if (!getGoals().getGoalFindNewWorld().isFinished()) {
                final ContinentTool ct = new ContinentTool();
                final Continents continents = ct
                        .findContinents(event.getModel());
                final Continent c = continents
                        .getForLocation(Location.of(19, 15))
                        .orElseThrow(() -> new AkropiaException(
                                "Continent is not at expected location"));
                if (event.getUnit().isAtPlaceLocation()
                        && c.getDistance(event.getUnit().getLocation()) < 4) {
                    fireEvent(new EventShowDialogWithMan(
                            Missions.default_m0_continentInSight));
                    getGoals().getGoalFindNewWorld().setFinished(true);
                }
            }
        }
    }

    public void onBeforeEndTurn() {
        if (isFirstTurn(getModel())) {
            final Player human = getHumanPlayer(getModel());
            final Unit ship = findFirstShip(getModel(), human);
            if (ship.getActionPoints() == ship.getSpeed()) {
                fireEvent(new EventShowDialogWithMan(
                        Missions.default_m0_moveUnitBeforeEndTurn));
            }
        }
    }

    public void onTurnStarted() {
        super.onTurnStarted();
        if (getGoals().getGoalFoundColony().isFinished()) {
            if (!getGoals().getGoalProduceWine().isFinished()) {
                // verify wine producing
                final Player human = getHumanPlayer(getModel());
                if (human.getPlayerStatistics().getGoodsStatistics()
                        .getGoods(GoodsType.WINE) >= TRAGET_AMOUNT_OF_WINE) {
                    fireEvent(new EventShowDialogWithMan(
                            Missions.default_m0_sellWineInEuropePort));
                    getGoals().getGoalProduceWine().setFinished(true);
                }

            }
        }
    }

    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        if (event.getGoods().getType() == GoodsType.WINE) {
            getGoals().getGoalSellWine()
                    .setWasSold(getGoals().getGoalSellWine().getWasSold()
                            + event.getGoods().getAmount());
        }
        if (getGoals().getGoalSellWine()
                .getWasSold() >= TRAGET_AMOUNT_OF_WINE) {
            getGoals().getGoalSellWine().setFinished(true);
            fireEvent(new EventShowDialogWithMan(
                    Missions.default_m0_wineWasSold));
        }
    }

    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            getGoals().getGoalFoundColony().setFinished(true);
            fireEvent(new EventShowDialogWithMan(
                    Missions.default_m0_firstColonyWasFounded,
                    Missions.default_m0_produce100wine));
        }
    }

    private boolean isPathAtHighSeas(final Model model, final Path path) {
        return path
                .getLocations().stream().filter(loc -> model.getMap()
                        .getTerrainTypeAt(loc).equals(TerrainType.HIGH_SEA))
                .findAny().isPresent();
    }

    private Unit findFirstShip(final Model model, final Player player) {
        return model.getAllUnits().stream()
                .filter(u -> u.getOwner().equals(player))
                .filter(u -> u.getType().isShip()).findAny()
                .orElseThrow(() -> new AkropiaException(
                        "human player doesn't have any ship."));
    }

    private boolean isPlayerHaveAnyColony(final Model model) {
        return !model.getColonies(getHumanPlayer(model)).isEmpty();
    }
}