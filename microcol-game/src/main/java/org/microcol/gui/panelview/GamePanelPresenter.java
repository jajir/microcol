package org.microcol.gui.panelview;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import org.microcol.gui.GamePreferences;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Localized;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.event.CenterViewController;
import org.microcol.gui.event.DebugRequestController;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.GameController;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NewGameController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.model.Location;
import org.microcol.model.Ship;
import org.microcol.model.Terrain;
import org.microcol.model.event.ShipMovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public final class GamePanelPresenter implements Localized {

	private final Logger logger = LoggerFactory.getLogger(GamePanelPresenter.class);

	public interface Display {

		GamePanelView getGamePanelView();

		void setCursorNormal();

		void setCursorGoto();

		void setWalkAnimator(WalkAnimator walkAnimator);

		WalkAnimator getWalkAnimator();

		void initGame(boolean idGridShown);

		void setGridShown(boolean isGridShown);

		Area getArea();

		void planScrollingAnimationToPoint(Point targetPoint);

		void stopTimer();

		VisualDebugInfo getVisualDebugInfo();

		void startMoveUnit(Ship ship);

		ViewState getViewState();
	}

	private final GameController gameController;

	private final FocusedTileController focusedTileController;

	private final PathPlanning pathPlanning;

	private final GamePanelPresenter.Display display;

	private final StatusBarMessageController statusBarMessageController;

	private Point lastMousePosition;

	private final LocalizationHelper localizationHelper;

	static class PopUpDemo extends JPopupMenu {

		/**
		 * Default serialVersionUID.
		 */
		private static final long serialVersionUID = 1L;

		public PopUpDemo() {
			for (int i = 0; i < 10; i++) {
				JMenuItem anItem = new JMenuItem("Item " + i + " click me!");
				add(anItem);
			}
		}
	}

	@Inject
	public GamePanelPresenter(final GamePanelPresenter.Display display, final GameController gameController,
			final KeyController keyController, final StatusBarMessageController statusBarMessageController,
			final FocusedTileController focusedTileController, final PathPlanning pathPlanning,
			final MoveUnitController moveUnitController, final NewGameController newGameController,
			final GamePreferences gamePreferences, final ShowGridController showGridController,
			final CenterViewController viewController, final ExitGameController exitGameController,
			final LocalizationHelper localizationHelper, final DebugRequestController debugRequestController) {
		this.focusedTileController = focusedTileController;
		this.gameController = Preconditions.checkNotNull(gameController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.display = Preconditions.checkNotNull(display);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);

		moveUnitController.addMoveUnitListener(event -> {
			scheduleWalkAnimation(event);
			/**
			 * Wait until animation is finished.
			 */
			while (display.getWalkAnimator() != null && display.getWalkAnimator().isNextAnimationLocationAvailable()) {
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
			if (27 == e.getKeyCode()) {
				onKeyPressed_escape();
			}
			/**
			 * Enter
			 */
			if (10 == e.getKeyCode()) {
				onKeyPressed_enter();
			}
			logger.debug("Pressed key: '" + e.getKeyChar() + "' has code '" + e.getKeyCode() + "', modifiers '"
					+ e.getModifiers() + "'");
		});

		final MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mousePressed(final MouseEvent e) {
				logger.debug("mouse pressed at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
				if (isMouseEnabled()) {
					if (e.isPopupTrigger()) {
						doPop(e);
					}
					onMousePressed(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (isMouseEnabled()) {
					if (e.isPopupTrigger()) {
						doPop(e);
					}
				}
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (isMouseEnabled()) {
					onMouseMoved(e);
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (isMouseEnabled()) {
					logger.debug("mouse dragged at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
					onMouseDragged(e);
				}
			}

			private void doPop(MouseEvent e) {
				PopUpDemo menu = new PopUpDemo();
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		};

		newGameController.addListener(event -> display.initGame(gamePreferences.isGridShown()));

		showGridController.addListener(e -> display.setGridShown(e.isGridShown()));
		debugRequestController.addListener(e -> {
			display.getVisualDebugInfo().setLocations(e.getLocations());
		});

		display.getGamePanelView().addMouseListener(ma);
		display.getGamePanelView().addMouseMotionListener(ma);
		display.getGamePanelView().getParent().addComponentListener(new GamePanelListener(display));
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
		final Point p = display.getArea().getCenterAreaTo(Point.of(display.getViewState().getSelectedTile().get()));
		display.planScrollingAnimationToPoint(p);
	}

	private void swithToMoveMode() {
		final List<Ship> units = gameController.getModel().getCurrentPlayer()
				.getShipsAt(display.getViewState().getSelectedTile().get());
		// TODO JJ Filter unit that have enough action points
		Preconditions.checkState(!units.isEmpty(), "there are some moveable units");
		final Ship unit = units.get(0);
		display.startMoveUnit(unit);
		display.getViewState().setMouseOverTile(Optional.ofNullable(lastMousePosition.toLocation()));
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
	}

	private void onKeyPressed_escape() {
		if (display.getViewState().isMoveMode()) {
			cancelGoToMode();
		}
	}

	private void cancelGoToMode() {
		display.setCursorNormal();
		focusedTileController.fireEvent(new FocusedTileEvent(gameController.getModel(),
				display.getViewState().getSelectedTile().get(),
				gameController.getModel().getMap().getTerrainAt(display.getViewState().getSelectedTile().get())));
	}

	private void onKeyPressed_enter() {
		if (display.getViewState().isMoveMode()) {
			switchToNormalMode(display.getViewState().getMouseOverTile().get());
		}
	}

	private void onMousePressed(final MouseEvent e) {
		final Location location = display.getArea().convertToLocation(Point.of(e.getX(), e.getY()));
		if (gameController.getModel().getMap().isValid(location)) {
			logger.debug("location of mouse: " + location);
			if (display.getViewState().isMoveMode()) {
				switchToNormalMode(location);
			} else {
				display.getViewState().setSelectedTile(Optional.of(location));
				focusedTileController.fireEvent(new FocusedTileEvent(gameController.getModel(), location,
						gameController.getModel().getMap().getTerrainAt(location)));
			}
		} else {
			logger.debug("invalid mouse location: " + location);
		}
	}

	private void onMouseDragged(final MouseEvent e) {
		if (lastMousePosition != null) {
			final JViewport viewPort = (JViewport) display.getGamePanelView().getParent();
			if (viewPort != null) {
				final Point currentPosition = Point.of(e.getX(), e.getY());
				final Point delta = lastMousePosition.substract(currentPosition);
				final Rectangle view = viewPort.getViewRect();
				view.x += delta.getX();
				view.y += delta.getY();
				display.getGamePanelView().scrollRectToVisible(view);
			}
		}
	}

	private void onMouseMoved(final MouseEvent e) {
		lastMousePosition = Point.of(e.getX(), e.getY());
		if (display.getViewState().isMoveMode()) {
			display.getViewState().setMouseOverTile(Optional.of(lastMousePosition.toLocation()));
		}
		/**
		 * Set status bar message
		 */
		final Location location = lastMousePosition.toLocation();
		if (gameController.getModel().getMap().isValid(location)) {
			final Terrain terrain = gameController.getModel().getMap().getTerrainAt(location);
			setStatusMessageForTile(terrain, location);
		} else {
			statusBarMessageController.fireEvent(new StatusBarMessageEvent());
		}
	}

	/**
	 * When mouse is over tile method set correct status message.
	 *
	 * @param terrain
	 *            required terrain
	 * @param where
	 *            required location over which is now mouse
	 */
	private void setStatusMessageForTile(final Terrain terrain, final Location where) {
		final StringBuilder buff = new StringBuilder();
		buff.append(getText().get("statusBar.tile.start"));
		buff.append(" ");
		buff.append(localizationHelper.getTerrainName(terrain));
		buff.append(" ");
		buff.append(getText().get("statusBar.tile.withUnit"));
		buff.append(" ");
		gameController.getModel().getShipsAt(where).forEach(ship -> {
			buff.append(ship.getClass().getSimpleName());
			buff.append(" ");
		});
		statusBarMessageController.fireEvent(new StatusBarMessageEvent(buff.toString()));
	}

	private void switchToNormalMode(final Location moveTo) {
		final Location selectedTile = display.getViewState().getSelectedTile().get();
		logger.debug("Switching to normal mode, from " + selectedTile + " to " + moveTo);
		if (selectedTile.equals(moveTo)) {
			return;
		}
		// TODO JJ active ship can be different from ship first at list
		final Ship ship = gameController.getModel().getCurrentPlayer().getShipsAt(selectedTile).get(0);
		if (ship.getPath(moveTo).isPresent()) {
			final List<Location> path = ship.getPath(moveTo).get();
			if (path.size() > 0) {
				gameController.performMove(ship, path);
				focusedTileController.fireEvent(new FocusedTileEvent(gameController.getModel(), selectedTile,
						gameController.getModel().getMap().getTerrainAt(selectedTile)));
			}
			display.getViewState().setSelectedTile(Optional.of(moveTo));
			display.setCursorNormal();
		}
	}

	private void scheduleWalkAnimation(final ShipMovedEvent event) {
		Preconditions.checkArgument(event.getPath().getLocations().size() >= 1,
				"Path for moving doesn't contains enought steps to move.");
		display.planScrollingAnimationToPoint(display.getArea().getCenterAreaTo(Point.of(event.getStart())));
		List<Location> path = new ArrayList<>(event.getPath().getLocations());
		path.add(0, event.getStart());
		final WalkAnimator walkAnimator = new WalkAnimator(pathPlanning, path, event.getShip());
		display.setWalkAnimator(walkAnimator);
	}

}
