package org.microcol.gui.gamepanel;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.DialogColonyWasCaptured;
import org.microcol.gui.DialogUnitCantFightWarning;
import org.microcol.gui.DialogUnitCantMoveHere;
import org.microcol.gui.Point;
import org.microcol.gui.colony.ColonyDialog;
import org.microcol.gui.event.EndMoveController;
import org.microcol.gui.event.EndMoveEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StartMoveController;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.CenterViewController;
import org.microcol.gui.mainmenu.CenterViewEvent;
import org.microcol.gui.mainmenu.QuitGameController;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.input.KeyCode;
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

    private final StartMoveController startMoveController;

    private final EndMoveController endMoveController;

    private final ColonyDialog colonyDialog;

    private final Text text;

    private final DialogColonyWasCaptured dialogColonyWasCaptured;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final VisibleArea visibleArea;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    @Inject
    public GamePanelPresenter(final GamePanelView gamePanelView,
            final DialogColonyWasCaptured dialogColonyWasCaptured,
            final GameModelController gameModelController, final KeyController keyController,
            final GamePreferences gamePreferences, final CenterViewController centerViewController,
            final QuitGameController quitGameController,
            final SelectedTileManager selectedTileManager, final ViewUtil viewUtil,
            final StartMoveController startMoveController,
            final EndMoveController endMoveController, final ColonyDialog colonyDialog,
            final MouseOverTileManager mouseOverTileManager, final ModeController modeController,
            final SelectedUnitManager selectedUnitManager, final Text text,
            final GamePanelController gamePanelController, final VisibleArea visibleArea,
            final PaneCanvas paneCanvas, final OneTurnMoveHighlighter oneTurnMoveHighlighter) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gamePreferences = gamePreferences;
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
        this.dialogColonyWasCaptured = Preconditions.checkNotNull(dialogColonyWasCaptured);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.startMoveController = Preconditions.checkNotNull(startMoveController);
        this.endMoveController = Preconditions.checkNotNull(endMoveController);
        this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
        this.text = Preconditions.checkNotNull(text);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);

        startMoveController.addListener(event -> swithToMoveMode());

        keyController.addListener(e -> {
            /**
             * Escape
             */
            if (KeyCode.ESCAPE == e.getCode()) {
                onKeyPressed_escape();
            }
            /**
             * Enter
             */
            if (KeyCode.ENTER == e.getCode()) {
                onKeyPressed_enter();
            }
            logger.debug("Pressed key: '" + e.getCode().getName() + "' has code '"
                    + e.getCharacter() + "', modifiers '" + e.getCode().isModifierKey() + "'");
        });

        paneCanvas.getCanvas().setOnMousePressed(e -> {
            if (gamePanelController.isMouseEnabled()) {
                onMousePressed(e);
            }
        });
        paneCanvas.getCanvas().setOnMouseReleased(e -> {
            if (gamePanelController.isMouseEnabled()) {
                onMouseReleased();
            }
        });
        paneCanvas.getCanvas().setOnMouseMoved(e -> {
            if (gamePanelController.isMouseEnabled()) {
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

        centerViewController.addListener(this::onCenterView);
        quitGameController.addListener(event -> gamePanelView.stopTimer());
    }

    @Subscribe
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        dialogColonyWasCaptured.showAndWait(event);
    }

    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        visibleArea.setMaxMapSize(event.getModel().getMap());
    }

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
        if (selectedUnitManager.isSelectedUnitMoveable()) {
            startMoveController.fireEvent(new StartMoveEvent());
            return true;
        }
        return false;
    }

    private void tryToOpenColonyDetail(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final Optional<Colony> oColony = gameModelController.getCurrentPlayer()
                .getColoniesAt(currentLocation);
        if (oColony.isPresent()) {
            // show colony details
            colonyDialog.showColony(oColony.get());
        }
    }

    private void swithToMoveMode() {
        Preconditions.checkArgument(selectedTileManager.getSelectedTile().isPresent(),
                "to move mode could be switched just when some tile is selected.");
        final List<Unit> units = gameModelController.getCurrentPlayer()
                .getUnitsAt(selectedTileManager.getSelectedTile().get());
        Preconditions.checkState(!units.isEmpty(), "there are some moveable units");
        final Unit unit = selectedUnitManager.getSelectedUnit().get();
        oneTurnMoveHighlighter.setLocations(unit.getAvailableLocations());
        logger.debug("Switching '" + unit + "' to go mode.");
        gamePanelView.setMoveModeOn();
    }

    private void onKeyPressed_escape() {
        if (modeController.isMoveMode()) {
            disableMoveMode();
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
            if (modeController.isMoveMode()) {
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
        if (moveFromLocation.equals(moveToLocation)) {
            disableMoveMode();
            // it's a click? is there a colony?
            tryToOpenColonyDetail(moveToLocation);
            return;
        }
        // TODO don't call selectedTileManager.setSelectedTile
        final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
        final UnitMove unitMove = new UnitMove(movingUnit, moveToLocation);
        if (movingUnit.isPossibleToCaptureColonyAt(moveToLocation)) {
            // use can capture target colony
            gameModelController.performMove(movingUnit, unitMove.getPath());
        } else if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
            // fight
            fight(movingUnit, moveToLocation);
        } else if (movingUnit.isPossibleToEmbarkAt(moveToLocation, true)) {
            // embark
            final Unit toLoad = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
            final Optional<CargoSlot> oCargoSlot = toLoad.getCargo().getEmptyCargoSlot();
            if (oCargoSlot.isPresent()) {
                gameModelController.embark(oCargoSlot.get(), movingUnit);
            }
        } else if (movingUnit.isPossibleToDisembarkAt(moveToLocation, true)) {
            // try to disembark
            gameModelController.disembark(movingUnit, moveToLocation);
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
            new DialogUnitCantMoveHere(viewUtil, text);
        }
        disableMoveMode();
    }

    private void disableMoveMode() {
        gamePanelView.setMoveModeOff();
        endMoveController.fireEvent(new EndMoveEvent());
    }

    private void fight(final Unit movingUnit, final Location moveToLocation) {
        if (!movingUnit.getType().canAttack()) {
            // TODO JJ consider which tile should have focus
            selectedTileManager.setSelectedTile(moveToLocation, ScrollToFocusedTile.smoothScroll);
            disableMoveMode();
            new DialogUnitCantFightWarning(viewUtil, text);
            return;
        }
        final Unit targetUnit = gameModelController.getModel().getUnitsAt(moveToLocation).get(0);
        if (gamePreferences.getShowFightAdvisorProperty().get()) {
            if (gamePanelView.performFightDialog(movingUnit, targetUnit)) {
                // User choose to fight
                disableMoveMode();
                gameModelController.performFight(movingUnit, targetUnit);
            } else {
                // User choose to quit fight
                selectedTileManager.setSelectedTile(moveToLocation,
                        ScrollToFocusedTile.smoothScroll);
                disableMoveMode();
            }
        } else {
            // implicit fight
            disableMoveMode();
            gameModelController.performFight(movingUnit, targetUnit);
        }
    }

}
