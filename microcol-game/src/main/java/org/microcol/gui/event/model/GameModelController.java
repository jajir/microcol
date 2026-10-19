package org.microcol.gui.event.model;

import java.util.List;

import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.i18n.I18n;
import com.google.inject.Provider;
import javafx.application.Platform;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.AttackOrder;
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

    private final static Logger logger = LoggerFactory
            .getLogger(GameModelController.class);

    private final SelectedTileManager selectedTileManager;

    private final ArtifitialPlayersManager artifitialPlayersManager;

    private final EventBus eventBus;

    private GameModel gameModel = null;

    private final Provider<DialogMessage> passengerMessage;
    private final I18n i18n;
    private final DiagnosticLog diagnosticLog;

    @Inject
    public GameModelController(final SelectedTileManager selectedTileManager,
            final ArtifitialPlayersManager artifitialPlayersManager,
            final EventBus eventBus,
            final Provider<DialogMessage> passengerMessage, final I18n i18n,
            final DiagnosticLog diagnosticLog) {
        this.diagnosticLog = Preconditions.checkNotNull(diagnosticLog);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.artifitialPlayersManager = Preconditions
                .checkNotNull(artifitialPlayersManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.passengerMessage = Preconditions.checkNotNull(passengerMessage);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * It start new game from model and prepare King AI and players AI.
     *
     * @param newModel required game model
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
     * @throws IllegalStateException when there is no running model
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
        return getHumanPlayer().getMaxBuyableGoods(goodsType);
    }

    public GoodsTrade getEuropeGoodsTradeForType(final GoodsType goodsType) {
        return getModel().getEurope().getGoodsTradeForType(goodsType,
                getHumanPlayer().getEuropeanNation());
    }

    /**
     * Get human player, even if human player if is not on turn.
     *
     * @return return human player
     */
    public Player getHumanPlayer() {
        return getModel().getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "There is no human player"));
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

    public void performFight(final AttackOrder order) {
        new Thread(() -> {
            try {
                Preconditions.checkState(
                        gameModel != null && getModel() == order.getModel(),
                        "The game changed during attack confirmation");
                order.execute();
            } catch (IllegalStateException rejected) {
                Platform.runLater(() -> {
                    final DialogMessage message = passengerMessage.get();
                    message.setTitle(i18n.get(Dialog.fight_title));
                    message.setText(i18n.get(Dialog.fight_unavailable));
                    message.showAndWait();
                });
            }
        }).start();
    }

    public void disembark(final Unit passenger, final Location targetLocation) {
        new Thread(() -> {
            try {
                passenger.disembarkToLocation(targetLocation);
            } catch (IllegalStateException rejected) {
                Platform.runLater(this::showPassengerTransferRejection);
            }
        }).start();
    }

    public void embark(final CargoSlot cargoSlot, final Unit unit) {
        new Thread(() -> {
            try {
                unit.embarkFromLocation(cargoSlot);
            } catch (IllegalStateException rejected) {
                Platform.runLater(this::showPassengerTransferRejection);
            }
        }).start();
    }

    public void showPassengerTransferRejection() {
        final DialogMessage message = passengerMessage.get();
        message.setTitle(i18n.get(Dialog.passenger_title));
        message.setText(i18n.get(Dialog.passenger_unavailable));
        message.showAndWait();
    }

    public boolean isHumanTurnReady() {
        return isGameModelReady() && getModel().isGameRunning()
                && getRealCurrentPlayer().isHuman();
    }

    public void showInvalidSavedGame() {
        final DialogMessage message = passengerMessage.get();
        message.setTitle(i18n.get(Dialog.loadGame_title));
        message.setText(i18n.get(Dialog.loadGame_invalid) + "\n\n"
                + diagnosticFolderText());
        message.showAndWait();
    }

    public void showRecoverySaveFailure() {
        final DialogMessage message = passengerMessage.get();
        message.setTitle(i18n.get(Dialog.recoverySave_title));
        message.setText(i18n.get(Dialog.recoverySave_failed) + "\n\n"
                + diagnosticFolderText());
        message.showAndWait();
    }

    public void showSaveFolderUnavailable() {
        final DialogMessage message = passengerMessage.get();
        message.setTitle(i18n.get(Dialog.loadGame_title));
        message.setText(i18n.get(Dialog.saveFolder_unavailable) + "\n\n"
                + diagnosticFolderText());
        message.showAndWait();
    }

    private String diagnosticFolderText() {
        return i18n.get(Dialog.diagnosticLog_folder) + " "
                + diagnosticLog.getDirectory();
    }

    public void showTurnPreparationUnavailable() {
        final DialogMessage message = passengerMessage.get();
        message.setTitle(i18n.get(Dialog.turnPreparation_title));
        message.setText(i18n.get(Dialog.turnPreparation_unavailable) + "\n\n"
                + diagnosticFolderText());
        message.showAndWait();
    }

    public void nextTurn() {
        if (!isHumanTurnReady()) {
            return;
        }
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
