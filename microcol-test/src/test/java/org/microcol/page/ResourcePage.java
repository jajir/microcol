package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.FogFrameProbe;
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
        final Image icon = getContext().getClassFromGuice(ImageProvider.class)
                .getResourceImage(expected);
        int opaquePixels = 0;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                if (icon.getPixelReader().getColor(x, y).getOpacity() == 1) {
                    opaquePixels++;
                }
            }
        }
        final int minimumMatching = (int) (opaquePixels * .95);
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> matchingResourcePixels(location,
                            icon) >= minimumMatching);
        } catch (final TimeoutException timeout) {
            // The verification below reports the missing selected picture.
        }
        assertTrue(opaquePixels > 50, "Resource has a readable silhouette");
        assertTrue(matchingResourcePixels(location, icon) >= minimumMatching,
                "Selected " + expected + " picture is painted at " + location);
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
        final Point tile = getContext().getArea().convertToCanvasPoint(hidden);
        final int fogRgb = toRgb(Color.web("#9B9486"));
        for (int y = 1; y < Tile.TILE_WIDTH_IN_PX - 1; y++) {
            for (int x = 1; x < Tile.TILE_WIDTH_IN_PX - 1; x++) {
                assertEquals(fogRgb,
                        toRgb(canvas.getPixelReader().getColor(tile.getX() + x,
                                tile.getY() + y)),
                        "Unexplored square reveals no picture at " + hidden);
            }
        }
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

    /**
     * Remaining bonus units of a finite deposit in the shared model, the same
     * reserve that the next update draws on.
     *
     * @param location square checked
     * @param goods    silver or ore of the deposit
     * @param expected units left
     */
    public void verifyMineralRemaining(final Location location,
            final GoodsType goods, final int expected) {
        assertEquals(expected,
                getModel().getMap().getMineralRemainingAt(location, goods),
                "Unexpected remaining " + goods + " units at " + location);
    }

    private int matchingResourcePixels(final Location location,
            final Image icon) throws Exception {
        final Image canvas = captureCanvas();
        final Point tile = getContext().getArea()
                .convertToCanvasPoint(location);
        int matching = 0;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                final Color expected = icon.getPixelReader().getColor(x, y);
                if (expected.getOpacity() == 1
                        && toRgb(expected) == toRgb(canvas.getPixelReader()
                                .getColor(tile.getX() + x, tile.getY() + y))) {
                    matching++;
                }
            }
        }
        return matching;
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
