package org.microcol.gui;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.microcol.model.GoToMode;
import org.microcol.model.Ship;
import org.microcol.model.Tile;
import org.microcol.model.Unit;
import org.microcol.model.World;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GamePanelPresenter {

	private final Logger logger = Logger.getLogger(GamePanelPresenter.class);

	public interface Display {

		GamePanelView getGamePanelView();

		Point getCursorTile();

		void setCursorTile(Point cursorTile);

		void setCursorNormal();

		void setCursorGoto();

		boolean isGotoMode();

		List<Point> getFloatingParts();

		void setGotoCursorTitle(Point gotoCursorTitle);

		Point getGotoCursorTitle();
	}

	private final World world;

	private final FocusedTileController focusedTileController;

	private final PathPlanning pathPlanning;

	private final GamePanelPresenter.Display display;

	@Inject
	public GamePanelPresenter(final GamePanelPresenter.Display display, final World world,
			final KeyController keyController, final StatusBarMessageController statusBarMessageController,
			final FocusedTileController focusedTileController, final PathPlanning pathPlanning,
			final MoveUnitController moveUnitController) {
		this.focusedTileController = focusedTileController;
		this.world = Preconditions.checkNotNull(world);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.display = Preconditions.checkNotNull(display);

		moveUnitController.addMoveUnitListener(path -> {
			scheduleWalkAnimation(path);
		});

		keyController.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if ('c' == e.getKeyChar()) {
					// chci centrovat
				}
				if ('g' == e.getKeyChar()) {
					// chci posunout jednotku
					if (display.getCursorTile() != null) {
						final Tile tile = world.getAt(display.getCursorTile());
						final Unit unit = tile.getFirstMovableUnit();
						// TODO in description panel show unit description
						if (unit != null) {
							switchToGoMode(unit);
						}
					}
				}
				if (27 == e.getKeyCode()) {
					if (display.isGotoMode()) {
						cancelGoToMode(display.getGotoCursorTitle());
					}
				}
				/**
				 * Enter
				 */
				if (10 == e.getKeyCode()) {
					if (display.isGotoMode()) {
						switchToNormalMode(display.getGotoCursorTitle());
					}
				}
				logger.debug("Pressed key: '" + e.getKeyChar() + "' has code '" + e.getKeyCode() + "', modifiers '"
						+ e.getModifiers() + "'");
			}
		});

		MouseAdapter ma = new MouseAdapter() {

			private Point origin;

			@Override
			public void mousePressed(MouseEvent e) {
				origin = Point.make(e.getX(), e.getY());
				if (display.isGotoMode()) {
					switchToNormalMode(convertToTilesCoordinates(origin));
				} else {
					display.setCursorTile(convertToTilesCoordinates(origin));
					focusedTileController.fireFocusedTileEvent(world.getAt(display.getCursorTile()));
				}
				statusBarMessageController.fireStatusMessageWasChangedEvent("clicket at " + display.getCursorTile());
				display.getGamePanelView().repaint();
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				if (display.isGotoMode()) {
					display.setGotoCursorTitle(convertToTilesCoordinates(Point.make(e.getX(), e.getY())));
					display.getGamePanelView().repaint();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (origin != null) {
					JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class,
							display.getGamePanelView());
					if (viewPort != null) {
						Point delta = origin.substract(Point.make(e.getX(), e.getY()));
						Rectangle view = viewPort.getViewRect();
						view.x += delta.getX();
						view.y += delta.getY();
						display.getGamePanelView().scrollRectToVisible(view);
					}
				}
			}
		};
		display.getGamePanelView().addMouseListener(ma);
		display.getGamePanelView().addMouseMotionListener(ma);

	}

	private Point convertToTilesCoordinates(final Point panelCoordinates) {
		return Point.make(panelCoordinates.getX() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX,
				panelCoordinates.getY() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX);
	}

	private final void switchToGoMode(final Unit unit) {
		logger.debug("Switching '" + unit + "' to go mode.");
		display.setCursorGoto();
	}

	private void cancelGoToMode(final Point moveTo) {
		display.setCursorNormal();
		display.setCursorTile(moveTo);
		focusedTileController.fireFocusedTileEvent(world.getAt(display.getCursorTile()));
	}

	private final void switchToNormalMode(final Point moveTo) {
		logger.debug("Switching to normalmode.");

		final List<Point> path = new ArrayList<Point>();
		pathPlanning.paintPath(display.getCursorTile(), moveTo, point -> path.add(point));
		// make first step

		Ship ship = (Ship) world.getAt(display.getCursorTile()).getFirstMovableUnit();
		ship.setGoToMode(new GoToMode(path));
		world.performMove(ship);

		display.setCursorTile(moveTo);
		display.setCursorNormal();
		focusedTileController.fireFocusedTileEvent(world.getAt(display.getCursorTile()));
	}

	private void scheduleWalkAnimation(final List<Point> path) {
		Preconditions.checkArgument(path.size() > 1);
		final Point from = path.get(0);
		final Point to = path.get(path.size() - 1);
		final Unit u = world.getAt(from).getFirstMovableUnit();
		world.getAt(from).getUnits().remove(u);
		final WalkAnimator walkAnimator = new WalkAnimator(pathPlanning, path, u);
		new Timer(1, actionEvent -> {
			final Point point = walkAnimator.getNextStepCoordinates();
			if (point == null) {
				display.getFloatingParts().remove(0);
				((Timer) actionEvent.getSource()).stop();
				world.getAt(to).getUnits().add(u);
				if (display.getCursorTile().equals(walkAnimator.getLastAnimateTo())) {
					focusedTileController.fireFocusedTileEvent(world.getAt(walkAnimator.getLastAnimateTo()));
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
