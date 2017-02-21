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

import org.microcol.gui.model.GameController;
import org.microcol.gui.model.Ship;
import org.microcol.gui.model.Unit;
import org.microcol.gui.model.World;

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

	private Point gotoCursorTitle;

	private Point cursorTile;

	private boolean gotoMode = false;

	/**
	 * In list will be placed elements which are not in map, because there are
	 * moving.
	 */
	private final List<Point> floatingParts = new ArrayList<>();

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
		dbImage = createImage(getGameMapWidth(), getGameMapHeight());
		final GamePanelView map = this;

		nextTurnController.addNextTurnListener(w -> map.repaint());

		setAutoscrolls(true);
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
			paintIntoGraphics(dbg, gameController.getWorld());
			paintNet(dbg);
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
		floatingParts.forEach(part -> {
			graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1), part.getX(), part.getY(), this);
			graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_MODE_GOTO),
					part.getX() + TILE_WIDTH_IN_PX - 12, part.getY(), this);
		});
	}

	/**
	 * Draw main game view.
	 * 
	 * @param g
	 */
	private void paintIntoGraphics(final Graphics2D graphics, final World world) {
		for (int i = 0; i < World.WIDTH; i++) {
			for (int j = 0; j < World.HEIGHT; j++) {
				int x = i * TOTAL_TILE_WIDTH_IN_PX;
				int y = j * TOTAL_TILE_WIDTH_IN_PX;
				graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN), x, y, this);

				if (!world.getMap()[i][j].getUnits().isEmpty()) {
					Unit u = world.getMap()[i][j].getUnits().get(0);
					if (u instanceof Ship) {
						Ship s = (Ship) u;
						if (s.getType() == 0) {
							graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1), x, y, this);
						} else {
							graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP2), x, y, this);
						}
						if (s.getGoToMode() != null && s.getGoToMode().isActive()) {
							graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_MODE_GOTO),
									x + TILE_WIDTH_IN_PX - 12, y, this);
						}
					}
				}

			}
		}
	}

	private void paintNet(final Graphics2D graphics) {
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.setStroke(new BasicStroke(1));
		for (int i = 1; i < World.WIDTH; i++) {
			int x = i * TOTAL_TILE_WIDTH_IN_PX - 1;
			graphics.drawLine(x, 0, x, World.HEIGHT * TILE_WIDTH_IN_PX + World.HEIGHT * (World.WIDTH - 1));
		}
		for (int j = 1; j < World.HEIGHT; j++) {
			int y = j * TOTAL_TILE_WIDTH_IN_PX - 1;
			graphics.drawLine(0, y, World.HEIGHT * TILE_WIDTH_IN_PX + World.WIDTH * (World.HEIGHT - 1), y);
		}
	}

	private void paintCursor(final Graphics2D graphics) {
		if (cursorTile != null) {
			graphics.setColor(Color.RED);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, cursorTile);
		}
	}

	private void paintCursor(final Graphics2D graphics, final Point tile) {
		int x = tile.getX() * TOTAL_TILE_WIDTH_IN_PX - 1;
		int y = tile.getY() * TOTAL_TILE_WIDTH_IN_PX - 1;
		graphics.drawLine(x, y, x + TILE_WIDTH_IN_PX, y);
		graphics.drawLine(x, y, x, y + TILE_WIDTH_IN_PX);
		graphics.drawLine(x + TILE_WIDTH_IN_PX, y, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
		graphics.drawLine(x, y + TILE_WIDTH_IN_PX, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
	}

	private void paintGoToPath(final Graphics2D graphics) {
		if (gotoMode && gotoCursorTitle != null) {
			graphics.setColor(Color.yellow);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, gotoCursorTitle);
			if (!cursorTile.equals(gotoCursorTitle)) {
				List<Point> steps = new ArrayList<>();
				pathPlanning.paintPath(cursorTile, gotoCursorTitle, point -> steps.add(point));
				steps.remove(0);
				steps.forEach(point -> paintStepsToTile(graphics, point));
			}
		}
	}

	private void paintStepsToTile(final Graphics2D graphics, final Point tile) {
		Point p = tile.multiply(TOTAL_TILE_WIDTH_IN_PX).add(4);
		graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_25x25), p.getX(), p.getY(), this);
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
		return World.WIDTH * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	private int getGameMapHeight() {
		return World.HEIGHT * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	@Override
	public Point getCursorTile() {
		return cursorTile;
	}

	@Override
	public void setCursorTile(Point cursorTile) {
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
	public List<Point> getFloatingParts() {
		return floatingParts;
	}

	@Override
	public Point getGotoCursorTitle() {
		return gotoCursorTitle;
	}

	@Override
	public void setGotoCursorTitle(Point gotoCursorTitle) {
		this.gotoCursorTitle = gotoCursorTitle;
	}

}
