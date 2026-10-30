package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.gui.screen.colony.ColonyMsg;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.GoodsType;
import org.microcol.model.ConstructionType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;

/** Geometry and player actions for the colony workspace. */
public final class ColonyLayoutPage extends AbstractScreen {
    public ColonyLayoutPage(final TestContext context) {
        super(context);
    }

    public void verifyActionNames(final boolean expanded,
            final String... expected) {
        final String[] ids = { "customHouseOpen", "educationOpen",
                "treasureOpen", "tradeRouteCargoOpen", "tradeRouteOpen",
                "quickLoad", "quickUnload", "closeButtonId" };
        getRobot().interact(() -> {
            assertEquals(ids.length, expected.length);
            final Node toolbar = getNodeFinder().lookup(".screen-toolbar")
                    .query();
            final Bounds bounds = toolbar
                    .localToScene(toolbar.getLayoutBounds());
            double right = 0;
            for (int i = 0; i < ids.length; i++) {
                final Button button = getButtonById(ids[i]);
                assertEquals(expected[i], button.getText(), ids[i]);
                assertEquals(expected[i], button.getAccessibleText(), ids[i]);
                assertNotNull(button.getAccessibleHelp(), ids[i]);
                assertFalse(button.getAccessibleHelp().isBlank(), ids[i]);
                assertEquals(
                        expanded || i == ids.length - 1 ? ContentDisplay.LEFT
                                : ContentDisplay.GRAPHIC_ONLY,
                        button.getContentDisplay(), ids[i]);
                final Bounds actual = button
                        .localToScene(button.getLayoutBounds());
                assertTrue(bounds.contains(actual),
                        ids[i] + " stays in toolbar");
                assertTrue(actual.getMinX() >= right,
                        ids[i] + " does not overlap");
                right = actual.getMaxX();
                if (expanded || i == ids.length - 1) {
                    final Text text = (Text) button.lookup(".text");
                    assertTrue(text.isVisible(), ids[i]);
                    assertEquals(expected[i], text.getText(),
                            ids[i] + " is not truncated");
                }
            }
        });
    }

