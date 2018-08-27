package org.microcol.gui.gamepanel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.microcol.gui.DialogFigth;
import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.MoveModeSupport.MoveMode;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.FpsCounter;
import org.microcol.gui.util.GamePreferences;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * View for main game panel.
 */
public final class GamePanelView {

    private final Logger logger = LoggerFactory.getLogger(GamePanelView.class);

    public static final int TILE_WIDTH_IN_PX = 45;

    private final PaneCanvas canvas;

    private final ImageProvider imageProvider;

    private final Cursor gotoModeCursor;

    private final GameModelController gameModelController;

    private final PathPlanning pathPlanning;

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final AnimationManager animationManager;
    
    private final ScrollingManager scrollingManager;

    private final ExcludePainting excludePainting;

    private final FpsCounter fpsCounter;

    private final GamePreferences gamePreferences;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    private final MoveModeSupport moveModeSupport;

    private final PaintService paintService;

    private final VisibleArea visibleArea;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final DialogFigth dialogFigth;

    @Inject
    public GamePanelView(final GameModelController gameModelController,
            final PathPlanning pathPlanning, final ImageProvider imageProvider,
            final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager, final MoveModeSupport moveModeSupport,
            final PaintService paintService, final GamePreferences gamePreferences,
            final MouseOverTileManager mouseOverTileManager,
            final AnimationManager animationManager, final ScrollingManager scrollingManager,
            final ModeController modeController, final ExcludePainting excludePainting,
            final DialogFigth dialogFigth, final VisibleArea visibleArea,
            final PaneCanvas paneCanvas, final OneTurnMoveHighlighter oneTurnMoveHighlighter) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.animationManager = Preconditions.checkNotNull(animationManager);
        this.scrollingManager = Preconditions.checkNotNull(scrollingManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.dialogFigth = Preconditions.checkNotNull(dialogFigth);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.canvas = Preconditions.checkNotNull(paneCanvas);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        gotoModeCursor = new ImageCursor(imageProvider.getImage(ImageProvider.IMG_CURSOR_GOTO), 1,
                1);

        canvas.widthProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < PaneCanvas.MAX_CANVAS_SIDE_LENGTH) {
                visibleArea.setCanvasWidth(newValue.intValue());
            }
        });
        canvas.heightProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.intValue() < PaneCanvas.MAX_CANVAS_SIDE_LENGTH) {
                visibleArea.setCanvasHeight(newValue.intValue());
            }
        });

        fpsCounter = new FpsCounter();
        fpsCounter.start();

        /**
         * Following class main define animation loop.
         */
        new SimpleAnimationTimer(this::onNextGameTick).start();
    }

    public void stopTimer() {
        fpsCounter.stop();
    }

    /**
     * Smallest game time interval. In ideal case it have time to draw world on
     * screen.
     */
    @SuppressWarnings("unused")
    private void onNextGameTick(final Long now) {
        if (gameModelController.isModelReady()) {
            paint();
        }
    }
    
    public void skipCenterViewAtLocation(final Location location){
        visibleArea.setOnCanvasReady(ok -> {
            final Area area = getArea();
            final Point p = visibleArea.scrollToPoint(area.getCenterToLocation(location));
            visibleArea.setX(p.getX());
            visibleArea.setY(p.getY());
        });
    }

    public void planScrollingAnimationToLocation(final Location location) {
        visibleArea.setOnCanvasReady(state -> {
            planScrollingAnimationToPoint(getArea().getCenterToLocation(location));
        });
    }

    public void planScrollingAnimationToPoint(final Point to) {
        final Point from = visibleArea.getTopLeft();
        if (from.distanceSimplified(to) > 0) {
            /**
             * Following precondition throws exception when scroll planning is called before
             * canvas was fully initialized. Method could be called just after canvas full
             * initialization.
             */
            Preconditions.checkState(visibleArea.isReady(),
                    "screen scroll is called before canvas initialization was finished.");
            scrollingManager.addAnimation(
                    new AnimatonScreenScroll(new ScreenScrolling(pathPlanning, from, to)));
        }
    }

    /**
     * It's called to redraw whole game. It should be called each game tick.
     */
    private void paint() {
        logger.debug("painting: " + visibleArea);
        paint(canvas.getCanvas().getGraphicsContext2D());
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
        paintColonies(g, gameModelController.getModel(), area);
        paintUnits(g, gameModelController.getModel(), area);
        paintSteps(g, area);
        paintAnimation(g, area);
        if (gameModelController.getCurrentPlayer().isComputer()) {
            /**
             * If move computer that make game field darker.
             */
            final Point topLeftPoint = area.convertToPoint(area.getTopLeft());
            final Point bottomRightPoint = area
                    .convertToPoint(area.getBottomRight().add(Location.of(1, 1)));
            final Point size = bottomRightPoint.substract(topLeftPoint);
            g.setFill(new Color(0, 0, 0, 0.34));
            g.fillRect(topLeftPoint.getX(), topLeftPoint.getY(), size.getX(), size.getY());
        }
        fpsCounter.screenWasPainted();
    }

    private void paintAnimation(final GraphicsContext graphics, final Area area) {
        if (animationManager.hasNextStep(area)) {
            animationManager.paint(graphics, area);
        }
        scrollingManager.paint();
    }

    /**
     * Draw main game tiles.
     * 
     * @param graphics
     *            required {@link GraphicsContext}
     */
    private void paintTerrain(final GraphicsContext graphics, final Area area) {
        final Player player = gameModelController.getCurrentPlayer();
        for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
            for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToPoint(location);
                if (player.isVisible(location)) {
                    final Terrain terrain = gameModelController.getModel().getMap()
                            .getTerrainAt(location);
                    paintService.paintTerrainOnTile(graphics, point, location, terrain,
                            oneTurnMoveHighlighter.isItHighlighted(location));
                } else {
                    final Image imageHidden = imageProvider.getImage(ImageProvider.IMG_TILE_HIDDEN);
                    graphics.drawImage(imageHidden, 0, 0, GamePanelView.TILE_WIDTH_IN_PX,
                            GamePanelView.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
                            GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);
                }
            }
        }
    }

    /**
     * Draw units.
     * <p>
     * Methods iterate through all location with ships, select first ship and draw
     * it.
     * </p>
     * 
     * @param graphics
     *            required {@link GraphicsContext}
     * @param game
     *            required {@link Game}
     */
    private void paintUnits(final GraphicsContext graphics, final Model model, final Area area) {
        final Map<Location, List<Unit>> ships = model.getUnitsAt();
        final Player player = gameModelController.getCurrentPlayer();

        final Map<Location, List<Unit>> ships2 = ships.entrySet().stream()
                .filter(entry -> area.isUnitVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        ships2.forEach((location, list) -> {
            final Unit unit = list.stream().findFirst().get();
            final Point point = area.convertToPoint(location);
            if (excludePainting.isUnitIncluded(unit)) {
                paintService.paintUnit(graphics, point, unit);
            }
        });
    }

    private void paintColonies(final GraphicsContext graphics, final Model model, final Area area) {
        final Player player = gameModelController.getCurrentPlayer();
        final Map<Location, Colony> colonies = model.getColoniesAt().entrySet().stream()
                .filter(entry -> area.isUnitVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        colonies.forEach((location, colony) -> {
            final Point point = area.convertToPoint(location);
            paintService.paintColony(graphics, point, colony, true);
        });

    }

    private void paintGrid(final GraphicsContext graphics, final Area area) {
        if (gamePreferences.isGridShown()) {
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

    private void drawNetLine(final GraphicsContext graphics, final Area area, final Location l_1,
            final Location l_2) {
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
    private void paintCursor(final GraphicsContext graphics, final Area area,
            final Location location) {
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
            if (!selectedUnit.isAtPlaceLocation()) {
                return;
            }
            paintCursor(graphics, area, mouseOverTileManager.getMouseOverTile().get());
            final List<Location> locations = moveModeSupport.getMoveLocations();
            final StepCounter stepCounter = new StepCounter(5, selectedUnit.getActionPoints());
            final List<Point> steps = Lists.transform(locations,
                    location -> area.convertToPoint(location));
            /**
             * Here could be check if particular step in on screen, but draw few images
             * outside screen is not big deal.
             */
            steps.forEach(point -> paintStep(graphics, point, stepCounter,
                    moveModeSupport.getMoveMode()));
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
    private void paintStep(final GraphicsContext graphics, final Point point,
            final StepCounter stepCounter, final MoveMode moveMode) {
        final Image image = imageProvider
                .getImage(moveMode.getImageForStep(stepCounter.canMakeMoveInSameTurn(1)));
        graphics.drawImage(image, point.getX(), point.getY());
    }

    public void setMoveModeOff() {
        oneTurnMoveHighlighter.setLocations(null);
        canvas.getCanvas().setCursor(Cursor.DEFAULT);
        modeController.setMoveMode(false);
    }

    public void setMoveModeOn() {
        canvas.getCanvas().setCursor(gotoModeCursor);
        modeController.setMoveMode(true);
    }

    public void addFightAnimation(final Unit attacker, final Unit defender) {
        animationManager.addAnimation(new AnimationFight(attacker.getLocation(),
                defender.getLocation(), imageProvider, gamePreferences.getAnimationSpeed()));
    }

    public boolean performFightDialog(final Unit unitAttacker, final Unit unitDefender) {
        dialogFigth.showAndWait(unitAttacker, unitDefender);
        return dialogFigth.isUserChooseFight();
    }

    public Area getArea() {
        return new Area(visibleArea, gameModelController.getModel().getMap());
    }

}
