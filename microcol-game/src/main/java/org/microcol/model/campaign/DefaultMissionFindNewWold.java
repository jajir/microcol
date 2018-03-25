package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;

import org.microcol.ai.Continent;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.store.ModelPo;

/**
 * First mission. Find New World.
 */
public class DefaultMissionFindNewWold extends AbstractMission {

    private final static String NAME = "findNewWorld";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    private final static String MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_WAS_SHOWN = "wasContinentOnSightMessageWasShown";

    private boolean wasContinentOnSightMessageWasShown = false;

    DefaultMissionFindNewWold() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
    }

    @Override
    public void startMission(final Model model, final MissionCallBack missionCallBack) {
        model.addListener(new ModelListenerAdapter() {

            @Override
            public void gameStarted(final GameStartedEvent event) {
                missionCallBack.addCallWhenReady(model -> {
                    missionCallBack.showMessage("campaign.default.start");
                });
            }

            @Override
            public void unitMoveStarted(final UnitMoveStartedEvent event) {
                final Player human = getHumanPlayer(event.getModel());
                if (!isPlayerHaveAnyColony(event.getModel(), human)
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
                    if (!wasContinentOnSightMessageWasShown) {
                        final ContinentTool ct = new ContinentTool();
                        final Continents continents = ct.findContinents(event.getModel(),
                                model.getCurrentPlayer());
                        final Continent c = continents.getForLocation(Location.of(9, 15))
                                .orElseThrow(() -> new MicroColException(
                                        "Continent is not at expected location"));
                        if (event.getUnit().isAtPlaceLocation()
                                && c.getDistance(event.getUnit().getLocation()) < 4) {
                            missionCallBack.showMessage("campaign.default.continentInSight");
                            wasContinentOnSightMessageWasShown = true;
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

            private boolean isAnyLocationAtHighSeas(final Model model, final Path path) {
                return path.getLocations().stream().filter(
                        loc -> model.getMap().getTerrainTypeAt(loc).equals(TerrainType.HIGH_SEA))
                        .findAny().isPresent();
            }

            private boolean isFirstTurn(final Model model) {
                return model.getCalendar().getCurrentYear() == model.getCalendar().getStartYear();
            }

            private Player getHumanPlayer(final Model model) {
                return model.getPlayers().stream().filter(p -> p.isHuman()).findAny()
                        .orElseThrow(() -> new MicroColException("There is no human player."));
            }

            private Unit findFirstShip(final Model model, final Player player) {
                return model.getAllUnits().stream().filter(u -> u.getOwner().equals(player))
                        .filter(u -> u.getType().isShip()).findAny().orElseThrow(
                                () -> new MicroColException("human player doesn't have any ship."));
            }

            private boolean isPlayerHaveAnyColony(final Model model, final Player player) {
                return !model.getColonies(player).isEmpty();
            }

        });
    }

    @Override
    public void initialize(ModelPo modelPo) {
        if (modelPo.getCampaign().getData() != null) {
            wasContinentOnSightMessageWasShown = Boolean.parseBoolean(modelPo.getCampaign()
                    .getData().get(MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_WAS_SHOWN));
        }
    }

    @Override
    public Map<String, String> saveToMap() {
        final Map<String, String> out = new HashMap<>();
        out.put(MAP_KEY_WAS_CONTINENT_ON_SIGHT_MESSAGE_WAS_SHOWN,
                Boolean.toString(wasContinentOnSightMessageWasShown));
        return out;
    }

}
