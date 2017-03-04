package org.microcol.gui;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NewGameController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.model.GameController;
import org.microcol.gui.model.TileOcean;
import org.microcol.model.Location;
import org.microcol.model.Ship;
import org.microcol.model.event.ShipMovedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GamePanelPresenter implements Localized {

	private final Logger logger = Logger.getLogger(GamePanelPresenter.class);

	public interface Display {

		GamePanelView getGamePanelView();

		Location getCursorTile();

		void setCursorTile(Location cursorTile);

		void setCursorNormal();

		void setCursorGoto();

		boolean isGotoMode();

		void setGotoCursorTitle(Location gotoCursorTitle);

		Location getGotoCursorTitle();

		void setWalkAnimator(WalkAnimator walkAnimator);

		WalkAnimator getWalkAnimator();

		void initGame();
	}

	private final GameController gameController;

	private final FocusedTileController focusedTileController;

	private final PathPlanning pathPlanning;

	private final GamePanelPresenter.Display display;

	private final StatusBarMessageController statusBarMessageController;

	private Location lastMousePosition;

	class PopUpDemo extends JPopupMenu {

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
			final MoveUnitController moveUnitController, final NewGameController newGameController) {
		this.focusedTileController = focusedTileController;
		this.gameController = Preconditions.checkNotNull(gameController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.display = Preconditions.checkNotNull(display);

		moveUnitController.addMoveUnitListener(event -> {
			scheduleWalkAnimation(event);
		});

		keyController.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if ('c' == e.getKeyChar()) {
				}
				if ('m' == e.getKeyChar()) {
					onKeyPressed_m();
				}
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
			}
		});

		final MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mousePressed(final MouseEvent e) {
				logger.debug("mouse pressed at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
				if (e.isPopupTrigger()) {
					doPop(e);
				}
				onMousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPop(e);
				}
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				onMouseMoved(e);
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				logger.debug("mouse dragged at " + e.getX() + ", " + e.getY() + ", " + e.getButton());
				onMouseDragged(e);
			}

			private void doPop(MouseEvent e) {
				PopUpDemo menu = new PopUpDemo();
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		};

		newGameController.addNewGameListener(event -> display.initGame());

		display.getGamePanelView().addMouseListener(ma);
		display.getGamePanelView().addMouseMotionListener(ma);
	}

	private void onKeyPressed_m() {
		if (display.getCursorTile() != null) {
			final List<Ship> units = gameController.getGame().getCurrentPlayer().getShipsAt(display.getCursorTile());
			if (units.isEmpty()) {
				logger.debug("At " + display.getCursorTile() + " there are no units to move.");
			} else {
				final Ship unit = units.get(0);
				display.setGotoCursorTitle(convertToTilesCoordinates(lastMousePosition));
				switchToGoMode(unit);
				display.getGamePanelView().repaint();
			}
		}
	}

	private void onKeyPressed_escape() {
		if (display.isGotoMode()) {
			cancelGoToMode(display.getGotoCursorTitle());
			display.getGamePanelView().repaint();
		}
	}

	private void onKeyPressed_enter() {
		if (display.isGotoMode()) {
			switchToNormalMode(display.getGotoCursorTitle());
			display.getGamePanelView().repaint();
		}
	}

	private void onMousePressed(final MouseEvent e) {
		final Location p = convertToTilesCoordinates(Location.of(e.getX(), e.getY()));
		if (display.isGotoMode()) {
			switchToNormalMode(p);
		} else {
			display.setCursorTile(p);
			focusedTileController.fireFocusedTileEvent(new FocusedTileEvent(p, new TileOcean()));
		}
		display.getGamePanelView().repaint();
	}

	private void onMouseDragged(final MouseEvent e) {
		if (lastMousePosition != null) {
			JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class,
					display.getGamePanelView());
			if (viewPort != null) {
				// Location delta =
				// lastMousePosition.substract(Location.make(e.getX(),
				// e.getY()));
				final int deltaX = lastMousePosition.getX() - e.getY();
				final int deltaY = lastMousePosition.getY() - e.getY();
				Rectangle view = viewPort.getViewRect();
				// view.x += delta.getX();
				// view.y += delta.getY();
				view.x += deltaX;
				view.y += deltaY;
				display.getGamePanelView().scrollRectToVisible(view);
			}
		}
	}

	private void onMouseMoved(final MouseEvent e) {
		lastMousePosition = Location.of(e.getX(), e.getY());
		if (display.isGotoMode()) {
			display.setGotoCursorTitle(convertToTilesCoordinates(Location.of(e.getX(), e.getY())));
			display.getGamePanelView().repaint();
		}
		/**
		 * Set status bar message
		 */
		Location where = convertToTilesCoordinates(Location.of(e.getX(), e.getY()));
		final TileOcean tile = new TileOcean();
		final StringBuilder buff = new StringBuilder();
		buff.append(getText().get("statusBar.tile.start"));
		buff.append(" ");
		buff.append(tile.getClass().getSimpleName());
		buff.append(" ");
		buff.append(getText().get("statusBar.tile.withUnit"));
		buff.append(" ");
		gameController.getGame().getShipsAt(where).forEach(ship -> {
			buff.append(ship.getClass().getSimpleName());
			buff.append(" ");
		});
		statusBarMessageController.fireStatusMessageWasChangedEvent(buff.toString());
	}

	private Location convertToTilesCoordinates(final Location panelCoordinates) {
		return Location.of(panelCoordinates.getX() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX,
				panelCoordinates.getY() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX);
	}

	private final void switchToGoMode(final Ship unit) {
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
		display.getGamePanelView().repaint();
	}

	private void cancelGoToMode(final Location moveTo) {
		display.setCursorNormal();
		display.setCursorTile(moveTo);
		// TODO JJ read tile ocean from map
		focusedTileController.fireFocusedTileEvent(new FocusedTileEvent(moveTo, new TileOcean()));
	}

	private final void switchToNormalMode(final Location moveTo) {
		logger.debug("Switching to normalmode.");

		final List<Location> path = new ArrayList<Location>();
		pathPlanning.paintPath(display.getCursorTile(), moveTo, point -> path.add(point));
		// TODO JJ - co to znamena? make first step
		if (path.size() > 0) {
			// TODO JJ active ship can be different from ship first at list
			Ship ship = gameController.getGame().getCurrentPlayer().getShipsAt(display.getCursorTile()).get(0);
			gameController.performMove(ship, path);
			focusedTileController.fireFocusedTileEvent(new FocusedTileEvent(display.getCursorTile(), new TileOcean()));
		}
		display.setCursorTile(moveTo);
		display.setCursorNormal();
	}

	private void scheduleWalkAnimation(final ShipMovedEvent event) {
		Preconditions.checkArgument(event.getPath().getLocations().size() >= 1,
				"Path for moving doesn't contains enought steps to move.");
		List<Location> path = new ArrayList<>(event.getPath().getLocations());
		path.add(0, event.getStartLocation());
		final WalkAnimator walkAnimator = new WalkAnimator(pathPlanning, path, event.getShip());
		display.setWalkAnimator(walkAnimator);
		new Timer(111, actionEvent -> {
			if (display.getWalkAnimator().isNextAnimationLocationAvailable()) {
				display.getWalkAnimator().countNextAnimationLocation();
			} else {
				((Timer) actionEvent.getSource()).stop();
				if (display.getCursorTile() != null
						&& display.getCursorTile().equals(display.getWalkAnimator().getTo())) {
					focusedTileController.fireFocusedTileEvent(
							new FocusedTileEvent(display.getWalkAnimator().getTo(), new TileOcean()));
				}
			}
			display.getGamePanelView().repaint();
		}).start();
	}

}