    public void hoverAction(final String id) {
        getRobot().moveTo(getButtonById(id));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverActionGap() {
        final Node panel = getNodeFinder().lookup("#colonyButtons").query();
        getRobot().moveTo(panel.localToScreen(1, 1));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void leaveActions() {
        getRobot().moveTo(getLabeledById("colony-population"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyActionExplanation(final String expected) {
        getRobot().interact(() -> {
            final Label status = (Label) getLabeledById("statusBarLabel");
            assertEquals(expected, status.getText());
            final Text text = (Text) status.lookup(".text");
            assertEquals(expected, text.getText(),
                    "Complete explanation is rendered");
            assertTrue(status.isVisible() && text.isVisible());
            final Node bar = getNodeFinder().lookup("#statusBar").query();
            assertTrue(
                    bar.localToScene(bar.getLayoutBounds()).contains(
                            text.localToScene(text.getLayoutBounds())),
                    "Explanation fits in the bottom bar");
            assertTrue(Window.getWindows().stream().noneMatch(
                    window -> window instanceof Tooltip && window.isShowing()));
        });
    }

    public void verifyNoActionExplanation() {
        getRobot().interact(() -> {
            assertEquals("", getLabeledById("statusBarLabel").getText());
            assertTrue(Window.getWindows().stream().noneMatch(
                    window -> window instanceof Tooltip && window.isShowing()));
        });
    }

    public void selectTile(final int column, final int row) {
        final Canvas canvas = getNodeFinder().lookup("#colony-field-map")
                .queryAs(Canvas.class);
        revealGameContent(canvas);
        getRobot().clickOn(
                canvas.localToScreen((column + .5) * canvas.getWidth() / 3,
                        (row + .5) * canvas.getHeight() / 3));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveSelection(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseOutput(final GoodsType type) {
        final ComboBox<?> output = getNodeFinder()
                .lookup("#colony-field-output").queryAs(ComboBox.class);
        revealGameContent(output);
        final int index = output.getItems().indexOf(type);
        getRobot().clickOn(output).push(KeyCode.HOME);
        for (int i = 0; i < index; i++)
            getRobot().push(KeyCode.DOWN);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void showStock() {
        revealGameContent(getNodeFinder().lookup(".colony-stock").query());
    }

    public void verifyLayout(final String colonyName,
            final int expectedUnbuilt) {
        getRobot().interact(() -> {
            final GridPane grid = getNodeFinder().lookup(".colony-structures")
                    .queryAs(GridPane.class);
            assertEquals(16, grid.getChildren().size());
            assertEquals(expectedUnbuilt, grid.getChildren().stream().filter(
                    node -> node.getStyleClass().contains("building-empty"))
                    .count());
            for (int i = 0; i < 16; i++) {
                final Node cell = grid.getChildren().get(i);
                assertEquals(i % 4, GridPane.getColumnIndex(cell));
                assertEquals(i / 4, GridPane.getRowIndex(cell));
                for (int j = i + 1; j < 16; j++) {
                    final Bounds a = cell.localToScene(cell.getLayoutBounds());
                    final Node next = grid.getChildren().get(j);
                    final Bounds b = next.localToScene(next.getLayoutBounds());
                    assertTrue(
                            a.getMaxX() <= b.getMinX() + 1
                                    || b.getMaxX() <= a.getMinX() + 1
                                    || a.getMaxY() <= b.getMinY() + 1
                                    || b.getMaxY() <= a.getMinY() + 1,
                            "Buildings overlap");
                }
            }
            final Node fields = getNodeFinder().lookup(".colony-fields")
                    .query();
            assertTrue(
                    grid.localToScene(grid.getLayoutBounds()).getMaxX() < fields
                            .localToScene(fields.getLayoutBounds()).getMinX());
            final Node detail = getNodeFinder().lookup(".tile-detail").query();
            final Node map = getNodeFinder().lookup("#colony-field-map")
                    .query();
            assertTrue(
                    map.localToScene(map.getLayoutBounds()).getMaxX() < detail
                            .localToScene(detail.getLayoutBounds()).getMinX());
            final Colony colony = getModel().getColonyByName(colonyName)
                    .orElseThrow();
            final I18n texts = getContext().getClassFromGuice(I18n.class);
            assertEquals(
                    texts.get(ColonyMsg.populationSummary,
                            colony.getPopulation()),
                    getLabeledById("colony-population").getText());
            assertEquals(
                    texts.get(ColonyMsg.supportSummary,
                            colony.getRebelSupport().getDisplayPercent()),
                    getLabeledById("colony-support").getText());
            assertEquals(
                    texts.get(ColonyMsg.bellsSummary, colony.getGoodsStats()
                            .getStatsByType(GoodsType.BELL).getRowProduction()),
                    getLabeledById("colony-bells").getText());
            assertEquals(texts.get(ColonyMsg.crossesSummary,
                    colony.getGoodsStats().getStatsByType(GoodsType.CROSS)
                            .getRowProduction()),
                    getLabeledById("colony-crosses").getText());
            assertEquals(
                    texts.get(ColonyMsg.foodSummary,
                            colony.getGoodsStats()
                                    .getStatsByType(GoodsType.CORN).getDiff()),
                    getLabeledById("colony-food").getText());
        });
    }

    public void verifyCenter() {
        assertTrue(getNodeFinder().lookup("#colony-field-output").query()
                .isDisabled());
        assertTrue(getNodeFinder().lookup("#colony-field-work").query()
                .isDisabled());
        final I18n texts = getContext().getClassFromGuice(I18n.class);
        assertEquals(texts.get(ColonyMsg.automaticProduction),
                getLabeledById("colony-field-worker").getText());
    }

    public void verifyMapFocused() {
        assertTrue(getNodeFinder().lookup("#colony-field-map").query()
                .isFocused());
    }

    public void verifyMapTileSize(final double expectedTileSize) {
        getRobot().interact(() -> {
            final Canvas canvas = getNodeFinder().lookup("#colony-field-map")
                    .queryAs(Canvas.class);
            final Bounds bounds = canvas.localToScene(canvas.getLayoutBounds());
            assertEquals(3 * expectedTileSize, bounds.getWidth(), .01,
                    "Surrounding map width");
            assertEquals(3 * expectedTileSize, bounds.getHeight(), .01,
                    "Surrounding map height");
        });
    }

    public void verifySelectedTileColor(final int column, final int row,
            final Color expected) {
        getRobot().interact(() -> {
            final Canvas canvas = getNodeFinder().lookup("#colony-field-map")
                    .queryAs(Canvas.class);
            final double size = canvas.getWidth() / 3;
            final Color actual = canvas.snapshot(null, null).getPixelReader()
                    .getColor((int) ((column + .5) * size),
                            (int) (row * size + 2));
            assertEquals(expected, actual, "Selected tile frame color");
        });
    }

    public void verifyOutput(final GoodsType expected,
            final int expectedYield) {
        final ComboBox<?> output = getNodeFinder()
                .lookup("#colony-field-output").queryAs(ComboBox.class);
        assertFalse(output.isDisabled());
        assertEquals(expected, output.getValue());
        assertEquals(
                getContext().getClassFromGuice(I18n.class)
                        .get(ColonyMsg.yieldSummary, expectedYield),
                getLabeledById("colony-field-yield").getText());
    }

    public void verifyBuildingLevel(final String id,
            final ConstructionType expected) {
        assertSame(expected,
                getNodeFinder().lookup("#" + id).query().getUserData());
    }
}
