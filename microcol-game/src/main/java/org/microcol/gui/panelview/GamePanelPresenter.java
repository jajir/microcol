package org.microcol.gui.panelview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.DialogWarning;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.Point;
import org.microcol.gui.event.CenterViewController;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.model.DebugRequestController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.MoveUnitController;
import org.microcol.gui.event.model.NewGameController;
import org.microcol.gui.util.Localized;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.event.UnitMovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public final class GamePanelPresenter implements Localized {

	private final Logger logger = LoggerFactory.getLogger(GamePanelPresenter.class);

	public interface Display {

		GamePanelView getGamePanelView();

		Canvas getCanvas();

		void setCursorNormal();

		void setCursorGoto();

		void addMoveAnimator(List<Location> path, Unit movingUnit);

		AnimationManager getAnimationManager();

		void initGame(boolean idGridShown, Model model);

		void setGridShown(boolean isGridShown);

		Area getArea();

		void planScrollingAnimationToPoint(Point targetPoint);

		void stopTimer();

		VisualDebugInfo getVisualDebugInfo();

		void startMoveUnit(Unit ship);

		boolean performFightDialog(Unit unitAttacker, Unit unitDefender);

		VisibleArea getVisibleArea();

	}

	private final GameController gameController;

	private final FocusedTileController focusedTileController;

	private final GamePanelPresenter.Display display;

	private Point lastMousePosition;

	private final ViewState viewState;

	private final ViewUtil viewUtil;

	// static class PopUpDemo extends PopupMenu {
	//
	// /**
	// * Default serialVersionUID.
	// */
	// private static final long serialVersionUID = 1L;
	//
	// public PopUpDemo() {
	// for (int i = 0; i < 10; i++) {
	// JMenuItem anItem = new JMenuItem("Item " + i + " click me!");
	// add(anItem);
	// }
	// }
	// }

	@Inject
	public GamePanelPresenter(final GamePanelPresenter.Display display, final GameController gameController,
			final KeyController keyController, final FocusedTileController focusedTileController,
			final MoveUnitController moveUnitController, final NewGameController newGameController,
			final GamePreferences gamePreferences, final ShowGridController showGridController,
			final CenterViewController viewController, final ExitGameController exitGameController,
			final DebugRequestController debugRequestController, final ViewState viewState, final ViewUtil viewUtil) {
		this.focusedTileController = focusedTileController;
		this.gameController = Preconditions.checkNotNull(gameController);
		this.display = Preconditions.checkNotNull(display);
		this.viewState = Preconditions.checkNotNull(viewState);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);

		moveUnitController.addMoveUnitListener(event -> {
			scheduleWalkAnimation(event);
			/**
			 * Wait until animation is finished.
			 */
			while (display.getAnimationManager().hasNextStep()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					/**
					 * Exception is intentionally sink.
					 */
				}
			}
		});
		moveUnitController.addStartMovingListener(event -> swithToMoveMode());

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
			logger.debug("Pressed key: '" + e.getCode().getName() + "' has code '" + e.getCharacter() + "', modifiers '"
					+ e.getCode().isModifierKey() + "'");
		});

		display.getCanvas().setOnMousePressed(e -> {
			logger.debug("mouse pressed at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
			if (isMouseEnabled()) {
				if (e.isPopupTrigger()) {
					// TODO JJ pop up menu
					// doPop(e);
				}
				onMousePressed(e);
			}
		});
		display.getCanvas().setOnMouseReleased(e -> {
			if (isMouseEnabled()) {
				if (e.isPopupTrigger()) {
					// TODO JJ pop up menu
					// doPop(e);
				}
			}
		});
		display.getCanvas().setOnMouseMoved(e -> {
			if (isMouseEnabled()) {
				onMouseMoved(e);
			}
		});
		display.getCanvas().setOnMouseDragged(e -> {
			if (isMouseEnabled()) {
				logger.debug("mouse dragged at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
				onMouseDragged(e);
				lastMousePosition = Point.of(e.getX(), e.getY());
			}
		});

		// private void doPop(MouseEvent e) {
		// PopUpDemo menu = new PopUpDemo();
		// menu.show(e.getComponent(), e.getX(), e.getY());
		// }

		newGameController.addListener(event -> display.initGame(gamePreferences.isGridShown(), event.getModel()));
		showGridController.addListener(e -> display.setGridShown(e.isGridShown()));
		debugRequestController.addListener(e -> {
			display.getVisualDebugInfo().setLocations(e.getLocations());
		});

		// display.getGamePanelView().getParent().addComponentListener(new
		// GamePanelListener(display));
		viewController.addListener(event -> onCenterView());
		exitGameController.addListener(event -> display.stopTimer());

	}

	private boolean isMouseEnabled() {
		return gameController.getModel().getCurrentPlayer().isHuman();
	}

	private void onCenterView() {
		logger.debug("Center view event");
		/**
		 * Here could be verification of race conditions like centering to
		 * bottom right corner of map. Luckily it's done by JViewport.
		 */
		final Point p = display.getArea().getCenterAreaTo(Point.of(viewState.getSelectedTile().get()));
		display.planScrollingAnimationToPoint(p);
	}

	private void swithToMoveMode() {
		Preconditions.checkArgument(viewState.getSelectedTile().isPresent(),
				"to move mode could be switched just when some tile is selected.");
		final List<Unit> units = gameController.getModel().getCurrentPlayer()
				.getUnitsAt(viewState.getSelectedTile().get());
		// TODO JJ Filter unit that have enough action points
		Preconditions.checkState(!units.isEmpty(), "there are some moveable units");
		final Unit unit = units.get(0);
		display.startMoveUnit(unit);
		viewState.setMouseOverTile(Optional.ofNullable(lastMousePosition.toLocation()));
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
	}

	private void onKeyPressed_escape() {
		if (viewState.isMoveMode()) {
			cancelGoToMode();
		}
	}

	private void cancelGoToMode() {
		display.setCursorNormal();
		focusedTileController
				.fireEvent(new FocusedTileEvent(gameController.getModel(), viewState.getSelectedTile().get(),
						gameController.getModel().getMap().getTerrainAt(viewState.getSelectedTile().get())));
	}

	private void onKeyPressed_enter() {
		if (viewState.isMoveMode()) {
			switchToNormalMode(viewState.getMouseOverTile().get());
		}
	}

	private void onMousePressed(final MouseEvent e) {
		final Point pressedAt = Point.of(e.getX(), e.getY());
		final Location location = display.getArea().convertToLocation(pressedAt);
		if (gameController.getModel().getMap().isValid(location)) {
			logger.debug("location of mouse: " + location);
			if (viewState.isMoveMode()) {
				switchToNormalMode(location);
			} else {
				viewState.setSelectedTile(Optional.of(location));
				focusedTileController.fireEvent(new FocusedTileEvent(gameController.getModel(), location,
						gameController.getModel().getMap().getTerrainAt(location)));
			}
		} else {
			logger.debug("invalid mouse location: " + location);
		}
	}

	private void onMouseDragged(final MouseEvent e) {
		if (lastMousePosition != null) {
			// FIXME JJ it's just faked
			// final Bounds bounds = null;
			// final JViewport viewPort = (JViewport)
			// display.getGamePanelView().getParent();
			// if (bounds != null) {
			final Point currentPosition = Point.of(e.getX(), e.getY());
			final Point delta = lastMousePosition.substract(currentPosition);
			display.getVisibleArea().addDeltaToPoint(delta);
			// FIXME JJ please fix it
			// view.x += delta.getX();
			// view.y += delta.getY();
			// display.getGamePanelView().scrollToPoint(Point.of());
			// }
		}
	}

	private void onMouseMoved(final MouseEvent e) {
		lastMousePosition = Point.of(e.getX(), e.getY());
		viewState.setMouseOverTile(Optional.of(lastMousePosition.toLocation()));
	}

	private void switchToNormalMode(final Location moveToLocation) {
		final Location selectedTile = viewState.getSelectedTile().get();
		logger.debug("Switching to normal mode, from " + selectedTile + " to " + moveToLocation);
		if (selectedTile.equals(moveToLocation)) {
			return;
		}
		// TODO JJ active ship can be different from ship first at list
		final Unit movingUnit = gameController.getModel().getCurrentPlayer().getUnitsAt(selectedTile).get(0);
		if (isFight(movingUnit, moveToLocation)) {
			if (!movingUnit.getType().canAttack()) {
				// TODO JJ consider which tile should have focus
				viewState.setSelectedTile(Optional.of(moveToLocation));
				display.setCursorNormal();
				new DialogWarning(viewUtil);
				return;
			}
			final Unit targetUnit = gameController.getModel().getUnitsAt(moveToLocation).get(0);
			if (display.performFightDialog(movingUnit, targetUnit)) {
				// User choose to fight
				display.setCursorNormal();
				gameController.performFight(movingUnit, targetUnit);
			} else {
				// User choose to quit fight
				viewState.setSelectedTile(Optional.of(moveToLocation));
				display.setCursorNormal();
			}
		} else {
			// user will move
			if (movingUnit.getPath(moveToLocation).isPresent()) {
				final List<Location> path = movingUnit.getPath(moveToLocation).get();
				if (path.size() > 0) {
					gameController.performMove(movingUnit, path);
					focusedTileController.fireEvent(new FocusedTileEvent(gameController.getModel(), selectedTile,
							gameController.getModel().getMap().getTerrainAt(selectedTile)));
				}
				viewState.setSelectedTile(Optional.of(moveToLocation));
				display.setCursorNormal();
			}
		}
	}

	private boolean isFight(final Unit movingShip, final Location moveToLocation) {
		if (gameController.getModel().getUnitsAt(moveToLocation).isEmpty()) {
			return false;
		} else {
			final Unit targetUnit = gameController.getModel().getUnitsAt(moveToLocation).get(0);
			if (targetUnit.getOwner().equals(movingShip.getOwner())) {
				return false;
			} else {
				return true;
			}
		}
	}

	private void scheduleWalkAnimation(final UnitMovedEvent event) {
		Preconditions.checkArgument(event.getPath().getLocations().size() >= 1,
				"Path for moving doesn't contains enought steps to move.");
		display.planScrollingAnimationToPoint(display.getArea().getCenterAreaTo(Point.of(event.getStart())));
		List<Location> path = new ArrayList<>(event.getPath().getLocations());
		path.add(0, event.getStart());
		display.addMoveAnimator(path, event.getUnit());
	}

}
