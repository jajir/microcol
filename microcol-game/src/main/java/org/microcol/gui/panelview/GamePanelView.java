package org.microcol.gui.panelview;

import java.awt.Graphics2D;
import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.DialogFigth;
import org.microcol.gui.FpsCounter;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.NextTurnController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sun.javafx.scene.control.skin.ScrollPaneSkin;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * View for main game panel.
 */
public class GamePanelView implements GamePanelPresenter.Display {

	private static final int TILE_WIDTH_IN_PX = 35;

	public static final int TOTAL_TILE_WIDTH_IN_PX = TILE_WIDTH_IN_PX;

	private final Canvas canvas;

	private final ImageProvider imageProvider;

	private final Cursor gotoModeCursor;

	private final GameController gameController;

	private final PathPlanning pathPlanning;

	private final VisualDebugInfo visualDebugInfo;

	private final ViewState viewState;

	private final AnimationManager animationManager;

	private final ExcludePainting excludePainting = new ExcludePainting();

	private final FpsCounter fpsCounter;

	private boolean isGridShown;

	private ScreenScrolling screenScrolling;

	private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

	private final MoveModeSupport moveModeSupport;

	private final Text text;

	private final LocalizationHelper localizationHelper;

	private final PaintService paintService;

	private final ViewUtil viewUtil;

	@Inject
	public GamePanelView(final GameController gameController, final NextTurnController nextTurnController,
			final PathPlanning pathPlanning, final ImageProvider imageProvider, final ViewState viewState,
			final MoveModeSupport moveModeSupport, final Text text, final ViewUtil viewUtil,
			final LocalizationHelper localizationHelper, final PaintService paintService) {
		this.gameController = Preconditions.checkNotNull(gameController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.viewState = Preconditions.checkNotNull(viewState);
		this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
		this.text = Preconditions.checkNotNull(text);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.paintService = Preconditions.checkNotNull(paintService);
		this.visualDebugInfo = new VisualDebugInfo();
		oneTurnMoveHighlighter = new OneTurnMoveHighlighter();
		gotoModeCursor = new ImageCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO), 1, 1);
		// excludePainting = new ExcludePainting();
		animationManager = new AnimationManager();
		final GamePanelView map = this;

		// TODO JJ specify canvas size
		canvas = new Canvas(800, 600);

		nextTurnController.addListener(w -> map.repaint());

		// setAutoscrolls(true);

		fpsCounter = new FpsCounter();
		fpsCounter.start();

