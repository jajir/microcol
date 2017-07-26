package org.microcol.gui.panelview;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.DialogFigth;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.panelview.MoveModeSupport.MoveMode;
import org.microcol.gui.util.FpsCounter;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * View for main game panel.
 */
public class GamePanelView implements GamePanelPresenter.Display {

	private final Logger logger = LoggerFactory.getLogger(GamePanelView.class);

	public static final int TILE_WIDTH_IN_PX = 35;

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

	private final GamePreferences gamePreferences;

	private boolean isGridShown;

	private Optional<ScreenScrolling> screenScrolling = Optional.empty();

	private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

	private final MoveModeSupport moveModeSupport;

	private final Text text;

	private final LocalizationHelper localizationHelper;

	private final PaintService paintService;

	private final ViewUtil viewUtil;

	private final VisibleArea visibleArea;

	@Inject
	public GamePanelView(final GameController gameController, final PathPlanning pathPlanning,
			final ImageProvider imageProvider, final ViewState viewState, final MoveModeSupport moveModeSupport,
			final Text text, final ViewUtil viewUtil, final LocalizationHelper localizationHelper,
			final PaintService paintService, final GamePreferences gamePreferences) {
		this.gameController = Preconditions.checkNotNull(gameController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.viewState = Preconditions.checkNotNull(viewState);
		this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
		this.text = Preconditions.checkNotNull(text);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.paintService = Preconditions.checkNotNull(paintService);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.visualDebugInfo = new VisualDebugInfo();
		oneTurnMoveHighlighter = new OneTurnMoveHighlighter();
		gotoModeCursor = new ImageCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO), 1, 1);
		// excludePainting = new ExcludePainting();
		animationManager = new AnimationManager();
		visibleArea = new VisibleArea();

		// TODO JJ specify canvas size
		canvas = new Canvas();

		fpsCounter = new FpsCounter();
		fpsCounter.start();

		isGridShown = true;

