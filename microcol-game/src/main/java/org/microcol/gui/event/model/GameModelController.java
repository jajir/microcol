package org.microcol.gui.event.model;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsTrade;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.WorldMap;
import org.microcol.model.campaign.GameModel;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 * <p>
 * There is one class instance in runtime.
 * </p>
 */
public class GameModelController {

    private final static Logger logger = LoggerFactory.getLogger(GameModelController.class);

    private final SelectedTileManager selectedTileManager;

    private final ArtifitialPlayersManager artifitialPlayersManager;

    private final EventBus eventBus;

    private GameModel gameModel = null;

    @Inject
    public GameModelController(final SelectedTileManager selectedTileManager,
            final ArtifitialPlayersManager artifitialPlayersManager, final EventBus eventBus) {
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.artifitialPlayersManager = Preconditions.checkNotNull(artifitialPlayersManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    /**
     * It start new game from model and prepare King AI and players AI.
     * 
     * @param newModel
     *            required game model
     */
    void setAndStartModel(final GameModel newModel) {
        tryToStopGame();
        gameModel = Preconditions.checkNotNull(newModel);
        artifitialPlayersManager.initRobotPlayers(getModel());
        gameModel.addListener(new ModelListenerImpl(this, eventBus));
        gameModel.startGame();
        if (getModel().getFocusedField() != null) {
            selectedTileManager.setSelectedTile(getModel().getFocusedField(),
                    ScrollToFocusedTile.skip);
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
        Preconditions.checkState(gameModel != null, "Model is not ready");
        return gameModel.getModel();
    }

    public WorldMap getMap() {
        return gameModel.getModel().getMap();
    }
    
    public Location getMapSize() {
        return getMap().getMapSize();
    }

    public int getKingsTaxPercentage() {
        return getModel().getKingsTaxForPlayer(getHumanPlayer());
    }

    public boolean isGameModelReady() {
        return gameModel != null;
    }

    public Player getRealCurrentPlayer() {
        return gameModel.getModel().getCurrentPlayer();
    }

    public Goods getMaxBuyableGoods(final GoodsType goodsType) {
        final GoodsTrade goodsTrade = getModel().getEurope().getGoodsTradeForType(goodsType);
        return goodsTrade.getAvailableAmountFor(getHumanPlayer().getGold());
    }

    public GoodsTrade getEuropeGoodsTradeForType(final GoodsType goodsType) {
        return getModel().getEurope().getGoodsTradeForType(goodsType);
    }

    /**
     * Get human player, even if human player if is not on turn.
     * 
     * @return return human player
     */
    public Player getHumanPlayer() {
        return getModel().getPlayers().stream().filter(Player::isHuman).findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no human player"));
    }

    private void tryToStopGame() {
        if (gameModel != null) {
            artifitialPlayersManager.destroyRobotPlayers();
            gameModel.stop();
            gameModel = null;
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

    public void disembark(final UnitWithCargo movingUnit, final Location targetLocation) {
        new Thread(() -> {
            movingUnit.getCargo().getSlots().stream()
                    .filter(cargoSlot -> cargoSlot.isLoadedUnit())
                    .map(cargoSlot -> cargoSlot.getUnit().get())
                    .filter(unit -> unit.getActionPoints() > 0)
                    .forEach(unit -> unit.disembarkToLocation(targetLocation));
        }).start();
    }

    public void embark(final CargoSlot cargoSlot, final Unit unit) {
        new Thread(() -> {
            unit.embarkFromLocation(cargoSlot);
        }).start();
    }

    public void nextTurn() {
        logger.debug("Next Turn event was triggered.");
        new Thread(() -> getModel().endTurn()).start();
    }

    /**
     * @return the modelCapgaign
     */
    public GameModel getGameModel() {
        return gameModel;
    }

    public void stop() {
        tryToStopGame();
        gameModel = null;
    }

}
