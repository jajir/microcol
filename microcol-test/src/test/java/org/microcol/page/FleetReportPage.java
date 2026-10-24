package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.microcol.model.ConcurrentModelEvents;
import org.microcol.model.unit.Ship;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;

/**
 * Opens the fleet report (R-0202) through the statistics screen. Ship rows are
 * addressed by their stable IDs; the verify methods read the shown text, and
 * the actions only press the controls or replace the world as a test
 * preparation.
 */
public final class FleetReportPage extends AbstractScreen {

    private static final int MAX_SCROLL_STEPS = 20;

    /**
     * Words that would reveal the cargo, passengers or route of a foreign ship.
     */
    private static final String[] HIDDEN_FOREIGN_WORDS = { "Passengers",
            "goods", "Sailing", "turns", "Ore", "Silver", "Fur", "Colonist" };

    public FleetReportPage(final TestContext context) {
        super(context);
    }

    /** Opens statistics and the fleet tab with the mouse. */
    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        getRobot().clickOn("#fleetTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Opens statistics and reaches the fleet tab with Tab and arrow keys only:
     * Tab enters the toggle group and the arrow keys select its other members.
     */
    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton fleet = fleetTab();
        focusWithKeyboard((Node) fleet.getToggleGroup().getSelectedToggle());
        for (int i = 0; i < fleet.getToggleGroup().getToggles().size()
                && !fleet.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!fleet.isSelected()) {
            throw new IllegalStateException(
                    "Fleet tab was not selected by keyboard");
        }
    }

