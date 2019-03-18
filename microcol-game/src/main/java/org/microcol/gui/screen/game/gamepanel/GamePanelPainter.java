package org.microcol.gui.screen.game.gamepanel;

import java.util.List;

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
import com.google.common.collect.Lists;
import com.google.inject.Inject;

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

    private final VisibleArea visibleArea;

    private final MouseOverTileManager mouseOverTileManager;

    private final ModeController modeController;

    private final DialogFight dialogFigth;

    private final GamePaintService gamePaintService;

    @Inject
    public GamePanelPainter(final GameModelController gameModelController,
            final PathPlanning pathPlanning, final ImageProvider imageProvider,
            final SelectedTileManager selectedTileManager,
            final SelectedUnitManager selectedUnitManager, final MoveModeSupport moveModeSupport,
            final GamePreferences gamePreferences, final MouseOverTileManager mouseOverTileManager,
            final AnimationManager animationManager, final ScrollingManager scrollingManager,
            final ModeController modeController, final ExcludePainting excludePainting,
            final DialogFight dialogFigth, final VisibleArea visibleArea,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final GamePaintService gamePaintService) {
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
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        this.oneTurnMoveHighlighter = Preconditions.checkNotNull(oneTurnMoveHighlighter);
        this.gamePaintService = Preconditions.checkNotNull(gamePaintService);

    }

    public void skipCenterViewAtLocation(final Location location) {
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
             * Following precondition throws exception when scroll planning is
             * called before canvas was fully initialized. Method could be
             * called just after canvas full initialization.
             */
            Preconditions.checkState(visibleArea.isReady(),
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
     */
    public void paint(final GraphicsContext g) {
        final Area area = getArea();
        gamePaintService.paintTerrain(g, area, oneTurnMoveHighlighter);
        gamePaintService.paintGrid(g, area);
        gamePaintService.paintSelectedTile(g, area, selectedTileManager.getSelectedTile());
        gamePaintService.paintColonies(g, gameModelController.getModel(), area);
        gamePaintService.paintUnits(g, gameModelController.getModel(), area, excludePainting);
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
            final List<Location> locations = moveModeSupport.getMoveLocations();
            final StepCounter stepCounter = new StepCounter(5, selectedUnit.getActionPoints());
            final List<Point> steps = Lists.transform(locations,
                    location -> area.convertToPoint(location));
            /**
             * Here could be check if particular step in on screen, but draw few
             * images outside screen is not big deal.
             */
            steps.forEach(point -> gamePaintService.paintStep(graphics, point, stepCounter,
                    moveModeSupport.getMoveMode()));
        }
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