		/**
		 * Following class main define animation loop.
		 */
		new AnimationTimer() {

			@Override
			public void handle(long now) {
				nextGameTick();
				paint();
			}
		}.start();
	}

	@Override
	public void initGame(final boolean idGridShown, final Model model) {
		this.isGridShown = idGridShown;
		visibleArea.setMaxMapSize(model.getMap());
	}

	@Override
	public void stopTimer() {
		fpsCounter.stop();
	}

	/**
	 * Smallest game time interval. In ideal case it have time to draw world on
	 * screen.
	 */
	private void nextGameTick() {
		if (screenScrolling.isPresent() && screenScrolling.get().isNextPointAvailable()) {
			scrollToPoint(screenScrolling.get().getNextPoint());
		}
	}

	private void scrollToPoint(final Point point) {
		visibleArea.setX(point.getX());
		visibleArea.setY(point.getY());
	}

	@Override
	public void planScrollingAnimationToPoint(final Point targetPoint) {
		/**
		 * Following if is a hack. Canvas width and height are set after game
		 * start. When game starts and scroll request is created than game
		 * scroll map outside of screen. Following hack solve it. But it's not
		 * correct.
		 */
		if (visibleArea.getCanvasHeight() != 0) {
			screenScrolling = Optional.of(new ScreenScrolling(pathPlanning, visibleArea.getTopLeft(), targetPoint));
		}
	}

	@Override
	public GamePanelView getGamePanelView() {
		return this;
	}

	/**
	 * It's called to redraw whole game. It should be called each game tick.
	 */
	private void paint() {
		logger.debug("painting: " + visibleArea);
		paint(canvas.getGraphicsContext2D());
	}

	/**
	 * Paint everything.
	 */
	private void paint(final GraphicsContext g) {
		final Area area = getArea();
		// TODO JJ get background color from css style
		g.setFill(Color.valueOf("#ececec"));
		g.fillRect(0, 0, visibleArea.getCanvasWidth(), visibleArea.getCanvasHeight());

		paintTiles(g, area);
		paintUnits(g, gameController.getModel(), area);
		paintGrid(g, area);
		paintCursor(g, area);
		paintSteps(g, area);
		paintAnimation(g, area);
		if (gamePreferences.isDevelopment()) {
			paintService.paintDebugInfo(g, visualDebugInfo, area);
		}
		if (gameController.getModel().getCurrentPlayer().isComputer()) {
			/**
			 * If move computer that make game field darker.
			 */
			final Point topLeftPoint = area.convertToPoint(area.getTopLeft());
			final Point bottomRightPoint = area.convertToPoint(area.getBottomRight().add(Location.of(1, 1)));
			final Point size = bottomRightPoint.substract(topLeftPoint);
			g.setFill(new Color(0, 0, 0, 0.34));
			g.fillRect(topLeftPoint.getX(), topLeftPoint.getY(), size.getX(), size.getY());
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
	 *            required {@link GraphicsContext}
	 */
	private void paintTiles(final GraphicsContext graphics, final Area area) {
		for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
			for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
				final Location location = Location.of(i, j);
				final Point point = area.convertToPoint(location);
				final Terrain terrain = gameController.getModel().getMap().getTerrainAt(location);
				graphics.drawImage(imageProvider.getTerrainImage(terrain), 0, 0, TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX,
						point.getX(), point.getY(), TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
				if (oneTurnMoveHighlighter.isItHighlighted(location)) {
					graphics.setFill(new Color(0.95, 0.75, 0.90, 0.4F));
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
	 *            required {@link GraphicsContext}
	 * @param game
	 *            required {@link Game}
	 */
	private void paintUnits(final GraphicsContext graphics, final Model world, final Area area) {
		final java.util.Map<Location, List<Unit>> ships = world.getUnitsAt();

		final java.util.Map<Location, List<Unit>> ships2 = ships.entrySet().stream()
				.filter(e -> area.isVisible(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		ships2.forEach((location, list) -> {
			final Unit unit = list.stream().findFirst().get();
			final Point point = area.convertToPoint(location);
			if (excludePainting.isUnitIncluded(unit)) {
				paintService.paintUnit(graphics, point, unit);
			}
		});
	}

	private void paintGrid(final GraphicsContext graphics, final Area area) {
		if (isGridShown) {
			graphics.setStroke(Color.LIGHTGREY);
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
		final Point p_1 = area.convertToPoint(l_1).add(-1, -1);
		final Point p_2 = area.convertToPoint(l_2).add(-1, -1);
		graphics.strokeLine(p_1.getX(), p_1.getY(), p_2.getX(), p_2.getY());
	}

	private void paintCursor(final GraphicsContext graphics, final Area area) {
		if (viewState.getSelectedTile().isPresent()) {
			graphics.setStroke(Color.GREY);
			graphics.setLineWidth(2);
			paintCursor(graphics, area, viewState.getSelectedTile().get());
		}
	}

	/**
	 * Draw highlight of some tile.
	 * 
	 * @param graphics
	 *            required {@link GraphicsContext}
	 * @param area
	 *            required displayed area
	 * @param location
	 *            required tiles where to draw cursor
	 */
	private void paintCursor(final GraphicsContext graphics, final Area area, final Location location) {
		final Point p = area.convertToPoint(location);
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
	 *            required {@link GraphicsContext}
	 * @param area
	 *            required displayed area
	 */
	private void paintSteps(final GraphicsContext graphics, final Area area) {
		if (viewState.isMoveMode() && viewState.getMouseOverTile().isPresent()) {
//			graphics.setFill(Color.YELLOW);
//			graphics.setLineWidth(1);
			paintCursor(graphics, area, viewState.getMouseOverTile().get());
			final List<Location> locations = moveModeSupport.getMoveLocations();
			// TODO JJ get moving unit in specific way
			final Unit movingUnit = gameController.getModel().getCurrentPlayer()
					.getUnitsAt(viewState.getSelectedTile().get()).get(0);
			final StepCounter stepCounter = new StepCounter(5, movingUnit.getAvailableMoves());
			final List<Point> steps = Lists.transform(locations, location -> area.convertToPoint((Location) location));
			/**
			 * Here could be check if particular step in on screen, but draw few
			 * images outside screen is not big deal.
			 */
			steps.forEach(point -> paintStep(graphics, point, stepCounter, moveModeSupport.getMoveMode()));
		}
	}

	/**
	 * Draw image of steps on tile. Image is part of highlighted path.
	 * 
	 * @param graphics
	 *            required {@link GraphicsContext}
	 * @param point
	 *            required point where to draw image
	 */
	private void paintStep(final GraphicsContext graphics, final Point point, final StepCounter stepCounter,
			final MoveMode moveMode) {
		final Image image = imageProvider.getImage(moveMode.getImageForStep(stepCounter.canMakeMoveInSameTurn(1)));
		graphics.drawImage(image, point.getX(), point.getY());
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
		animationManager.addAnimation(new AnimationWalk(pathPlanning, path, movingUnit, paintService, excludePainting),
				animation -> excludePainting.includeUnit(movingUnit));
	}

	public void addFightAnimation(final Unit attacker, final Unit defender) {
		animationManager.addAnimation(
				new AnimationFight(attacker, defender, imageProvider, gamePreferences.getAnimationSpeed()));
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
		DialogFigth dialogFight = new DialogFigth(text, viewUtil, imageProvider, localizationHelper, gamePreferences,
				unitAttacker, unitDefender);
		return dialogFight.isUserChooseFight();
	}

	@Override
	public Area getArea() {
		return new Area(visibleArea, gameController.getModel().getMap());
	}

	@Override
	public VisualDebugInfo getVisualDebugInfo() {
		return visualDebugInfo;
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public VisibleArea getVisibleArea() {
		return visibleArea;
	}

}
