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
import javafx.scene.input.ScrollEvent;

@Singleton
@Listener
public final class GamePanelMouseListener {

    private final static double DEFAULT_SCROLL_SPEED_DIVIDER = 2.25;

    private final Logger logger = LoggerFactory.getLogger(GamePanelMouseListener.class);

    private final GameModelController gameModelController;

    private final GamePanelPainter gamePanelView;

    private final SelectedTileManager selectedTileManager;

    private final EventBus eventBus;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final VisibleAreaService visibleArea;

    private final GamePanelController gamePanelController;

    private final GamePanelPresenter gamePanelPresenter;

    private Optional<Point> lastMousePosition = Optional.empty();

    @Inject
    public GamePanelMouseListener(final GamePanelPainter gamePanelPainter,
            final GameModelController gameModelController,
            final SelectedTileManager selectedTileManager, final EventBus eventBus,
            final MouseOverTileManager mouseOverTileManager, final ModeController modeController,
            final GamePanelController gamePanelController,
            final @Named("game") VisibleAreaService visibleArea,
            final GamePanelComponent gamePanelComponent,
            final GamePanelPresenter gamePanelPresenter) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.gamePanelView = Preconditions.checkNotNull(gamePanelPainter);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.gamePanelController = Preconditions.checkNotNull(gamePanelController);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.gamePanelPresenter = Preconditions.checkNotNull(gamePanelPresenter);

        gamePanelComponent.getCanvas().setOnMousePressed(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
                onMousePressed(e);
            }
        });
        gamePanelComponent.getCanvas().setOnMouseReleased(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
                onMouseReleased();
            }
        });
        gamePanelComponent.getCanvas().setOnMouseMoved(e -> {
            if (gamePanelController.isMouseEnabled() && !gamePanelController.isUnitMoving()) {
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
        visibleArea.addDeltaToTopLeftPoint(Point.of(-event.getDeltaX(), -event.getDeltaY())
                .divide(DEFAULT_SCROLL_SPEED_DIVIDER));
    }

    private boolean tryToSwitchToMoveMode(final Location currentLocation) {
        Preconditions.checkNotNull(currentLocation);
        final List<Unit> availableUnits = gameModelController.getModel()
                .getMoveableUnitAtOwnedBy(currentLocation, gameModelController.getHumanPlayer());
        if (availableUnits.isEmpty()) {
            return false;
        } else {
            eventBus.post(new StartMoveEvent());
            return true;
        }
    }

    private void onMousePressed(final MouseEvent e) {
        final Point pressedAt = Point.of(e.getX(), e.getY());
        final Location location = gamePanelView.getArea().convertToLocation(pressedAt);
        if (gameModelController.getModel().getMap().isValid(location)) {
            logger.debug("location of mouse: " + location);
            if (modeController.isMoveMode()) {
                gamePanelPresenter.switchToNormalMode(location);
            } else {
                if (e.isPrimaryButtonDown()) {
                    if (e.isControlDown() || e.isAltDown()) {
                        selectedTileManager.setSelectedTile(location,
                                ScrollToFocusedTile.smoothScroll);
                    } else {
                        selectedTileManager.setSelectedTile(location, ScrollToFocusedTile.no);
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

    private void onMouseReleased() {
        if (modeController.isMoveMode() && lastMousePosition.isPresent()) {
            final Location loc = gamePanelView.getArea().convertToLocation(lastMousePosition.get());
            gamePanelPresenter.switchToNormalMode(loc);
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

}
