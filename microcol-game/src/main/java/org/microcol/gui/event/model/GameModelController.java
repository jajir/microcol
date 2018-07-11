package org.microcol.gui.event.model;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.gamepanel.SelectedTileManager;
import org.microcol.model.GoodTrade;
import org.microcol.model.GoodType;
import org.microcol.model.GoodsAmount;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.campaign.ModelMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 * <p>
 * There is one class instance in runtime.
 * </p>
 */
public class GameModelController {

    private final static Logger logger = LoggerFactory.getLogger(GameModelController.class);

    private final ModelEventManager modelEventManager;

    private final SelectedTileManager selectedTileManager;

    private final ArtifitialPlayersManager artifitialPlayersManager;

    private ModelMission modelMission = null;

    @Inject
    public GameModelController(final ModelEventManager modelEventManager,
            final SelectedTileManager selectedTileManager,
            final ArtifitialPlayersManager artifitialPlayersManager) {
        this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.artifitialPlayersManager = Preconditions.checkNotNull(artifitialPlayersManager);
    }

    /**
     * It start new game from model and prepare King AI and players AI.
     * 
     * @param newModel
     *            required game model
     */
    void setAndStartModel(final ModelMission newModel) {
        tryToStopGame();
        modelMission = Preconditions.checkNotNull(newModel);
        artifitialPlayersManager.initRobotPlayers(getModel());
        modelMission.addListener(new ModelListenerImpl(modelEventManager, this));
        modelMission.startGame();
        if (getModel().getFocusedField() != null) {
            selectedTileManager.setSelectedTile(getModel().getFocusedField());
        }
    }

    /**
     * Provide actually played model.
     * <p>
     * This should be main model access method.
     * </p>
     * 
     * @return Return currently running model.
     * @throws IllegalStateException
     *             when there is no running model
     */
    public Model getModel() {
        Preconditions.checkState(modelMission != null, "Model is not ready");
        return modelMission.getModel();
    }

    public void captureColonyAt(final Unit movingUnit, final Location moveToLocation) {
        new Thread(() -> movingUnit.captureColony(moveToLocation)).start();
    }

    public boolean isModelReady() {
        return modelMission != null;
    }

    public Player getCurrentPlayer() {
        return getModel().getPlayers().stream().filter(Player::isHuman).findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no human player"));
    }

    public GoodsAmount getMaxBuyableGoodsAmount(final GoodType goodType) {
        final GoodTrade goodTrade = getModel().getEurope().getGoodTradeForType(goodType);
        return goodTrade.getAvailableAmountFor(getCurrentPlayer().getGold());
    }

    /**
     * Get human player, even in not on turn.
     * 
     * @return return human player
     */
    public Player getHumanPlayer() {
        // It use getCurrentPLaye implementation which is not correct.
        return getCurrentPlayer();
    }

    private void tryToStopGame() {
        if (modelMission != null) {
            artifitialPlayersManager.destroyRobotPlayers();
            modelMission.stop();
            modelMission = null;
        }
    }

    public void performMove(final Unit ship, final List<Location> path) {
        logger.debug("Start move ship: " + ship);
        new Thread(() -> getModel().moveUnit(ship, Path.of(path))).start();
    }

    public void performFight(final Unit attacker, final Unit defender) {
        logger.debug("Start move ship: " + attacker);
        new Thread(() -> {
            /**
             * If it's necessary than move.
             */
            final Optional<List<Location>> locations = attacker.getPath(defender.getLocation(),
                    true);
            if (locations.isPresent() && !locations.get().isEmpty()) {
                getModel().moveUnit(attacker, Path.of(locations.get()));
            }
            attacker.attack(defender.getLocation());
        }).start();
    }

    public void nextTurn() {
        logger.debug("Next Turn event was triggered.");
        new Thread(() -> getModel().endTurn()).start();
    }

    public void suspendAi() {
        artifitialPlayersManager.suspendAi();
    }

    public void resumeAi() {
        artifitialPlayersManager.resumeAi();
    }

    /**
     * @return the modelCapgaign
     */
    public ModelMission getModelMission() {
        return modelMission;
    }

    public void stop() {
        tryToStopGame();
        modelMission = null;
    }

}
