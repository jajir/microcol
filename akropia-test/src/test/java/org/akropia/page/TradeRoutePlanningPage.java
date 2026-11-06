package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.akropia.gui.screen.game.components.StatusBarView;
import org.akropia.gui.screen.game.gamepanel.TradeRoutePlanning;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRoutePreview;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.collect.Lists;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/**
 * Map trade route planner: the switch in the map's upper-left corner, the New
 * trade route button of the game bar and the planner's right panel. Routes are
 * chosen in the visible list with the keyboard or by clicking the map, as a
 * player would; the model and the planner state are only read to verify.
 */
public final class TradeRoutePlanningPage extends AbstractScreen {

    private static final String PANEL = "tradeRoutePlanningPanel";

    public TradeRoutePlanningPage(final TestContext context) {
        super(context);
    }

    /** Switches the map to the planner with its corner switch. */
    public void open() {
        clickOnButtonWithId("tradeRoutePlanningToggle");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Returns to the normal map with the same corner switch. */
    public void close() {
        clickOnButtonWithId("tradeRoutePlanningToggle");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Returns to the normal map with the panel's own button. */
    public void back() {
        clickOnButtonWithId("tradeRoutePlanningBack");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Presses New trade route in the game bar; the editor opens modally. */
    public void newRoute() {
        clickOnButtonWithId("tradeRoutePlanningNew");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Opens the shared route window for the selected route. */
    public void editSelected() {
        clickOnButtonWithId("tradeRoutePlanningEdit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Chooses a route by its name in the panel list with the keyboard, which
     * also separates routes drawn over the same squares.
     */
    public void chooseRoute(final String name) {
        final ListView<TradeRoutePreview> list = list();
        int index = -1;
        for (int i = 0; i < list.getItems().size(); i++) {
            if (list.getItems().get(i).getRoute().getName().equals(name)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException("Route is not listed: " + name);
        }
        getRobot().interact(list::requestFocus);
        getRobot().push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Saves the whole game window for visual review. */
    public void captureScreen(final String fileName) throws IOException {
        captureDialog("mainPanel", fileName);
    }

    private ListView<TradeRoutePreview> list() {
        return getNodeFinder().lookup("#tradeRoutePlanningList")
                .queryListView();
    }

    private TradeRoutePlanning planning() {
        return getContext().getClassFromGuice(TradeRoutePlanning.class);
    }

    private boolean isInScene(final String id) {
        return getNodeFinder().lookup("#" + id).tryQuery()
                .filter(node -> node.getScene() != null
                        && node.getScene() == getPrimaryStage().getScene()
                        && node.isVisible())
                .isPresent();
    }

    /**
     * @param expected whether the planner replaces the normal right panel
     */
    public void verifyPlannerShown(final boolean expected) {
        assertEquals(expected, planning().isActive());
        assertEquals(expected, isInScene(PANEL));
        assertEquals(!expected, isInScene("rightPanel"));
    }

    public void verifyToggleText(final String expected) {
        assertEquals(expected,
                getButtonById("tradeRoutePlanningToggle").getText());
    }

    /** The switch stays in the map's upper-left corner. */
    public void verifyToggleInMapCorner() {
        final Button toggle = getButtonById("tradeRoutePlanningToggle");
        final Node canvas = getNodeFinder().lookup("#canvas").query();
        final Bounds map = canvas.localToScene(canvas.getLayoutBounds());
        final Bounds button = toggle.localToScene(toggle.getLayoutBounds());
        assertTrue(
                button.getMinX() >= map.getMinX()
                        && button.getMinX() < map.getMinX() + 40,
                button.toString());
        assertTrue(
                button.getMinY() >= map.getMinY()
                        && button.getMinY() < map.getMinY() + 40,
                button.toString());
    }

    /**
     * @param expectedText text of the button, or {@code null} when it is
     *                     expected to be hidden
     */
    public void verifyNewRouteButton(final String expectedText) {
        final Button button = getButtonById("tradeRoutePlanningNew");
        assertEquals(expectedText != null,
                button.isVisible() && button.isManaged());
        if (expectedText != null) {
            assertEquals(expectedText, button.getText());
            assertTrue(
                    button.getParent().getStyleClass().contains("buttonPanel"),
                    "New trade route is part of the game bar");
        }
    }

    public void verifyEmpty(final String expected) {
        final Node label = getNodeFinder().lookup("#tradeRoutePlanningEmpty")
                .query();
        assertTrue(label.isVisible());
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningEmpty").getText());
    }

    public void verifyMessage(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningMessage").getText());
    }

    public void verifyRouteList(final String... expected) {
        assertEquals(List.of(expected),
                list().getItems().stream()
                        .map(preview -> preview.getRoute().getName())
                        .collect(Collectors.toList()));
    }

    public void verifySelectedRoute(final String expected) {
        final TradeRoutePreview selected = list().getSelectionModel()
                .getSelectedItem();
        assertNotNull(selected, "A route is selected in the list");
        assertEquals(expected, selected.getRoute().getName());
        assertEquals(expected,
                planning().getSelected().orElseThrow().getRoute().getName());
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningName").getText());
    }

    public void verifyNoSelection(final String expectedHint) {
        assertNull(list().getSelectionModel().getSelectedItem());
        assertTrue(planning().getSelected().isEmpty());
        assertEquals(expectedHint,
                getLabeledById("tradeRoutePlanningName").getText());
    }

    public void verifyKind(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningKind").getText());
    }

    public void verifyCarriers(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningCarriers").getText());
    }

    public void verifyPreviewNote(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningPreviewFor").getText());
    }

    public void verifyStops(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRoutePlanningStops").getText());
    }

    /** @param expectedLine one line of the selected route's path list */
    public void verifyLegsContain(final String expectedLine) {
        final String legs = getLabeledById("tradeRoutePlanningLegs").getText();
        assertTrue(
                List.of(legs.split("\n")).stream()
                        .anyMatch(line -> line.startsWith(expectedLine)),
                () -> "Expected a path line starting '" + expectedLine
                        + "' in:\n" + legs);
    }

    public void verifyEditEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRoutePlanningEdit").isDisabled());
    }

    /**
     * The map draws the route over the square with the model's own preview.
     *
     * @param routeName required route name
     * @param square    required map square on its path
     */
    public void verifyRouteDrawnOver(final String routeName,
            final Location square) {
        assertTrue(
                planning().getRoutesAt(square).stream()
                        .anyMatch(preview -> preview.getRoute().getName()
                                .equals(routeName)),
                () -> routeName + " is not drawn over " + square);
    }

    /** @return the planner's preview of the named route */
    private TradeRoutePreview previewOf(final String routeName) {
        return planning().getPreviews().stream().filter(
                preview -> preview.getRoute().getName().equals(routeName))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Route is not planned: " + routeName));
    }

    private List<TradeRoutePreview.LegPath> drawnConnections(
            final String routeName, final TradeRouteKind kind) {
        return previewOf(routeName).getConnections().stream()
                .filter(path -> path.getKind() == kind)
                .collect(Collectors.toList());
    }

    /**
     * The map draws the route's connections of one transport as this many
     * lines.
     *
     * @param routeName required route name
     * @param kind      required transport
     * @param expected  number of drawn lines
     */
    public void verifyDrawnConnectionCount(final String routeName,
            final TradeRouteKind kind, final int expected) {
        assertEquals(expected, drawnConnections(routeName, kind).size(),
                () -> routeName + " " + kind + " lines: "
                        + drawnConnections(routeName, kind).stream()
                                .map(TradeRoutePreview.LegPath::getSquares)
                                .collect(Collectors.toList()));
    }

    /**
     * The map draws one line of the transport over exactly these squares, and
     * every leg of the route uses that same line in one direction or the other.
     *
     * @param routeName       required route name
     * @param kind            required transport
     * @param expectedSquares squares of the line from one end to the other
     */
    public void verifyDrawnConnection(final String routeName,
            final TradeRouteKind kind, final Location... expectedSquares) {
        final List<TradeRoutePreview.LegPath> lines = drawnConnections(
                routeName, kind);
        assertEquals(1, lines.size(),
                () -> routeName + " " + kind + " lines: "
                        + lines.stream()
                                .map(TradeRoutePreview.LegPath::getSquares)
                                .collect(Collectors.toList()));
        final List<Location> drawn = lines.get(0).getSquares();
        final List<Location> expected = List.of(expectedSquares);
        assertTrue(
                drawn.equals(expected) || Lists.reverse(drawn).equals(expected),
                () -> routeName + " is drawn over " + drawn + ", expected "
                        + expected);
        for (final TradeRoutePreview.Leg leg : previewOf(routeName).getLegs()) {
            leg.getPaths().stream().filter(path -> path.getKind() == kind)
                    .forEach(path -> assertTrue(
                            path.isSameConnection(lines.get(0)),
                            () -> "Leg " + leg.getFrom() + " to " + leg.getTo()
                                    + " leaves the drawn line: "
                                    + path.getSquares()));
        }
    }

    /** @param expected full text the status bar shows for the hovered square */
    public void verifyStatusBar(final String expected) {
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils.asyncFx(() -> expected.equals(
                            getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID)
                                    .getText()))
                            .get(10, TimeUnit.SECONDS));
        } catch (TimeoutException notShown) {
            // The assertion below reports the text actually shown.
        }
        assertEquals(expected,
                getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID).getText());
    }

    /**
     * In a small window the planner panel, its list and its buttons stay inside
     * the window.
     */
    public void verifyPanelFitsWindow() {
        final double width = getPrimaryStage().getScene().getWidth();
        final double height = getPrimaryStage().getScene().getHeight();
        for (final String id : List.of(PANEL, "tradeRoutePlanningList",
                "tradeRoutePlanningEdit", "tradeRoutePlanningBack")) {
            final Node node = getNodeFinder().lookup("#" + id).query();
            final Bounds bounds = node.localToScene(node.getLayoutBounds());
            assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= width + 1,
                    id + " " + bounds);
            assertTrue(bounds.getMinY() >= 0 && bounds.getMaxY() <= height + 1,
                    id + " " + bounds);
        }
    }
}
