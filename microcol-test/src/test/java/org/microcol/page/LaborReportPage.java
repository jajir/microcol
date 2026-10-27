package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
 * Opens the labor report (R-0198) through the statistics screen. Type rows and
 * person rows are addressed by their stable IDs; the verify methods read the
 * shown text, and the actions only press the controls.
 */
public final class LaborReportPage extends AbstractScreen {

    private static final int MAX_SCROLL_STEPS = 20;

    /** Movement of each person when it was last remembered. */
    private final Map<Integer, Integer> rememberedMoves = new HashMap<>();

    public LaborReportPage(final TestContext context) {
        super(context);
    }

    /** Opens statistics and the labor tab with the mouse. */
    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        clickOnStatisticsTab("laborTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Opens statistics and reaches the labor tab with Tab and arrow keys only:
     * Tab enters the toggle group and the arrow keys select its other members.
     */
    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton labor = laborTab();
        focusWithKeyboard((Node) labor.getToggleGroup().getSelectedToggle());
        for (int i = 0; i < labor.getToggleGroup().getToggles().size()
                && !labor.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!labor.isSelected()) {
            throw new IllegalStateException(
                    "Labor tab was not selected by keyboard");
        }
    }

    /** Closes the report with the existing Escape path of the statistics. */
    public GamePage closeWithEscape() {
        return new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    /** Lists the people of one unit type, the type row being pressed. */
    public void selectType(final String typeName) {
        getRobot().clickOn("#laborType-" + typeName);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Presses Show for one person, scrolling its row into view first. */
    public void show(final int unitId) {
        revealRow(unitId);
        getRobot().clickOn("#laborShow-" + unitId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private ToggleButton laborTab() {
        return getNodeFinder().lookup("#laborTab").queryAs(ToggleButton.class);
    }

    private ScrollPane membersScroll() {
        return getNodeFinder().lookup("#laborScroll").queryAs(ScrollPane.class);
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

    /** Scrolls the list down until the person row lies inside the viewport. */
    private void revealRow(final int unitId) {
        final ScrollPane scroll = membersScroll();
        for (int i = 0; i < MAX_SCROLL_STEPS
                && !rowInside(scroll, unitId); i++) {
            getRobot().moveTo(scroll);
            getRobot().scroll(1, VerticalDirection.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private boolean rowInside(final ScrollPane scroll, final int unitId) {
        final Node row = getNodeFinder().lookup("#laborRow-" + unitId)
                .tryQuery().orElse(null);
        if (row == null) {
            return false;
        }
        final Bounds viewport = scroll.localToScene(scroll.getLayoutBounds());
        final Bounds shown = row.localToScene(row.getBoundsInLocal());
        return shown.getMinY() >= viewport.getMinY() - 1
                && shown.getMaxY() <= viewport.getMaxY() + 1;
    }

    private String text(final String id) {
        return getNodeFinder().lookup("#" + id).queryAs(Label.class).getText();
    }

    public void verifyTabSelected() {
        assertTrue(laborTab().isSelected(), "Labor tab selected");
    }

    /** Keyboard arrival leaves the labor tab both selected and focused. */
    public void verifyTabSelectedAndFocused() {
        assertTrue(laborTab().isSelected(), "Labor tab selected");
        assertTrue(laborTab().isFocused(), "Labor tab focused");
    }

    public void verifyTotal(final int expected) {
        assertEquals("Colonists in total: " + expected, text("laborTotal"),
                "Total colonists");
    }

    /** The row names the type and its count, for example "Free Colonist: 5". */
    public void verifyTypeRow(final String typeName, final String expected) {
        final ToggleButton row = getNodeFinder()
                .lookup("#laborType-" + typeName).queryAs(ToggleButton.class);
        assertEquals(expected, row.getText(), "Row of " + typeName);
    }

    public void verifyTypeSelected(final String typeName,
            final boolean expected) {
        final ToggleButton row = getNodeFinder()
                .lookup("#laborType-" + typeName).queryAs(ToggleButton.class);
        assertEquals(expected, row.isSelected(), "Selected " + typeName);
    }

    /** A type without people has no row at all. */
    public void verifyTypeAbsent(final String typeName) {
        assertTrue(
                getNodeFinder().lookup("#laborType-" + typeName).tryQuery()
                        .isEmpty(),
                "Row of " + typeName + " must not be shown");
    }

    /** The place line of a person names the place the model reports. */
    public void verifyPlaceContains(final int unitId, final String expected) {
        final String place = text("laborLocation-" + unitId);
        assertTrue(place.contains(expected),
                "Place of " + unitId + ": " + place);
    }

    public void verifyShowEnabled(final int unitId, final boolean expected) {
        final Button show = getNodeFinder().lookup("#laborShow-" + unitId)
                .queryAs(Button.class);
        assertEquals(expected, !show.isDisabled(),
                "Show enabled for " + unitId);
    }

    /** Stores the person's movement before the report is used. */
    public void rememberMoves(final int unitId) {
        rememberedMoves.put(unitId,
                getModel().getUnitById(unitId).getActionPoints());
    }

    /** Opening and leaving the report spend no movement of the person. */
    public void verifyMovesUnchanged(final int unitId) {
        assertEquals(rememberedMoves.get(unitId).intValue(),
                getModel().getUnitById(unitId).getActionPoints(),
                "Movement of " + unitId);
    }

    /**
     * The person is taken by the Crown while the report stays open, the way a
     * loss happens in play; the open list is not refreshed by itself.
     */
    public void loseToCrownWhileOpen(final int unitId) throws Exception {
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getUnitById(unitId)
                        .takeOver(getModel().getPlayerByName("Dutch's King")))
                .get(5, TimeUnit.SECONDS);
    }

    public void verifyFeedback(final String expected) {
        assertEquals(expected, text("laborFeedback"), "Labor feedback");
    }

    /** A person that is no longer one of the nation has no row. */
    public void verifyPersonRowAbsent(final int unitId) {
        assertTrue(getNodeFinder().lookup("#laborRow-" + unitId).tryQuery()
                .isEmpty(), "Row of " + unitId + " must not be shown");
    }
}
