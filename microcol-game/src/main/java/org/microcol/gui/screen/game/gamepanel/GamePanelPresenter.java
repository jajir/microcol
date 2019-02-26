package org.microcol.gui.screen.game.gamepanel;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.dialog.DialogColonyWasCaptured;
import org.microcol.gui.dialog.DialogUnitCantFightWarning;
import org.microcol.gui.dialog.DialogUnitCantMoveHere;
import org.microcol.gui.event.AboutGameEvent;
import org.microcol.gui.event.BuildColonyEvent;
import org.microcol.gui.event.CenterViewEvent;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.PlowFieldEvent;
import org.microcol.gui.event.QuitGameEvent;
import org.microcol.gui.event.SelectNextUnitEvent;
import org.microcol.gui.event.ShowGoalsEvent;
import org.microcol.gui.event.ShowStatisticsEvent;
import org.microcol.gui.event.ShowTurnReportEvent;
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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

@Listener
public final class GamePanelPresenter {

    private final Logger logger = LoggerFactory.getLogger(GamePanelPresenter.class);

    private final GamePreferences gamePreferences;

    private final GameModelController gameModelController;

    private final GamePanelView gamePanelView;

    private Optional<Point> lastMousePosition = Optional.empty();

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final ViewUtil viewUtil;

    private final EventBus eventBus;

    private final I18n i18n;

    private final DialogColonyWasCaptured dialogColonyWasCaptured;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final VisibleArea visibleArea;

    private final GamePanelController gamePanelController;

    private final UnitUtil unitUtil;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    @Inject
    public GamePanelPresenter(final GamePanelView gamePanelView,
            final DialogColonyWasCaptured dialogColonyWasCaptured,
            final GameModelController gameModelController, final GamePreferences gamePreferences,
            final SelectedTileManager selectedTileManager, final ViewUtil viewUtil,
            final EventBus eventBus, final MouseOverTileManager mouseOverTileManager,
            final ModeController modeController, final SelectedUnitManager selectedUnitManager,
            final I18n i18n, final GamePanelController gamePanelController,
            final VisibleArea visibleArea, final PaneCanvas paneCanvas,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter, final UnitUtil unitUtil) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gamePreferences = gamePreferences;
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
        this.dialogColonyWasCaptured = Preconditions.checkNotNull(dialogColonyWasCaptured);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.gamePanelController = Preconditions.checkNotNull(gamePanelController);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        this.unitUtil = Preconditions.checkNotNull(unitUtil);

