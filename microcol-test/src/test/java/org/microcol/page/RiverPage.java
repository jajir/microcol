package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.gui.util.PaintService;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.RiverSize;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * River checks through the real map canvas and the shared model. The page keeps
 * the ID of the one pioneer it captured, so every check reads that same unit
 * from the current model, also after Continue.
 */
public final class RiverPage extends AbstractScreen {

    private static final String MOVE_CONTROL_ID = "move";

    /** Canvas column inside the west half of a square, away from its edges. */
    private static final int[] WEST_HALF_COLUMNS = { 6, 14 };

    /** Canvas column inside the east half of a square, away from its edges. */
    private static final int[] EAST_HALF_COLUMNS = { 31, 39 };

    /** The pioneer under test, captured right after it reached the square. */
    private int capturedUnitId = -1;

    public RiverPage(final TestContext context) {
        super(context);
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

    /** Movement of the captured pioneer, exact to the third. */
    public void verifyUnitMovement(final int expectedThirds) {
        assertEquals(expectedThirds, unit().getMovementThirds(),
                "Unexpected movement in thirds of the captured pioneer");
    }

    public void verifyMoveControlVisible(final boolean expectedVisible) {
        assertEquals(expectedVisible, isCssIdVisible(MOVE_CONTROL_ID),
                "Unexpected move control visibility");
        closeUnitActions();
    }

    /**
     * Measure water across each connected half, allowing curvature and the
     * shared width transition at tile edges while distinguishing both sizes.
     *
     * @param location square whose painted river is checked
     * @param expected size the square must show
     * @param joined   east or west neighbours joined by the course
     */
    public void verifyRiverWidth(final Location location,
            final RiverSize expected, final Location... joined)
            throws Exception {
        // The map is repainted on its own tick, so give the frame time to show.
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> countMissingRiverPixels(location, expected,
                            joined) == 0);
        } catch (final TimeoutException timeout) {
            // The assertion below reports the pixels that never changed.
        }
        assertEquals(0, countMissingRiverPixels(location, expected, joined),
                "Sampled pixels do not show a " + expected + " river at "
                        + location);
    }

    private int countMissingRiverPixels(final Location location,
            final RiverSize expected, final Location... joined)
            throws Exception {
        final Image canvas = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        final int riverRgb = toRgb(PaintService.RIVER_COLOR);
        int missing = 0;
        for (final Location neighbour : joined) {
            for (final int x : sideColumns(location, neighbour)) {
                int water = 0;
                for (int y = 8; y <= 36; y++) {
                    if (isRiverPixel(canvas, tile, x, y, riverRgb)) {
                        water++;
                    }
                }
                final int minimum = expected == RiverSize.LARGE ? 8 : 3;
                final int maximum = expected == RiverSize.LARGE ? 16 : 8;
                if (water < minimum || water > maximum) {
                    missing++;
                }
            }
        }
        return missing;
    }

    /** The actual empty river square bends and retains readable water. */
    public void verifyCurvedCourse(final Location at) throws Exception {
        final Image canvas = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point tile = getContext().getArea().convertToCanvasPoint(at);
        final int riverRgb = toRgb(PaintService.RIVER_COLOR);
        double minimum = 45, maximum = 0;
        for (int x = 6; x <= 38; x += 4) {
            int top = 45, bottom = -1;
            for (int y = 8; y <= 36; y++) {
                if (isRiverPixel(canvas, tile, x, y, riverRgb)) {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
            assertTrue(bottom >= top,
                    "Connected river has a gap at column " + x);
            minimum = Math.min(minimum, (top + bottom) / 2.0);
            maximum = Math.max(maximum, (top + bottom) / 2.0);
        }
        assertTrue(maximum - minimum >= 1,
                "The course is still a straight line");
        javax.imageio.ImageIO.write(
                javafx.embed.swing.SwingFXUtils.fromFXImage(canvas, null),
                "png", new java.io.File(
                        "target/surefire-reports/river-hand-drawn.png"));
    }

    /**
     * Read the square's painted river on the side of the given neighbour and
     * check that no river pixel is there. A river toward a square the player
     * has not explored must stay off this square.
     *
     * @param location square whose painted river is checked
     * @param hidden   unexplored east or west neighbour
     */
    public void verifyNoRiverToward(final Location location,
            final Location hidden) throws Exception {
        // The map is repainted on its own tick, so give the frame time to show.
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> countRiverPixelsToward(location, hidden) == 0);
        } catch (final TimeoutException timeout) {
            // The assertion below reports the river pixels that remain.
        }
        assertEquals(0, countRiverPixelsToward(location, hidden),
                "River drawn toward unexplored " + hidden + " from "
                        + location);
    }

    private int countRiverPixelsToward(final Location location,
            final Location hidden) throws Exception {
        final Image canvas = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        final int riverRgb = toRgb(PaintService.RIVER_COLOR);
        int painted = 0;
        for (final int x : sideColumns(location, hidden)) {
            for (int y = 1; y < 44; y++) {
                if (isRiverPixel(canvas, tile, x, y, riverRgb)) {
                    painted++;
                }
            }
        }
        return painted;
    }

    /** Canvas columns inside the half of a square that faces a neighbour. */
    private static int[] sideColumns(final Location location,
            final Location neighbour) {
        return neighbour.getX() < location.getX() ? WEST_HALF_COLUMNS
                : EAST_HALF_COLUMNS;
    }

    /**
     * Reveal the map from the captured pioneer, as after its completed move.
     * Use it after forgetting exploration to start from the pioneer's own
     * sight.
     */
    public void observeCapturedUnit() {
        new FogPage(getContext()).observeFromUnit(capturedUnitId);
    }

    private static boolean isRiverPixel(final Image canvas, final Point tile,
            final int x, final int y, final int riverRgb) {
        return riverRgb == toRgb(canvas.getPixelReader()
                .getColor(tile.getX() + x, tile.getY() + y));
    }

    private static int toRgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }

    /**
     * Terrain output as the shared model reports it, so the river bonus is the
     * same value the colony preview and the next production use.
     *
     * @param location square checked
     * @param goods    goods type checked
     * @param expected exact output of the square
     */
    public void verifyOutput(final Location location, final GoodsType goods,
            final int expected) {
        assertEquals(expected,
                getModel().getMap().getTerrainAt(location)
                        .canProduceAmmount(goods),
                "Unexpected " + goods + " output at " + location);
    }

    public void useControl(final String id) {
        clickOnButtonWithId(id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyRejectedWork(final Location at, final int expectedTools,
            final int expectedThirds) {
        final UnitFreeColonist worker = (UnitFreeColonist) unit();
        assertEquals(expectedTools, worker.getTools());
        assertEquals(expectedThirds, worker.getMovementThirds());
        assertEquals(org.microcol.model.unit.UnitActionType.noAction,
                worker.getAction().getType());
        assertEquals(false, getModel().getColonyAt(at).isPresent());
        assertEquals(false, getModel().getMap().getTerrainAt(at).isHasField());
        closeUnitActions();
    }

    public void verifyDockRequired(final int colonyId, final Location at,
            final boolean expected) {
        final org.microcol.model.ColonyField field = getModel()
                .getColonyById(colonyId).orElseThrow().getColonyFields()
                .stream().filter(f -> f.getLocation().equals(at)).findFirst()
                .orElseThrow();
        assertEquals(expected, field.isMissingDock());
        assertEquals(false,
                getModel().getColonyById(colonyId).orElseThrow()
                        .wasConstructionBuilded(
                                org.microcol.model.ConstructionType.DOCK));
    }

    public void verifyRemainingDeposit(final Location at, final GoodsType goods,
            final int expected) {
        assertEquals(expected,
                getModel().getMap().getMineralRemainingAt(at, goods));
    }

    public void verifyBridge(final Location at) throws Exception {
        assertEquals(true, getModel().getMap().getTerrainAt(at).isHasRoad());
        assertEquals(false, getModel().getMap().getTerrainAt(at).isHasTrees());
        final Point tile = getContext().getArea().convertToCanvasPoint(at);
        final Image canvas = WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        int deckOutsideWater = 0;
        // A resource may cover the upper deck; the lower deck must cross the
        // bank.
        for (int y = 29; y < 37; y++) {
            for (int x = 8; x < 37; x++) {
                if (toRgb(canvas.getPixelReader().getColor(tile.getX() + x,
                        tile.getY() + y)) == toRgb(PaintService.ROAD_COLOR)) {
                    deckOutsideWater++;
                }
            }
        }
        javax.imageio.ImageIO.write(
                javafx.embed.swing.SwingFXUtils.fromFXImage(canvas, null),
                "png",
                new java.io.File("target/surefire-reports/river-bridge-"
                        + getModel().getMap().getResourceOverlayAt(at)
                        + ".png"));
        assertTrue(deckOutsideWater >= 12,
                "Bridge lower deck must cross the river bank: "
                        + deckOutsideWater);
    }
}
