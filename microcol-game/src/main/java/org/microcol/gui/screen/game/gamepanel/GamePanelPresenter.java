package org.microcol.gui.screen.game.gamepanel;

import javafx.application.Platform;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.microcol.gui.dialog.ChoosePassengerDialog;

import org.microcol.gui.dialog.DialogColonyWasCaptured;
import org.microcol.gui.dialog.DialogUnitCantMoveHere;
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
import org.microcol.model.Location;
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
