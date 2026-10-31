package org.microcol.gui.screen.game.gamepanel;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import com.google.inject.Provider;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import javafx.scene.input.ScrollEvent;

@Singleton
@Listener
public final class GamePanelMouseListener {

    private final static double DEFAULT_SCROLL_SPEED_DIVIDER = 2.25;

    private final Logger logger = LoggerFactory
            .getLogger(GamePanelMouseListener.class);

    private final GameModelController gameModelController;

    private final GamePanelPainter gamePanelView;

    private final SelectedTileManager selectedTileManager;

    private final EventBus eventBus;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final VisibleAreaService visibleArea;

    private final GamePanelController gamePanelController;

    private final GamePanelPresenter gamePanelPresenter;

    private final SelectedUnitManager selectedUnitManager;
    private final Provider<ButtonsGamePanel> gameButtons;
    private final Canvas canvas;
    private Point contextPress;
    private boolean contextDragged;

    private Optional<Point> lastMousePosition = Optional.empty();

    @Inject
    public GamePanelMouseListener(final GamePanelPainter gamePanelPainter,
            final GameModelController gameModelController,
            final SelectedTileManager selectedTileManager,
            final EventBus eventBus,
            final MouseOverTileManager mouseOverTileManager,
            final ModeController modeController,
            final GamePanelController gamePanelController,
            final @Named("game") VisibleAreaService visibleArea,
            final GamePanelComponent gamePanelComponent,
            final GamePanelPresenter gamePanelPresenter,
            final SelectedUnitManager selectedUnitManager,
            final Provider<ButtonsGamePanel> gameButtons) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.gamePanelView = Preconditions.checkNotNull(gamePanelPainter);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.mouseOverTileManager = Preconditions
                .checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.gamePanelController = Preconditions
                .checkNotNull(gamePanelController);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.selectedUnitManager = selectedUnitManager;
        this.gameButtons = gameButtons;
        canvas = gamePanelComponent.getCanvas();
        canvas.setFocusTraversable(true);
        canvas.setOnContextMenuRequested(event -> {
            // Mouse gestures finish on release so dragging cannot open a menu.
            if (event.isKeyboardTrigger()) {
                showSelectedUnitContext();
            }
            event.consume();
        });
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.CONTEXT_MENU
                    || event.getCode() == javafx.scene.input.KeyCode.F10
                            && event.isShiftDown()) {
                showSelectedUnitContext();
                event.consume();
            }
        });
        this.gamePanelPresenter = Preconditions
                .checkNotNull(gamePanelPresenter);

        gamePanelComponent.getCanvas().setOnMousePressed(e -> {
            if (gamePanelController.isMouseEnabled()
                    && !gamePanelController.isUnitMoving()) {
                onMousePressed(e);
            }
        });
        gamePanelComponent.getCanvas().setOnMouseReleased(e -> {
            if (gamePanelController.isMouseEnabled()
                    && !gamePanelController.isUnitMoving()) {
                onMouseReleased(e);
            }
        });
        gamePanelComponent.getCanvas().setOnMouseExited(e -> {
            mouseOverTileManager.clearMouseOverTile();
            eventBus.post(
                    new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                            org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME));
        });
        gamePanelComponent.getCanvas().setOnMouseEntered(
                e -> mouseOverTileManager.clearMouseOverTile());
        gamePanelComponent.getCanvas().setOnMouseMoved(e -> {
            if (gamePanelController.isMouseEnabled()
                    && !gamePanelController.isUnitMoving()) {
                onMouseMoved(e);
            }
            lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
        });
        gamePanelComponent.getCanvas().setOnMouseDragged(e -> {
            if (gamePanelController.isMouseEnabled()) {
                onMouseDragged(e);
                lastMousePosition = Optional.of(Point.of(e.getX(), e.getY()));
            }
        });
        gamePanelComponent.getCanvas().setOnScroll(this::onScroll);
    }

    private void onScroll(final ScrollEvent event) {
        visibleArea.addDeltaToTopLeftPoint(
                Point.of(-event.getDeltaX(), -event.getDeltaY())
                        .divide(DEFAULT_SCROLL_SPEED_DIVIDER));
    }

    private boolean tryToSwitchToMoveMode(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final List<Unit> availableUnits = gameModelController.getModel()
                .getMoveableUnitAtOwnedBy(currentLocation,
                        gameModelController.getHumanPlayer());
        if (availableUnits.isEmpty()) {
            return false;
        } else {
            eventBus.post(new StartMoveEvent());
            return true;
        }
    }

    private void onMousePressed(final MouseEvent e) {
        final Point pressedAt = Point.of(e.getX(), e.getY());
        lastMousePosition = Optional.of(pressedAt);
        canvas.requestFocus();
        gameButtons.get().hideUnitActions();
        contextPress = null;
        if (isContextClick(e)) {
            contextPress = pressedAt;
            contextDragged = false;
            if (modeController.isMoveMode()) {
                selectedUnitManager.getSelectedUnit()
                        .ifPresent(gamePanelPresenter::disableMoveMode);
            }
            return;
        }
        final Location location = gamePanelView.getArea()
                .convertToLocation(pressedAt);
        if (gameModelController.getModel().getMap().isValid(location)) {
            logger.debug("location of mouse: " + location);
            // Toolbar focus loss clears its hint even on the same map tile.
            mouseOverTileManager.clearMouseOverTile();
            mouseOverTileManager.setMouseOverTile(location);
            if (modeController.isMoveMode()) {
                gamePanelPresenter.switchToNormalMode(location);
            } else {
                if (e.isPrimaryButtonDown()) {
                    if (e.isAltDown()) {
                        selectedTileManager.setSelectedTile(location,
                                ScrollToFocusedTile.smoothScroll);
                    } else {
                        selectedTileManager.setSelectedTile(location,
                                ScrollToFocusedTile.no);
                    }
                    if (!tryToSwitchToMoveMode(location)) {
                        gamePanelPresenter.tryToOpenColonyDetail(location);
                    }
                }
            }
        } else {
            logger.debug("invalid mouse location: " + location);
        }
    }

    private boolean isContextClick(final MouseEvent event) {
        return event.getButton() == MouseButton.SECONDARY
                || event.getButton() == MouseButton.PRIMARY
                        && event.isControlDown()
                || event.isPopupTrigger();
    }

    private void onMouseReleased(final MouseEvent event) {
        if (contextPress != null || isContextClick(event)) {
            if (contextPress != null && !contextDragged) {
                showUnitContext(
                        gamePanelView.getArea().convertToLocation(contextPress),
                        event.getScreenX(), event.getScreenY());
            }
            contextPress = null;
            return;
        }
        if (modeController.isMoveMode() && lastMousePosition.isPresent()) {
            final Location loc = gamePanelView.getArea()
                    .convertToLocation(lastMousePosition.get());
            gamePanelPresenter.switchToNormalMode(loc);
        }

    }

    private void onMouseDragged(final MouseEvent e) {
        if (lastMousePosition.isPresent()) {
            if (contextPress != null) {
                final Point currentPosition = Point.of(e.getX(), e.getY());
                if (contextPress.distanceSimplified(currentPosition) > 5) {
                    contextDragged = true;
                }
                if (contextDragged) {
                    final Point delta = lastMousePosition.get()
                            .substract(currentPosition);
                    visibleArea.addDeltaToTopLeftPoint(delta);
                }
            }
            if (modeController.isMoveMode()
                    && !gamePanelController.isUnitMoving()) {
                final Point currentPosition = Point.of(e.getX(), e.getY());
                final Location loc = gamePanelView.getArea()
                        .convertToLocation(currentPosition);
                mouseOverTileManager.setMouseOverTile(loc);
            }
        }
    }

    private void showSelectedUnitContext() {
        if (!gamePanelController.isMouseEnabled()
                || gamePanelController.isUnitMoving()) {
            return;
        }
        selectedUnitManager.getSelectedUnit()
                .filter(unit -> unit.getOwner().isHuman()).ifPresent(unit -> {
                    if (modeController.isMoveMode()) {
                        gamePanelPresenter.disableMoveMode(unit);
                    }
                    final Point tile = gamePanelView.getArea()
                            .convertToCanvasPoint(unit.getLocation());
                    final javafx.geometry.Point2D point = canvas.localToScreen(
                            tile.getX()
                                    + org.microcol.gui.Tile.TILE_CENTER.getX(),
                            tile.getY()
                                    + org.microcol.gui.Tile.TILE_CENTER.getY());
                    showUnitContext(unit.getLocation(), point.getX(),
                            point.getY());
                });
    }

    private void showUnitContext(final Location location, final double screenX,
            final double screenY) {
        if (!gameModelController.getModel().getMap().isValid(location)) {
            return;
        }
        selectedTileManager.setSelectedTile(location, ScrollToFocusedTile.no);
        final List<Unit> units = gameModelController.getHumanPlayer()
                .getUnitsAt(location);
        if (units.isEmpty()) {
            return;
        }
        final Unit selected = selectedUnitManager.getSelectedUnit()
                .filter(units::contains).orElse(units.get(0));
        selectedUnitManager.setSelectedUnit(selected);
        // Selection refreshes button availability and the information panel
        // first.
        Platform.runLater(() -> {
            if (canvas.getScene() != null
                    && gamePanelController.isMouseEnabled()
                    && !gamePanelController.isUnitMoving()
                    && selectedUnitManager.getSelectedUnit()
                            .filter(selected::equals).isPresent()) {
                gameButtons.get().showUnitActions(canvas, screenX, screenY,
                        units, selected, unit -> {
                            selectedUnitManager.setSelectedUnit(unit);
                            showUnitContext(location, screenX, screenY);
                        });
            }
        });
    }

    private void onMouseMoved(final MouseEvent e) {
        final Point currentPosition = Point.of(e.getX(), e.getY());
        final Location loc = gamePanelView.getArea()
                .convertToLocation(currentPosition);
        mouseOverTileManager.setMouseOverTile(loc);
    }

}
