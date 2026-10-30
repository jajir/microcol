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
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import java.util.List;
import java.util.stream.Collectors;
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

    public void verifyCompactBuildings(final double expectedPictureRow,
            final double expectedNameMinimum, final double expectedWorkerSize,
            final double expectedOutputGap) {
        getRobot().interact(() -> {
            final GridPane grid = getNodeFinder().lookup(".colony-structures")
                    .queryAs(GridPane.class);
            for (final Node building : grid.getChildren()) {
                final Node top = building.lookup(".building-top");
                final var image = (javafx.scene.image.ImageView) building
                        .lookup(".building-art");
                final Label name = (Label) building.lookup(".building-name");
                final Bounds cell = building
                        .localToScene(building.getLayoutBounds());
                final Bounds art = image.localToScene(image.getBoundsInLocal());
                final Bounds title = name.localToScene(name.getLayoutBounds());
                assertEquals(expectedPictureRow,
                        top.getLayoutBounds().getHeight(), 0.1);
                assertEquals(cell.getMinX(), art.getMinX(), 0.1);
                assertEquals(cell.getMinX(), title.getMinX(), 0.1);
                assertTrue(title.getHeight() >= expectedNameMinimum);
                assertTrue(title.getMinY() >= art.getMaxY());
                final Text renderedName = (Text) name.lookup(".text");
                assertEquals(name.getText(), renderedName.getText());
                assertTrue(
                        renderedName.getLayoutBounds()
                                .getHeight() <= name.getHeight() + 1,
                        name.getText());
                assertTrue(title.getMaxX() <= cell.getMaxX() + 0.1);
                final Node worker = building.lookup(".canvas");
                if (worker != null) {
                    assertTrue(building.isPickOnBounds(),
                            "The whole building remains a hover and drop target");
                    final Bounds slots = worker
                            .localToScene(worker.getLayoutBounds());
                    assertEquals(expectedWorkerSize, slots.getHeight(), 0.1);
                    assertEquals(cell.getMinX(), slots.getMinX(), 0.1);
                    assertEquals(title.getMaxY(), slots.getMinY(), 0.1);
                }
                final Node output = building.lookup(".production-icon");
                if (output != null) {
                    final Bounds production = output.getParent()
                            .localToScene(output.getParent().getLayoutBounds());
                    assertEquals(expectedOutputGap,
                            ((javafx.scene.layout.HBox) top).getSpacing());
                    // JavaFX snaps the row's fractional image width to pixels.
                    assertEquals(expectedOutputGap,
                            production.getMinX() - art.getMaxX(), 1);
                    assertTrue(production.getMaxX() <= cell.getMaxX() + 0.1);
                    final Node value = output.getParent().lookup(".label");
                    assertTrue(value.localToScene(value.getLayoutBounds())
                            .getMinY() >= output
                                    .localToScene(output.getBoundsInLocal())
                                    .getMaxY());
                }
            }
            assertTrue(grid.getHeight() < 500,
                    "Compact buildings must use less than 500 px");
        });
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
        getRobot().clickOn("#colony-field-output-" + type.name());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseOutputWithKeyboard(final GoodsType type) {
        final VBox output = getNodeFinder().lookup("#colony-field-output")
                .queryAs(VBox.class);
        final RadioButton selected = output.getChildren().stream()
                .map(node -> (RadioButton) node).filter(RadioButton::isSelected)
                .findFirst().orElseThrow();
        final RadioButton target = getNodeFinder()
                .lookup("#colony-field-output-" + type.name())
                .queryAs(RadioButton.class);
        final int difference = output.getChildren().indexOf(target)
                - output.getChildren().indexOf(selected);
        for (int index = 0; index < Math.abs(difference); index++) {
            moveSelection(difference < 0 ? KeyCode.UP : KeyCode.DOWN);
        }
        moveSelection(KeyCode.SPACE);
    }

    public void changeSourceCropWithoutRefresh(final GoodsType type) {
        getRobot().interact(() -> getModel().getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.west)
                .setProducedGoodsType(type));
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
        verifyNoProductionChoices();
        final I18n texts = getContext().getClassFromGuice(I18n.class);
        assertEquals(texts.get(ColonyMsg.automaticProduction),
                getLabeledById("colony-field-worker").getText());
    }

    public void verifyEmptyField() {
        verifyNoProductionChoices();
        assertEquals(
                getContext().getClassFromGuice(I18n.class)
                        .get(ColonyMsg.noWorker),
                getLabeledById("colony-field-worker").getText());
    }

    public void verifyNoProductionChoices() {
        final VBox output = getNodeFinder().lookup("#colony-field-output")
                .queryAs(VBox.class);
        assertTrue(output.getChildren().isEmpty());
        assertFalse(output.getParent().isVisible());
        assertFalse(output.getParent().isManaged());
    }

    public void verifyPreviewMatchesMap(final int column, final int row) {
        getRobot().interact(() -> {
            final Canvas map = getNodeFinder().lookup("#colony-field-map")
                    .queryAs(Canvas.class);
            final Canvas preview = getNodeFinder()
                    .lookup("#colony-field-preview").queryAs(Canvas.class);
            final int size = (int) (map.getWidth() / 3);
            final Bounds actual = preview
                    .localToScene(preview.getLayoutBounds());
            assertEquals(size, actual.getWidth(), .01);
            assertEquals(size, actual.getHeight(), .01);
            final javafx.scene.image.PixelReader mapPixels = map
                    .snapshot(null, null).getPixelReader();
            final javafx.scene.image.PixelReader detailPixels = preview
                    .snapshot(null, null).getPixelReader();
            // Compare terrain and worker pixels outside the map's crop badge
            // and frame.
            for (int x = 28; x < size - 4; x++) {
                for (int y = 4; y < size - 4; y++) {
                    assertEquals(
                            mapPixels.getArgb(column * size + x,
                                    row * size + y),
                            detailPixels.getArgb(x, y),
                            "Preview pixel " + x + "," + y);
                }
            }
        });
    }

    public void verifyProductionChoices(final int colonyId) {
        getRobot().interact(() -> {
            final Colony colony = getModel().getColonyById(colonyId)
                    .orElseThrow();
            final List<ColonyJobChoice> expected = colony
                    .getJobChoices(
                            colony.getColonyFieldInDirection(Direction.west)
                                    .getUnit().orElseThrow())
                    .stream()
                    .filter(choice -> choice
                            .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD
                            && choice.getUnavailableReason().isEmpty())
                    .collect(Collectors.toList());
            final VBox output = getNodeFinder().lookup("#colony-field-output")
                    .queryAs(VBox.class);
            assertTrue(output.getParent().isVisible());
            assertTrue(output.getParent().isManaged());
            final Node viewport = getNodeFinder()
                    .lookup(".screen-scroll > .viewport").query();
            final Bounds visible = viewport
                    .localToScene(viewport.getLayoutBounds());
            for (final Node node : output.getParent().lookupAll(".label")) {
                final Label label = (Label) node;
                final Text rendered = (Text) label.lookup(".text");
                assertEquals(label.getText(), rendered.getText(),
                        "Complete product text");
                assertTrue(
                        visible.contains(rendered
                                .localToScene(rendered.getLayoutBounds())),
                        "Product text stays in the viewport: "
                                + label.getText());
            }
            assertEquals(expected.size(), output.getChildren().size());
            assertTrue(expected.size() > 1);
            final I18n texts = getContext().getClassFromGuice(I18n.class);
            for (final ColonyJobChoice choice : expected) {
                final Goods goods = choice.getPotential().orElseThrow();
                final RadioButton option = getNodeFinder()
                        .lookup("#colony-field-output-"
                                + goods.getType().name())
                        .queryAs(RadioButton.class);
                assertTrue(option.isVisible());
                assertFalse(option.isDisabled());
                assertEquals(goods.getType(), option.getUserData());
                assertEquals(Integer.toString(goods.getAmount()),
                        getLabeledById(
                                "colony-field-yield-" + goods.getType().name())
                                .getText());
                final String name = texts.get(org.microcol.gui.GoodsTypeName
                        .getNameForGoodsType(goods.getType()));
                assertEquals(
                        name + ", "
                                + texts.get(ColonyMsg.yieldSummary,
                                        goods.getAmount()),
                        option.getAccessibleText());
                assertEquals(texts.get(ColonyMsg.outputHelp),
                        option.getAccessibleHelp());
                assertTrue(option.lookupAll(".label").stream().anyMatch(
                        node -> name.equals(((Label) node).getText())));
                assertTrue(
                        output.localToScene(output.getLayoutBounds()).contains(
                                option.localToScene(option.getLayoutBounds())));
            }
            assertEquals(1, output.getChildren().stream()
                    .filter(node -> ((RadioButton) node).isSelected()).count());
        });
    }

    public void verifyOutputFocused(final GoodsType expected) {
        assertTrue(getNodeFinder()
                .lookup("#colony-field-output-" + expected.name()).query()
                .isFocused());
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
        final RadioButton output = getNodeFinder()
                .lookup("#colony-field-output-" + expected.name())
                .queryAs(RadioButton.class);
        assertFalse(output.isDisabled());
        assertTrue(output.isSelected());
        assertEquals(Integer.toString(expectedYield),
                getLabeledById("colony-field-yield-" + expected.name())
                        .getText());
    }

    public void verifyBuildingLevel(final String id,
            final ConstructionType expected) {
        assertSame(expected,
                getNodeFinder().lookup("#" + id).query().getUserData());
    }
}
