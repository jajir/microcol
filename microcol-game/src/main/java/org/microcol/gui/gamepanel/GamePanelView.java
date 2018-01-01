package org.microcol.gui.gamepanel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.DialogFigth;
import org.microcol.gui.GamePreferences;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.MoveModeSupport.MoveMode;
import org.microcol.gui.util.FpsCounter;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Colony;
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

	private final GameModelController gameModelController;

	private final PathPlanning pathPlanning;

	private final VisualDebugInfo visualDebugInfo;

	private final SelectedTileManager selectedTileManager;
	
	private final SelectedUnitManager selectedUnitManager;

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
	
	private final MouseOverTileManager mouseOverTileManager;
	
	private final ModeController modeController;

	@Inject
	public GamePanelView(final GameModelController gameModelController, final PathPlanning pathPlanning,
			final ImageProvider imageProvider,
			final SelectedTileManager selectedTileManager,
			final SelectedUnitManager selectedUnitManager,
			final MoveModeSupport moveModeSupport,
			final Text text,
			final ViewUtil viewUtil,
			final LocalizationHelper localizationHelper,
			final PaintService paintService,
			final GamePreferences gamePreferences,
			final MouseOverTileManager mouseOverTileManager,
			final AnimationManager animationManager,
			final ModeController modeController) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
		this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
		this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
		this.text = Preconditions.checkNotNull(text);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.paintService = Preconditions.checkNotNull(paintService);
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
		this.visualDebugInfo = new VisualDebugInfo();
		this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
		this.animationManager = Preconditions.checkNotNull(animationManager);
		this.modeController = Preconditions.checkNotNull(modeController);
		oneTurnMoveHighlighter = new OneTurnMoveHighlighter();
		gotoModeCursor = new ImageCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO), 1, 1);
		// excludePainting = new ExcludePainting();
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
				if (gameModelController.isModelReady()) {
					paint();
				}
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

		paintTerrain(g, area);
		paintGrid(g, area);
		paintSelectedTile(g, area);
		paintUnits(g, gameModelController.getModel(), area);
		paintColonies(g, gameModelController.getModel(), area);
		paintSteps(g, area);
		paintAnimation(g, area);
		if (gamePreferences.isDevelopment()) {
			paintService.paintDebugInfo(g, visualDebugInfo, area);
		}
		if (gameModelController.getCurrentPlayer().isComputer()) {
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
	private void paintTerrain(final GraphicsContext graphics, final Area area) {
		for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
			for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
				final Location location = Location.of(i, j);
				final Point point = area.convertToPoint(location);
				final Terrain terrain = gameModelController.getModel().getMap().getTerrainAt(location);
				paintService.paintTerrainOnTile(graphics, point, location, terrain,
						oneTurnMoveHighlighter.isItHighlighted(location));
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
		final Map<Location, List<Unit>> ships = world.getUnitsAt();

		final Map<Location, List<Unit>> ships2 = ships.entrySet().stream().filter(e -> area.isVisible(e.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		ships2.forEach((location, list) -> {
			final Unit unit = list.stream().findFirst().get();
			final Point point = area.convertToPoint(location);
			if (excludePainting.isUnitIncluded(unit)) {
				paintService.paintUnit(graphics, point, unit);
			}
		});
	}

	private void paintColonies(final GraphicsContext graphics, final Model world, final Area area) {
		final Map<Location, Colony> colonies = world.getColoniesAt().entrySet().stream()
				.filter(entry -> area.isVisible(entry.getKey()))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		colonies.forEach((location, colony) -> {
			final Point point = area.convertToPoint(location);
			paintService.paintColony(graphics, point, colony);
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

	private void paintSelectedTile(final GraphicsContext graphics, final Area area) {
		if (selectedTileManager.getSelectedTile().isPresent()) {
			graphics.setStroke(Color.GREY);
			graphics.setLineWidth(2);
			paintCursor(graphics, area, selectedTileManager.getSelectedTile().get());
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
		if (modeController.isMoveMode() && mouseOverTileManager.getMouseOverTile().isPresent()) {
			final Unit selectedUnit = selectedUnitManager.getSelectedUnit().get();
			if(!selectedUnit.isAtPlaceLocation()){
				return;
			}
			paintCursor(graphics, area, mouseOverTileManager.getMouseOverTile().get());
			final List<Location> locations = moveModeSupport.getMoveLocations();
			final StepCounter stepCounter = new StepCounter(5, selectedUnit.getAvailableMoves());
			final List<Point> steps = Lists.transform(locations, location -> area.convertToPoint(location));
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

	@Override
	public void setMoveModeOff() {
		oneTurnMoveHighlighter.setLocations(null);
		canvas.setCursor(Cursor.DEFAULT);
		modeController.setMoveMode(false);
	}

	@Override
	public void setMoveModeOn() {
		canvas.setCursor(gotoModeCursor);
		modeController.setMoveMode(true);
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
		return new Area(visibleArea, gameModelController.getModel().getMap());
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
