package org.microcol.gui.screen.game.gamepanel;

import javafx.application.Platform;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.microcol.gui.dialog.ChoosePassengerDialog;

import org.microcol.gui.Loc;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.dialog.DialogColonyWasCaptured;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.DialogUnitCantMoveHere;
import org.microcol.gui.screen.nativesettlements.DialogNativePurchase;
import org.microcol.gui.screen.nativesettlements.DialogNativeSale;
import org.microcol.gui.screen.nativesettlements.DialogNativeVisit;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.event.EndMoveEvent;
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
import org.microcol.model.Model;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.NativeMarketView;
import org.microcol.model.NativePurchaseQuote;
import org.microcol.model.NativeSaleQuote;
import org.microcol.model.NativeSettlement;
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
            choosePassenger.choose(passengers, moveToLocation)
                    .ifPresent(passenger -> gameModelController
                            .disembark(passenger, moveToLocation));
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
                    }, kept);
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
