package org.microcol.gui.event.model;

import java.util.List;
import java.util.Optional;

import org.microcol.diagnostics.DiagnosticLog;
import org.microcol.gui.Loc;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.gamepanel.ScrollToFocusedTile;
import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.i18n.I18n;
import com.google.inject.Provider;
import javafx.application.Platform;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.AttackOrder;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ForeignColonyTermsChangedException;
import org.microcol.model.ForeignColonyTradeQuote;
import org.microcol.model.Goods;
import org.microcol.model.GoodsTrade;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeChiefPreview;
import org.microcol.model.NativeChiefResult;
import org.microcol.model.NativeMissionContestOutcome;
import org.microcol.model.NativeConquestQuote;
import org.microcol.model.NativeConquestResult;
import org.microcol.model.NativeIncitementOutcome;
import org.microcol.model.NativeIncitementQuote;
import org.microcol.model.NativeMissionContestView;
import org.microcol.model.NativeMissionView;
import org.microcol.model.NativePurchaseQuote;
import org.microcol.model.NativeSaleQuote;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeTributeQuote;
import org.microcol.model.NativeTributeResult;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
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
        new Thread(() -> {
            try {
                getModel().moveUnit(ship, Path.of(path));
            } catch (IllegalStateException
                    | IllegalArgumentException rejected) {
                Platform.runLater(() -> eventBus.post(new StatusBarMessageEvent(
                        i18n.get(ship.isShip() && ship.isAtPlaceLocation()
                                && !ship.getOwner().canTradeInEurope()
                                && path.stream().anyMatch(step -> getModel()
                                        .getHighSea()
                                        .isDepartureStep(ship.getLocation(),
                                                step)) ? Buttons.moveEuropeClosed
                                                        : Buttons.moveChanged),
                        StatusBarMessageEvent.Source.GAME)));
            }
        }).start();
    }

    /**
     * Issue a go-to order and let the unit walk its first steps, like
     * {@link #performMove}, on its own thread: the walk waits for animations. A
     * request that became stale is rejected by the model without a change and
     * explained in the status bar.
     *
     * @param unit        required selected unit
     * @param destination required destination square
     * @param colony      the named colony chosen as destination, or
     *                    {@code null} for a plain map square
     */
    public void performGoTo(final Unit unit, final Location destination,
            final Colony colony) {
        new Thread(() -> {
            try {
                if (colony == null) {
                    getModel().issueGoTo(unit, destination);
                } else {
                    getModel().issueGoToColony(unit, colony);
                }
            } catch (IllegalStateException
                    | IllegalArgumentException rejected) {
                Platform.runLater(() -> eventBus.post(
                        new StatusBarMessageEvent(i18n.get(Buttons.goToChanged),
                                StatusBarMessageEvent.Source.GAME)));
            }
        }).start();
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

    /**
     * Start learning the skill the player was shown (R-0148). The model checks
     * the offer again, so a changed offer or situation is reported and nothing
     * starts. The model call is quick, so it runs here and the caller learns
     * whether the colonist left the map.
     *
     * @param offeredSkill the skill the confirmation displayed
     * @return whether the course started
     */
    public boolean startNativeTeaching(final Unit visitor,
            final NativeSettlement settlement, final UnitType offeredSkill) {
        try {
            getModel().startNativeTeaching(visitor, settlement, offeredSkill);
            return true;
        } catch (IllegalStateException rejected) {
            showNativeTeachingRejection(Loc.nativeTeachingRejected);
            return false;
        }
    }

    /**
     * Visit the native chief with the terms the player confirmed (R-0158). The
     * model checks the terms and the visitor again; a refused visit is
     * explained and changes nothing. The model call is quick, so it runs here.
     *
     * @param visitor    scout beside the settlement
     * @param settlement native settlement
     * @param shown      terms the confirmation displayed
     * @return the actual result, empty when the visit was refused
     */
    public Optional<NativeChiefResult> visitNativeChief(final Unit visitor,
            final NativeSettlement settlement, final NativeChiefPreview shown) {
        try {
            return Optional.of(
                    getModel().visitNativeChief(visitor, settlement, shown));
        } catch (IllegalStateException rejected) {
            showNativeChiefRejection();
            return Optional.empty();
        }
    }

    /**
     * Establish the mission the player confirmed (R-0164). The model checks the
     * terms and the missionary again; a refused mission is explained and
     * changes nothing. The model call is quick, so it runs here and the caller
     * learns whether the missionary left the map.
     *
     * @param missionary required missionary beside the settlement
     * @param settlement required native settlement
     * @param shown      terms the confirmation displayed
     * @return whether the mission was established
     */
    public boolean establishNativeMission(final Unit missionary,
            final NativeSettlement settlement, final NativeMissionView shown) {
        try {
            getModel().establishNativeMission(missionary, settlement, shown);
            return true;
        } catch (IllegalStateException rejected) {
            showNativeMissionRejection();
            return false;
        }
    }

    /**
     * Challenge the rival mission the player confirmed (R-0165). The model
     * checks the terms and both missionaries again and draws once; a refused
     * contest is explained and changes nothing. The model call is quick, so it
     * runs here and the caller reports the actual outcome.
     *
     * @param visitor    required missionary beside the settlement
     * @param settlement required native settlement
     * @param shown      terms the confirmation displayed
     * @return the actual outcome, empty when the contest was refused
     */
    public Optional<NativeMissionContestOutcome> contestNativeMission(
            final Unit visitor, final NativeSettlement settlement,
            final NativeMissionContestView shown) {
        try {
            return Optional.of(getModel().contestNativeMission(visitor,
                    settlement, shown));
        } catch (IllegalStateException rejected) {
            showNativeContestRejection();
            return Optional.empty();
        }
    }

    /**
     * Confirm the paid incitement the player chose (R-0166). The model checks
     * the terms, the purse and the order revision, then pays and draws once; a
     * refused incitement is explained and changes nothing.
     *
     * @param visitor    required missionary beside the settlement
     * @param settlement required native settlement
     * @param shown      terms the confirmation displayed
     * @return the actual outcome, empty when the incitement was refused
     */
    public Optional<NativeIncitementOutcome> incite(final Unit visitor,
            final NativeSettlement settlement,
            final NativeIncitementQuote shown) {
        try {
            return Optional.of(getModel().incite(visitor, settlement, shown));
        } catch (IllegalStateException rejected) {
            showNativeIncitementRejection();
            return Optional.empty();
        }
    }

    /**
     * Confirm the attack on a native settlement the player chose (R-0174). The
     * model re-checks the frozen terms before its first draw; a refused attack
     * is explained and changes nothing.
     *
     * @param visitor    required armed unit beside the settlement
     * @param settlement required native settlement
     * @param shown      terms the confirmation displayed
     * @return the actual result, empty when the attack was refused
     */
    public Optional<NativeConquestResult> attack(final Unit visitor,
            final NativeSettlement settlement,
            final NativeConquestQuote shown) {
        try {
            return Optional.of(getModel().attackNativeSettlement(visitor,
                    settlement, shown));
        } catch (IllegalStateException rejected) {
            showNativeAttackRejection();
            return Optional.empty();
        }
    }

    /**
     * Demand tribute with the terms the player confirmed (R-0162). The model
     * re-checks the frozen terms, the wait and the treasury before any change;
     * a refused demand is explained and changes nothing. The model call is
     * quick, so it runs here.
     *
     * @param visitor    required armed unit beside the settlement
     * @param settlement required native settlement
     * @param shown      terms the confirmation displayed
     * @return the actual result, empty when the demand was refused
     */
    public Optional<NativeTributeResult> demandNativeTribute(final Unit visitor,
            final NativeSettlement settlement, final NativeTributeQuote shown) {
        try {
            return Optional.of(
                    getModel().demandNativeTribute(visitor, settlement, shown));
        } catch (IllegalStateException rejected) {
            showNativeTributeRejection();
            return Optional.empty();
        }
    }

    /**
     * Stop a native course by the player's choice, or let a finished or
     * canceled colonist leave (R-0148). The colonist returns beside the
     * settlement without a new grant. A blocked leave is explained and changes
     * nothing.
     */
    public void cancelNativeTeaching(final Unit pupil) {
        if (!getModel().hasNativeTeachingExit(pupil)) {
            showNativeTeachingRejection(Loc.nativeTeachingNoSquare);
            return;
        }
        try {
            getModel().cancelNativeTeaching(pupil);
        } catch (IllegalStateException rejected) {
            showNativeTeachingRejection(Loc.nativeTeachingCancelRejected);
        }
    }

    /**
     * Introduce the player's nation to a native tribe by land (R-0159). The
     * model checks the visitor again; a refused introduction is explained and
     * changes nothing.
     *
     * @param introducer land unit beside the settlement
     * @param settlement native settlement
     */
    public void introduceNativeContact(final Unit introducer,
            final NativeSettlement settlement) {
        try {
            getModel().introduceNativeContact(introducer, settlement);
        } catch (IllegalStateException rejected) {
            showNativeMarketRejection();
        }
    }

    /**
     * Open or reuse the trading expedition of a carrier at a settlement
     * (R-0159). The model re-checks admission, so a changed situation is
     * explained and no expedition starts.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     */
    public void enterNativeMarket(final Unit carrier,
            final NativeSettlement settlement) {
        try {
            getModel().enterNativeMarket(carrier, settlement);
        } catch (IllegalStateException rejected) {
            showNativeMarketRejection();
        }
    }

    /**
     * Haggle once over the carried goods of a carrier at a settlement (R-0160).
     * The model draws the result after its checks and keeps it with the
     * expedition; a refused attempt is explained and changes nothing.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     * @param shown      the offer the chooser displayed, with its haggle open
     */
    public void haggleNativeSale(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote shown) {
        try {
            getModel().haggleNativeSale(carrier, settlement, shown);
        } catch (IllegalStateException rejected) {
            showNativeSaleRejection();
        }
    }

    /**
     * Sell goods at the terms the player confirmed (R-0160). Stale or illegal
     * terms are explained and nothing is traded.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     * @param offered    sale terms the confirmation displayed
     */
    public void sellToNative(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote offered) {
        try {
            getModel().sellToNative(carrier, settlement, offered);
        } catch (IllegalStateException rejected) {
            showNativeSaleRejection();
        }
    }

    /**
     * Give goods at the terms the player confirmed (R-0160). Stale or illegal
     * terms are explained and nothing is given.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     * @param offered    gift terms the confirmation displayed
     */
    public void giftToNative(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote offered) {
        try {
            getModel().giftToNative(carrier, settlement, offered);
        } catch (IllegalStateException rejected) {
            showNativeSaleRejection();
        }
    }

    /**
     * Ask for the one discount of the second stage (R-0161). The model draws
     * the result after its checks and keeps it with the stage; a refused
     * attempt is explained and changes nothing.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     * @param shown      the purchase the chooser displayed, with its request
     *                   open
     */
    public void haggleNativePurchase(final Unit carrier,
            final NativeSettlement settlement,
            final NativePurchaseQuote shown) {
        try {
            getModel().haggleNativePurchase(carrier, settlement, shown);
        } catch (IllegalStateException rejected) {
            showNativePurchaseRejection();
        }
    }

    /**
     * Buy offered goods at the terms the player confirmed (R-0161). Stale or
     * illegal terms are explained and nothing is bought.
     *
     * @param carrier    wagon or ship beside the settlement
     * @param settlement native settlement
     * @param offered    purchase terms the confirmation displayed
     * @return whether the goods were bought
     */
    public boolean buyFromNative(final Unit carrier,
            final NativeSettlement settlement,
            final NativePurchaseQuote offered) {
        try {
            getModel().buyFromNative(carrier, settlement, offered);
            return true;
        } catch (IllegalStateException rejected) {
            showNativePurchaseRejection();
            return false;
        }
    }

    /**
     * Decline the pending second stage (R-0161); nothing is bought.
     *
     * @param carrier    unit beside the settlement
     * @param settlement native settlement
     * @return whether the stage was declined
     */
    public boolean rejectNativePurchase(final Unit carrier,
            final NativeSettlement settlement) {
        try {
            getModel().rejectNativePurchase(carrier, settlement);
            return true;
        } catch (IllegalStateException rejected) {
            showNativePurchaseRejection();
            return false;
        }
    }

    /**
     * Trade with an adjacent foreign colony at the terms the player confirmed
     * (R-0167). Only the expected stale-terms rejection is caught: the model
     * then moves nothing and the dialog shows the live state again.
     *
     * @param visitor wagon or ship beside the colony
     * @param colony  foreign colony
     * @param offered quote the confirmation displayed
     * @return whether the trade was made
     */
    public boolean tradeWithForeignColony(final Unit visitor,
            final Colony colony, final ForeignColonyTradeQuote offered) {
        try {
            getModel().tradeWithForeignColony(visitor, colony, offered);
            return true;
        } catch (ForeignColonyTermsChangedException changed) {
            return false;
        }
    }

    private void showNativePurchaseRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeMarketTitle));
        dialog.setText(i18n.get(Loc.nativePurchaseRejected));
        dialog.showAndWait();
    }

    private void showNativeMarketRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeMarketTitle));
        dialog.setText(i18n.get(Loc.nativeMarketRejected));
        dialog.showAndWait();
    }

    private void showNativeSaleRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeMarketTitle));
        dialog.setText(i18n.get(Loc.nativeSaleRejected));
        dialog.showAndWait();
    }

    private void showNativeTeachingRejection(final Loc message) {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeTeachingTitle));
        dialog.setText(i18n.get(message));
        dialog.showAndWait();
    }

    private void showNativeMissionRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeMissionTitle));
        dialog.setText(i18n.get(Loc.nativeMissionRejected));
        dialog.showAndWait();
    }

    private void showNativeContestRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeContestTitle));
        dialog.setText(i18n.get(Loc.nativeContestRejected));
        dialog.showAndWait();
    }

    private void showNativeTributeRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeTributeTitle));
        dialog.setText(i18n.get(Loc.nativeTributeRejected));
        dialog.showAndWait();
    }

    private void showNativeAttackRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeAttackTitle));
        dialog.setText(i18n.get(Loc.nativeAttackRejected));
        dialog.showAndWait();
    }

    private void showNativeIncitementRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeIncitementTitle));
        dialog.setText(i18n.get(Loc.nativeIncitementRejected));
        dialog.showAndWait();
    }

    private void showNativeChiefRejection() {
        final DialogMessage dialog = passengerMessage.get();
        dialog.setTitle(i18n.get(Loc.nativeChiefTitle));
        dialog.setText(i18n.get(Loc.nativeChiefRejected));
        dialog.showAndWait();
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
