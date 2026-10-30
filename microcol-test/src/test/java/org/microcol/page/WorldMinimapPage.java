package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.Point;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Player navigation and visible evidence; no assertions in action helpers. */
public final class WorldMinimapPage extends AbstractScreen {
    private String savedState;
    private Point savedView;

    public WorldMinimapPage(final TestContext context) {
        super(context);
    }

    private Node node(final String id) {
        return getNodeFinder().lookup("#" + id).query();
    }

    private VisibleAreaService view() {
        return getMicroCol().getInjector().getInstance(
                Key.get(VisibleAreaService.class, Names.named("game")));
    }

    private double scale() {
        return Math.min(212.0 / (getModel().getMap().getMaxLocationX() * 45),
                172.0 / (getModel().getMap().getMaxLocationY() * 45));
    }

    private Point2D minimapPoint(final double xFraction,
            final double yFraction) {
        final double w = getModel().getMap().getMaxLocationX() * 45 * scale();
        final double h = getModel().getMap().getMaxLocationY() * 45 * scale();
        return new Point2D((220 - w) / 2 + xFraction * w,
                (180 - h) / 2 + yFraction * h);
    }

    public void clickWorld(final double xFraction, final double yFraction) {
        final Point2D p = minimapPoint(xFraction, yFraction);
        getRobot().clickOn(node("worldMinimap").localToScreen(p));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void rememberGameAndView() {
        getRobot().interact(() -> {
            savedState = new Gson().toJson(getModel().save());
            savedView = view().getTopLeft();
        });
    }

    public void movePointer(final double xFraction, final double yFraction) {
        getRobot().moveTo(node("worldMinimap")
                .localToScreen(minimapPoint(xFraction, yFraction)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void beginDrag(final double xFraction, final double yFraction,
            final MouseButton button) {
        movePointer(xFraction, yFraction);
        getRobot().press(button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void endDrag(final MouseButton button) {
        getRobot().release(button);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void press(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyGameUnchanged() {
        getRobot().interact(() -> assertEquals(savedState,
                new Gson().toJson(getModel().save()),
                "Camera navigation does not mutate units, selection, turn, gold or exploration"));
    }

    public void verifyCentered(final double xFraction, final double yFraction) {
        getRobot().interact(() -> {
            final Point size = view().getCanvasSize();
            final int width = getModel().getMap().getMaxLocationX() * 45;
            final int height = getModel().getMap().getMaxLocationY() * 45;
            final double x = size.getX() >= width ? (width - size.getX()) / 2
                    : Math.max(0, Math.min(width - size.getX(),
                            width * xFraction - size.getX() / 2));
            final double y = size.getY() >= height ? (height - size.getY()) / 2
                    : Math.max(0, Math.min(height - size.getY(),
                            height * yFraction - size.getY() / 2));
            // Native pointer coordinates round to a minimap pixel.
            assertEquals(x, view().getTopLeft().getX(), 1 / scale() + 1);
            assertEquals(y, view().getTopLeft().getY(), 1 / scale() + 1);
        });
    }

    public void verifyPan(final int dx, final int dy) {
        getRobot().interact(() -> assertEquals(savedView.add(Point.of(dx, dy)),
                view().getTopLeft()));
    }

    public void verifyViewport() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> {
                    final Rectangle frame = (Rectangle) node("minimapViewport");
                    final Point start = view().getTopLeft();
                    final Point size = view().getCanvasSize();
                    final Point2D origin = minimapPoint(0, 0);
                    return Math
                            .abs(frame.getX() - origin.getX()
                                    - Math.max(0, start.getX()) * scale()) < .01
                            && Math.abs(frame.getWidth() - Math.min(size.getX(),
                                    getModel().getMap().getMaxLocationX() * 45)
                                    * scale()) < .01;
                }).get());
        getRobot().interact(() -> {
            final Rectangle frame = (Rectangle) node("minimapViewport");
            final Point2D origin = minimapPoint(0, 0);
            assertEquals(
                    origin.getY()
                            + Math.max(0, view().getTopLeft().getY()) * scale(),
                    frame.getY(), .01);
            assertEquals(Math.min(view().getCanvasSize().getY(),
                    getModel().getMap().getMaxLocationY() * 45) * scale(),
                    frame.getHeight(), .01);
            assertEquals(Color.web("#fff9ec"), frame.getStroke());
            assertEquals(2, frame.getStrokeWidth());
        });
    }

    public void verifyTerrain(final Location location,
            final String expectedColor, final boolean explored)
            throws Exception {
        assertEquals(explored,
                getContext().getClassFromGuice(GameModelController.class)
                        .getHumanPlayer().isVisible(location));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> {
                    final Canvas canvas = (Canvas) node("minimapTerrain");
                    final Point2D p = minimapPoint(
                            (location.getX() - .5)
                                    / getModel().getMap().getMaxLocationX(),
                            (location.getY() - .5)
                                    / getModel().getMap().getMaxLocationY());
                    final Point2D origin = canvas.localToScene(0, 0);
                    return snapshotScene(canvas.getScene()).getPixelReader()
                            .getColor((int) (origin.getX() + p.getX()),
                                    (int) (origin.getY() + p.getY()))
                            .equals(Color.web(expectedColor));
                }).get());

    }

    public void verifyLayoutAndLanguage(final String title, final String hint) {
        getRobot().interact(() -> {
            final Node map = node("worldMinimap");
            final Bounds b = map.localToScene(map.getLayoutBounds());
            assertEquals(220, b.getWidth(), .01);
            assertEquals(180, b.getHeight(), .01);
            assertTrue(b.getMinY() >= 0
                    && b.getMaxY() < map.getScene().getHeight());
            final Node tile = node("tileImage");
            assertTrue(b.getMaxY() < tile.localToScene(tile.getLayoutBounds())
                    .getMinY());
            final Label terrain = (Label) node("tileName");
            assertTrue(
                    terrain.getHeight() >= terrain
                            .prefHeight(terrain.getWidth()) - .5,
                    "Terrain name and movement cost must remain fully visible");
            final Node difficulty = node("gameDifficulty");
            assertTrue(difficulty.localToScene(difficulty.getLayoutBounds())
                    .getMaxY() <= map.getScene().getHeight());
            assertEquals(title, map.getAccessibleText());
            assertEquals(hint, map.getAccessibleHelp());
            final Label label = (Label) node("minimapHint");
            assertEquals(hint, label.getText());
            assertTrue(
                    label.getHeight() >= label.prefHeight(label.getWidth())
                            - .5,
                    "Minimap hint must have room for all wrapped lines");
            assertTrue(map.isFocusTraversable());
        });
    }

    public void verifyFocusLeftMinimap() {
        getRobot()
                .interact(() -> assertFalse(node("worldMinimap").isFocused()));
    }
}
