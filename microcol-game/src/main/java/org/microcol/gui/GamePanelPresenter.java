package org.microcol.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import org.apache.log4j.Logger;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.NewGameController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.ViewController;
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

		Location getCursorLocation();

		void setCursorLocation(Location cursorLocation);

		void setCursorNormal();

		void setCursorGoto();

		boolean isGotoMode();

		void setGotoCursorTitle(Location gotoCursorTitle);

		Location getGotoCursorTitle();

		void setWalkAnimator(WalkAnimator walkAnimator);

		WalkAnimator getWalkAnimator();

		void initGame(boolean idGridShown);

		void setGridShown(boolean isGridShown);

		Area getArea();
	}

	private final GameController gameController;

	private final FocusedTileController focusedTileController;

	private final PathPlanning pathPlanning;

	private final GamePanelPresenter.Display display;

	private final StatusBarMessageController statusBarMessageController;

	private Point lastMousePosition;

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
			final MoveUnitController moveUnitController, final NewGameController newGameController,
			final GamePreferences gamePreferences, final ShowGridController showGridController,
			final ViewController viewController) {
		this.focusedTileController = focusedTileController;
		this.gameController = Preconditions.checkNotNull(gameController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.display = Preconditions.checkNotNull(display);

		moveUnitController.addMoveUnitListener(event -> {
			scheduleWalkAnimation(event);
			// TODO JJ it's ugly, should use wait & notify
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

		keyController.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
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

		// TODO JJ move to separate class
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

		newGameController.addNewGameListener(event -> display.initGame(gamePreferences.isGridShown()));

		showGridController.addShowGridListener(e -> display.setGridShown(e.isGridShown()));

		display.getGamePanelView().addMouseListener(ma);
		display.getGamePanelView().addMouseMotionListener(ma);
		// TODO JJ move listener to separate class
		display.getGamePanelView().getParent().addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(final ComponentEvent e) {
				// TODO JJ Auto-generated method stub

			}

			@Override
			public void componentResized(final ComponentEvent e) {
				display.getGamePanelView().onViewPortResize();
			}

			@Override
			public void componentMoved(final ComponentEvent e) {
				// TODO JJ Auto-generated method stub

			}

			@Override
			public void componentHidden(final ComponentEvent e) {
				// TODO JJ Auto-generated method stub

			}
		});
		viewController.addCenterViewListener(() -> onCenterView());
	}

	private void onCenterView() {
		logger.debug("Center view event");
		Preconditions.checkNotNull(display.getCursorLocation(), "Cursor location is empty");
		// TODO add scrolling to center point, not top left corner
		final Point p = Point.of(display.getCursorLocation());
		final JViewport viewPort = (JViewport) display.getGamePanelView().getParent();
		final Rectangle view = viewPort.getViewRect();
		view.x = p.getX();
		view.y = p.getY();
		display.getGamePanelView().scrollRectToVisible(view);
	}

	private void swithToMoveMode() {
		Preconditions.checkNotNull(display.getCursorLocation(), "Cursor location is empty");
		final List<Ship> units = gameController.getGame().getCurrentPlayer().getShipsAt(display.getCursorLocation());
		// TODO JJ Filter unit that have enough action points
		Preconditions.checkState(!units.isEmpty(), "there are some moveable units");
		final Ship unit = units.get(0);
		display.setGotoCursorTitle(lastMousePosition.toLocation());
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
	}

	private void onKeyPressed_escape() {
		if (display.isGotoMode()) {
			cancelGoToMode(display.getGotoCursorTitle());
		}
	}

	private void onKeyPressed_enter() {
		if (display.isGotoMode()) {
			switchToNormalMode(display.getGotoCursorTitle());
		}
	}

	private void onMousePressed(final MouseEvent e) {
		final Location p = display.getArea().convertToLocation(Point.of(e.getX(), e.getY()));
		System.out.println("location p: " + p);
		if (display.isGotoMode()) {
			switchToNormalMode(p);
		} else {
			display.setCursorLocation(p);
			focusedTileController
					.fireFocusedTileEvent(new FocusedTileEvent(gameController.getGame(), p, new TileOcean()));
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
		if (display.isGotoMode()) {
			display.setGotoCursorTitle(lastMousePosition.toLocation());
		}
		/**
		 * Set status bar message
		 */
		final Location where = lastMousePosition.toLocation();
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

	private void cancelGoToMode(final Location moveTo) {
		display.setCursorNormal();
		display.setCursorLocation(moveTo);
		// TODO JJ read tile ocean from map
		focusedTileController
				.fireFocusedTileEvent(new FocusedTileEvent(gameController.getGame(), moveTo, new TileOcean()));
	}

	private final void switchToNormalMode(final Location moveTo) {
		logger.debug("Switching to normal mode, from " + display.getCursorLocation() + " to " + moveTo);
		final List<Location> path = new ArrayList<Location>();
		pathPlanning.paintPath(display.getCursorLocation(), moveTo, location -> path.add(location));
		if (path.size() > 0) {
			// TODO JJ active ship can be different from ship first at list
			Ship ship = gameController.getGame().getCurrentPlayer().getShipsAt(display.getCursorLocation()).get(0);
			gameController.performMove(ship, path);
			// TODO JJ tile ocean should be obtained from map
			focusedTileController.fireFocusedTileEvent(
					new FocusedTileEvent(gameController.getGame(), display.getCursorLocation(), new TileOcean()));
		}
		display.setCursorLocation(moveTo);
		display.setCursorNormal();
	}

	private void scheduleWalkAnimation(final ShipMovedEvent event) {
		Preconditions.checkArgument(event.getPath().getLocations().size() >= 1,
				"Path for moving doesn't contains enought steps to move.");
		List<Location> path = new ArrayList<>(event.getPath().getLocations());
		path.add(0, event.getStartLocation());
		final WalkAnimator walkAnimator = new WalkAnimator(pathPlanning, path, event.getShip());
		display.setWalkAnimator(walkAnimator);
	}

}
