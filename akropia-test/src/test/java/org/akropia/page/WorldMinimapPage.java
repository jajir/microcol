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
        final Point2D[] target = new Point2D[1];
        getRobot().interact(() -> {
            final Node map = node("worldMinimap");
            map.getScene().getRoot().applyCss();
            map.getScene().getRoot().layout();
            target[0] = map.localToScreen(minimapPoint(xFraction, yFraction));
        });
        getRobot().moveTo(target[0]);
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

    private WritableImage snapshotCanvas(final Canvas canvas,
            final SnapshotParameters parameters) {
        final Region root = (Region) canvas.getScene().getRoot();
        final double width = canvas.getScene().getWidth();
        final double height = canvas.getScene().getHeight();
        final double preferredWidth = root.getPrefWidth();
        final double preferredHeight = root.getPrefHeight();
        try {
            // Node snapshots otherwise autosize the map root to its 100000px
            // preference.
            root.setPrefSize(width, height);
            return canvas.snapshot(parameters, null);
        } finally {
            root.setPrefSize(preferredWidth, preferredHeight);
            root.resize(width, height);
            root.layout();
        }
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
                    // Check painted geography independently of scaled sampling
                    // and the viewport marker drawn above this canvas.
                    return snapshotCanvas(canvas, new SnapshotParameters())
                            .getPixelReader()
                            .getColor((int) p.getX(), (int) p.getY())
                            .equals(Color.web(expectedColor));
                }).get());

    }

    public void verifyLayoutAndLanguage(final String accessibleName,
            final String hint) {
        getRobot().interact(() -> {
            final Node map = node("worldMinimap");
            final Bounds b = map.localToScene(map.getLayoutBounds());
            assertTrue(b.getWidth() > 0 && b.getWidth() <= 220.01);
            assertEquals(220.0 / 180, b.getWidth() / b.getHeight(), .01);
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
            assertTrue(getNodeFinder().lookup("#gameDifficulty").queryAll()
                    .isEmpty(), "The map has no difficulty label");
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
            final Node frame = node("floatingMapPanel");
            final Bounds side = frame.localToScene(frame.getLayoutBounds());
            assertTrue(b.getMinY() >= side.getMinY() + 10);
            assertTrue(b.getMaxX() <= side.getMaxX() - 10);
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
            final WritableImage image = snapshotCanvas(canvas, params);
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
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils
                            .asyncFx(() -> expected.equals(
                                    ((Label) node("statusBarLabel")).getText()))
                            .get());
        } catch (Exception failure) {
            // Keep the assertion below reporting the actual visible message.
        }
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

    private final org.akropia.gui.screen.game.gamepanel.ScrollingFrameProbe scrollProbe = new org.akropia.gui.screen.game.gamepanel.ScrollingFrameProbe();
    private Point scrollSample;

    public void captureScrollingFrame(final KeyCode direction, final int speed,
            final Location sample) {
        getRobot().interact(() -> {
            getContext().getClassFromGuice(
                    org.akropia.gui.preferences.PreferencesProperties.class)
                    .getMapScrollSpeedProperty().set(speed);
            getContext().getClassFromGuice(
                    org.akropia.gui.screen.game.gamepanel.GamePanelComponent.class)
                    .getCanvas().requestFocus();
        });
        getRobot().press(direction);
        try {
            getRobot().interact(() -> {
                scrollProbe.capture(getContext(), direction);
                scrollSample = getContext().getArea()
                        .convertToCanvasPoint(sample);
            });
        } finally {
            getRobot().release(direction);
        }
    }

    public void verifyScrollingFrame(final int dx, final int dy) {
        assertEquals(scrollProbe.before.add(Point.of(dx, dy)),
                scrollProbe.after,
                "The captured frame must include an actual scroll step");
        // The frontier crop includes both explored and unknown squares, but
        // excludes units and the independently moving cloud near the south.
        final int width = 4 * org.akropia.gui.Tile.TILE_WIDTH_IN_PX;
        final int height = org.akropia.gui.Tile.TILE_WIDTH_IN_PX;
        assertTrue(scrollSample.getX() >= 0 && scrollSample.getY() >= 0);
        assertTrue(scrollSample.getX() + width < scrollProbe.moving.getWidth());
        assertTrue(
                scrollSample.getY() + height < scrollProbe.moving.getHeight());
        for (int y = scrollSample.getY(); y < scrollSample.getY()
                + height; y++) {
            for (int x = scrollSample.getX(); x < scrollSample.getX()
                    + width; x++) {
                assertEquals(
                        scrollProbe.stationary.getPixelReader().getArgb(x, y),
                        scrollProbe.moving.getPixelReader().getArgb(x, y),
                        "Moving terrain and fog must match the stationary frame at "
                                + x + "," + y);
            }
        }
    }

    public void togglePlanner() {
        getRobot().clickOn(node("tradeRoutePlanningToggle"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void scrollPanel(final String scrollId) {
        getRobot().moveTo(node(scrollId)).scroll(20,
                javafx.geometry.VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void clickPanelBorder() {
        final Bounds b = node("floatingMapPanel")
                .localToScreen(node("floatingMapPanel").getLayoutBounds());
        getRobot().clickOn(b.getMinX() + 5, b.getCenterY());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void centerSelection() {
        getRobot().clickOn(node("center"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyFloatingPanel(final boolean planner) {
        getRobot().interact(() -> {
            final Region panel = (Region) node("floatingMapPanel");
            final Bounds b = panel.localToScene(panel.getLayoutBounds());
            final Node canvas = node("canvas");
            final Bounds map = canvas.localToScene(canvas.getLayoutBounds());
            final double width = panel.getScene().getWidth();
            assertEquals(width, map.getWidth(), 1);
            assertEquals(260, b.getWidth(), 1);
            assertEquals((width + Math.min(width, 1440)) / 2, b.getMaxX(), 1);
            assertEquals(16, b.getMinY() - map.getMinY(), 1);
            assertEquals(16, map.getMaxY() - b.getMaxY(), 1);
            assertNotNull(panel.getShape());
            assertEquals(Color.BLACK,
                    panel.getBorder().getStrokes().get(0).getTopStroke());
            assertEquals(2,
                    panel.getBorder().getStrokes().get(0).getWidths().getTop());
            final Node mini = node("worldMinimap");
            final Bounds m = mini.localToScene(mini.getLayoutBounds());
            assertTrue(m.getHeight() > 60 && m.getWidth() <= 220.01,
                    "Minimap remains usable in " + b + ": " + m);
            assertTrue(b.contains(m));
            final javafx.scene.control.ScrollPane scroll = (javafx.scene.control.ScrollPane) node(
                    planner ? "tradeRoutePlanningScroll" : "mapDetailsScroll");
            assertEquals(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER,
                    scroll.getHbarPolicy());
            final Bounds content = scroll
                    .localToScene(scroll.getLayoutBounds());
            assertTrue(m.getMaxY() < content.getMinY());
            assertTrue(b.contains(content));
        });
    }

    public void verifyCameraUnchanged() {
        getRobot().interact(() -> assertEquals(savedView, view().getTopLeft()));
    }

    public void verifySelectionCenteredOutsidePanel() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> {
                    final Location selected = getContext().getClassFromGuice(
                            org.akropia.gui.screen.game.gamepanel.SelectedTileManager.class)
                            .getSelectedTile().orElseThrow();
                    final Point point = new org.akropia.gui.screen.game.gamepanel.Area(
                            view()).convertToCanvasPoint(selected);
                    return Math.abs(
                            point.getX() - view().getFocusWidth() / 2) <= 1;
                }).get());
        getRobot().interact(() -> {
            final Location selected = getContext().getClassFromGuice(
                    org.akropia.gui.screen.game.gamepanel.SelectedTileManager.class)
                    .getSelectedTile().orElseThrow();
            final Point point = new org.akropia.gui.screen.game.gamepanel.Area(
                    view()).convertToCanvasPoint(selected);
            final Node canvas = node("canvas");
            final Point2D screen = canvas.localToScene(point.getX() + 22,
                    point.getY() + 22);
            final Node panel = node("floatingMapPanel");
            assertFalse(panel.localToScene(panel.getLayoutBounds())
                    .contains(screen));
        });
    }

}
