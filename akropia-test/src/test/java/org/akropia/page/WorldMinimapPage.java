package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.akropia.gui.Point;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.model.Location;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Player navigation and visible evidence; no assertions in action helpers. */
public final class WorldMinimapPage extends AbstractScreen {
    private String savedState;
    private Point savedView;
    private java.util.Optional<Location> savedSelection;

    public WorldMinimapPage(final TestContext context) {
        super(context);
    }

    private Node node(final String id) {
        return getNodeFinder().lookup("#" + id).query();
    }

    private VisibleAreaService view() {
        return getAkropia().getInjector().getInstance(
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
            savedSelection = getContext().getClassFromGuice(
                    org.akropia.gui.screen.game.gamepanel.SelectedTileManager.class)
                    .getSelectedTile();
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

    public void verifySelectionUnchanged() {
        getRobot().interact(() -> assertEquals(savedSelection,
                getContext().getClassFromGuice(
                        org.akropia.gui.screen.game.gamepanel.SelectedTileManager.class)
                        .getSelectedTile()));
    }

    public void verifyCentered(final double xFraction, final double yFraction)
            throws Exception {
        // Native motion events can arrive after the JavaFX queue was drained.
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> {
                    final Point size = view().getCanvasSize();
                    final Point start = view().getTopLeft();
                    final double x = Math.max(0, Math.min(1, xFraction))
                            * getModel().getMap().getMaxLocationX() * 45;
                    final double y = Math.max(0, Math.min(1, yFraction))
                            * getModel().getMap().getMaxLocationY() * 45;
                    return Math
                            .abs(start.getX() + size.getX() / 2
                                    - x) <= 1 / scale() + 1
                            && Math.abs(start.getY() + size.getY() / 2 - y) <= 1
                                    / scale() + 1;
                }).get());
        getRobot().interact(() -> {
            final Point size = view().getCanvasSize();
            final int width = getModel().getMap().getMaxLocationX() * 45;
            final int height = getModel().getMap().getMaxLocationY() * 45;
            final double x = Math.max(0, Math.min(1, xFraction)) * width
                    - size.getX() / 2;
            final double y = Math.max(0, Math.min(1, yFraction)) * height
                    - size.getY() / 2;
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
                            && Math.abs(frame.getWidth() - (Math.min(
                                    start.getX() + size.getX(),
                                    getModel().getMap().getMaxLocationX() * 45)
                                    - Math.max(0, start.getX()))
                                    * scale()) < .01;
                }).get());
        getRobot().interact(() -> {
            final Rectangle frame = (Rectangle) node("minimapViewport");
            final Point2D origin = minimapPoint(0, 0);
            assertEquals(
                    origin.getY()
                            + Math.max(0, view().getTopLeft().getY()) * scale(),
                    frame.getY(), .01);
            assertEquals(
                    (Math.min(
                            view().getTopLeft().getY()
                                    + view().getCanvasSize().getY(),
                            getModel().getMap().getMaxLocationY() * 45)
                            - Math.max(0, view().getTopLeft().getY()))
                            * scale(),
                    frame.getHeight(), .01);
            assertEquals(Color.web("#fff9ec"), frame.getStroke());
            assertEquals(2, frame.getStrokeWidth());
        });
    }

    public void clickOutsideWorld() {
        final Canvas canvas = getContext().getClassFromGuice(
                org.akropia.gui.screen.game.gamepanel.GamePanelComponent.class)
                .getCanvas();
        // One pixel beyond the west edge must not round into the first tile.
        getRobot().clickOn(canvas.localToScreen(-view().getTopLeft().getX() - 1,
                canvas.getHeight() / 2));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyUnknownMargin() {
        getRobot().interact(() -> {
            final Canvas canvas = getContext().getClassFromGuice(
                    org.akropia.gui.screen.game.gamepanel.GamePanelComponent.class)
                    .getCanvas();
            final Point2D origin = canvas.localToScene(0, 0);
            final WritableImage image = snapshotScene(canvas.getScene());
            assertEquals(Color.web("#9B9486"), image.getPixelReader().getColor(
                    (int) origin.getX() + 10, (int) origin.getY() + 10));
            assertTrue(view().getTopLeft().getX() < 0,
                    "West margin: " + view().getTopLeft());
            assertTrue(view().getTopLeft().getY() < 0,
                    "North margin: " + view().getTopLeft());
        });
    }

    public void verifyHamburgerMenu() {
        getRobot().interact(() -> {
            final javafx.scene.control.Button menu = (javafx.scene.control.Button) node(
                    "exit");
            final javafx.scene.image.ImageView icon = (javafx.scene.image.ImageView) menu
                    .getGraphic();
            assertTrue(
                    icon.getImage().getUrl().endsWith("/menu-hamburger.png"));
            assertEquals(32, icon.getFitWidth());
            assertEquals(32, icon.getFitHeight());
            assertEquals("Menu", menu.getText());
            assertFalse(menu.isDisabled());
            assertTrue(menu.isFocusTraversable());
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

    public void verifyLayoutAndLanguage(final String accessibleName,
            final String hint) {
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
            assertEquals(accessibleName, map.getAccessibleText());
            assertEquals(hint, map.getAccessibleHelp());
            assertTrue(getNodeFinder().lookup("#minimapTitle").queryAll()
                    .isEmpty(), "The minimap has no visible heading");
            final Node panel = node("rightPanel");
            assertTrue(
                    panel.lookupAll(".label").stream()
                            .noneMatch(label -> accessibleName
                                    .equals(((Label) label).getText())),
                    "No heading text remains in the right panel");
            // The map starts at the right panel's normal 12 px top padding,
            // with no extra space of its own.
            assertEquals(12, ((Region) panel).getPadding().getTop(), .01);
            final Bounds side = panel.localToScene(panel.getLayoutBounds());
            assertEquals(side.getMinY() + 12, b.getMinY(), .5);
            assertTrue(
                    getNodeFinder().lookup("#minimapHint").queryAll().isEmpty(),
                    "The instruction is accessible help, not a visible footer");
            assertTrue(map.isFocusTraversable());
        });
    }

    public void verifyMapFocus(final boolean focused) {
        getRobot().interact(() -> {
            assertEquals(focused, node("worldMinimap").isFocused());
            // Focus adds no outer frame; only the viewport marker remains.
            final Region map = (Region) node("worldMinimap");
            assertTrue(getNodeFinder().lookup("#minimapFocus").tryQuery()
                    .isEmpty(), "Focus must not add a frame node");
            assertTrue(map.getBorder() == null || map.getBorder().isEmpty(),
                    "Focus must not add a border");
            final Bounds viewport = node("minimapViewport").getBoundsInParent();
            for (Node child : map.getChildrenUnmodifiable()) {
                if (child instanceof Rectangle) {
                    assertEquals(viewport.getCenterX(),
                            child.getBoundsInParent().getCenterX(), .01,
                            "Only viewport rectangles are drawn");
                    assertEquals(viewport.getCenterY(),
                            child.getBoundsInParent().getCenterY(), .01,
                            "Only viewport rectangles are drawn");
                }
            }
            final Canvas canvas = (Canvas) node("minimapTerrain");
            final SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            final WritableImage image = canvas.snapshot(params, null);
            for (int x = 0; x < image.getWidth(); x++) {
                assertEquals(0,
                        image.getPixelReader().getColor(x, 1).getOpacity(),
                        "Focus must not draw an outer frame");
            }
        });
    }

    public void hoverMinimap() {
        movePointer(.5, .5);
    }

    /** Moves the pointer from the minimap to the tile picture below it. */
    public void leaveMinimap() {
        getRobot().moveTo(node("tileImage"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyStatusMessage(final String expected) {
        getRobot().interact(() -> {
            final Label status = (Label) node("statusBarLabel");
            assertEquals(expected, status.getText());
            final javafx.scene.text.Text text = (javafx.scene.text.Text) status
                    .lookup(".text");
            assertEquals(expected, text.getText(),
                    "Complete status message is rendered");
        });
    }

    /** Leaving the minimap restores the right panel's ordinary help. */
    public void verifyRightPanelStatus() {
        verifyStatusMessage(
                getContext().getClassFromGuice(org.akropia.i18n.I18n.class)
                        .get(org.akropia.gui.Loc.rightPanel_description));
    }

    public void verifyFocusLeftMinimap() {
        getRobot()
                .interact(() -> assertFalse(node("worldMinimap").isFocused()));
    }
}