        paneCanvas.getCanvas().setOnMousePressed(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
                onMousePressed(e);
            }
        });
        paneCanvas.getCanvas().setOnMouseReleased(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
                onMouseReleased();
            }
        });
        paneCanvas.getCanvas().setOnMouseMoved(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
                onMouseMoved(e);
            }
            lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
        });
        paneCanvas.getCanvas().setOnMouseDragged(e -> {
            if (gamePanelController.isMouseEnabled()) {
                onMouseDragged(e);
                lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
            }
        });
    }

    @Subscribe
    private void onStartMove(@SuppressWarnings("unused") final StartMoveEvent event) {
        swithToMoveMode();
    }

    @Subscribe
    private void onKeyEvent(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            onKeyPressed_escape();
        }
        /**
         * Enter
         */
        if (KeyCode.ENTER == event.getCode()) {
            onKeyPressed_enter();
        }

        if (KeyCode.R == event.getCode()) {
            eventBus.post(new ShowStatisticsEvent());
        }
        if (KeyCode.C == event.getCode()) {
            eventBus.post(new CenterViewEvent());
        }
        if (KeyCode.T == event.getCode()) {
            eventBus.post(new ShowTurnReportEvent());
        }
        if (KeyCode.G == event.getCode()) {
            eventBus.post(new ShowGoalsEvent());
        }
        if (KeyCode.H == event.getCode()) {
            eventBus.post(new AboutGameEvent());
        }
        if (KeyCode.E == event.getCode()) {
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
        if (KeyCode.M == event.getCode()) {
            eventBus.post(new StartMoveEvent());
        }
        if (KeyCode.P == event.getCode()) {
            eventBus.post(new PlowFieldEvent());
        }
        if (KeyCode.B == event.getCode()) {
            eventBus.post(new BuildColonyEvent());
        }

        if (KeyCode.TAB == event.getCode()) {
            eventBus.post(new SelectNextUnitEvent());
        }
        logger.debug("Pressed key: '" + event.getCode().getName() + "' has code '"
                + event.getCharacter() + "', modifiers '" + event.getCode().isModifierKey() + "'");
    }

    @Subscribe
    private void onQuitGame(@SuppressWarnings("unused") final QuitGameEvent event) {
        gamePanelView.stopTimer();
    }

    @Subscribe
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        dialogColonyWasCaptured.showAndWait(event);
    }

    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        visibleArea.setMaxMapSize(event.getModel().getMap());
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

    private boolean tryToSwitchToMoveMode(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final List<Unit> availableUnits = gameModelController.getModel()
                .getMoveableUnitAtOwnedBy(currentLocation, gameModelController.getCurrentPlayer());
        if (availableUnits.isEmpty()) {
            return false;
        } else {
            eventBus.post(new StartMoveEvent());
            return true;
        }
    }
    
    private void tryToOpenColonyDetail(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final Optional<Colony> oColony = gameModelController.getCurrentPlayer()
                .getColoniesAt(currentLocation);
        if (oColony.isPresent()) {
            // show colony details
            eventBus.post(new ShowScreenEvent(Screen.COLONY, oColony.get()));
        }
    }

    private void swithToMoveMode() {
        Preconditions.checkArgument(selectedTileManager.getSelectedTile().isPresent(),
                "to move mode could be switched just when some tile is selected.");
        final List<Unit> units = gameModelController.getCurrentPlayer()
                .getUnitsAt(selectedTileManager.getSelectedTile().get());
        Preconditions.checkState(!units.isEmpty(),
                "There is no moveable unit or other entity to interact with.");
        final Unit unit = selectedUnitManager.getSelectedUnit().get();
        oneTurnMoveHighlighter.setLocations(unit.getAvailableLocations());
        logger.debug("Switching '" + unit + "' to go mode.");
        gamePanelView.setMoveModeOn();
    }

    private void onKeyPressed_escape() {
        if (modeController.isMoveMode()) {
            Preconditions.checkArgument(modeController.isMoveMode(),
                    "switch to move mode was called from move mode");
            final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
            disableMoveMode(movingUnit);
        }
    }

    private void onKeyPressed_enter() {
        if (modeController.isMoveMode()) {
            switchToNormalMode(mouseOverTileManager.getMouseOverTile().get());
        }
    }

    private void onMousePressed(final MouseEvent e) {
        final Point pressedAt = Point.of(e.getX(), e.getY());
        final Location location = gamePanelView.getArea().convertToLocation(pressedAt);
        if (gameModelController.getModel().getMap().isValid(location)) {
            logger.debug("location of mouse: " + location);
            if (modeController.isMoveMode()) {
                switchToNormalMode(location);
            } else {
                if (e.isPrimaryButtonDown()) {
                    if (e.isControlDown() || e.isAltDown()) {
                        selectedTileManager.setSelectedTile(location,
                                ScrollToFocusedTile.smoothScroll);
                    } else {
                        selectedTileManager.setSelectedTile(location, ScrollToFocusedTile.no);
                    }
                    if (!tryToSwitchToMoveMode(location)) {
                        tryToOpenColonyDetail(location);
                    }
                }
            }
        } else {
            logger.debug("invalid mouse location: " + location);
        }
    }

    private void onMouseReleased() {
        if (modeController.isMoveMode() && lastMousePosition.isPresent()) {
            final Location loc = gamePanelView.getArea().convertToLocation(lastMousePosition.get());
            switchToNormalMode(loc);
        }

    }

    private void onMouseDragged(final MouseEvent e) {
        if (lastMousePosition.isPresent()) {
            if (e.isSecondaryButtonDown()) {
                final Point currentPosition = Point.of(e.getX(), e.getY());
                final Point delta = lastMousePosition.get().substract(currentPosition);
                visibleArea.addDeltaToTopLeftPoint(delta);
            }
            if (modeController.isMoveMode() && !gamePanelController.isUnitMoving()) {
                final Point currentPosition = Point.of(e.getX(), e.getY());
                final Location loc = gamePanelView.getArea().convertToLocation(currentPosition);
                mouseOverTileManager.setMouseOverTile(loc);
            }
        }
    }

    private void onMouseMoved(final MouseEvent e) {
        final Point currentPosition = Point.of(e.getX(), e.getY());
        final Location loc = gamePanelView.getArea().convertToLocation(currentPosition);
        mouseOverTileManager.setMouseOverTile(loc);
    }

    /**
     * This is called when user ends action mode.
     * 
     * @param moveToLocation
     *            required target location
     */
    private void switchToNormalMode(final Location moveToLocation) {
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
        // TODO don't call selectedTileManager.setSelectedTile
        final UnitMove unitMove = new UnitMove(movingUnit, moveToLocation);
        if (movingUnit.isPossibleToCaptureColonyAt(moveToLocation)) {
            // use can capture target colony
            gameModelController.performMove(movingUnit, unitMove.getPath());
        } else if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
            // fight
            fight(movingUnit, moveToLocation);
        } else if (movingUnit.isPossibleToEmbarkAt(moveToLocation)) {
            // embark
            final Optional<UnitWithCargo> oEmbartAtUnit = movingUnit
                    .getFirstUnitToEmbarkAt(moveToLocation);
            final Optional<CargoSlot> oCargoSlot = oEmbartAtUnit.get().getCargo()
                    .getEmptyCargoSlot();
            if (oCargoSlot.isPresent()) {
                gameModelController.embark(oCargoSlot.get(), movingUnit);
            }
        } else if (unitUtil.isPossibleToDisembarkAt(movingUnit, moveToLocation)) {
            // try to disembark
            gameModelController.disembark((UnitWithCargo) movingUnit, moveToLocation);
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
            new DialogUnitCantMoveHere(viewUtil, i18n);
        }
        disableMoveMode(movingUnit);
    }

    private void disableMoveMode(final Unit movingUnit) {
        gamePanelView.setMoveModeOff();
        eventBus.post(new EndMoveEvent(movingUnit));
    }

    private void fight(final Unit movingUnit, final Location moveToLocation) {
        if (!movingUnit.getType().canAttack()) {
            // TODO JJ consider which tile should have focus
            selectedTileManager.setSelectedTile(moveToLocation, ScrollToFocusedTile.smoothScroll);
            disableMoveMode(movingUnit);
            new DialogUnitCantFightWarning(viewUtil, i18n);
            return;
        }
        final Unit targetUnit = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
        if (gamePreferences.getShowFightAdvisorProperty().get()) {
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
