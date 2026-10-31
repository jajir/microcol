package org.microcol.gui.screen.game.gamepanel;

import javafx.application.Platform;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.microcol.gui.dialog.ChoosePassengerDialog;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogColonyWasCaptured;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.DialogUnitCantMoveHere;
import org.microcol.gui.screen.foreigncolony.DialogForeignColonyTrade;
import org.microcol.gui.screen.nativesettlements.DialogNativeAttack;
import org.microcol.gui.screen.nativesettlements.DialogNativeChief;
import org.microcol.gui.screen.nativesettlements.DialogNativeContest;
import org.microcol.gui.screen.nativesettlements.DialogNativeIncitement;
import org.microcol.gui.screen.nativesettlements.DialogNativeMission;
import org.microcol.gui.screen.nativesettlements.DialogNativePurchase;
import org.microcol.gui.screen.nativesettlements.DialogNativeSale;
import org.microcol.gui.screen.nativesettlements.DialogNativeTribute;
import org.microcol.gui.screen.nativesettlements.DialogNativeVisit;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.RefreshRightPanelEvent;
import org.microcol.gui.event.StartGoToEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UnitUtil;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.AttackOrder;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ForeignColonyTradeProblem;
import org.microcol.model.Model;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.NativeChiefOutcome;
import org.microcol.model.NativeConquestQuote;
import org.microcol.model.NativeConquestResult;
import org.microcol.model.NativeChiefPreview;
import org.microcol.model.NativeChiefResult;
import org.microcol.model.NativeMarketView;
import org.microcol.model.EuropeanNation;
import org.microcol.model.NativeIncitementOutcome;
import org.microcol.model.NativeIncitementQuote;
import org.microcol.model.NativeMissionContestOutcome;
import org.microcol.model.NativeMissionContestView;
import org.microcol.model.NativeMissionView;
import org.microcol.model.NativePurchaseQuote;
import org.microcol.model.NativeSaleQuote;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeTributeQuote;
import org.microcol.model.NativeTributeResult;
import org.microcol.model.NativeVisitChoice;
import org.microcol.model.Unit;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;

@Listener
public final class GamePanelPresenter {

    private final Logger logger = LoggerFactory
            .getLogger(GamePanelPresenter.class);

    private final GamePreferences gamePreferences;

    private final GameModelController gameModelController;

    private final GamePanelPainter gamePanelView;

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final ViewUtil viewUtil;

    private final EventBus eventBus;

    private final I18n i18n;

    private final DialogColonyWasCaptured dialogColonyWasCaptured;

    private final ModeController modeController;

    private final VisibleAreaService visibleArea;

    private final UnitUtil unitUtil;

    private final MoveModeController moveModeController;
    /** Whether the next normal map click chooses a go-to destination. */
    private boolean goToPicking;

