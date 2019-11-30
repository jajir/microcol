package org.microcol.gui.screen.game.gamepanel;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.dialog.DialogColonyWasCaptured;
import org.microcol.gui.dialog.DialogUnitCantFightWarning;
import org.microcol.gui.dialog.DialogUnitCantMoveHere;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UnitUtil;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
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

    private final Logger logger = LoggerFactory.getLogger(GamePanelPresenter.class);

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

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    @Inject
    public GamePanelPresenter(final GamePanelPainter gamePanelPainter,
            final DialogColonyWasCaptured dialogColonyWasCaptured,
            final GameModelController gameModelController, final GamePreferences gamePreferences,
            final SelectedTileManager selectedTileManager, final ViewUtil viewUtil,
            final EventBus eventBus, final ModeController modeController,
            final SelectedUnitManager selectedUnitManager, final I18n i18n,
            final @Named("game") VisibleAreaService visibleArea,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter, final UnitUtil unitUtil,
            final MoveModeController moveModeController) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gamePreferences = gamePreferences;
        this.gamePanelView = Preconditions.checkNotNull(gamePanelPainter);
        this.dialogColonyWasCaptured = Preconditions.checkNotNull(dialogColonyWasCaptured);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        this.unitUtil = Preconditions.checkNotNull(unitUtil);
        this.moveModeController = Preconditions.checkNotNull(moveModeController);
    }

    @Subscribe
    private void onStartMove(@SuppressWarnings("unused") final StartMoveEvent event) {
        swithToMoveMode();
    }

    @Subscribe
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        dialogColonyWasCaptured.showAndWait(event);
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
        Preconditions.checkArgument(selectedTileManager.getSelectedTile().isPresent(),
                "to move mode could be switched just when some tile is selected.");
        final List<Unit> units = gameModelController.getHumanPlayer()
                .getUnitsAt(selectedTileManager.getSelectedTile().get());
        Preconditions.checkState(!units.isEmpty(),
                "There is no moveable unit or other entity to interact with.");
        final Unit unit = selectedUnitManager.getSelectedUnit().get();
        oneTurnMoveHighlighter.setLocations(unit.getAvailableLocations());
        logger.debug("Switching '" + unit + "' to go mode.");
        moveModeController.setMoveModeOn();
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
     * @param moveToLocation
     *            required target location
     */
    public void switchToNormalMode(final Location moveToLocation) {
        Preconditions.checkArgument(modeController.isMoveMode(),
                "switch to move mode was called from move mode");
        final Location moveFromLocation = selectedTileManager.getSelectedTile().get();
        logger.debug(
                "Switching to normal mode, from " + moveFromLocation + " to " + moveToLocation);
        final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();

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
            final Optional<CargoSlot> oCargoSlot = oEmbartAtUnit.get().getCargo()
                    .getEmptyCargoSlot();
            if (oCargoSlot.isPresent()) {
                gameModelController.embark(oCargoSlot.get(), movingUnit);
            }
            disableMoveMode(movingUnit);
            return;
        }

        if (unitUtil.isPossibleToDisembarkAt(movingUnit, moveToLocation)) {
            // try to disembark
            gameModelController.disembark((UnitWithCargo) movingUnit, moveToLocation);
            disableMoveMode(movingUnit);
            return;
        }

        final UnitMove unitMove = new UnitMove(movingUnit, moveToLocation);
        if (unitMove.requiredActionPoints() > movingUnit.getActionPoints()) {
            new DialogUnitCantMoveHere(viewUtil, i18n);
        } else if (movingUnit.isPossibleToCaptureColonyAt(moveToLocation)) {
            // use can capture target colony
            gameModelController.performMove(movingUnit, unitMove.getPath());
        } else if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
            // fight
            fight(movingUnit, moveToLocation);
            // } else if (movingUnit.isPossibleToEmbarkAt(moveToLocation)) {
        } else if (unitMove.isOneTurnMove()) {
            // user will move
            if (!unitMove.getPath().isEmpty()) {
                gameModelController.performMove(movingUnit, unitMove.getPath());
            }
        } else if (movingUnit.isPossibleToGoToPort(moveToLocation)) {
            if (movingUnit.getPath(moveToLocation).isPresent()) {
                final List<Location> path = movingUnit.getPath(moveToLocation).get();
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

    public void disableMoveMode(final Unit movingUnit) {
        moveModeController.setMoveModeOff();
        eventBus.post(new EndMoveEvent(movingUnit));
    }

    private void fight(final Unit movingUnit, final Location moveToLocation) {
        if (!movingUnit.getType().canAttack()) {
            selectedTileManager.setSelectedTile(moveToLocation, ScrollToFocusedTile.smoothScroll);
            disableMoveMode(movingUnit);
            new DialogUnitCantFightWarning(viewUtil, i18n);
            return;
        }
        final Unit targetUnit = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
        if (gamePreferences.isShowFightAdvisor()) {
            if (gamePanelView.performFightDialog(movingUnit, targetUnit)) {
                // User choose to fight
                disableMoveMode(movingUnit);
                gameModelController.performFight(movingUnit, targetUnit);
            } else {
                // User choose to quit fight
                selectedTileManager.setSelectedTile(moveToLocation,
                        ScrollToFocusedTile.smoothScroll);
                disableMoveMode(movingUnit);
            }
        } else {
            // implicit fight
            disableMoveMode(movingUnit);
            gameModelController.performFight(movingUnit, targetUnit);
        }
    }

}
