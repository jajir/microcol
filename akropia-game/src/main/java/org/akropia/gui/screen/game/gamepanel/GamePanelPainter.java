package org.akropia.gui.screen.game.gamepanel;

import org.akropia.gui.Point;
import org.akropia.gui.StepCounter;
import org.akropia.gui.dialog.DialogFight;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.PathPlanning;
import org.akropia.model.Location;
import org.akropia.model.AttackOrder;
import org.akropia.model.MovementPoints;
import org.akropia.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * View for main game panel.
 */
public final class GamePanelPainter {

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final PathPlanning pathPlanning;

    private final SelectedTileManager selectedTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final AnimationManager animationManager;

    private final ScrollingManager scrollingManager;

    private final ExcludePainting excludePainting;

    private final GamePreferences gamePreferences;

    private final OneTurnMoveHighlighter oneTurnMoveHighlighter;

    private final MoveModeSupport moveModeSupport;

    private final VisibleAreaService visibleAreaService;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final DialogFight dialogFigth;

    private final AnimationClouds animationClouds;

    private final GamePaintService gamePaintService;

    private final MapManager mapManager;

    private final TradeRoutePlanning tradeRoutePlanning;

    private final TradeRoutePlanningPainter tradeRoutePlanningPainter;

    @Inject
    public GamePanelPainter(final GameModelController gameModelController,
            final PathPlanning pathPlanning, final ImageProvider imageProvider,
            final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager,
            final MoveModeSupport moveModeSupport,
            final GamePreferences gamePreferences,
            final MouseOverTileManager mouseOverTileManager,
            final AnimationManager animationManager,
            final ScrollingManager scrollingManager,
            final ModeController modeController,
            final ExcludePainting excludePainting,
            final DialogFight dialogFigth,
            final @Named("game") VisibleAreaService visibleArea,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final AnimationClouds animationClouds,
            final GamePaintService gamePaintService,
            final MapManager mapManager,
            final TradeRoutePlanning tradeRoutePlanning,
            final TradeRoutePlanningPainter tradeRoutePlanningPainter) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.selectedTileManager = Preconditions
                .checkNotNull(selectedTileManager);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.mouseOverTileManager = Preconditions
                .checkNotNull(mouseOverTileManager);
        this.animationManager = Preconditions.checkNotNull(animationManager);
        this.scrollingManager = Preconditions.checkNotNull(scrollingManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.dialogFigth = Preconditions.checkNotNull(dialogFigth);
        this.visibleAreaService = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions
                .checkNotNull(oneTurnMoveHighlighter);
        this.animationClouds = Preconditions.checkNotNull(animationClouds);
        this.gamePaintService = Preconditions.checkNotNull(gamePaintService);
        this.mapManager = Preconditions.checkNotNull(mapManager);
        this.tradeRoutePlanning = Preconditions
                .checkNotNull(tradeRoutePlanning);
        this.tradeRoutePlanningPainter = Preconditions
                .checkNotNull(tradeRoutePlanningPainter);
    }

    void skipCenterViewAtLocation(final Location location) {
        visibleAreaService.setOnCanvasReady(ok -> {
            final Area area = getArea();
            visibleAreaService.setTopLeftPosionOfCanvas(
                    area.getCanvasTopLeftForLocation(location));
        });
    }

    void planScrollingAnimationToLocation(final Location location) {
        visibleAreaService.setOnCanvasReady(state -> {
            planScrollingAnimationToPoint(
                    getArea().getCenterToLocation(location));
        });
    }

    private void planScrollingAnimationToPoint(final Point to) {
        final Point from = visibleAreaService.getTopLeft();
        if (from.distanceSimplified(to) > 0) {
            /**
             * Following precondition throws exception when scroll planning is
             * called before canvas was fully initialized. Method could be
             * called just after canvas full initialization.
             */
            Preconditions.checkState(visibleAreaService.isReady(),
                    "screen scroll is called before canvas initialization was finished.");
            scrollingManager.addAnimation(new AnimatonScreenScroll(
                    new ScreenScrolling(pathPlanning, from, to)));
        }
    }

    /**
     * Paint everything.
     *
     * @param g               required graphics context
     * @param currentGameTick required long representing how many game ticks was
     *                        already done. It allows to time animations.
     */
    void paint(final GraphicsContext g, final long currentGameTick) {
        final Area area = getArea();
        final CanvasInMapCoordinates canvasInMapCoordinates = CanvasInMapCoordinates
                .make(visibleAreaService, gameModelController.getMapSize());
        if (tradeRoutePlanning.isActive()) {
            paintTradeRoutePlanning(g, area, canvasInMapCoordinates,
                    currentGameTick);
            return;
        }
        gamePaintService.paintTerrain(g, area, canvasInMapCoordinates,
                currentGameTick);
        gamePaintService.paintRivers(g, area, canvasInMapCoordinates);
        gamePaintService.paintTerrainOverlays(g, area, oneTurnMoveHighlighter,
                canvasInMapCoordinates);
        gamePaintService.paintColonies(g, gameModelController.getModel(), area);
        gamePaintService.paintUnits(g, gameModelController.getModel(), area,
                excludePainting, selectedUnitManager.getSelectedUnit());
        paintSteps(g, area);
        paintAnimation(g, area);
        gamePaintService.paintGrid(g, area, canvasInMapCoordinates);
        gamePaintService.paintFog(g, area, canvasInMapCoordinates);
        gamePaintService.paintSelectedTile(g, area,
                selectedTileManager.getSelectedTile());
        animationClouds.paint(g);
        paintWorldBoundary(g);
        if (gameModelController.getRealCurrentPlayer().isComputer()) {
            /**
             * If move computer that make game field darker.
             */
            g.setFill(new Color(0, 0, 0, 0.34));
            g.fillRect(0, 0, visibleAreaService.getCanvasSize().getX(),
                    visibleAreaService.getCanvasSize().getY());
        }
    }

