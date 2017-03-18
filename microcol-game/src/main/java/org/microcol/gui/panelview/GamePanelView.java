package org.microcol.gui.panelview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Timer;

import org.microcol.gui.FpsCounter;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.GameController;
import org.microcol.gui.event.NextTurnController;
import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Ship;
import org.microcol.model.Terrain;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GamePanelView extends JPanel implements GamePanelPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final static int TILE_WIDTH_IN_PX = 35;

	public final static int TOTAL_TILE_WIDTH_IN_PX = TILE_WIDTH_IN_PX;

	private final ImageProvider imageProvider;

	private VolatileImage dbImage;

	private final Cursor gotoModeCursor;

	private final GameController gameController;

	private final PathPlanning pathPlanning;

	private Location gotoCursorTitle;

	private Location cursorLocation;

	private boolean gotoMode = false;

	private WalkAnimator walkAnimator;

	private final FpsCounter fpsCounter;

	private boolean isGridShown;

	private ScreenScrolling screenScrolling;

	@Inject
	public GamePanelView(final GameController gameController, final NextTurnController nextTurnController,
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

		fpsCounter = new FpsCounter();
		fpsCounter.start();

		isGridShown = true;
		timer = new Timer(1000 / DEFAULT_FRAME_PER_SECOND, event -> nextGameTick());
	}

	private final Timer timer;

	/**
	 * Define how many times per second will be screen repainted (FPS). Real FPS
	 * will be always lover than this value. It's because not all
	 * {@link JComponent#repaint()} leads to real screen repaint.
	 */
	private final static int DEFAULT_FRAME_PER_SECOND = 50;

	@Override
	public void initGame(final boolean idGridShown) {
		this.isGridShown = idGridShown;
		if (!timer.isRunning()) {
			timer.start();
		}
	}

	@Override
	public void stopTimer() {
		fpsCounter.stop();
		timer.stop();
	}

	/**
	 * Smallest game time interval. In ideal case it have time to draw world on
	 * screen.
	 */
	private void nextGameTick() {
		if (screenScrolling != null && screenScrolling.isNextPointAvailable()) {
			scrollToPoint(screenScrolling.getNextPoint());
		}
		repaint();
	}

	@Override
	public void planScrollingAnimationToPoint(final Point targetPoint) {
		screenScrolling = new ScreenScrolling(pathPlanning, getArea().getPointTopLeft(), targetPoint);
	}

	private void scrollToPoint(final Point point) {
		final JViewport viewPort = (JViewport) getParent();
		final Rectangle view = viewPort.getViewRect();
		view.x = point.getX();
		view.y = point.getY();
		scrollRectToVisible(view);
	}

	@Override
	public GamePanelView getGamePanelView() {
		return this;
	}

	private VolatileImage prepareImage(final Area area) {
		final Point p = Point.of(area.getWidth() + 1, area.getHeight() + 1).multiply(TILE_WIDTH_IN_PX);
		return createVolatileImage(p.getX(), p.getY());
	}

	/**
	 * Call original paint with antialising on.
	 */
	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final Area area = new Area((JViewport) this.getParent(), gameController.getGame().getMap());
		if (dbImage == null) {
			dbImage = prepareImage(area);
			if (dbImage == null) {
				/**
				 * This could happens when Graphics is not ready.
				 */
				return;
			}
		}
		if (dbImage.validate(getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
			dbImage = prepareImage(area);
		}
		if (dbImage != null) {
			final Graphics2D dbg = (Graphics2D) dbImage.getGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			/**
			 * Following background drawing just verify that there are no
			 * uncovered pixels.
			 */
			// dbg.setColor(Color.YELLOW);
			// dbg.fillRect(0, 0, getWidth(), getHeight());

			paintTiles(dbg, area);
			paintUnits(dbg, gameController.getGame(), area);
			paintGrid(dbg, area);
			paintCursor(dbg, area);
			paintGoToPath(dbg, area);
			paintMovingAnimation(dbg, area);
			final Point p = Point.of(area.getTopLeft());
			g.drawImage(dbImage, p.getX(), p.getY(), null);
			if (gameController.getGame().getCurrentPlayer().isComputer()) {
				/**
				 * If move computer that make game field darker.
				 */
				g.drawImage(dbImage, p.getX(), p.getY(), null);
				g.setColor(new Color(0, 0, 0, 64));
				g.fillRect(p.getX(), p.getY(), dbImage.getWidth(this), dbImage.getHeight(this));
			}
			dbg.dispose();
		}
		// Sync the display on some systems.
		// (on Linux, this fixes event queue problems)
		Toolkit.getDefaultToolkit().sync();
		fpsCounter.screenWasPainted();
	}

	private void paintMovingAnimation(final Graphics2D graphics, final Area area) {
		if (walkAnimator != null && walkAnimator.getNextCoordinates() != null) {
			if (area.isInArea(walkAnimator.getNextCoordinates())) {
				final Point part = area.convert(walkAnimator.getNextCoordinates());
				paintUnit(graphics, part, walkAnimator.getUnit());
				graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_MODE_GOTO),
						part.getX() + TILE_WIDTH_IN_PX - 12, part.getY(), this);
			}
			if (walkAnimator.isNextAnimationLocationAvailable()) {
				walkAnimator.countNextAnimationLocation();
			}
		}

	}

	/**
	 * Draw main game tiles.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 */
	private void paintTiles(final Graphics2D graphics, final Area area) {
		for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
			for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
				final Location location = Location.of(i, j);
				final Point point = area.convert(location);
				final Terrain terrain = gameController.getGame().getMap().getTerrainAt(location);
				graphics.drawImage(imageProvider.getTerrainImage(terrain), point.getX(), point.getY(),
						point.getX() + 35, point.getY() + 35, 0, 0, 35, 35, this);
			}
		}
	}

	/**
	 * Draw units.
	 * <p>
	 * Methods iterate through all location with ships, select first ship and
	 * draw it.
	 * </p>
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param game
	 *            required {@link Game}
	 */
	private void paintUnits(final Graphics2D graphics, final Game world, final Area area) {
		final java.util.Map<Location, List<Ship>> ships = world.getShipsAt();

		final java.util.Map<Location, List<Ship>> ships2 = ships.entrySet().stream()
				.filter(e -> area.isInArea(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		ships2.forEach((location, list) -> {
			final Ship ship = list.stream().findFirst().get();
			final Point point = area.convert(location);
			if (walkAnimator == null || !walkAnimator.isNextAnimationLocationAvailable()
					|| !walkAnimator.getTo().equals(location)) {
				paintUnit(graphics, point, ship);
			}
		});
	}

	private final static int FLAG_WIDTH = 7;

	private final static int FLAG_HEIGHT = 12;

	private void paintUnit(final Graphics2D graphics, final Point point, final Ship ship) {
		Point p = point.add(2, 4);
		graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1), p.getX(), p.getY(), this);
		paintOwnersFlag(graphics, point.add(1, 5), ship.getOwner());
	}

	/**
	 * All units have flag containing color of owner. Method draw this flag.
	 */
	private void paintOwnersFlag(final Graphics2D graphics, final Point point, final Player player) {
		graphics.setColor(Color.BLACK);
		graphics.drawRect(point.getX(), point.getY(), FLAG_WIDTH, FLAG_HEIGHT);
		// TODO JJ player's color should be property
		if (player.isHuman()) {
			graphics.setColor(Color.YELLOW);
		} else {
			switch (player.getName().hashCode() % 4) {
			case 0:
				graphics.setColor(Color.RED);
				break;
			case 1:
				graphics.setColor(Color.GREEN);
				break;
			case 2:
				graphics.setColor(Color.MAGENTA);
				break;
			case 3:
				graphics.setColor(Color.BLUE);
				break;
			default:
				graphics.setColor(Color.gray);
				break;
			}
		}
		graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 1, FLAG_HEIGHT - 1);
	}

	private void paintGrid(final Graphics2D graphics, final Area area) {
		if (isGridShown) {
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.setStroke(new BasicStroke(1));
			for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
				final Location l_1 = Location.of(i, area.getTopLeft().getY());
				final Location l_2 = Location.of(i, area.getBottomRight().getY() + 1);
				drawNetLine(graphics, area, l_1, l_2);
			}
			for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
				final Location l_1 = Location.of(area.getTopLeft().getX(), j);
				final Location l_2 = Location.of(area.getBottomRight().getX() + 1, j);
				drawNetLine(graphics, area, l_1, l_2);
			}
		}
	}

	private void drawNetLine(final Graphics2D graphics, final Area area, final Location l_1, Location l_2) {
		final Point p_1 = area.convert(l_1).add(-1, -1);
		final Point p_2 = area.convert(l_2).add(-1, -1);
		graphics.drawLine(p_1.getX(), p_1.getY(), p_2.getX(), p_2.getY());
	}

	private void paintCursor(final Graphics2D graphics, final Area area) {
		if (cursorLocation != null) {
			graphics.setColor(Color.RED);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, area, cursorLocation);
		}
	}

	/**
	 * Draw highlight of some tile.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param area
	 *            required displayed area
	 * @param location
	 *            required tiles where to draw cursor
	 */
	private void paintCursor(final Graphics2D graphics, final Area area, final Location location) {
		final Point p = area.convert(location);
		graphics.drawLine(p.getX(), p.getY(), p.getX() + TILE_WIDTH_IN_PX, p.getY());
		graphics.drawLine(p.getX(), p.getY(), p.getX(), p.getY() + TILE_WIDTH_IN_PX);
		graphics.drawLine(p.getX() + TILE_WIDTH_IN_PX, p.getY(), p.getX() + TILE_WIDTH_IN_PX,
				p.getY() + TILE_WIDTH_IN_PX);
		graphics.drawLine(p.getX(), p.getY() + TILE_WIDTH_IN_PX, p.getX() + TILE_WIDTH_IN_PX,
				p.getY() + TILE_WIDTH_IN_PX);
	}

	/**
	 * When move mode is enabled mouse cursor is followed by highlighted path.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param area
	 *            required displayed area
	 */
	private void paintGoToPath(final Graphics2D graphics, final Area area) {
		if (gotoMode && gotoCursorTitle != null) {
			graphics.setColor(Color.yellow);
			graphics.setStroke(new BasicStroke(1));
			paintCursor(graphics, area, gotoCursorTitle);
			if (!cursorLocation.equals(gotoCursorTitle)) {
				List<Point> steps = new ArrayList<>();
				pathPlanning.paintPath(cursorLocation, gotoCursorTitle, location -> steps.add(area.convert(location)));
				// TODO JJ get(0) could return different ship that is really
				// moved
				final Ship unit = gameController.getGame().getCurrentPlayer().getShipsAt(cursorLocation).get(0);
				final StepCounter stepCounter = new StepCounter(5, unit.getAvailableMoves());
				/**
				 * Here could be check if particular step in on screen, but draw
				 * few images outside screen is not big deal.
				 */
				steps.forEach(point -> paintStepsToTile(graphics, point, stepCounter));
			}
		}
	}

	/**
	 * Draw image on tile. Image is part of highlighted path.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param point
	 *            required point where to draw image
	 */
	private void paintStepsToTile(final Graphics2D graphics, final Point point, final StepCounter stepCounter) {
		graphics.drawImage(getImageFoStep(stepCounter.canMakeMoveInSameTurn(1)), point.getX(), point.getY(), this);
	}

	private Image getImageFoStep(final boolean normalStep) {
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
		/**
		 * Code solve state when component have to be painted and game doesn't
		 * exists. Because than graphics is not initialize and initGame can't
		 * prepare dbImage.
		 */
		if (gameController.getGame() == null) {
			return new Dimension(100, 100);
		} else {
			return new Dimension(getGameMapWidth(), getGameMapHeight());
		}
	}

	private int getGameMapWidth() {
		return (gameController.getGame().getMap().getMaxX()) * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	private int getGameMapHeight() {
		return (gameController.getGame().getMap().getMaxY()) * TOTAL_TILE_WIDTH_IN_PX - 1;
	}

	@Override
	public Location getCursorLocation() {
		return cursorLocation;
	}

	@Override
	public void setCursorLocation(Location cursorTile) {
		this.cursorLocation = cursorTile;
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
		System.out.println(gotoCursorTitle);
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

	@Override
	public void setGridShown(final boolean isGridShown) {
		this.isGridShown = isGridShown;
	}

	public void onViewPortResize() {
		dbImage = null;
	}

	@Override
	public Area getArea() {
		return new Area((JViewport) getParent(), gameController.getGame().getMap());
	}

}
