package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionBuildRoad;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Real road controls with explicit model and visible results. The page keeps
 * the ID of the one pioneer it captured, so every check reads that same unit
 * from the current model, also after Continue.
 */
public final class RoadPage extends AbstractScreen {

    private static final String BUILD_CONTROL_ID = "buildRoad";

    private static final String CANCEL_CONTROL_ID = "cancelBuildRoad";

    private static final String MOVE_CONTROL_ID = "move";

    private static final String PROGRESS_ID_PREFIX = "unitRoadWork-";

    /** The pioneer under test, captured right after it reached the square. */
    private int capturedUnitId = -1;

    public RoadPage(final TestContext context) {
        super(context);
    }

    /** Use the visible control; the road starts at once, without a question. */
    public void useControl() {
        clickOnButtonWithId(BUILD_CONTROL_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Use the separate control that discards unfinished road work. */
    public void cancelWork() {
        clickOnButtonWithId(CANCEL_CONTROL_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Remember the one human pioneer standing on the square. Call it once, when
     * the test has just moved that pioneer there.
     *
     * @param location square where the pioneer stands
     */
    public void captureUnit(final Location location) {
        capturedUnitId = getModel().getUnitsAt(location).stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && unit.getOwner().isHuman())
                .findFirst().orElseThrow().getId();
    }

    /** The captured pioneer as the current model holds it. */
    private Unit unit() {
        return getModel().getUnitById(capturedUnitId);
    }

    public void verifyCapturedUnitAt(final Location location) {
        assertEquals(location, unit().getLocation(),
                "Unexpected location of the captured pioneer");
    }

    public void verifyControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isCssIdVisible(BUILD_CONTROL_ID),
                "Unexpected build road control visibility");
        closeUnitActions();
    }

    public void verifyCancelControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isCssIdVisible(CANCEL_CONTROL_ID),
                "Unexpected cancel road control visibility");
        closeUnitActions();
    }

    public void verifyMoveControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isCssIdVisible(MOVE_CONTROL_ID),
                "Unexpected move control visibility");
        closeUnitActions();
    }

    public void verifyUnitWork(final Location location,
            final UnitActionType expectedAction, final int expectedThirds,
            final int expectedTools) {
        verifyCapturedUnitAt(location);
        assertEquals(expectedAction, unit().getAction().getType(),
                "Unexpected unit action");
        assertEquals(expectedThirds, unit().getMovementThirds(),
                "Unexpected unit movement in thirds");
        assertEquals(expectedTools, ((UnitFreeColonist) unit()).getTools(),
                "Unexpected unit tools");
    }

    public void verifyRemainingUpdates(final int expectedUpdates) {
        assertEquals(expectedUpdates,
                ((UnitActionBuildRoad) unit().getAction()).getRemainingTurns(),
                "Unexpected road countdown");
    }

    /** Movement of the captured pioneer, exact to the third. */
    public void verifyUnitMovement(final int expectedThirds) {
        assertEquals(expectedThirds, unit().getMovementThirds(),
                "Unexpected movement in thirds of the captured pioneer");
    }

    /**
     * The unit panel shows the owner updates left while the work runs; the
     * label is found by the captured pioneer's ID.
     *
     * @param expectedUpdates owner updates still needed
     */
    public void verifyRoadProgressShown(final int expectedUpdates) {
        final Label label = getNodeFinder()
                .lookup("#" + PROGRESS_ID_PREFIX + capturedUnitId)
                .queryAs(Label.class);
        assertEquals("Road work: " + expectedUpdates + " owner updates left",
                label.getText(), "Unexpected road progress text");
    }

    public void verifyRoadProgressHidden() {
        assertTrue(
                getNodeFinder()
                        .lookup("#" + PROGRESS_ID_PREFIX + capturedUnitId)
                        .tryQuery().isEmpty(),
                "Road progress must not be shown");
    }

    /**
     * Read the real map canvas and check that the road band is painted across
     * the square. The square must have no unit on it and must not be under the
     * mouse, because a unit or the highlight would cover the band. Sampled rows
     * lie fully inside the road surface, away from its dark edge and the tile
     * borders.
     *
     * @param location square whose painted road is checked
     */
    public void verifyRoadDrawn(final Location location) throws Exception {
        // The map is repainted on its own tick, so give the frame time to show.
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> countUnpaintedRoadPixels(location) == 0);
        } catch (final TimeoutException timeout) {
            // The assertion below reports the pixels that never changed.
        }
        assertEquals(0, countUnpaintedRoadPixels(location),
                "Sampled pixels without road surface at " + location);
    }

    private int countUnpaintedRoadPixels(final Location location)
            throws Exception {
        final Image canvas = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        final int expectedRgb = toRgb(PaintService.ROAD_COLOR);
        int unpainted = 0;
        for (final int y : new int[] { 21, 23 }) {
            for (final int x : new int[] { 6, 14, 31, 39 }) {
                if (expectedRgb != toRgb(canvas.getPixelReader()
                        .getColor(tile.getX() + x, tile.getY() + y))) {
                    unpainted++;
                }
            }
        }
        return unpainted;
    }

    private static int toRgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }

    public void verifyRoadAt(final Location location,
            final boolean expectedRoad) {
        assertEquals(expectedRoad,
                getModel().getMap().getTerrainAt(location).isHasRoad(),
                "Unexpected road at " + location);
    }
}