    /** Closes the report with the existing Escape path of the statistics. */
    public GamePage closeWithEscape() {
        return new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    /** Presses Show for one own ship, scrolling its row into view first. */
    public void show(final int unitId) {
        revealRow("#fleetShip-" + unitId);
        getRobot().clickOn("#fleetShow-" + unitId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Presses Show for one foreign ship in sight, scrolling it into view. */
    public void showForeign(final int unitId) {
        revealRow("#fleetForeign-" + unitId);
        getRobot().clickOn("#fleetForeignShow-" + unitId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Makes the window the given size, the way a player shrinks it; the report
     * is then read in the smaller window.
     */
    public void resizeWindow(final int width, final int height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Removes a foreign ship from the world, the way it leaves sight when the
     * owner's ship sinks; the report must not show it afterwards.
     */
    public void removeForeignShip(final int unitId) {
        getRobot().interact(() -> ConcurrentModelEvents.destroyUnit(getModel(),
                getModel().getUnitById(unitId)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private ToggleButton fleetTab() {
        return getNodeFinder().lookup("#fleetTab").queryAs(ToggleButton.class);
    }

    private ScrollPane reportScroll() {
        return getNodeFinder().lookup("#fleetScroll").queryAs(ScrollPane.class);
    }

    private void focusWithKeyboard(final Node node) {
        for (int i = 0; i < 40 && !node.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!node.isFocused()) {
            throw new IllegalStateException(
                    "Statistics control is not keyboard reachable: "
                            + node.getId());
        }
    }

    /** Scrolls the list down until the row lies inside the viewport. */
    private void revealRow(final String rowSelector) {
        final ScrollPane scroll = reportScroll();
        for (int i = 0; i < MAX_SCROLL_STEPS && !insideViewport(scroll,
                getNodeFinder().lookup(rowSelector).query()); i++) {
            getRobot().moveTo(scroll);
            getRobot().scroll(1, VerticalDirection.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private static boolean insideViewport(final ScrollPane scroll,
            final Node node) {
        final Bounds viewport = scroll.localToScene(scroll.getLayoutBounds());
        final Bounds shown = node.localToScene(node.getBoundsInLocal());
        return shown.getMinY() >= viewport.getMinY() - 1
                && shown.getMaxY() <= viewport.getMaxY() + 1;
    }

    private String text(final String id) {
        return getNodeFinder().lookup("#" + id).queryAs(Label.class).getText();
    }

    private Bounds sceneBounds(final String id) {
        final Node node = getNodeFinder().lookup("#" + id).query();
        return node.localToScene(node.getBoundsInLocal());
    }

    private double sceneRight() {
        return reportScroll().getScene().getWidth();
    }

    public void verifyTabSelected() {
        assertTrue(fleetTab().isSelected(), "Fleet tab selected");
    }

    /** Keyboard arrival leaves the fleet tab both selected and focused. */
    public void verifyTabSelectedAndFocused() {
        assertTrue(fleetTab().isSelected(), "Fleet tab selected");
        assertTrue(fleetTab().isFocused(), "Fleet tab focused");
    }

    /** The total line in the language of the window, for example English. */
    public void verifyTotal(final String expected) {
        assertEquals(expected, text("fleetTotal"), "Total ships");
    }

    /** The place line of a ship names the place the model reports. */
    public void verifyPlace(final int unitId, final String expected) {
        assertEquals(expected, text("fleetPlace-" + unitId),
                "Place of ship " + unitId);
    }

    /** The cargo line names the passengers by type and the goods. */
    public void verifyCargo(final int unitId, final String expected) {
        assertEquals(expected, text("fleetCargo-" + unitId),
                "Cargo of ship " + unitId);
    }

    /** The holds line names the occupied, total and free holds. */
    public void verifyHolds(final int unitId, final String expected) {
        assertEquals(expected, text("fleetHolds-" + unitId),
                "Holds of ship " + unitId);
    }

    /** The voyage line names the destination and the turns left. */
    public void verifyVoyage(final int unitId, final String expected) {
        assertEquals(expected, text("fleetVoyage-" + unitId),
                "Voyage of ship " + unitId);
    }

    /** A ship that is not sailing has no voyage line at all. */
    public void verifyVoyageAbsent(final int unitId) {
        assertTrue(getNodeFinder().lookup("#fleetVoyage-" + unitId).tryQuery()
                .isEmpty(), "Voyage line of " + unitId);
    }

    /** The repair line names the owner updates left and the destination. */
    public void verifyRepair(final int unitId, final String expected) {
        assertEquals(expected, text("fleetRepair-" + unitId),
                "Repair of ship " + unitId);
    }

    /** A ship that is not under repair has no repair line at all. */
    public void verifyRepairAbsent(final int unitId) {
        assertTrue(getNodeFinder().lookup("#fleetRepair-" + unitId).tryQuery()
                .isEmpty(), "Repair line of " + unitId);
    }

    public void verifyShowEnabled(final int unitId, final boolean expected) {
        final Button show = getNodeFinder().lookup("#fleetShow-" + unitId)
                .queryAs(Button.class);
        assertEquals(expected, !show.isDisabled(),
                "Show enabled for ship " + unitId);
    }

    /**
     * A foreign ship in sight shows its type, its tile and the count of its
     * occupied holds, and nothing else.
     */
    public void verifyForeignShip(final int unitId, final String place,
            final String holds) {
        assertEquals(place, text("fleetForeignPlace-" + unitId),
                "Tile of foreign ship " + unitId);
        assertEquals(holds, text("fleetForeignHolds-" + unitId),
                "Holds of foreign ship " + unitId);
        verifyForeignShowsNoCargo(unitId);
    }

    /** No line of the foreign ship names its cargo, passengers or route. */
    private void verifyForeignShowsNoCargo(final int unitId) {
        for (final String line : new String[] {
                text("fleetForeignName-" + unitId),
                text("fleetForeignPlace-" + unitId),
                text("fleetForeignHolds-" + unitId) }) {
            for (final String word : HIDDEN_FOREIGN_WORDS) {
                assertTrue(!line.contains(word),
                        "Foreign ship line leaks '" + word + "': " + line);
            }
        }
    }

    /** A foreign ship that is out of sight has no row at all. */
    public void verifyForeignShipAbsent(final int unitId) {
        assertTrue(getNodeFinder().lookup("#fleetForeign-" + unitId).tryQuery()
                .isEmpty(), "Row of foreign ship " + unitId);
    }

    /**
     * The model still holds the own ship with the given passengers and occupied
     * holds, so the report shows the real cargo and reading it changed nothing.
     */
    public void verifyOwnShipCargoInModel(final int unitId,
            final int passengers, final int occupied) {
        final Ship ship = (Ship) getModel().getUnitById(unitId);
        assertEquals(passengers,
                ship.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnit().isPresent()).count(),
                "Passengers of ship " + unitId);
        assertEquals(occupied,
                ship.getCargo().getSlots().stream()
                        .filter(slot -> !slot.isEmpty()).count(),
                "Occupied holds of ship " + unitId);
    }

    /**
     * The window has the given size, so the report is read in the smaller
     * window the requirement names.
     */
    public void verifyWindowSize(final int width, final int height) {
        final javafx.stage.Stage stage = getPrimaryStage();
        assertTrue(stage.getWidth() <= width + 1,
                "Window width " + stage.getWidth());
        assertTrue(stage.getHeight() <= height + 1,
                "Window height " + stage.getHeight());
    }

    /**
     * The whole report, including its note, ends inside the window, so no part
     * of it is cut off at the right edge.
     */
    public void verifyReportInsideWindow() {
        assertTrue(sceneBounds("fleet").getMaxX() <= sceneRight() + 1,
                "Report ends beyond window width " + sceneRight());
        assertTrue(sceneBounds("fleetNote").getMaxX() <= sceneRight() + 1,
                "Note is cut off at the window edge");
    }

    /**
     * The name, place, cargo and holds lines of a ship wrap within the visible
     * part of the report, so they are readable without scrolling sideways.
     */
    public void verifySentencesInsideViewport(final int unitId) {
        final Bounds viewport = reportScroll()
                .localToScene(reportScroll().getLayoutBounds());
        for (final String id : new String[] { "fleetName-" + unitId,
                "fleetPlace-" + unitId, "fleetCargo-" + unitId,
                "fleetHolds-" + unitId }) {
            final Bounds shown = sceneBounds(id);
            assertTrue(
                    shown.getMinX() >= viewport.getMinX() - 1
                            && shown.getMaxX() <= viewport.getMaxX() + 1,
                    id + " reaches beyond the visible width");
        }
    }
}
