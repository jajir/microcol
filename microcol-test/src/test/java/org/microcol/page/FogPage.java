package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.microcol.gui.screen.game.gamepanel.MoveModeSupport;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/** Inspect exploration through the actual map and its information controls. */
public final class FogPage extends AbstractScreen {
    private Location previewOrigin;
    private final org.microcol.gui.screen.game.gamepanel.FogFrameProbe probe = new org.microcol.gui.screen.game.gamepanel.FogFrameProbe();

    public FogPage(final TestContext context) {
        super(context);
    }

    private Point2D screenPoint(final Location location) {
        final Canvas canvas = getContext()
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location).add(Tile.TILE_CENTER);
        return canvas.localToScreen(point.getX(), point.getY());
    }

    public void centerFrontier() throws Exception {
        final Location center = Location.of(29, 16);
        getRobot().press(KeyCode.CONTROL).clickOn(screenPoint(center))
                .release(KeyCode.CONTROL);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
            final Canvas canvas = getContext()
                    .getClassFromGuice(GamePanelComponent.class).getCanvas();
            return Math.abs(
                    getContext().getArea().convertToCanvasPoint(center).getX()
                            - canvas.getWidth() / 2) < 3;
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void inspect(final Location location) {
        getRobot().clickOn(screenPoint(location));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void preview(final Location from, final Location target)
            throws Exception {
        previewOrigin = from;
        getRobot().moveTo(screenPoint(from)).press(MouseButton.PRIMARY)
                .moveTo(screenPoint(target));
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getContext().getClassFromGuice(
                        org.microcol.gui.screen.game.gamepanel.MouseOverTileManager.class)
                        .getMouseOverTile().filter(target::equals).isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelPreview() {
        getRobot().moveTo(screenPoint(previewOrigin))
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void move(final Location from, final Location to, final int unitId)
            throws Exception {
        getRobot().moveTo(screenPoint(from)).press(MouseButton.PRIMARY)
                .moveTo(screenPoint(to)).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).getLocation().equals(to));
        GamePage.of(getContext()).waitWhileMoving();
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyExplored(final Location location,
            final boolean expected) {
        assertEquals(expected,
                getModel().getPlayerByName("Dutch").isVisible(location));
    }

    public void verifyUnknownInformation(final String expectedText,
            final String hiddenName) {
        GamePage.of(getContext()).verifyThatStatusBarContains(expectedText);
        GamePage.of(getContext()).verifyNumberOfUnitInRightPanel(0);
        assertFalse(getNodeFinder().lookup("#rightPanel").lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Labeled
                        && ((Labeled) node).getText().contains(hiddenName))
                .tryQuery().isPresent());
        assertTrue(getNodeFinder().lookup("#rightPanel").lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Labeled
                        && ((Labeled) node).getText().equals("Unknown"))
                .tryQuery().isPresent());
    }

    public void verifyPreviewEmpty() {
        assertTrue(
                getContext().getClassFromGuice(MoveModeSupport.class)
                        .getMoveLocations().isEmpty(),
                () -> "Preview path: "
                        + getContext().getClassFromGuice(
                                MoveModeSupport.class).getMoveLocations()
                        + "; pointer: "
                        + getContext().getClassFromGuice(
                                org.microcol.gui.screen.game.gamepanel.MouseOverTileManager.class)
                                .getMouseOverTile()
                        + "; explored enemy: "
                        + getModel().getPlayerByName("Dutch")
                                .isVisible(Location.of(31, 16)));
    }

    public void verifyPreviewIncludes(final Location destination) {
        assertTrue(getContext().getClassFromGuice(MoveModeSupport.class)
                .getMoveLocations().contains(destination));
    }

    public void verifyUnitLocation(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    public void drawDiagnosticAnimation(final Location known,
            final Location hidden) {
        getRobot().interact(() -> probe.paint(getContext(), known, hidden));
    }

    public void verifyAnimationCoveredOnlyInFog(final Location known,
            final Location hidden) {
        final Point knownPoint = getContext().getArea()
                .convertToCanvasPoint(known).add(Tile.TILE_CENTER);
        final Point hiddenPoint = getContext().getArea()
                .convertToCanvasPoint(hidden).add(Tile.TILE_CENTER);
        assertTrue(probe.isCompleted(),
                "Hidden animation must still finish and release the turn");
        assertEquals(javafx.scene.paint.Color.MAGENTA,
                probe.getFrame().getPixelReader().getColor(knownPoint.getX(),
                        knownPoint.getY()));
        assertNotEquals(javafx.scene.paint.Color.MAGENTA,
                probe.getFrame().getPixelReader().getColor(hiddenPoint.getX(),
                        hiddenPoint.getY()));
    }

    public void verifyFogPixels(final Location location) throws Exception {
        final Canvas canvas = getContext()
                .getClassFromGuice(GamePanelComponent.class).getCanvas();
        final Image actual = WaitForAsyncUtils.asyncFx(
                () -> org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                        .captureCanvas(getContext()))
                .get(10, TimeUnit.SECONDS);
        final Image hidden = getContext().getClassFromGuice(ImageProvider.class)
                .getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN);
        final Point point = getContext().getArea()
                .convertToCanvasPoint(location);
        // Interior pixels avoid the independently drawn grid and selection
        // border.
        for (int y = 12; y < Tile.TILE_WIDTH_IN_PX - 12; y += 8) {
            for (int x = 12; x < Tile.TILE_WIDTH_IN_PX - 12; x += 8) {
                assertEquals(hidden.getPixelReader().getArgb(x, y),
                        actual.getPixelReader().getArgb(point.getX() + x,
                                point.getY() + y),
                        "Hidden tile " + location);
            }
        }
    }
}