		isGridShown = true;
		// timer = new Timer(1000 / DEFAULT_FRAME_PER_SECOND, event ->
		// nextGameTick());

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				nextGameTick();
				repaint();
			}
		}.start();
	}

	// private final Timer timer;

	/**
	 * Define how many times per second will be screen repainted (FPS). Real FPS
	 * will be always lover than this value. It's because not all
	 * {@link javax.swing.JComponent#repaint()} leads to real screen repaint.
	 */
	private static final int DEFAULT_FRAME_PER_SECOND = 50;

	@Override
	public void initGame(final boolean idGridShown) {
		this.isGridShown = idGridShown;
		// FIXME JJ correct it
		// if (!timer.isRunning()) {
		// timer.start();
		// }
	}

	@Override
	public void stopTimer() {
		fpsCounter.stop();
		// timer.stop();
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

	public void scrollToPoint(final Point point) {
		// FIXME JJ scroll to specified point
		// final ScrollPane sp = (ScrollPane)canvas.getParent();
		// final Bounds bounds =sp.getViewportBounds();
		// final JViewport viewPort = (JViewport) getParent();
		// final Rectangle view = viewPort.getViewRect();
		// view.x = point.getX();
		// view.y = point.getY();
		// scrollRectToVisible(view);
	}

	@Override
	public GamePanelView getGamePanelView() {
		return this;
	}

	public void repaint() {
		paint(canvas.getGraphicsContext2D());
	}

	/**
	 * Paint everything.
	 */
	public void paint(final GraphicsContext g) {
		final Area area = getArea();

		/**
		 * Following background drawing just verify that there are no uncovered
		 * pixels.
		 */
		// dbg.setFill(Color.YELLOW);
		// dbg.fillRect(0, 0, getWidth(), getHeight());

		paintTiles(g, area);
		paintUnits(g, gameController.getModel(), area);
		paintGrid(g, area);
		paintCursor(g, area);
		paintSteps(g, area);
		paintAnimation(g, area);
		paintService.paintDebugInfo(g, visualDebugInfo, area);
		final Point p = Point.of(area.getTopLeft().add(Location.of(-1, -1)));
		// g.drawImage(dbImage, p.getX(), p.getY());
		if (gameController.getModel().getCurrentPlayer().isComputer()) {
			/**
			 * If move computer that make game field darker.
			 */
			g.setFill(new Color(0, 0, 0, 0.34));
			g.fillRect(p.getX(), p.getY(), 600, 400);
		}
		fpsCounter.screenWasPainted();
	}

	private void paintAnimation(final GraphicsContext graphics, final Area area) {
		if (animationManager.hasNextStep()) {
			animationManager.paint(graphics, area);
			animationManager.performStep();
		}
	}

	/**
	 * Draw main game tiles.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 */
	private void paintTiles(final GraphicsContext graphics, final Area area) {
		for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
			for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
				final Location location = Location.of(i, j);
				final Point point = area.convert(location);
				final Terrain terrain = gameController.getModel().getMap().getTerrainAt(location);
				graphics.drawImage(imageProvider.getTerrainImage(terrain), 0, 0, TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX,
						point.getX(), point.getY(), TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
				if (oneTurnMoveHighlighter.isItHighlighted(location)) {
					graphics.setFill(new Color(255, 200, 255, 100));
					graphics.fillRect(point.getX(), point.getY(), TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
				}
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
	private void paintUnits(final GraphicsContext graphics, final Model world, final Area area) {
		final java.util.Map<Location, List<Unit>> ships = world.getUnitsAt();

		final java.util.Map<Location, List<Unit>> ships2 = ships.entrySet().stream()
				.filter(e -> area.isInArea(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		ships2.forEach((location, list) -> {
			final Unit unit = list.stream().findFirst().get();
			final Point point = area.convert(location);
			if (excludePainting.isUnitIncluded(unit)) {
				paintService.paintUnit(graphics, point, unit);
			}
		});
	}

	private void paintGrid(final GraphicsContext graphics, final Area area) {
		if (isGridShown) {
			graphics.setFill(Color.LIGHTGRAY);
			graphics.setLineWidth(1);
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

	private void drawNetLine(final GraphicsContext graphics, final Area area, final Location l_1, Location l_2) {
		final Point p_1 = area.convert(l_1).add(-1, -1);
		final Point p_2 = area.convert(l_2).add(-1, -1);
		graphics.strokeLine(p_1.getX(), p_1.getY(), p_2.getX(), p_2.getY());
	}

	private void paintCursor(final GraphicsContext graphics, final Area area) {
		if (viewState.getSelectedTile().isPresent()) {
			graphics.setFill(Color.RED);
			graphics.setLineWidth(1);
			paintCursor(graphics, area, viewState.getSelectedTile().get());
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
	private void paintCursor(final GraphicsContext graphics, final Area area, final Location location) {
		final Point p = area.convert(location);
		graphics.strokeLine(p.getX(), p.getY(), p.getX() + TILE_WIDTH_IN_PX, p.getY());
		graphics.strokeLine(p.getX(), p.getY(), p.getX(), p.getY() + TILE_WIDTH_IN_PX);
		graphics.strokeLine(p.getX() + TILE_WIDTH_IN_PX, p.getY(), p.getX() + TILE_WIDTH_IN_PX,
				p.getY() + TILE_WIDTH_IN_PX);
		graphics.strokeLine(p.getX(), p.getY() + TILE_WIDTH_IN_PX, p.getX() + TILE_WIDTH_IN_PX,
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
	private void paintSteps(final GraphicsContext graphics, final Area area) {
		if (viewState.isMoveMode() && viewState.getMouseOverTile().isPresent()) {
			graphics.setFill(Color.YELLOW);
			graphics.setLineWidth(1);
			paintCursor(graphics, area, viewState.getMouseOverTile().get());
			final List<Location> locations = moveModeSupport.getMoveLocations();
			// TODO JJ get moving unit in specific way
			final Unit movingUnit = gameController.getModel().getCurrentPlayer()
					.getUnitsAt(viewState.getSelectedTile().get()).get(0);
			final StepCounter stepCounter = new StepCounter(5, movingUnit.getAvailableMoves());
			final List<Point> steps = Lists.transform(locations, location -> area.convert((Location) location));
			/**
			 * Here could be check if particular step in on screen, but draw few
			 * images outside screen is not big deal.
			 */
			steps.forEach(point -> paintStep(graphics, point, stepCounter, moveModeSupport.isFight()));
		}
	}

	/**
	 * Draw image of steps on tile. Image is part of highlighted path.
	 * 
	 * @param graphics
	 *            required {@link Graphics2D}
	 * @param point
	 *            required point where to draw image
	 */
	private void paintStep(final GraphicsContext graphics, final Point point, final StepCounter stepCounter,
			final boolean isFight) {
		graphics.drawImage(getImageFoStep(stepCounter.canMakeMoveInSameTurn(1), isFight), point.getX(), point.getY());
	}

	private Image getImageFoStep(final boolean normalStep, final boolean isFight) {
		if (normalStep) {
			if (isFight) {
				return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_FIGHT_25x25);
			} else {
				return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_25x25);
			}
		} else {
			if (isFight) {
				return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_FIGHT_TURN_25x25);
			} else {
				return imageProvider.getImage(ImageProvider.IMG_ICON_STEPS_TURN_25x25);
			}
		}
	}

	@Override
	public void startMoveUnit(final Unit ship) {
		oneTurnMoveHighlighter.setLocations(ship.getAvailableLocations());
	}

	// TODO JJ rename it
	@Override
	public void setCursorNormal() {
		oneTurnMoveHighlighter.setLocations(null);
		canvas.setCursor(Cursor.DEFAULT);
		viewState.setMoveMode(false);
	}

	// TODO JJ rename it
	@Override
	public void setCursorGoto() {
		canvas.setCursor(gotoModeCursor);
		viewState.setMoveMode(true);
	}

	// TODO JJ animation scheduling should be in separate class.
	@Override
	public void addMoveAnimator(final List<Location> path, final Unit movingUnit) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(movingUnit);
		animationManager
				.addAnimationPart(new AnimationPartWalk(pathPlanning, path, movingUnit, paintService, excludePainting));
	}

	// TODO JJ animation scheduling should be in separate class.
	public void addFightAnimation(final Unit attacker, final Unit defender) {
		// TODO JJ animation speed should come from game preferences
		animationManager.addAnimationPart(new AnimationPartFight(attacker, defender, imageProvider, 2));
	}

	@Override
	public AnimationManager getAnimationManager() {
		return animationManager;
	}

	@Override
	public void setGridShown(final boolean isGridShown) {
		this.isGridShown = isGridShown;
	}

	@Override
	public boolean performFightDialog(final Unit unitAttacker, final Unit unitDefender) {
		DialogFigth dialogFight = new DialogFigth(text, viewUtil, imageProvider, localizationHelper, unitAttacker,
				unitDefender);
		return dialogFight.isUserChooseFight();
	}

	public void onViewPortResize() {
		// FIXME JJ is it necessary?
		// dbImage = null;
	}

	private ScrollPane getScrollPane() {
		final ScrollPane sp = (ScrollPane) canvas.getParent();
		return sp;
	}

	@Override
	public Bounds getViewportBounds() {
		return getScrollPane().getViewportBounds();
	}

	@Override
	public Area getArea() {
		return new Area(canvas.getBoundsInParent(), gameController.getModel().getMap());
	}

	@Override
	public VisualDebugInfo getVisualDebugInfo() {
		return visualDebugInfo;
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

}
