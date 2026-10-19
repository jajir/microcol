package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
import org.microcol.gui.util.PaintService;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.ResourceOverlay;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Special-resource checks through the real map canvas, the right panel and the
 * shared model. The marker centre is read at the position PaintService uses, in
 * the top-right part of a square.
 */
public final class ResourcePage extends AbstractScreen {

    private static final String TILE_NAME_ID = "tileName";

    /** Marker centre across a square: 80% of its width. */
    private static final int MARKER_CENTRE_X = (int) (Tile.TILE_WIDTH_IN_PX
            * 0.80);

    /** Marker centre down a square: 20% of its height. */
    private static final int MARKER_CENTRE_Y = (int) (Tile.TILE_WIDTH_IN_PX
            * 0.20);

    public ResourcePage(final TestContext context) {
        super(context);
    }

    /**
     * Read the painted marker of a known square and check its colour, which is
     * unique to the kind. The map is repainted on its own tick, so the page
     * waits for the frame.
     *
     * @param location square whose marker is checked
     * @param expected kind the marker must show
     */
    public void verifyMarkerPainted(final Location location,
            final ResourceOverlay expected) throws Exception {
        final int expectedRgb = toRgb(
                PaintService.resourceMarkerColor(expected));
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> markerRgb(location) == expectedRgb);
        } catch (final TimeoutException timeout) {
            // The assertion below reports the colour that remained.
        }
        assertEquals(expectedRgb, markerRgb(location),
                "Unexpected " + expected + " marker at " + location);
    }

    /**
     * Read the painted position of an unexplored square and check that it shows
     * the fog image, so the square reveals no resource.
     *
     * @param hidden square that the player has not explored
     */
    public void verifyNoMarkerOnHiddenSquare(final Location hidden)
            throws Exception {
        final Image canvas = captureCanvas();
        final Image fog = getContext().getClassFromGuice(ImageProvider.class)
                .getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN);
        final Point tile = getContext().getArea().convertToCanvasPoint(hidden);
        assertEquals(
                fog.getPixelReader().getArgb(MARKER_CENTRE_X, MARKER_CENTRE_Y),
                canvas.getPixelReader().getArgb(tile.getX() + MARKER_CENTRE_X,
                        tile.getY() + MARKER_CENTRE_Y),
                "Unexplored square shows a resource marker at " + hidden);
    }

    /**
     * Check the name line of the inspected square in the real right panel.
     *
     * @param expectedName localized name of the resource as the player reads it
     */
    public void verifyInspectedName(final String expectedName) {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(
                getLabeledById(TILE_NAME_ID).getText().contains(expectedName),
                () -> "Unexpected inspection text: "
                        + getLabeledById(TILE_NAME_ID).getText());
    }

    /**
     * Check that the inspected unexplored square does not name its resource.
     *
     * @param hiddenName localized name that must not appear
     */
    public void verifyInspectionOmits(final String hiddenName) {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(getLabeledById(TILE_NAME_ID).getText().contains(hiddenName),
                () -> "Unexplored inspection names a resource: "
                        + getLabeledById(TILE_NAME_ID).getText());
    }

    /**
     * Check the resource identity of a square in the current model.
     *
     * @param location square checked
     * @param expected stored resource kind
     */
    public void verifyResource(final Location location,
            final ResourceOverlay expected) {
        assertEquals(expected,
                getModel().getMap().getTerrainAt(location).getResourceOverlay(),
                "Unexpected resource at " + location);
    }

    /**
     * Terrain output as the shared model reports it, the same value the colony
     * preview and the next production use.
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

    private int markerRgb(final Location location) throws Exception {
        final Image canvas = captureCanvas();
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        return toRgb(canvas.getPixelReader().getColor(
                tile.getX() + MARKER_CENTRE_X, tile.getY() + MARKER_CENTRE_Y));
    }

    private Image captureCanvas() throws Exception {
        return WaitForAsyncUtils
                .asyncFx(() -> FogFrameProbe.captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
    }

    private static int toRgb(final Color color) {
        return (int) Math.round(color.getRed() * 255) << 16
                | (int) Math.round(color.getGreen() * 255) << 8
                | (int) Math.round(color.getBlue() * 255);
    }
}
