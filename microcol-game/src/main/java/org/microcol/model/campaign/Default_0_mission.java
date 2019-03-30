package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.ai.Continent;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.gui.MicroColException;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;

import com.google.common.collect.Lists;

final class Default_0_mission extends AbstractMission<Default_0_goals> {

    private final static Integer TRAGET_AMOUNT_OF_CIGARS = 30;

    Default_0_mission(final MissionCreationContext context, final Default_0_goals goals) {
        super(context, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        fireEvent(new EventFinishMission(Missions.default_m0_gameOver));
                        return "ok";
                    }
                    return null;
                });
    }

    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        /*
         * Disable declaring of independence.
         */
        event.stopEventExecution();
        fireEvent(new EventShowMessages(Missions.default_m0_cantDeclareIndependence));
    }

    public void onGameStarted(final GameStartedEvent event) {
        if (isFirstTurn(event.getModel())) {
            fireEvent(new EventShowMessages(Missions.default_m0_start));
        }
    }

    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        if (!isPlayerHaveAnyColony(event.getModel())
                && isPathAtHighSeas(event.getModel(), event.getPath())) {
            event.stopEventExecution();
            fireEvent(new EventShowMessages(Missions.default_m0_cantMoveToHighSeas));
        }
    }

    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        if (isFirstTurn(event.getModel()) && event.getUnit().getActionPoints() == 0) {
            fireEvent(new EventShowMessages(Missions.default_m0_pressNextTurn));
        } else {
            if (!getGoals().getGoalFindNewWorld().isFinished()) {
                final ContinentTool ct = new ContinentTool();
                final Continents continents = ct.findContinents(event.getModel(),
                        getModel().getCurrentPlayer());
                final Continent c = continents.getForLocation(Location.of(19, 15)).orElseThrow(
                        () -> new MicroColException("Continent is not at expected location"));
                if (event.getUnit().isAtPlaceLocation()
                        && c.getDistance(event.getUnit().getLocation()) < 4) {
                    fireEvent(new EventShowMessages(Missions.default_m0_continentInSight));
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
                fireEvent(new EventShowMessages(Missions.default_m0_moveUnitBeforeEndTurn));
            }
        }
    }

    public void onTurnStarted() {
        if (getGoals().getGoalFoundColony().isFinished()) {
            if (!getGoals().getGoalProduceCigars().isFinished()) {
                // verify cigars producing
                final Player human = getHumanPlayer(getModel());
                if (human.getPlayerStatistics().getGoodsStatistics()
                        .getGoods(GoodsType.CIGARS) >= TRAGET_AMOUNT_OF_CIGARS) {
                    fireEvent(new EventShowMessages(Missions.default_m0_sellCigarsInEuropePort));
                    getGoals().getGoalProduceCigars().setFinished(true);
                }

            }
        }
    }

    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        if (event.getGoods().getType() == GoodsType.CIGARS) {
            getGoals().getGoalSellCigars().setWasSold(
                    getGoals().getGoalSellCigars().getWasSold() + event.getGoods().getAmount());
        }
        if (getGoals().getGoalSellCigars().getWasSold() >= TRAGET_AMOUNT_OF_CIGARS) {
            getGoals().getGoalSellCigars().setFinished(true);
            fireEvent(new EventShowMessages(Missions.default_m0_cigarsWasSold));
        }
    }

    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (isPlayerHaveAnyColony(event.getModel())) {
            getGoals().getGoalFoundColony().setFinished(true);
            fireEvent(new EventShowMessages(Missions.default_m0_firstColonyWasFounded,
                    Missions.default_m0_produce100cigars));
        }
    }

    private boolean isPathAtHighSeas(final Model model, final Path path) {
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