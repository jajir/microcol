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
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.MoveUnitController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.model.GameController;
import org.microcol.gui.model.Ship;
import org.microcol.gui.model.Tile;
import org.microcol.gui.model.Unit;
import org.microcol.model.Location;

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

		List<Location> getFloatingParts();

		void setGotoCursorTitle(Location gotoCursorTitle);

		Location getGotoCursorTitle();
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
			final MoveUnitController moveUnitController) {
		this.focusedTileController = focusedTileController;
		this.gameController = Preconditions.checkNotNull(gameController);
		this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.display = Preconditions.checkNotNull(display);

		moveUnitController.addMoveUnitListener(path -> {
			scheduleWalkAnimation(path);
		});

		keyController.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if ('c' == e.getKeyChar()) {
				}
				if ('g' == e.getKeyChar()) {
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

		MouseAdapter ma = new MouseAdapter() {

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
		display.getGamePanelView().addMouseListener(ma);
		display.getGamePanelView().addMouseMotionListener(ma);
	}

	private void onKeyPressed_m() {
		if (display.getCursorTile() != null) {
			final Tile tile = gameController.getWorld().getAt(display.getCursorTile());
			final Unit unit = tile.getFirstMovableUnit();
			if (unit != null) {
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
		final Location p = convertToTilesCoordinates(Location.make(e.getX(), e.getY()));
		if (display.isGotoMode()) {
			switchToNormalMode(p);
		} else {
			display.setCursorTile(p);
			focusedTileController.fireFocusedTileEvent(gameController.getWorld().getAt(p));
		}
		display.getGamePanelView().repaint();
	}

	private void onMouseDragged(final MouseEvent e) {
		if (lastMousePosition != null) {
			JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class,
					display.getGamePanelView());
			if (viewPort != null) {
				Location delta = lastMousePosition.substract(Location.make(e.getX(), e.getY()));
				Rectangle view = viewPort.getViewRect();
				view.x += delta.getX();
				view.y += delta.getY();
				display.getGamePanelView().scrollRectToVisible(view);
			}
		}
	}

	private void onMouseMoved(final MouseEvent e) {
		lastMousePosition = Location.make(e.getX(), e.getY());
		if (display.isGotoMode()) {
			display.setGotoCursorTitle(convertToTilesCoordinates(Location.make(e.getX(), e.getY())));
			display.getGamePanelView().repaint();
		}
		/**
		 * Set status bar message
		 */
		Location where = convertToTilesCoordinates(Location.make(e.getX(), e.getY()));
		final Tile tile = gameController.getWorld().getAt(where);
		final StringBuilder buff = new StringBuilder();
		buff.append(getText().get("statusBar.tile.start"));
		buff.append(" ");
		buff.append(tile.getName());
		if (!tile.getUnits().isEmpty()) {
			buff.append(" ");
			buff.append(getText().get("statusBar.tile.withUnit"));
			tile.getUnits().forEach(unit -> {
				buff.append("Ship");
			});

		}
		statusBarMessageController.fireStatusMessageWasChangedEvent(buff.toString());
	}

	private Location convertToTilesCoordinates(final Location panelCoordinates) {
		return Location.make(panelCoordinates.getX() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX,
				panelCoordinates.getY() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX);
	}

	private final void switchToGoMode(final Unit unit) {
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
		display.getGamePanelView().repaint();
	}

	private void cancelGoToMode(final Location moveTo) {
		display.setCursorNormal();
		display.setCursorTile(moveTo);
		focusedTileController.fireFocusedTileEvent(gameController.getWorld().getAt(display.getCursorTile()));
	}

	private final void switchToNormalMode(final Location moveTo) {
		logger.debug("Switching to normalmode.");

		final List<Location> path = new ArrayList<Location>();
		pathPlanning.paintPath(display.getCursorTile(), moveTo, point -> path.add(point));
		// make first step
		if (!path.isEmpty()) {
			Ship ship = (Ship) gameController.getWorld().getAt(display.getCursorTile()).getFirstMovableUnit();
			gameController.performMove(ship, path);
			focusedTileController.fireFocusedTileEvent(gameController.getWorld().getAt(display.getCursorTile()));
		}
		display.setCursorTile(moveTo);
		display.setCursorNormal();
	}

	private void scheduleWalkAnimation(final List<Location> path) {
		Preconditions.checkArgument(path.size() > 1);
		final Location from = path.get(0);
		final Location to = path.get(path.size() - 1);
		final Unit u = gameController.getWorld().getAt(from).getFirstMovableUnit();
		gameController.getWorld().getAt(from).getUnits().remove(u);
		final WalkAnimator walkAnimator = new WalkAnimator(pathPlanning, path, u);
		new Timer(1, actionEvent -> {
			final Location point = walkAnimator.getNextStepCoordinates();
			if (point == null) {
				display.getFloatingParts().remove(0);
				((Timer) actionEvent.getSource()).stop();
				gameController.getWorld().getAt(to).getUnits().add(u);
				if (display.getCursorTile().equals(walkAnimator.getLastAnimateTo())) {
					focusedTileController
							.fireFocusedTileEvent(gameController.getWorld().getAt(walkAnimator.getLastAnimateTo()));
				}
			} else {
				if (display.getFloatingParts().isEmpty()) {
					display.getFloatingParts().add(point);
				} else {
					display.getFloatingParts().set(0, point);
				}
			}
			display.getGamePanelView().repaint();
		}).start();
	}

}
