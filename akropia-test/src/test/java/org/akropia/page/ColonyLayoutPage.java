package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import org.akropia.gui.screen.colony.ColonyMsg;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.GoodsType;
import org.akropia.model.ConstructionType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.Direction;
import org.akropia.model.Goods;
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

    public void verifyNoSectionSeparators() {
        getRobot().interact(() -> {
            for (String selector : List.of(".colony-stock", "#colony-detail")) {
                final javafx.scene.layout.Region panel = getNodeFinder()
                        .lookup(selector)
                        .queryAs(javafx.scene.layout.Region.class);
                assertTrue(
                        panel.getBorder() == null
                                || panel.getBorder().isEmpty(),
                        "No separator above " + selector);
            }
        });
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
        final String[] ids = getNodeFinder().lookup("#colonyButtons")
                .queryAs(javafx.scene.layout.Pane.class).getChildren().stream()
                .filter(Node::isVisible).map(Node::getId)
                .toArray(String[]::new);
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

    public void verifyConditionalActions(final boolean exports,
            final boolean treasure) {
        assertEquals(exports, getButtonById("customHouseOpen").isVisible());
        assertEquals(exports, getButtonById("customHouseOpen").isManaged());
        assertEquals(treasure, getButtonById("treasureOpen").isVisible());
        assertEquals(treasure, getButtonById("treasureOpen").isManaged());
        verifyRemovedCargoActions();
    }

    public void verifyRemovedCargoActions() {
        for (final String id : List.of("quickLoad", "quickUnload",
                "tradeRouteCargoOpen", "tradeRouteOpen")) {
            assertTrue(getNodeFinder().lookup("#" + id).tryQuery().isEmpty(),
                    id);
        }
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
        getRobot().moveTo(getLabeledById("colony-field-name"));
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

    private double scrollBeforeTileClick;

    public void selectTile(final int column, final int row) {
        final Canvas canvas = getNodeFinder().lookup("#colony-field-map")
                .queryAs(Canvas.class);
        revealGameContent(canvas);
        scrollBeforeTileClick = getNodeFinder().lookup(".screen-scroll")
                .queryAs(javafx.scene.control.ScrollPane.class).getVvalue();
        getRobot().clickOn(
                canvas.localToScreen((column + .5) * canvas.getWidth() / 3,
                        (row + .5) * canvas.getHeight() / 3));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyTileClickKeepsScroll() {
        getRobot().interact(() -> {
            assertEquals(scrollBeforeTileClick,
                    getNodeFinder().lookup(".screen-scroll")
                            .queryAs(javafx.scene.control.ScrollPane.class)
                            .getVvalue(),
                    0.001);
        });
        verifyMapFocused();
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
                assertEquals(i % 8, GridPane.getColumnIndex(cell));
                assertEquals(i / 8, GridPane.getRowIndex(cell));
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
            final Node stock = getNodeFinder().lookup(".colony-stock").query();
            final javafx.scene.layout.HBox cards = getNodeFinder()
                    .lookup("#colony-overviews")
                    .queryAs(javafx.scene.layout.HBox.class);
            final Node detail = getNodeFinder().lookup("#colony-detail")
                    .query();
            assertEquals(4, cards.getChildren().size());
            assertTrue(
                    grid.localToScene(grid.getLayoutBounds()).getMaxY() <= stock
                            .localToScene(stock.getLayoutBounds()).getMinY());
            assertTrue(stock.localToScene(stock.getLayoutBounds())
                    .getMaxY() <= cards.localToScene(cards.getLayoutBounds())
                            .getMinY());
            assertTrue(cards.localToScene(cards.getLayoutBounds())
                    .getMaxY() <= detail.localToScene(detail.getLayoutBounds())
                            .getMinY());
            double right = 0;
            for (Node card : cards.getChildren()) {
                final Bounds bounds = card.localToScene(card.getLayoutBounds());
                assertTrue(card.isVisible());
                assertTrue(bounds.getMinX() >= right);
                assertTrue(bounds.getMaxX() <= card.getScene().getWidth());
                right = bounds.getMaxX();
            }
            assertTrue(isCssIdVisible("colony-politics"));
            assertFalse(isCssIdVisible("colony-summary"));
            assertFalse(isCssIdVisible("townCommons"));
        });
    }

    private double workspaceHeight;
    private double workspaceWidth;
    private boolean workspaceScrollbar;

    public void rememberWorkspaceGeometry() {
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            final javafx.scene.control.ScrollPane scroll = getNodeFinder()
                    .lookup(".screen-scroll")
                    .queryAs(javafx.scene.control.ScrollPane.class);
            scroll.applyCss();
            scroll.layout();
            workspaceHeight = scroll.getContent().getLayoutBounds().getHeight();
            workspaceWidth = scroll.getViewportBounds().getWidth();
            workspaceScrollbar = scroll.lookupAll(".scroll-bar").stream()
                    .map(node -> (javafx.scene.control.ScrollBar) node)
                    .anyMatch(bar -> bar
                            .getOrientation() == javafx.geometry.Orientation.VERTICAL
                            && bar.isVisible());
        });
    }

    public void verifyStableWorkspaceGeometry() {
        getRobot().interact(() -> {
            final javafx.scene.control.ScrollPane scroll = getNodeFinder()
                    .lookup(".screen-scroll")
                    .queryAs(javafx.scene.control.ScrollPane.class);
            assertEquals(workspaceHeight,
                    scroll.getContent().getLayoutBounds().getHeight(), 1,
                    "Tab changes keep the reserved page height");
            assertEquals(workspaceWidth, scroll.getViewportBounds().getWidth(),
                    1, "Tab changes do not shift the viewport width");
            assertEquals(workspaceScrollbar, scroll.lookupAll(".scroll-bar")
                    .stream().map(node -> (javafx.scene.control.ScrollBar) node)
                    .anyMatch(bar -> bar
                            .getOrientation() == javafx.geometry.Orientation.VERTICAL
                            && bar.isVisible()));
        });
    }

    public void verifyEmptyConstructionMeters() {
        for (String resource : java.util.List.of("constructionWork",
                "constructionTools")) {
            final javafx.scene.control.ProgressBar bar = getNodeFinder()
                    .lookup("#" + resource + "Bar")
                    .queryAs(javafx.scene.control.ProgressBar.class);
            assertTrue(bar.isVisible());
            assertEquals(0, bar.getProgress());
            assertEquals("—", getLabeledById(resource + "Amount").getText());
            assertTrue(bar.getWidth() > 0);
        }
    }

    public void openSection(final String section) {
        clickOnButtonWithId("colony-section-" + section);
        final Node detail = getNodeFinder().lookup("#colony-detail").query();
        revealGameContent(detail);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySimplifiedCards() {
        assertTrue(getNodeFinder().lookup("#colony-move-right-area").queryAll()
                .isEmpty());
        assertTrue(getNodeFinder().lookup("#constructionOpen").queryAll()
                .isEmpty());
        assertEquals("Surrounding area",
                getLabeledById("colony-section-area").getText());
        assertEquals("Construction",
                getLabeledById("colony-section-construction").getText());
        assertEquals("School",
                getLabeledById("colony-section-school").getText());
        for (String id : java.util.List.of("colony-section-cargo",
                "colony-section-people", "educationOpen", "tradeRouteOpen")) {
            assertTrue(getNodeFinder().lookup("#" + id).queryAll().isEmpty());
        }
        assertEquals("No transport units in the cargo depot.",
                getLabeledById("europe-cargo-title").getText());
        assertEquals("Outside colony",
                getLabeledById("colony-outside-title").getText());
        final VBox card = getNodeFinder().lookup("#colony-card-area")
                .queryAs(VBox.class);
        assertTrue(card.getBorder() == null
                || card.getBorder().getStrokes().isEmpty() || card.getBorder()
                        .getStrokes().get(0).getWidths().getTop() == 0);
    }

    public void verifyConstructionLists() {
        for (String id : java.util.List.of("constructionProjects",
                "constructionQueue")) {
            final javafx.scene.control.ListView<?> list = getNodeFinder()
                    .lookup("#" + id)
                    .queryAs(javafx.scene.control.ListView.class);
            assertTrue(list.getHeight() >= 150);
            assertTrue(list.getStyleClass().contains("paper-input"));
        }
    }

    public void placeCarrierAtColony(int carrier, int colony) {
        getRobot().interact(
                () -> getModel().getUnitById(carrier).placeToLocation(getModel()
                        .getColonyById(colony).orElseThrow().getLocation()));
    }

    public void clearDepot(final int colonyId) {
        getRobot().interact(() -> {
            final Colony colony = getModel().getColonyById(colonyId)
                    .orElseThrow();
            final org.akropia.model.Location destination = colony.getLocation()
                    .getNeighbors().get(0);
            new java.util.ArrayList<>(
                    getModel().getUnitsAt(colony.getLocation()))
                    .forEach(unit -> unit.placeToLocation(destination));
        });
    }

    public void verifyEmptyDepot() {
        getRobot().interact(() -> {
            assertEquals("No transport units in the cargo depot.",
                    getLabeledById("europe-cargo-title").getText());
            assertTrue(getNodeFinder().lookup("#colony-depot-empty").queryAll()
                    .isEmpty());
            assertEquals("No units outside colony.",
                    getLabeledById("colony-outside-title").getText());
            assertTrue(getNodeFinder().lookup("#colony-outside-empty")
                    .queryAll().isEmpty());
            assertTrue(getNodeFinder().lookup("#colony-outside-scroll").query()
                    .isManaged());
            final Node crate = getNodeFinder()
                    .lookup("#colony-cargo-depot .cratePanel").query();
            assertFalse(crate.getParent().isVisible());
            assertFalse(crate.getParent().isManaged());
        });
    }

    private final java.util.Map<String, Bounds> carrierGeometry = new java.util.HashMap<>();

    public int addSmallCarrier(final int colonyId) {
        final org.akropia.model.UnitType type = org.akropia.model.UnitType.CARAVEL;
        final int[] id = new int[1];
        getRobot().interact(() -> {
            final org.akropia.model.Player owner = getModel()
                    .getCurrentPlayer();
            owner.setGold(owner.getGold() + 10000);
            owner.buy(type);
            final org.akropia.model.Unit carrier = owner.getAllUnits().stream()
                    .filter(unit -> unit.getType() == type).findFirst()
                    .orElseThrow();
            carrier.placeToLocation(getModel().getColonyById(colonyId)
                    .orElseThrow().getLocation());
            id[0] = carrier.getId();
        });
        return id[0];
    }

    public void hoverCarrier(final int id) {
        hoverDepot();
        final Node carrier = getNodeFinder().lookup("#europe-dock-ship-" + id)
                .query();
        revealGameContent(carrier);
        getRobot().moveTo(carrier);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyCarrierSelected(final int id) {
        assertTrue(getNodeFinder().lookup("#europe-dock-ship-" + id)
                .queryAs(javafx.scene.control.ToggleButton.class).isSelected());
        assertEquals(1, getNodeFinder()
                .lookup("#colony-cargo-depot .harbor-unit").queryAll().stream()
                .filter(node -> ((javafx.scene.control.ToggleButton) node)
                        .isSelected())
                .count());
    }

    public void prepareDepotScenario() throws Exception {
        final com.google.gson.JsonObject saved = new com.google.gson.Gson()
                .fromJson(java.nio.file.Files.readString(java.nio.file.Path
                        .of("src/test/scenarios/F227-colony-layout.akropia")),
                        com.google.gson.JsonObject.class);
        final var units = saved.getAsJsonObject("model")
                .getAsJsonArray("units");
        final var wagon = units.get(0).getAsJsonObject().deepCopy();
        wagon.addProperty("id", 139);
        wagon.addProperty("type", "WAGON");
        wagon.addProperty("movementThirds", 3);
        final var location = wagon.getAsJsonObject("placeMap")
                .getAsJsonObject("location");
        location.addProperty("x", 24);
        location.addProperty("y", 16);
        final var slots = new com.google.gson.JsonArray();
        slots.add(new com.google.gson.JsonObject());
        slots.add(new com.google.gson.JsonObject());
        wagon.getAsJsonObject("cargo").add("slots", slots);
        units.add(wagon);
        final com.google.gson.JsonObject colony = saved.getAsJsonObject("model")
                .getAsJsonArray("colonies").get(0).getAsJsonObject();
        for (var entry : colony.getAsJsonArray("constructions")) {
            final var construction = entry.getAsJsonObject();
            if (construction.get("type").getAsString().equals("TOWN_HALL")) {
                construction.getAsJsonArray("slots").get(0).getAsJsonObject()
                        .remove("workerId");
            }
            if (construction.get("type").getAsString()
                    .equals("CARPENTERS_STAND")) {
                construction.addProperty("type", "LUMBER_MILL");
                construction.getAsJsonArray("slots").get(0).getAsJsonObject()
                        .addProperty("workerId", 6);
            }
        }
        final java.io.File prepared = new java.io.File(
                System.getProperty("user.home"), "depot-lumber-mill.akropia");
        java.nio.file.Files.writeString(prepared.toPath(), saved.toString());
        org.mockito.Mockito.when(getContext()
                .getClassFromGuice(org.akropia.gui.FileSelectingService.class)
                .loadFile(org.mockito.ArgumentMatchers.any(java.io.File.class)))
                .thenReturn(prepared);
    }

    public void dragLumberWorkerToOutside() {
        final javafx.scene.Parent building = getNodeFinder()
                .lookup("#id_carpenter").queryAs(javafx.scene.Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        final Node target = getNodeFinder().lookup("#colony-outside-title")
                .query();
        revealGameContent(canvas, target);
        getRobot().drag(canvas.localToScreen(16, 16),
                javafx.scene.input.MouseButton.PRIMARY).moveTo(target);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyOutsideHighlight(final boolean expected) {
        getRobot().interact(() -> {
            final javafx.scene.layout.Region area = getNodeFinder()
                    .lookup("#colony-outside-area")
                    .queryAs(javafx.scene.layout.Region.class);
            final boolean highlighted = area.getBackground() != null
                    && area.getBackground().getFills().stream().anyMatch(
                            fill -> Color.BURLYWOOD.equals(fill.getFill()));
            assertEquals(expected, highlighted);
        });
    }

    public void dropWorkerOutside() {
        getRobot().release(javafx.scene.input.MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyWorkerOutside(final int workerId, final int colonyId) {
        final Colony colony = getModel().getColonyById(colonyId).orElseThrow();
        final org.akropia.model.Unit worker = getModel().getUnitById(workerId);
        assertTrue(worker.isAtPlaceLocation());
        assertEquals(colony.getLocation(), worker.getLocation());
        assertTrue(colony.getUnitsOutSideColony().contains(worker));
        assertEquals("Outside colony",
                getLabeledById("colony-outside-title").getText());
        assertTrue(colony.getConstructions().stream()
                .filter(c -> c.getType() == ConstructionType.LUMBER_MILL)
                .findFirst().orElseThrow().getConstructionSlots().get(0)
                .isEmpty());
    }

    public void rememberCarrierGeometry() {
        carrierGeometry.clear();
        getRobot().interact(() -> {
            for (Node node : getNodeFinder()
                    .lookup("#colony-cargo-depot .harbor-unit").queryAll()) {
                final Node icon = ((javafx.scene.control.ToggleButton) node)
                        .getGraphic();
                carrierGeometry.put(node.getId(),
                        icon.localToScene(icon.getBoundsInLocal()));
            }
        });
    }

    public void verifyCarrierGeometryUnchanged() {
        getRobot().interact(() -> {
            for (var entry : carrierGeometry.entrySet()) {
                final Node icon = getNodeFinder().lookup("#" + entry.getKey())
                        .queryAs(javafx.scene.control.ToggleButton.class)
                        .getGraphic();
                final Bounds actual = icon
                        .localToScene(icon.getBoundsInLocal());
                assertEquals(entry.getValue().getMinX(), actual.getMinX(), 0.1);
                assertEquals(entry.getValue().getMinY(), actual.getMinY(), 0.1);
                assertEquals(entry.getValue().getWidth(), actual.getWidth(),
                        0.1);
                assertEquals(entry.getValue().getHeight(), actual.getHeight(),
                        0.1);
            }
        });
    }

    public void verifyAvailableContainers(final int expectedCount) {
        getRobot().interact(() -> {
            final java.util.Set<Node> crates = getNodeFinder()
                    .lookup("#colony-cargo-depot .cratePanel").queryAll();
            assertEquals(expectedCount,
                    crates.stream().filter(Node::isVisible).count());
            assertEquals(expectedCount,
                    crates.stream().filter(Node::isManaged).count());
        });
    }

    public void hoverDepot() {
        final Node title = getNodeFinder().lookup("#europe-cargo-title")
                .query();
        revealGameContent(title);
        getRobot().moveTo(title);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverContainer() {
        final Node crate = getNodeFinder()
                .lookup("#colony-cargo-depot .cratePanel").queryAll().stream()
                .filter(Node::isVisible).findFirst().orElseThrow();
        revealGameContent(crate);
        getRobot().moveTo(crate);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressCarrier(final int id) {
        final Node carrier = getNodeFinder().lookup("#europe-dock-ship-" + id)
                .query();
        getRobot().moveTo(carrier)
                .press(javafx.scene.input.MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void releaseCarrier() {
        getRobot().release(javafx.scene.input.MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDepotSpacing() {
        getRobot().interact(() -> {
            final VBox depot = getNodeFinder().lookup("#colony-cargo-depot")
                    .queryAs(VBox.class);
            final Node title = depot.lookup("#colony-outside-area");
            assertEquals(9, depot.getSpacing() + VBox.getMargin(title).getTop(),
                    0.01);
            final javafx.scene.layout.Region scroll = (javafx.scene.layout.Region) depot
                    .lookup("#europe-dock-scroll");
            assertEquals(57.6, scroll.getPrefHeight(), 0.01);
        });
    }

    public void verifyDepot(String expectedTitle) {
        getRobot().interact(() -> {
            final VBox depot = getNodeFinder().lookup("#colony-cargo-depot")
                    .queryAs(VBox.class);
            assertEquals(expectedTitle,
                    getLabeledById("europe-cargo-title").getText());
            assertNotNull(depot.lookup("#colony-outside-scroll"));
            final javafx.scene.layout.Region dock = (javafx.scene.layout.Region) depot
                    .lookup(".panel-dock");
            assertTrue(dock.getBackground().getFills().stream().allMatch(
                    fill -> Color.TRANSPARENT.equals(fill.getFill())));
            assertTrue(depot.isVisible());
            assertTrue(depot.lookupAll(".separator").isEmpty());
            assertFalse(getNodeFinder().lookup("#colony-depot-empty-spacer")
                    .query().isVisible());
            for (Node node : depot.lookupAll(".harbor-crate-face")) {
                final javafx.scene.shape.SVGPath face = (javafx.scene.shape.SVGPath) node;
                assertEquals(Color.TRANSPARENT, face.getFill());
                assertEquals(Color.BLACK, face.getStroke());
                assertEquals(2, face.getStrokeWidth());
                if (!face.getParent().getPseudoClassStates().contains(
                        javafx.css.PseudoClass.getPseudoClass("closed"))) {
                    assertEquals("M 3 3 L 42 2 L 43 42 L 2 43 Z ",
                            face.getContent());
                }
            }
            assertTrue(depot.getBorder() == null || depot.getBorder()
                    .getInsets().equals(javafx.geometry.Insets.EMPTY));
            Node title = getNodeFinder().lookup("#europe-cargo-title").query();
            Node area = getNodeFinder().lookup("#colony-section-area").query();
            assertEquals(area.localToScene(area.getBoundsInLocal()).getMinY(),
                    title.localToScene(title.getBoundsInLocal()).getMinY(), 2);
        });
    }

    public void verifySchoolInputs() {
        for (int column = 0; column < 3; column++) {
            assertNull(getNodeFinder().lookup("#schoolTeacher-" + column)
                    .queryAs(javafx.scene.image.ImageView.class).getImage());
            for (String prefix : java.util.List.of("schoolStudent-",
                    "schoolSkill-")) {
                javafx.scene.Node box = getNodeFinder()
                        .lookup("#" + prefix + column).query();
                assertFalse(box.getParent().isVisible());
                assertFalse(box.getParent().isManaged());
            }
        }
        for (String id : java.util.List.of("educationAssign", "educationClose",
                "educationCourses", "educationCancelCourse")) {
            assertTrue(getNodeFinder().lookup("#" + id).queryAll().isEmpty());
        }
    }

    public void verifySectionOrder(final String... expected) {
        final javafx.scene.layout.HBox cards = getNodeFinder()
                .lookup("#colony-overviews")
                .queryAs(javafx.scene.layout.HBox.class);
        assertEquals(java.util.Arrays.asList(expected), cards.getChildren()
                .stream().map(Node::getId).collect(Collectors.toList()));
    }

    public void verifyDetail(final String expectedControl) {
        final Node detail = getNodeFinder().lookup("#colony-detail").query();
        final Node control = detail.lookup("#" + expectedControl);
        assertNotNull(control);
        assertTrue(control.isVisible());
        assertTrue(control.isManaged());
        assertEquals(4, getNodeFinder().lookup("#colony-overviews")
                .queryAs(javafx.scene.layout.HBox.class).getChildren().size());
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

    private javafx.scene.image.WritableImage snapshotCanvas(
            final Canvas canvas) {
        javafx.scene.Scene scene = canvas.getScene();
        javafx.scene.layout.Region root = (javafx.scene.layout.Region) scene
                .getRoot();
        double width = scene.getWidth(), height = scene.getHeight();
        double preferredWidth = root.getPrefWidth(),
                preferredHeight = root.getPrefHeight();
        try {
            // A node snapshot must not autosize the live scene to all scroll
            // content.
            root.setPrefSize(width, height);
            return canvas.snapshot(null, null);
        } finally {
            root.setPrefSize(preferredWidth, preferredHeight);
            root.resize(width, height);
            root.layout();
        }
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
            final javafx.scene.image.PixelReader mapPixels = snapshotCanvas(map)
                    .getPixelReader();
            final javafx.scene.image.PixelReader detailPixels = snapshotCanvas(
                    preview).getPixelReader();
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

    /**
     * The west field lists each product once with this field's yield, both for
     * a product whose best field is this one and for a product yielding more on
     * another field.
     */
    public void revealProductionChoices() {
        revealGameContent(getNodeFinder().lookup("#colony-field-output").query()
                .getParent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverProductionChoices() {
        getRobot().moveTo(getNodeFinder().lookup("#colony-field-output")
                .queryAs(Node.class));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyTileFacts(final int colonyId) {
        assertTrue(getNodeFinder().lookup("#colony-field-output").query()
                .isHover());
        final I18n texts = getContext().getClassFromGuice(I18n.class);
        org.akropia.model.Terrain terrain = getModel().getMap()
                .getTerrainAt(getModel().getColonyById(colonyId).orElseThrow()
                        .getColonyFieldInDirection(Direction.west)
                        .getLocation());
        String facts = getNodeFinder().lookup("#colony-field-info")
                .queryLabeled().getText();
        assertTrue(facts.contains(
                texts.get(ColonyMsg.tileMovement, terrain.getMoveCost())));
        assertTrue(facts.contains(texts.get(ColonyMsg.tileDefense,
                terrain.isHasTrees()
                        ? terrain.getTerrainType().getDefenseBonusWithTree()
                        : terrain.getTerrainType().getDefenseBonus())));
        javafx.scene.layout.Region production = (javafx.scene.layout.Region) getNodeFinder()
                .lookup("#colony-field-output").query().getParent();
        assertTrue(production.getWidth() <= 480.1);
        javafx.scene.layout.Region detail = (javafx.scene.layout.Region) production
                .getParent();
        assertTrue(detail.getBorder() == null
                || detail.getBorder().getInsets().getLeft() == 0);
        assertTrue(production.lookupAll(".label").stream().noneMatch(
                node -> node instanceof Label && texts.get(ColonyMsg.outputHelp)
                        .equals(((Label) node).getText())));
        assertEquals(texts.get(ColonyMsg.outputHelp), getNodeFinder()
                .lookup("#statusBarLabel").queryLabeled().getText());
    }

    public void verifyProductionChoices(final int colonyId,
            final GoodsType bestHere, final GoodsType betterElsewhere) {
        getRobot().interact(() -> {
            final Colony colony = getModel().getColonyById(colonyId)
                    .orElseThrow();
            final org.akropia.model.ColonyField field = colony
                    .getColonyFieldInDirection(Direction.west);
            final List<ColonyJobChoice> all = colony
                    .getJobChoices(field.getUnit().orElseThrow());
            final ColonyJobChoice here = bestChoice(all, bestHere);
            assertEquals(field.getLocation(),
                    here.getTargetLocation().orElseThrow(),
                    bestHere + " is best on the selected field");
            final ColonyJobChoice elsewhere = bestChoice(all, betterElsewhere);
            assertNotEquals(field.getLocation(),
                    elsewhere.getTargetLocation().orElseThrow(),
                    betterElsewhere + " is best on another field");
            assertTrue(elsewhere.getPotential().orElseThrow()
                    .getAmount() > all.stream().filter(choice -> choice
                            .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD
                            && choice.getPotential().map(Goods::getType)
                                    .orElse(null) == betterElsewhere)
                            .findFirst().orElseThrow().getPotential()
                            .orElseThrow().getAmount(),
                    betterElsewhere + " yields more on another field");
            final List<ColonyJobChoice> expected = all.stream()
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
            assertEquals(expected.size(),
                    output.getChildren().stream().map(Node::getUserData)
                            .distinct().count(),
                    "Each product is offered once");
            assertTrue(expected.stream().anyMatch(choice -> choice
                    .getPotential().orElseThrow().getType() == bestHere));
            assertTrue(
                    expected.stream().anyMatch(choice -> choice.getPotential()
                            .orElseThrow().getType() == betterElsewhere));
            assertTrue(expected.size() > 1);
            final I18n texts = getContext().getClassFromGuice(I18n.class);
            for (final ColonyJobChoice choice : expected) {
                final Goods goods = choice.getPotential().orElseThrow();
                assertEquals(1,
                        getNodeFinder()
                                .lookup("#colony-field-output-"
                                        + goods.getType().name())
                                .queryAll().size(),
                        goods.getType() + " row");
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
                final String name = texts.get(org.akropia.gui.GoodsTypeName
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

    private ColonyJobChoice bestChoice(final List<ColonyJobChoice> choices,
            final GoodsType type) {
        return choices.stream().filter(
                choice -> choice.getKind() == ColonyJobChoice.Kind.BEST_FIELD
                        && choice.getUnavailableReason().isEmpty()
                        && choice.getPotential().map(Goods::getType)
                                .orElse(null) == type)
                .findFirst().orElseThrow();
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
            final Color actual = snapshotCanvas(canvas).getPixelReader()
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
