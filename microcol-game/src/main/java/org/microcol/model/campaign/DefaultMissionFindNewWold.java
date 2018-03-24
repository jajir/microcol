package org.microcol.model.campaign;

import org.microcol.gui.MicroColException;
import org.microcol.gui.event.model.MissionCallBack;
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

/**
 * First mission. Find New World.
 */
public class DefaultMissionFindNewWold extends AbstractMission {

    private final static String NAME = "findNewWorld";

    private final static String MODEL_FIND_NEW_WORLD = "/maps/default-" + NAME + ".json";

    DefaultMissionFindNewWold() {
        super(NAME, 0, MODEL_FIND_NEW_WORLD);
        setFinished(true);
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
                if (isAnyLocationAtHighSeas(event.getModel(), event.getPath())) {
                    event.stopEventExecution();
                    missionCallBack.showMessage("campaign.default.cantMoveToHighSeas");
                }
            }

            @Override
            public void unitMoveFinished(final UnitMoveFinishedEvent event) {
                if (isFirstTurn(event.getModel()) && event.getUnit().getAvailableMoves() == 0) {
                    missionCallBack.showMessage("campaign.default.pressNextTurn");
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

        });
    }

}
