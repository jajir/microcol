package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Opens the economic trade report through the statistics screen. Rows are
 * addressed by nation and commodity; the verify methods read the shown text.
 */
public final class EuropeanTradePage extends AbstractScreen {

    public EuropeanTradePage(final TestContext context) {
        super(context);
    }

    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        clickOnStatisticsTab("tradeTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public GamePage close() {
        clickOnButtonWithId("closeButtonId");
        return GamePage.of(getContext());
    }

    /**
     * Opens statistics and reaches the trade tab with Tab and arrow keys only:
     * Tab enters the toggle group and the arrow keys select its other members.
     */
    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton trade = tradeTab();
        focusWithKeyboard((Node) trade.getToggleGroup().getSelectedToggle());
        for (int i = 0; i < trade.getToggleGroup().getToggles().size()
                && !trade.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!trade.isSelected()) {
            throw new IllegalStateException(
                    "Trade tab was not selected by keyboard");
        }
    }

    /** Closes the report with the existing Escape path of the statistics. */
    public GamePage closeWithEscape() {
        return new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    /** Scrolls the report with the mouse wheel over its scroll pane. */
    public void scrollReportDown() {
        getRobot().moveTo(reportScroll());
        getRobot().scroll(20, VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private ToggleButton tradeTab() {
        return getNodeFinder().lookup("#tradeTab").queryAs(ToggleButton.class);
    }

    private ScrollPane reportScroll() {
        return getNodeFinder().lookup("#tradeScroll").queryAs(ScrollPane.class);
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

    public void verifyTradeTabSelectedAndFocused(final boolean expected) {
        assertEquals(expected, tradeTab().isSelected(), "Trade tab selected");
        assertEquals(expected, tradeTab().isFocused(), "Trade tab focused");
    }

    public void verifyTradeTabLabel(final String expected) {
        assertEquals(expected, tradeTab().getText(), "Trade tab label");
    }

    /** The tab must lie inside the window, otherwise it cannot be reached. */
    public void verifyTradeTabInsideWindow() {
        final Bounds bounds = tradeTab()
                .localToScene(tradeTab().getBoundsInLocal());
        final javafx.scene.Scene scene = tradeTab().getScene();
        assertEquals(true,
                bounds.getMinY() >= 0 && bounds.getMaxY() <= scene.getHeight()
                        && bounds.getMinX() >= 0
                        && bounds.getMaxX() <= scene.getWidth(),
                "Trade tab bounds " + bounds + " in scene " + scene.getWidth()
                        + "x" + scene.getHeight());
    }

    public void verifyReportScrolled(final boolean expectedScrolled) {
        assertEquals(expectedScrolled, reportScroll().getVvalue() > 0,
                "Report scrolled");
    }

    /** A row is visible when it lies completely inside the report viewport. */
    public void verifyRowFullyVisible(final String rowId,
            final boolean expected) {
        final ScrollPane scroll = reportScroll();
        final Node row = getNodeFinder().lookup("#" + rowId).query();
        final Bounds viewport = scroll.localToScene(scroll.getLayoutBounds());
        final Bounds shown = row.localToScene(row.getBoundsInLocal());
        final boolean inside = shown.getMinY() >= viewport.getMinY() - 1
                && shown.getMaxY() <= viewport.getMaxY() + 1;
        assertEquals(expected, inside,
                "Row " + rowId + " " + shown + " in viewport " + viewport);
    }

    /** @return the title and then every shown value of the row, in order */
    private List<String> texts(final String rowId) {
        final VBox row = getNodeFinder().lookup("#" + rowId)
                .queryAs(VBox.class);
        return row.getChildren().stream().filter(Label.class::isInstance)
                .map(node -> ((Label) node).getText())
                .collect(Collectors.toList());
    }

    public void verifyTradeRow(final String rowId, final String bought,
            final String spent, final String sold, final String received) {
        assertEquals(List.of(bought, spent, sold, received),
                texts(rowId).subList(1, 5), "Trade values of " + rowId);
    }

    public void verifyNoRevolutionarySource(final String rowId) {
        assertEquals(5, texts(rowId).size(), "Revolutionary line of " + rowId);
    }

    public void verifyRevolutionarySource(final String rowId,
            final String expected) {
        assertEquals(6, texts(rowId).size(), "Revolutionary line of " + rowId);
        assertEquals(expected, texts(rowId).get(5),
                "Revolutionary source of " + rowId);
    }

}