    /**
     * The planner shows the known map darkened, with towns and route paths on
     * top. Fog is painted last, so neither a route nor its pen wobble can show
     * an unexplored square. No movement preview or tile cursor is drawn.
     */
    private void paintTradeRoutePlanning(final GraphicsContext g,
            final Area area, final CanvasInMapCoordinates coordinates,
            final long currentGameTick) {
        gamePaintService.paintTerrain(g, area, coordinates, currentGameTick);
        gamePaintService.paintRivers(g, area, coordinates);
        gamePaintService.paintTerrainOverlays(g, area, oneTurnMoveHighlighter,
                coordinates);
        gamePaintService.paintUnits(g, gameModelController.getModel(), area,
                excludePainting, selectedUnitManager.getSelectedUnit());
        paintAnimation(g, area);
        paintWorldBoundary(g);
        tradeRoutePlanningPainter.darken(g,
                visibleAreaService.getCanvasSize().getX(),
                visibleAreaService.getCanvasSize().getY());
        gamePaintService.paintColonies(g, gameModelController.getModel(), area);
        tradeRoutePlanningPainter.paintRoutes(g, area);
        gamePaintService.paintFog(g, area, coordinates);
    }

    /**
     * A pen outline distinguishes the inaccessible margin from unexplored land.
     */
    private void paintWorldBoundary(final GraphicsContext g) {
        final Point start = visibleAreaService.getTopLeft();
        final Point world = visibleAreaService.getMapSize();
        final double x = -start.getX(), y = -start.getY();
        final double right = x + world.getX(), bottom = y + world.getY();
        g.save();
        g.setStroke(Color.BLACK);
        g.setLineWidth(2);
        g.beginPath();
        g.moveTo(x, y);
        g.quadraticCurveTo((x + right) / 2, y + .6, right, y);
        g.quadraticCurveTo(right - .6, (y + bottom) / 2, right, bottom);
        g.quadraticCurveTo((x + right) / 2, bottom - .6, x, bottom);
        g.quadraticCurveTo(x + .6, (y + bottom) / 2, x, y);
        g.stroke();
        g.restore();
    }

    private void paintAnimation(final GraphicsContext graphics,
            final Area area) {
        if (animationManager.hasNextStep(area)) {
            animationManager.paint(graphics, area);
        }
        scrollingManager.paint();
    }

    /**
     * When move mode is enabled mouse cursor is followed by highlighted path.
     *
     * @param graphics required {@link GraphicsContext}
     * @param area     required displayed area
     */
    private void paintSteps(final GraphicsContext graphics, final Area area) {
        if (modeController.isMoveMode()
                && mouseOverTileManager.getMouseOverTile().isPresent()) {
            final Unit selectedUnit = selectedUnitManager.getSelectedUnit()
                    .get();
            if (!selectedUnit.isAtPlaceLocation()) {
                return;
            }
            gamePaintService.paintCursor(graphics, area,
                    mouseOverTileManager.getMouseOverTile().get());
            final StepCounter stepCounter = new StepCounter(
                    MovementPoints.ofPoints(selectedUnit.getSpeed()),
                    selectedUnit.getMovementThirds());
            /**
             * Here could be check if particular step in on screen, but draw few
             * images outside screen is not big deal.
             */
            // Each step is priced from the square it leaves, as movement is.
            Location from = selectedUnit.getLocation();
            for (final Location location : moveModeSupport.getMoveLocations()) {
                gamePaintService.paintStep(graphics,
                        area.convertToCanvasPoint(location), stepCounter,
                        moveModeSupport.getMoveMode(),
                        selectedUnit.getEntryCostThirds(from, location));
                from = location;
            }
        }
    }

    void addFightAnimation(final Location attacker, final Location defender,
            final Runnable onFinished) {
        animationManager.addAnimation(
                new AnimationFight(attacker, defender, imageProvider,
                        gamePreferences.getAnimationSpeed()),
                animation -> onFinished.run());
    }

    boolean performFightDialog(final AttackOrder order) {
        return dialogFigth.showAndWait(order);
    }

    public Area getArea() {
        return new Area(visibleAreaService);
    }

}
