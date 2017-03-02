package org.microcol.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.microcol.gui.event.NextTurnController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.model.GameController;
import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.Map;
import org.microcol.model.Ship;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GamePanelView extends JPanel implements GamePanelPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final static int TILE_WIDTH_IN_PX = 30;

	private final static int GRID_LINE_WIDTH = 1;

	public final static int TOTAL_TILE_WIDTH_IN_PX = TILE_WIDTH_IN_PX + GRID_LINE_WIDTH;

	private final ImageProvider imageProvider;

	private Image dbImage;

	private final Cursor gotoModeCursor;

	private final GameController gameController;

	private final PathPlanning pathPlanning;

	private Location gotoCursorTitle;

	private Location cursorTile;

	private boolean gotoMode = false;

	private WalkAnimator walkAnimator;

	@Inject
	public GamePanelView(final StatusBarMessageController statusBarMessageController,
			final GameController gameController, final NextTurnController nextTurnController,
			final PathPlanning pathPlanning, final ImageProvider imageProvider) {
		this.gameController = Preconditions.checkNotNull(gameController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		gotoModeCursor = toolkit.createCustomCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO),
				new java.awt.Point(1, 1), "gotoModeCursor");
		final GamePanelView map = this;

		nextTurnController.addNextTurnListener(w -> map.repaint());

		setAutoscrolls(true);
	}

	@Override
	public void initGame() {
		dbImage = createImage(getGameMapWidth(), getGameMapHeight());
	}

	@Override
	public GamePanelView getGamePanelView() {
		return this;
	}

	/**
	 * Call original paint with antialising on.
	 */
	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (dbImage == null) {
			dbImage = createImage(getGameMapWidth(), getGameMapHeight());
			if (dbImage == null) {
				return;
			} else {
			}
		}
		if (dbImage != null) {
			final Graphics2D dbg = (Graphics2D) dbImage.getGraphics();
			paintTilesAndUnits(dbg, gameController.getGame());
			paintNet(dbg, gameController.getGame().getMap());
			paintCursor(dbg);
			paintGoToPath(dbg);
			paintMovingAnimation(dbg);
			g.drawImage(dbImage, 0, 0, null);
			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)
		}
		Toolkit.getDefaultToolkit().sync();
	}

	private void paintMovingAnimation(final Graphics2D graphics) {
		if (walkAnimator != null && walkAnimator.getNextCoordinates() != null) {
			Location part = walkAnimator.getNextCoordinates();
			graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1), part.getX(), part.getY(), this);
			graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_MODE_GOTO),
					part.getX() + TILE_WIDTH_IN_PX - 12, part.getY(), this);
		}
	}

	/**
	 * Draw main game view.
	 * 
	 * @param g
	 */
	private void paintTilesAndUnits(final Graphics2D graphics, final Game world) {
		for (int i = 0; i < world.getMap().getMaxX(); i++) {
			for (int j = 0; j < world.getMap().getMaxY(); j++) {
				int x = i * TOTAL_TILE_WIDTH_IN_PX;
				int y = j * TOTAL_TILE_WIDTH_IN_PX;
				final Location loc = Location.make(i, j);
				graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN), x, y, this);
				if (!world.getShipsAt(loc).isEmpty()) {
					Ship s = world.getShipsAt(loc).get(0);
					if (walkAnimator == null || (!walkAnimator.isNextAnimationLocationAvailable()
							|| !walkAnimator.getTo().equals(loc))) {
						// TODO JJ ship owner should show by color.
						if (s.getOwner().isHuman()) {
							graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1), x, y, this);
						} else {
							graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP2), x, y, this);
						}
						// TODO JJ kdys se bude kreslit goto mode, nasledujici
						// kod
						// vykresli ikonu k lodi
						// if (moveAutomatization.isShipMoving(s)) {
						// graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_MODE_GOTO),
						// x + TILE_WIDTH_IN_PX - 12, y, this);
						// }
					}
				}

			}
		}
	}

	private void paintNet(final Graphics2D graphics, final Map map) {
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.setStroke(new BasicStroke(1));
		for (int i = 1; i < map.getMaxX(); i++) {
			int x = i * TOTAL_TILE_WIDTH_IN_PX - 1;
			graphics.drawLine(x, 0, x, map.getMaxY() * TILE_WIDTH_IN_PX + map.getMaxY() * (map.getMaxX() - 1));
		}
		for (int j = 1; j < map.getMaxY(); j++) {
			int y = j * TOTAL_TILE_WIDTH_IN_PX - 1;
			graphics.drawLine(0, y, map.getMaxY() * TILE_WIDTH_IN_PX + map.getMaxX() * (map.getMaxY() - 1), y);
		}
	}

	private void paintCursor(final Graphics2D graphics) {
		if (cursorTile != null) {
			graphics.setColor(Color.RED);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, cursorTile);
		}
	}

	/**
	 * Draw highlight of some tile.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param tile
	 *            required tiles where to draw cursor
	 */
	private void paintCursor(final Graphics2D graphics, final Location tile) {
		int x = tile.getX() * TOTAL_TILE_WIDTH_IN_PX - 1;
		int y = tile.getY() * TOTAL_TILE_WIDTH_IN_PX - 1;
		graphics.drawLine(x, y, x + TILE_WIDTH_IN_PX, y);
		graphics.drawLine(x, y, x, y + TILE_WIDTH_IN_PX);
		graphics.drawLine(x + TILE_WIDTH_IN_PX, y, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
		graphics.drawLine(x, y + TILE_WIDTH_IN_PX, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
	}

	/**
	 * When move mode is enabled mouse cursor is followed by highlighted path.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 */
	private void paintGoToPath(final Graphics2D graphics) {
		if (gotoMode && gotoCursorTitle != null) {
			graphics.setColor(Color.yellow);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, gotoCursorTitle);
			if (!cursorTile.equals(gotoCursorTitle)) {
				List<Location> steps = new ArrayList<>();
				pathPlanning.paintPath(cursorTile, gotoCursorTitle, point -> steps.add(point));
				// TODO get(0) could return different ship that is really moved
				final Ship unit = gameController.getGame().getCurrentPlayerShipsAt(cursorTile).get(0);
				final StepCounter stepCounter = new StepCounter(5, unit.getAvailableMoves());
				steps.remove(0);
				steps.forEach(point -> paintStepsToTile(graphics, point, stepCounter));
			}
		}
	}

	/**
	 * Draw image on tile. Image is part of highlighted path.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param tile
	 *            required location of tile where to draw image
	 */
	private void paintStepsToTile(final Graphics2D graphics, final Location tile, final StepCounter stepCounter) {
		// final Location p = tile.multiply(TOTAL_TILE_WIDTH_IN_PX).add(4);
		final int x = tile.getX() * TOTAL_TILE_WIDTH_IN_PX + 4;
		final int y = tile.getY() * TOTAL_TILE_WIDTH_IN_PX + 4;
		graphics.drawImage(getImageFoStep(stepCounter.canMakeMoveInSameTurn(1)), x, y, this);
	}

	private Image getImageFoStep(boolean normalStep) {
		if (normalStep) {
			return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_25x25);
		} else {
			return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_TURN_25x25);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(getGameMapWidth(), getGameMapHeight());
	}

	private int getGameMapWidth() {
		return gameController.getGame().getMap().getMaxX() * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	private int getGameMapHeight() {
		return gameController.getGame().getMap().getMaxY() * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	@Override
	public Location getCursorTile() {
		return cursorTile;
	}

	@Override
	public void setCursorTile(Location cursorTile) {
		this.cursorTile = cursorTile;
	}

	@Override
	public void setCursorNormal() {
		setCursor(Cursor.getDefaultCursor());
		gotoMode = false;
	}

	@Override
	public void setCursorGoto() {
		setCursor(gotoModeCursor);
		gotoMode = true;
	}

	@Override
	public boolean isGotoMode() {
		return gotoMode;
	}

	@Override
	public Location getGotoCursorTitle() {
		return gotoCursorTitle;
	}

	@Override
	public void setGotoCursorTitle(final Location gotoCursorTitle) {
		this.gotoCursorTitle = gotoCursorTitle;
	}

	@Override
	public void setWalkAnimator(final WalkAnimator walkAnimator) {
		this.walkAnimator = walkAnimator;
	}

	@Override
	public WalkAnimator getWalkAnimator() {
		return walkAnimator;
	}

}