    private final ChoosePassengerDialog choosePassenger;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    @Inject
    public GamePanelPresenter(final GamePanelPainter gamePanelPainter,
            final DialogColonyWasCaptured dialogColonyWasCaptured,
            final GameModelController gameModelController,
            final GamePreferences gamePreferences,
            final SelectedTileManager selectedTileManager,
            final ViewUtil viewUtil, final EventBus eventBus,
            final ModeController modeController,
            final SelectedUnitManager selectedUnitManager, final I18n i18n,
            final @Named("game") VisibleAreaService visibleArea,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final UnitUtil unitUtil,
            final MoveModeController moveModeController,
            final ChoosePassengerDialog choosePassenger) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gamePreferences = gamePreferences;
        this.choosePassenger = Preconditions.checkNotNull(choosePassenger);
        this.gamePanelView = Preconditions.checkNotNull(gamePanelPainter);
        this.dialogColonyWasCaptured = Preconditions
                .checkNotNull(dialogColonyWasCaptured);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions
                .checkNotNull(oneTurnMoveHighlighter);
        this.unitUtil = Preconditions.checkNotNull(unitUtil);
        this.moveModeController = Preconditions
                .checkNotNull(moveModeController);
    }

    @Subscribe
    private void onStartMove(
            @SuppressWarnings("unused") final StartMoveEvent event) {
        if (gameModelController.isHumanTurnReady()) {
            goToPicking = false;
            swithToMoveMode();
        }
    }

    /**
     * The next map click chooses the go-to destination of the selected unit;
     * Escape leaves the unit unchanged.
     */
    @Subscribe
    private void onStartGoTo(
            @SuppressWarnings("unused") final StartGoToEvent event) {
        if (gameModelController.isHumanTurnReady()
                && selectedUnitManager.getSelectedUnit()
                        .map(gameModelController.getModel()::canStartGoTo)
                        .orElse(false)) {
            goToPicking = true;
            swithToMoveMode();
            if (modeController.isMoveMode()) {
                eventBus.post(new StatusBarMessageEvent(
                        i18n.get(Buttons.goToPickingHint),
                        StatusBarMessageEvent.Source.GAME));
            } else {
                goToPicking = false;
            }
        }
    }

    @Subscribe
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        Platform.runLater(() -> {
            if (gameModelController.isGameModelReady()
                    && gameModelController.getModel() == event.getModel()) {
                dialogColonyWasCaptured.showAndWait(event);
            }
        });
    }

    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        visibleArea.setMapSize(event.getModel().getMap().getMapSize());
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onCenterView(final CenterViewEvent event) {
        logger.debug("Center view event");
        /**
         * Here could be verification of race conditions like centering to
         * bottom right corner of map. Luckily it's done by JViewport.
         */
        visibleArea.setOnCanvasReady(str -> {
            if (selectedTileManager.getSelectedTile().isPresent()) {
                gamePanelView.planScrollingAnimationToLocation(
                        selectedTileManager.getSelectedTile().get());
            }
        });
    }

    void tryToOpenColonyDetail(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final Optional<Colony> oColony = gameModelController.getHumanPlayer()
                .getColoniesAt(currentLocation);
        if (oColony.isPresent()) {
            // show colony details
            eventBus.post(new ShowScreenEvent(Screen.COLONY, oColony.get()));
        }
    }

    private void swithToMoveMode() {
        Preconditions.checkArgument(
                selectedTileManager.getSelectedTile().isPresent(),
                "to move mode could be switched just when some tile is selected.");
        final List<Unit> units = gameModelController.getHumanPlayer()
                .getUnitsAt(selectedTileManager.getSelectedTile().get());
        Preconditions.checkState(!units.isEmpty(),
                "There is no moveable unit or other entity to interact with.");
        final Unit unit = selectedUnitManager.getSelectedUnit().get();
        if (gameModelController.getModel().getGoToDestination(unit)
                .isPresent()) {
            // An ordered unit is not moved by hand; cancel the order first.
            final Model model = gameModelController.getModel();
            eventBus.post(new StatusBarMessageEvent(i18n.get(
                    model.isGoToPaused(unit) ? Buttons.goToPausedExplanation
                            : Buttons.goToActiveExplanation,
                    model.getGoToDestinationName(unit).get()),
                    StatusBarMessageEvent.Source.GAME));
            return;
        }
        if (unit.getAction()
                .getType() == org.microcol.model.unit.UnitActionType.sentry) {
            eventBus.post(
                    new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                            i18n.get(
                                    org.microcol.gui.buttonpanel.Buttons.activateSentryExplanation),
                            org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME));
            return;
        }
        if (unit.getAction()
                .getType() == org.microcol.model.unit.UnitActionType.fortify) {
            eventBus.post(
                    new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                            i18n.get(gameModelController.getModel().isFortified(
                                    unit) ? org.microcol.gui.buttonpanel.Buttons.fortifiedExplanation
                                            : org.microcol.gui.buttonpanel.Buttons.fortifyingExplanation),
                            org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME));
            return;
        }
        oneTurnMoveHighlighter.setLocations(unit.getAvailableLocations());
        logger.debug("Switching '" + unit + "' to go mode.");
        moveModeController.setMoveModeOn();
    }

    @Subscribe
    private void onSelectedUnitWasChanged(
            @SuppressWarnings("unused") final SelectedUnitWasChangedEvent event) {
        goToPicking = false;
        if (modeController.isMoveMode()) {
            moveModeController.setMoveModeOff();
        }
    }

    /**
     * When move mode is enabled than it cancel it.
     */
    public void quitFromMoveMode() {
        if (modeController.isMoveMode()) {
            final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
            disableMoveMode(movingUnit);
        }
    }

    /**
     * This is called when user ends action mode.
     *
     * @param moveToLocation required target location
     */
    public void switchToNormalMode(final Location moveToLocation) {
        Preconditions.checkArgument(modeController.isMoveMode(),
                "switch to move mode was called from move mode");
        final Location moveFromLocation = selectedTileManager.getSelectedTile()
                .get();
        logger.debug("Switching to normal mode, from " + moveFromLocation
                + " to " + moveToLocation);
        final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
        if (goToPicking) {
            // A click on the unit's own square chooses nothing: Escape or
            // another square still ends the pick without changing the unit.
            if (!moveFromLocation.equals(moveToLocation)) {
                issueGoTo(movingUnit, moveToLocation);
                disableMoveMode(movingUnit);
            }
            return;
        }

        if (moveFromLocation.equals(moveToLocation)) {
            disableMoveMode(movingUnit);
            // it's a click? is there a colony?
            tryToOpenColonyDetail(moveToLocation);
            return;
        }

        // Beside a foreign colony the trade choice opens instead of a move or
        // attack; war keeps the existing attack path and never trades (R-0167).
        final Optional<Colony> foreignColony = gameModelController.getModel()
                .getColonyAt(moveToLocation).filter(colony -> colony
                        .getOwner() != gameModelController.getHumanPlayer());
        if (foreignColony.isPresent() && isForeignTradeOffered(movingUnit,
                foreignColony.get(), moveToLocation)) {
            disableMoveMode(movingUnit);
            showForeignColonyTrade(movingUnit, foreignColony.get());
            return;
        }
        // Entering a known adjacent native settlement opens its chooser first,
        // so no move, embark or attack reaches the settlement square (R-0157).
        final Optional<NativeSettlement> settlement = gameModelController
                .getModel().getNativeSettlementKnownTo(
                        gameModelController.getHumanPlayer(), moveToLocation);
        if (settlement.isPresent() && movingUnit.isAtPlaceLocation()
                && movingUnit.getLocation().isNeighbor(moveToLocation)) {
            disableMoveMode(movingUnit);
            showNativeVisit(movingUnit, settlement.get());
            return;
        }

        if (movingUnit.isPossibleToEmbarkAt(moveToLocation)) {
            // embark
            final Optional<UnitWithCargo> oEmbartAtUnit = movingUnit
                    .getFirstUnitToEmbarkAt(moveToLocation);
            final Optional<CargoSlot> oCargoSlot = oEmbartAtUnit.get()
                    .getCargo().getEmptyCargoSlot();
            if (oCargoSlot.isPresent()) {
                gameModelController.embark(oCargoSlot.get(), movingUnit);
            }
            disableMoveMode(movingUnit);
            return;
        }

        if (unitUtil.isPossibleToDisembarkAt(movingUnit, moveToLocation)) {
            // try to disembark
            disableMoveMode(movingUnit);
            final List<Unit> passengers = ((UnitWithCargo) movingUnit)
                    .getCargo().getSlots().stream()
                    .flatMap(slot -> slot.getUnit().stream())
                    .filter(passenger -> passenger
                            .canUnitDisembarkAt(moveToLocation))
                    .collect(Collectors.toList());
            // A sole eligible passenger lands directly; several are chosen
            // with check boxes (R-0379).
            final List<Unit> landing = passengers.size() > 1
                    ? choosePassenger.choose(passengers, moveToLocation)
                    : passengers;
            if (!landing.isEmpty()) {
                gameModelController.disembark(landing, moveToLocation);
            }
            return;
        }

        final Optional<AttackOrder> attack = movingUnit
                .prepareAttack(moveToLocation, true);
        if (attack.isPresent()) {
            disableMoveMode(movingUnit);
            fight(attack.get());
            return;
        }
        final UnitMove unitMove = new UnitMove(movingUnit, moveToLocation);
        if (!unitMove.isOneTurnMove()) {
            new DialogUnitCantMoveHere(viewUtil, i18n);
        } else if (unitMove.isOneTurnMove()) {
            // user will move
            if (!unitMove.getPath().isEmpty()) {
                gameModelController.performMove(movingUnit, unitMove.getPath());
            }
        } else if (movingUnit.isPossibleToGoToPort(moveToLocation)) {
            if (movingUnit.getPath(moveToLocation).isPresent()) {
                final List<Location> path = movingUnit.getPath(moveToLocation)
                        .get();
                if (!path.isEmpty()) {
                    gameModelController.performMove(movingUnit, path);
                }
                selectedTileManager.setSelectedTile(moveToLocation,
                        ScrollToFocusedTile.smoothScroll);
            } else {
                /*
                 * This is case when user try to move to place where it's not
                 * possible. Form example ship can't move at ground.
                 */
                return;
            }
        } else {
            logger.error("It's not possible to determine correct operation");
            disableMoveMode(movingUnit);
            new DialogUnitCantMoveHere(viewUtil, i18n);
        }
        disableMoveMode(movingUnit);
    }

    /**
     * Whether the adjacent foreign colony offers its trade choice (R-0167).
     * Open trade is always offered. A refusal the player can fix, such as the
     * missing founder or empty holds, is explained in the same window, but
     * never hides an attack the unit could make. Refused access and war keep
     * the existing movement and attack path.
     */
    private boolean isForeignTradeOffered(final Unit unit, final Colony colony,
            final Location target) {
        final ForeignColonyTradeProblem access = gameModelController.getModel()
                .getForeignColonyTradeAccess(unit, colony);
        if (access == ForeignColonyTradeProblem.NONE) {
            return true;
        }
        final boolean explainable = access == ForeignColonyTradeProblem.FOUNDER_ABSENT
                || access == ForeignColonyTradeProblem.EMPTY_CARGO
                || access == ForeignColonyTradeProblem.DAMAGED_CARRIER;
        return explainable && unit.prepareAttack(target, true).isEmpty();
    }

    /**
     * Show the trade window and refresh the right panel after a completed
     * trade, because the carrier's cargo changed without any movement.
     */
    private void showForeignColonyTrade(final Unit carrier,
            final Colony colony) {
        final DialogForeignColonyTrade dialog = new DialogForeignColonyTrade(
                viewUtil, i18n, gameModelController, carrier, colony);
        dialog.showAndWait();
        if (dialog.isTraded()) {
            eventBus.post(new RefreshRightPanelEvent());
        }
    }

    /**
     * Open the chooser of a visitor beside a known native settlement (R-0157),
     * with its land contact and market admission (R-0159). An introduction or
     * market entry chosen in the chooser runs only after that window has closed
     * and its button handler has returned; a new chooser then shows the new
     * state. No chooser is opened from inside another chooser's handler.
     */
    private void showNativeVisit(final Unit visitor,
            final NativeSettlement settlement) {
        DialogNativeVisit.FollowUp followUp;
        // Carried to the next chooser only after a backed-out confirmation.
        DialogNativeVisit.Selection kept = null;
        do {
            final List<NativeVisitChoice> choices = gameModelController
                    .getModel().getNativeVisitChoices(visitor, settlement);
            if (choices.isEmpty()) {
                return;
            }
            final NativeMarketView market = gameModelController.getModel()
                    .getNativeMarketView(visitor, settlement).orElseThrow();
            // The model checks this shown skill again on confirmation.
            final org.microcol.model.UnitType shownSkill = settlement
                    .getTeachableSkill();
            final DialogNativeVisit dialog = new DialogNativeVisit(viewUtil,
                    i18n, settlement, choices, visitor.getType(), market,
                    nativeSaleTerms(visitor, settlement), () -> {
                        if (gameModelController.startNativeTeaching(visitor,
                                settlement, shownSkill)) {
                            // The colonist left the map for the course.
                            selectedUnitManager.forgetRemovedUnit(visitor);
                        }
                    },
                    gameModelController.getModel()
                            .getNativeChiefPreview(visitor, settlement)
                            .orElse(null),
                    gameModelController.getModel()
                            .getNativeMissionPreview(visitor, settlement)
                            .orElse(null),
                    gameModelController.getModel()
                            .getNativeMissionContestPreview(visitor, settlement)
                            .orElse(null),
                    kept);
            dialog.showAndWait();
            followUp = dialog.getFollowUp();
            kept = null;
            // A rejected command is explained by the controller and changes
            // nothing; the next chooser then shows the unchanged state.
            if (followUp == DialogNativeVisit.FollowUp.INTRODUCE) {
                gameModelController.introduceNativeContact(visitor, settlement);
            } else if (followUp == DialogNativeVisit.FollowUp.OPEN_MARKET) {
                gameModelController.enterNativeMarket(visitor, settlement);
            } else if (followUp == DialogNativeVisit.FollowUp.HAGGLE) {
                gameModelController.haggleNativeSale(visitor, settlement,
                        dialog.getPendingSale());
            } else if (followUp == DialogNativeVisit.FollowUp.SELL
                    || followUp == DialogNativeVisit.FollowUp.GIFT) {
                if (confirmNativeSale(visitor, settlement, followUp,
                        dialog.getPendingSale())) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.PURCHASE_HAGGLE) {
                gameModelController.haggleNativePurchase(visitor, settlement,
                        dialog.getPendingPurchase());
            } else if (followUp == DialogNativeVisit.FollowUp.BUY) {
                if (confirmNativePurchase(visitor, settlement,
                        dialog.getPendingPurchase())) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.DECLINE_PURCHASE) {
                if (gameModelController.rejectNativePurchase(visitor,
                        settlement)) {
                    showNativeMessage(Loc.nativePurchaseDeclined);
                }
            } else if (followUp == DialogNativeVisit.FollowUp.SCOUT_CHIEF) {
                if (confirmNativeChief(visitor, settlement,
                        dialog.getPendingChief())) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.ESTABLISH_MISSION) {
                if (confirmNativeMission(visitor, settlement,
                        dialog.getPendingMission())) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.CONTEST_MISSION) {
                if (confirmNativeContest(visitor, settlement,
                        dialog.getPendingContest())) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.INCITE) {
                if (confirmNativeIncitement(visitor, settlement)) {
                    kept = dialog.getSelection();
                }
            } else if (followUp == DialogNativeVisit.FollowUp.ATTACK) {
                // A confirmed attack is not carried back to the chooser: the
                // chooser reopens on the actual state after the battle.
                confirmNativeAttack(visitor, settlement);
            } else if (followUp == DialogNativeVisit.FollowUp.TRIBUTE) {
                // A confirmed demand is not carried back either: the chooser
                // reopens on the actual state after the payment or refusal.
                confirmNativeTribute(visitor, settlement);
            }
        } while (followUp != DialogNativeVisit.FollowUp.NONE);
    }

    /**
     * Model reads for the sale controls of the chooser (R-0160). They only ask
     * the model, so nothing changes while the chooser is open.
     */
    private DialogNativeVisit.SaleTerms nativeSaleTerms(final Unit visitor,
            final NativeSettlement settlement) {
        return new DialogNativeVisit.SaleTerms() {

            @Override
            public int maxQuantity(final GoodsType goods, final boolean gift) {
                return gameModelController.getModel().getNativeMaxSaleQuantity(
                        visitor, settlement, goods, gift);
            }

            @Override
            public Optional<NativeSaleQuote> quote(final GoodsType goods,
                    final int quantity, final boolean gift) {
                return gameModelController.getModel().getNativeSaleQuote(
                        visitor, settlement, goods, quantity, gift);
            }

            @Override
            public int maxPurchaseQuantity(final GoodsType goods) {
                return gameModelController.getModel()
                        .getNativeMaxPurchaseQuantity(visitor, settlement,
                                goods);
            }

            @Override
            public Optional<NativePurchaseQuote> purchaseQuote(
                    final GoodsType goods, final int quantity) {
                return gameModelController.getModel().getNativePurchaseQuote(
                        visitor, settlement, goods, quantity);
            }

        };
    }

    /**
     * Ask for an explicit confirmation of the exact sale or gift, after the
     * chooser has closed. Only a confirmed choice reaches the model, which
     * re-checks it; a closed or cancelled confirmation changes nothing.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativeSale(final Unit visitor,
            final NativeSettlement settlement,
            final DialogNativeVisit.FollowUp followUp,
            final NativeSaleQuote offered) {
        if (offered == null) {
            return false;
        }
        final DialogNativeSale confirmation = new DialogNativeSale(viewUtil,
                i18n, offered);
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        if (followUp == DialogNativeVisit.FollowUp.GIFT) {
            gameModelController.giftToNative(visitor, settlement, offered);
        } else {
            gameModelController.sellToNative(visitor, settlement, offered);
        }
        return false;
    }

    /**
     * Ask for an explicit confirmation of the exact purchase, after the chooser
     * has closed (R-0161). Only a confirmed choice reaches the model, which
     * re-checks it; a closed or cancelled confirmation changes nothing. A
     * bought purchase is reported, and the chooser then reopens with the new
     * state.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativePurchase(final Unit visitor,
            final NativeSettlement settlement,
            final NativePurchaseQuote offered) {
        if (offered == null) {
            return false;
        }
        final DialogNativePurchase confirmation = new DialogNativePurchase(
                viewUtil, i18n, offered);
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        if (gameModelController.buyFromNative(visitor, settlement, offered)) {
            showNativeMessage(Loc.nativePurchaseBought, offered.getQuantity(),
                    i18n.get(GoodsTypeName
                            .getNameForGoodsType(offered.getGoods())),
                    offered.getGold());
        }
        return false;
    }

    /**
     * Ask for an explicit confirmation of the chief visit after the chooser has
     * closed (R-0158). The mood and the exact risk are shown there, before the
     * one draw. Only a confirmed visit reaches the model, which re-checks the
     * terms; a closed or cancelled confirmation changes nothing. The actual
     * result is then reported, and a killed scout leaves the selection.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativeChief(final Unit visitor,
            final NativeSettlement settlement, final NativeChiefPreview shown) {
        if (shown == null) {
            return false;
        }
        final DialogNativeChief confirmation = new DialogNativeChief(viewUtil,
                i18n, shown);
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        gameModelController.visitNativeChief(visitor, settlement, shown)
                .ifPresent(result -> {
                    if (result.getOutcome() == NativeChiefOutcome.DEATH) {
                        selectedUnitManager.forgetRemovedUnit(visitor);
                    }
                    showNativeChiefResult(result);
                });
        return false;
    }

    /**
     * Ask for an explicit confirmation of the mission after the chooser has
     * closed (R-0164). Only a confirmed mission reaches the model, which
     * re-checks the terms; a closed or cancelled confirmation changes nothing.
     * An established mission takes the missionary off the map, so it leaves the
     * selection.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativeMission(final Unit visitor,
            final NativeSettlement settlement, final NativeMissionView shown) {
        if (shown == null) {
            return false;
        }
        final DialogNativeMission confirmation = new DialogNativeMission(
                viewUtil, i18n, shown);
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        if (gameModelController.establishNativeMission(visitor, settlement,
                shown)) {
            selectedUnitManager.forgetRemovedUnit(visitor);
        }
        return false;
    }

    /**
     * Ask for an explicit confirmation of the rival-mission contest after the
     * chooser has closed (R-0165). Only a confirmed contest reaches the model,
     * which re-checks the terms and draws once; a closed or cancelled
     * confirmation changes nothing. Either outcome takes the visitor off the
     * map, so it leaves the selection.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativeContest(final Unit visitor,
            final NativeSettlement settlement,
            final NativeMissionContestView shown) {
        if (shown == null) {
            return false;
        }
        final DialogNativeContest confirmation = new DialogNativeContest(
                viewUtil, i18n, shown);
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        gameModelController.contestNativeMission(visitor, settlement, shown)
                .ifPresent(outcome -> {
                    selectedUnitManager.forgetRemovedUnit(visitor);
                    showNativeContestResult(outcome);
                });
        return false;
    }

    /**
     * Ask for the target and an explicit confirmation of the paid incitement
     * (R-0166), after the chooser has closed. The picker only reads the model's
     * terms and names; only a confirmed choice reaches the model, which
     * re-checks the terms, the purse and the order, then pays and draws once.
     * The actual result is then reported with the exact debit and the order.
     *
     * @return whether the player backed out of the confirmation, so the next
     *         chooser returns to the same offer
     */
    private boolean confirmNativeIncitement(final Unit visitor,
            final NativeSettlement settlement) {
        final Model model = gameModelController.getModel();
        final List<EuropeanNation> targets = model
                .getNativeIncitementTargets(visitor, settlement);
        if (targets.isEmpty()) {
            return false;
        }
        final DialogNativeIncitement confirmation = new DialogNativeIncitement(
                viewUtil, i18n, settlement.getNation(), targets,
                target -> model.getNativeIncitementQuote(visitor, settlement,
                        target),
                target -> model.getPlayerByName(target.getPlayerName())
                        .getDisplayName());
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return true;
        }
        final NativeIncitementQuote shown = confirmation.getShownQuote();
        gameModelController.incite(visitor, settlement, shown)
                .ifPresent(outcome -> showNativeIncitementResult(outcome,
                        visitor, settlement, shown));
        return false;
    }

    /**
     * Report the actual result of the incitement: the exact gold now held after
     * the payment, and on success the tribe's order read from the model.
     */
    private void showNativeIncitementResult(
            final NativeIncitementOutcome outcome, final Unit visitor,
            final NativeSettlement settlement,
            final NativeIncitementQuote shown) {
        final Model model = gameModelController.getModel();
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeIncitementTitle));
        final String target = model
                .getPlayerByName(shown.getTarget().getPlayerName())
                .getDisplayName();
        if (outcome == NativeIncitementOutcome.SUCCESS) {
            final int turns = model
                    .getNativeIncitementOrder(settlement.getNation())
                    .orElseThrow().getRemainingTurns();
            dialog.setText(
                    i18n.get(Loc.nativeIncitementSucceeded, shown.getPrice(),
                            visitor.getOwner().getGold(), target, turns));
        } else {
            dialog.setText(i18n.get(Loc.nativeIncitementFailed,
                    shown.getPrice(), visitor.getOwner().getGold()));
        }
        dialog.showAndWait();
    }

    /**
     * Show the attack terms read from the model and ask for an explicit
     * confirmation (R-0174). Back changes nothing. A confirmed attack reaches
     * the model, which re-checks the frozen terms before its first draw; its
     * actual result is then reported.
     */
    private void confirmNativeAttack(final Unit visitor,
            final NativeSettlement settlement) {
        final Model model = gameModelController.getModel();
        final Optional<NativeConquestQuote> terms = model
                .getNativeConquestQuote(visitor, settlement);
        if (terms.isEmpty()) {
            return;
        }
        final DialogNativeAttack confirmation = new DialogNativeAttack(viewUtil,
                i18n, terms.get());
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return;
        }
        gameModelController
                .attack(visitor, settlement, confirmation.getShownQuote())
                .ifPresent(result -> showNativeAttackResult(result, visitor,
                        settlement));
    }

    /**
     * Show the tribute terms read from the model and ask for an explicit
     * confirmation (R-0162). Back changes nothing. A confirmed demand reaches
     * the model, which re-checks the frozen terms before any change; its actual
     * result is then reported.
     */
    private void confirmNativeTribute(final Unit visitor,
            final NativeSettlement settlement) {
        final Optional<NativeTributeQuote> terms = gameModelController
                .getModel().getNativeTributeQuote(visitor, settlement);
        if (terms.isEmpty()) {
            return;
        }
        final DialogNativeTribute confirmation = new DialogNativeTribute(
                viewUtil, i18n, terms.get());
        confirmation.showAndWait();
        if (!confirmation.isConfirmed()) {
            return;
        }
        gameModelController
                .demandNativeTribute(visitor, settlement,
                        confirmation.getShownQuote())
                .ifPresent(this::showNativeTributeResult);
    }

    /**
     * Report the actual result of the demand: the gold moved, an empty treasury
     * that paid nothing, or the refusal, whose anger the relations already
     * show.
     */
    private void showNativeTributeResult(final NativeTributeResult result) {
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeTributeTitle));
        if (!result.isAccepted()) {
            dialog.setText(i18n.get(Loc.nativeTributeResultRefused));
        } else if (result.getGold() == 0) {
            dialog.setText(i18n.get(Loc.nativeTributeResultEmpty));
        } else {
            dialog.setText(
                    i18n.get(Loc.nativeTributeResultPaid, result.getGold()));
        }
        dialog.showAndWait();
    }

    /**
     * Report the actual outcome of the attack: the defenders overcome, whether
     * the settlement was conquered, a lost attacking unit and a trapped
     * colonist or missionary that died with the settlement.
     */
    private void showNativeAttackResult(final NativeConquestResult result,
            final Unit visitor, final NativeSettlement settlement) {
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeAttackTitle));
        final StringBuilder text = new StringBuilder(i18n.get(
                result.isConquered() ? Loc.nativeAttackConquered
                        : Loc.nativeAttackRepelled,
                result.getDefendersDefeated()));
        if (result.isConquered()) {
            // The one draw of the conquest, reported with the outcome (R-0175).
            text.append(' ').append(result.getTreasureValue()
                    .map(value -> i18n.get(Loc.nativeAttackTreasure, value))
                    .orElseGet(() -> i18n.get(Loc.nativeAttackNoTreasure)));
        }
        if (result.isAttackerLost()) {
            text.append(' ').append(i18n.get(Loc.nativeAttackUnitLost));
        }
        result.getPromotion().ifPresent(promotion -> {
            final String status = promotion.isVeteran()
                    ? i18n.get(
                            org.microcol.gui.dialog.Dialog.fight_veteranStatus)
                    : i18n.get(org.microcol.gui.UnitTypes
                            .getUnitName(promotion.getToType()));
            text.append(' ')
                    .append(i18n.get(
                            org.microcol.gui.dialog.Dialog.fight_promoted,
                            visitor.getOwner().getDisplayName(),
                            promotion.getUnitId(), status));
        });
        // Only the player's own pupil and missionary are named here; a rival's
        // loss reaches its owner through the saved turn report.
        for (final NativeConquestResult.Loss loss : result.getLosses()) {
            if (loss.getOwnerName().equals(visitor.getOwner().getName())) {
                text.append(' ')
                        .append(i18n.get(
                                loss.isPupil() ? Loc.nativeConquestLossPupil
                                        : Loc.nativeConquestLossMissionary,
                                i18n.get(org.microcol.gui.UnitTypes
                                        .getUnitName(loss.getType())),
                                i18n.get(StatusBarMessageService
                                        .nationLabel(settlement.getNation()))));
            }
        }
        dialog.setText(text.toString());
        dialog.showAndWait();
    }

    /**
     * Report the actual result of the contest: the rival missionary died and
     * the visitor holds the mission, or the visitor died and the rival mission
     * stays.
     */
    private void showNativeContestResult(
            final NativeMissionContestOutcome outcome) {
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeContestTitle));
        dialog.setText(i18n.get(outcome == NativeMissionContestOutcome.SUCCESS
                ? Loc.nativeContestWon
                : Loc.nativeContestLost));
        dialog.showAndWait();
    }

    /**
     * Report the actual result of the chief visit: the gold given, the death of
     * the scout, or the information, which a repeat visit gives again.
     */
    private void showNativeChiefResult(final NativeChiefResult result) {
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeChiefTitle));
        dialog.setText(chiefResultText(result));
        dialog.showAndWait();
    }

    private String chiefResultText(final NativeChiefResult result) {
        if (result.getOutcome() == NativeChiefOutcome.GOLD) {
            return i18n.get(Loc.nativeChiefGold, result.getGold());
        }
        if (result.getOutcome() == NativeChiefOutcome.DEATH) {
            return i18n.get(Loc.nativeChiefDeath);
        }
        final NativeChiefResult.Information information = result
                .getInformation().orElseThrow();
        final LocalizationHelper names = new LocalizationHelper(i18n);
        return i18n.get(
                result.isRepeat() ? Loc.nativeChiefRepeatInformation
                        : Loc.nativeChiefInformation,
                chiefList(information.getOfferedGoods().stream()
                        .map(goods -> i18n
                                .get(GoodsTypeName.getNameForGoodsType(goods)))
                        .collect(Collectors.toList())),
                chiefList(information.getWantedGoods().stream()
                        .map(goods -> i18n
                                .get(GoodsTypeName.getNameForGoodsType(goods)))
                        .collect(Collectors.toList())),
                names.getUnitName(information.getTeachableSkill()));
    }

    private String chiefList(final List<String> items) {
        return items.isEmpty() ? i18n.get(Loc.nativeChiefNone)
                : String.join(", ", items);
    }

    /**
     * Report the outcome of a native market command in a plain message.
     */
    private void showNativeMessage(final Loc message,
            final Object... arguments) {
        final DialogMessage dialog = new DialogMessage(viewUtil, i18n);
        dialog.setTitle(i18n.get(Loc.nativeMarketTitle));
        dialog.setText(i18n.get(message, arguments));
        dialog.showAndWait();
    }

    /**
     * Sends the unit to a map square, or to the friendly colony standing on it,
     * as chosen by the model. A destination that changed since it was offered
     * is rejected without any change.
     */
    private void issueGoTo(final Unit unit, final Location destination) {
        final Model model = gameModelController.getModel();
        final Optional<Colony> colony = model.getGoToColonies(unit).stream()
                .filter(candidate -> candidate.getLocation()
                        .equals(destination))
                .findFirst();
        gameModelController.performGoTo(unit, destination, colony.orElse(null));
    }

    public void disableMoveMode(final Unit movingUnit) {
        goToPicking = false;
        moveModeController.setMoveModeOff();
        eventBus.post(new EndMoveEvent(movingUnit));
    }

    private void fight(final AttackOrder order) {
        if ((!order.isDeclarationRequired() && !order.isPrivateerException()
                && !gamePreferences.isShowFightAdvisor())
                || gamePanelView.performFightDialog(order)) {
            gameModelController.performFight(order);
        }
    }

}
