package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.dialog.DialogFight;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.PathPlanning;
import org.microcol.model.Location;
import org.microcol.model.Unit;

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

    @Inject
    public GamePanelPainter(final GameModelController gameModelController,
            final PathPlanning pathPlanning, final ImageProvider imageProvider,
            final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager, final MoveModeSupport moveModeSupport,
            final GamePreferences gamePreferences, final MouseOverTileManager mouseOverTileManager,
            final AnimationManager animationManager, final ScrollingManager scrollingManager,
            final ModeController modeController, final ExcludePainting excludePainting,
            final DialogFight dialogFigth, final @Named("game") VisibleAreaService visibleArea,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final AnimationClouds animationClouds, final GamePaintService gamePaintService,
            final MapManager mapManager) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.moveModeSupport = Preconditions.checkNotNull(moveModeSupport);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.animationManager = Preconditions.checkNotNull(animationManager);
        this.scrollingManager = Preconditions.checkNotNull(scrollingManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.dialogFigth = Preconditions.checkNotNull(dialogFigth);
        this.visibleAreaService = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        this.animationClouds = Preconditions.checkNotNull(animationClouds);
        this.gamePaintService = Preconditions.checkNotNull(gamePaintService);
        this.mapManager = Preconditions.checkNotNull(mapManager);

    }

    void skipCenterViewAtLocation(final Location location) {
        visibleAreaService.setOnCanvasReady(ok -> {
            final Area area = getArea();
            visibleAreaService.setTopLeftPosionOfCanvas(area.getCanvasTopLeftForLocation(location));
        });
    }

    void planScrollingAnimationToLocation(final Location location) {
        visibleAreaService.setOnCanvasReady(state -> {
            planScrollingAnimationToPoint(getArea().getCenterToLocation(location));
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
            scrollingManager.addAnimation(
                    new AnimatonScreenScroll(new ScreenScrolling(pathPlanning, from, to)));
        }
    }

    /**
     * Paint everything.
     * 
     * @param g
     *            required graphics context
     * @param gameTick
     *            required long representing how many game ticks was already
     *            done. It allows to time animations.
     */
    void paint(final GraphicsContext g, final long currentGameTick) {
        final Area area = getArea();
        final CanvasInMapCoordinates canvasInMapCoordinates = CanvasInMapCoordinates
                .make(visibleAreaService, gameModelController.getMapSize());
        gamePaintService.paintTerrain(g, area, oneTurnMoveHighlighter, canvasInMapCoordinates,
                currentGameTick);
        gamePaintService.paintGrid(g, area, canvasInMapCoordinates);
        gamePaintService.paintSelectedTile(g, area, selectedTileManager.getSelectedTile());
        gamePaintService.paintColonies(g, gameModelController.getModel(), area);
        gamePaintService.paintUnits(g, gameModelController.getModel(), area, excludePainting,
                selectedUnitManager.getSelectedUnit());
        paintSteps(g, area);
        paintAnimation(g, area);
        animationClouds.paint(g);
        mapManager.gameTickUpdate(currentGameTick);
        if (gameModelController.getRealCurrentPlayer().isComputer()) {
            /**
             * If move computer that make game field darker.
             */
            g.setFill(new Color(0, 0, 0, 0.34));
            g.fillRect(0, 0, visibleAreaService.getCanvasSize().getX(),
                    visibleAreaService.getCanvasSize().getY());
        }
    }

    private void paintAnimation(final GraphicsContext graphics, final Area area) {
        if (animationManager.hasNextStep(area)) {
            animationManager.paint(graphics, area);
        }
        scrollingManager.paint();
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
            gamePaintService.paintCursor(graphics, area,
                    mouseOverTileManager.getMouseOverTile().get());
            final StepCounter stepCounter = new StepCounter(5, selectedUnit.getActionPoints());
            /**
             * Here could be check if particular step in on screen, but draw few
             * images outside screen is not big deal.
             */
            moveModeSupport.getMoveLocations().stream().map(area::convertToCanvasPoint)
                    .forEach(point -> gamePaintService.paintStep(graphics, point, stepCounter,
                            moveModeSupport.getMoveMode()));
        }
    }

    void addFightAnimation(final Unit attacker, final Unit defender) {
        animationManager.addAnimation(new AnimationFight(attacker.getLocation(),
                defender.getLocation(), imageProvider, gamePreferences.getAnimationSpeed()));
    }

    boolean performFightDialog(final Unit unitAttacker, final Unit unitDefender) {
        dialogFigth.showAndWait(unitAttacker, unitDefender);
        return dialogFigth.isUserChooseFight();
    }

    public Area getArea() {
        return new Area(visibleAreaService);
    }

}
