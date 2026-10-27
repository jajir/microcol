package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.gui.screen.colonizopedia.ScreenColonizopedia;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import javafx.scene.image.Image;
import org.microcol.model.TerrainType;
import org.microcol.model.ResourceOverlay;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.microcol.gui.screen.colonizopedia.ColonizopediaTopic;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import javafx.scene.image.ImageView;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Player gestures and observations for the Colonizopedia: opening from the map
 * help button or H, topic selection, search, scrolling and closing. Closing
 * must leave the game unchanged.
 */
public final class ColonizopediaPage extends AbstractScreen {

    private static final int MAX_LIST_SCROLL_STEPS = 20;

    public static ColonizopediaPage of(final TestContext context) {
        return new ColonizopediaPage(context);
    }

    private ColonizopediaPage(final TestContext context) {
        super(context);
    }

    /** Press the map help button; the topic list is expected to open. */
    public void openFromButton() throws Exception {
        getRobot().clickOn("#" + ButtonsGamePanel.BUTTON_HELP_ID);
        waitUntilDialogOpen();
    }

    /** Press H on the game screen, the same shortcut as the help button. */
    public void openWithHKey() throws Exception {
        getRobot().push(KeyCode.H);
        waitUntilDialogOpen();
    }

    private void waitUntilDialogOpen() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> isCssIdVisible(ScreenColonizopedia.TOPICS_ID));
    }

    /** Move the topic selection with the Down arrow from the focused list. */
    public void pressDownArrow(final int times) {
        for (int i = 0; i < times; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Click the topic row whose localized title is given. The list builds rows
     * only for its visible part, so the mouse wheel moves the list until the
     * row is shown inside the list window; the wheel is tried downwards first
     * and then upwards, and the search is bounded.
     */
    public void selectTopicWithMouse(final String title) throws Exception {
        Optional<Node> row = findTopicRow(title);
        for (int step = 0; row.isEmpty()
                && step < MAX_LIST_SCROLL_STEPS; step++) {
            scrollTopicList(VerticalDirection.DOWN);
            row = findTopicRow(title);
        }
        for (int step = 0; row.isEmpty()
                && step < 2 * MAX_LIST_SCROLL_STEPS; step++) {
            scrollTopicList(VerticalDirection.UP);
            row = findTopicRow(title);
        }
        final Node shownRow = row.orElseThrow(() -> new IllegalStateException(
                "Topic is not reachable in the list: " + title));
        getRobot().clickOn(shownRow, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Replace the search text; an empty text shows every topic again. */
    public void typeSearch(final String text) throws Exception {
        final TextField field = getSearchField();
        getRobot().clickOn(field, MouseButton.PRIMARY);
        getRobot().push(KeyCode.END);
        final int length = WaitForAsyncUtils
                .asyncFx(() -> field.getText().length())
                .get(5, TimeUnit.SECONDS);
        for (int i = 0; i < length; i++) {
            getRobot().push(KeyCode.BACK_SPACE);
        }
        if (!text.isEmpty()) {
            getRobot().write(text);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Scroll the body text down with the mouse wheel over its window. */
    public void scrollBodyDown() {
        getRobot().moveTo("#" + ScreenColonizopedia.BODY_SCROLL_ID);
        getRobot().scroll(10, VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeWithOk() {
        getRobot().clickOn("#" + ButtonBarOk.BUTTON_OK_ID, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Close a Colonizopedia that a failed step left open. The dialog is modal,
     * so it would otherwise block the next scenario.
     */
    public void closeIfOpen() {
        if (isCssIdVisible(ScreenColonizopedia.TOPICS_ID)) {
            closeWithEscape();
        }
    }

    /**
     * Describe the turn, the Dutch treasury, every unit's place, exact movement
     * thirds, action and cargo (goods and passengers), and each colony's
     * warehouse, so a later verification compares the same facts. Only getters
     * are read. Units at sea or in Europe have no map location, so the place is
     * described by its type there.
     */
    public String snapshotGameState() {
        final StringBuilder state = new StringBuilder();
        state.append("turn=")
                .append(getModel().getCalendar().getNumberOfPlayedTurns());
        state.append(";gold=")
                .append(getModel().getPlayerByName("Dutch").getGold());
        for (final Unit unit : getModel().getAllUnits()) {
            state.append(";unit=").append(unit.getId()).append(' ')
                    .append(unit.getType()).append(' ')
                    .append(describePlacement(unit)).append(" thirds=")
                    .append(unit.getMovementThirds()).append(" action=")
                    .append(unit.getAction().getType());
            if (unit instanceof UnitWithCargo) {
                for (final CargoSlot slot : ((UnitWithCargo) unit).getCargo()
                        .getSlots()) {
                    state.append(" slot=").append(describeSlot(slot));
                }
            }
        }
        for (final Colony colony : getModel().getColonies()) {
            state.append(";colony=").append(colony.getName());
            for (final GoodsType type : GoodsType.GOOD_TYPES) {
                state.append(' ').append(type).append('=').append(
                        colony.getWarehouse().getGoods(type).getAmount());
            }
        }
        return state.toString();
    }

    private String describePlacement(final Unit unit) {
        return unit.isAtPlaceLocation() ? "map " + unit.getLocation()
                : unit.getPlace().getClass().getSimpleName();
    }

    private String describeSlot(final CargoSlot slot) {
        if (slot.isLoadedUnit()) {
            return "unit" + slot.getUnit().orElseThrow().getId();
        }
        if (slot.isLoadedGood()) {
            final Goods goods = slot.getGoods().orElseThrow();
            return goods.getType() + "x" + goods.getAmount();
        }
        return "empty";
    }

    /**
     * Find the built row with the given title whose centre lies inside the list
     * window, so a click on it hits the list and not the dialog around it.
     */
    private Optional<Node> findTopicRow(final String title) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            final Node list = getNodeFinder()
                    .lookup("#" + ScreenColonizopedia.TOPICS_ID).query();
            final Node viewport = list.lookup(".clipped-container");
            final Bounds listBounds = viewport
                    .localToScreen(viewport.getLayoutBounds());
            final Set<Node> cells = getNodeFinder()
                    .lookup((Node node) -> node instanceof ListCell
                            && node.isVisible()
                            && title.equals(((ListCell<?>) node).getText()))
                    .queryAll();
            return cells.stream().filter(cell -> {
                final Bounds bounds = cell
                        .localToScreen(cell.getBoundsInLocal());
                return bounds != null
                        && bounds.getMinY() >= listBounds.getMinY()
                        && bounds.getMaxY() <= listBounds.getMaxY()
                        && listBounds.contains(bounds.getCenterX(),
                                bounds.getCenterY());
            }).findFirst();
        }).get(5, TimeUnit.SECONDS);
    }

    private void scrollTopicList(final VerticalDirection direction) {
        getRobot().moveTo("#" + ScreenColonizopedia.TOPICS_ID);
        getRobot().scroll(1, direction);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void scrollReferenceToEnd() {
        getRobot().moveTo("#" + ScreenColonizopedia.BODY_SCROLL_ID);
        getRobot().scroll(150, VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyReferenceRows(final int expectedRows) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final GridPane table = getNodeFinder()
                    .lookup("#colonizopediaReference").queryAs(GridPane.class);
            assertEquals((expectedRows + 1) * 2, table.getChildren().size(),
                    "Every reference row has an image/name and properties cell");
            table.lookupAll(".label").forEach(node -> {
                final Labeled label = (Labeled) node;
                assertEquals("Playpen Sans Regular", label.getFont().getName(),
                        "Reference uses the shared font");
                assertTrue(label.isWrapText(),
                        "Reference text must wrap within its column");
            });
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifyUnitIllustrations() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final LocalizationHelper names = new LocalizationHelper(getI18n());
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            for (final UnitType type : UnitType.UNIT_TYPES) {
                final VBox cell = getNodeFinder()
                        .lookup("#colonizopediaUnit_" + type.name())
                        .queryAs(VBox.class);
                assertEquals(names.getUnitName(type),
                        ((Labeled) cell.lookup(".label")).getText());
                final ImageView picture = (ImageView) cell.lookup("ImageView");
                assertEquals(images.getUnitImage(type), picture.getImage(),
                        type.name());
                assertFalse(picture.getImage().isError(), type.name());
                assertEquals(names.getUnitName(type),
                        picture.getAccessibleText());
                assertTrue(picture.isPreserveRatio(), type.name());
            }
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifyTerrainIllustrations() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final LocalizationHelper names = new LocalizationHelper(getI18n());
            for (final TerrainType terrain : TerrainType.TERRAINS) {
                final VBox cell = getNodeFinder()
                        .lookup("#colonizopediaTerrain_" + terrain.name())
                        .queryAs(VBox.class);
                assertEquals(names.getTerrainName(terrain),
                        ((Labeled) cell.lookup(".label")).getText());
                final Set<Node> canvases = cell.lookupAll("Canvas");
                assertEquals(terrain.isCanHaveTree() ? 2 : 1, canvases.size(),
                        "Open and supported forest pictures: "
                                + terrain.name());
                for (final Node node : canvases) {
                    final WritableImage image = node.snapshot(null, null);
                    final Set<Integer> colors = new java.util.HashSet<>();
                    for (int y = 0; y < image.getHeight(); y++) {
                        for (int x = 0; x < image.getWidth(); x++) {
                            colors.add(image.getPixelReader().getArgb(x, y));
                        }
                    }
                    assertTrue(
                            colors.stream().anyMatch(
                                    color -> color != 0 && color != -1),
                            "Terrain image contains painted pixels: "
                                    + terrain.name());
                    assertFalse(node.getAccessibleText().isBlank(),
                            "Terrain picture has a readable name");
                }
            }
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifySelectedTerrainArtwork() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final ImageProvider images = getContext()
                    .getClassFromGuice(ImageProvider.class);
            final MapManager map = getContext()
                    .getClassFromGuice(MapManager.class);
            final LocalizationHelper names = new LocalizationHelper(getI18n());
            for (final TerrainType terrain : TerrainType.TERRAINS) {
                final Image selected = images.getTerrainImage(terrain);
                assertEquals(selected, map.getTerrainImage(terrain),
                        "Map and reference share the selected terrain: "
                                + terrain.name());
                final VBox cell = getNodeFinder()
                        .lookup("#colonizopediaTerrain_" + terrain.name())
                        .queryAs(VBox.class);
                final Canvas open = cell.lookupAll("Canvas").stream()
                        .map(node -> (Canvas) node)
                        .filter(node -> names.getTerrainName(terrain)
                                .equals(node.getAccessibleText()))
                        .findFirst().orElseThrow();
                final Image painted = open.snapshot(null, null);
                for (int y = 0; y < 45; y++) {
                    for (int x = 0; x < 45; x++) {
                        assertEquals(selected.getPixelReader().getArgb(x, y),
                                painted.getPixelReader().getArgb(x, y),
                                "Selected terrain picture: " + terrain.name());
                    }
                }
            }
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifyResourceIllustrations() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final LocalizationHelper names = new LocalizationHelper(getI18n());
            for (final ResourceOverlay resource : ResourceOverlay.values()) {
                if (resource == ResourceOverlay.NONE) {
                    continue;
                }
                final VBox cell = getNodeFinder()
                        .lookup("#colonizopediaResource_" + resource.name())
                        .queryAs(VBox.class);
                assertEquals(names.getResourceOverlayName(resource),
                        ((Labeled) cell.lookup(".label")).getText());
                final Canvas canvas = (Canvas) cell.lookup("Canvas");
                final Image icon = getContext()
                        .getClassFromGuice(ImageProvider.class)
                        .getResourceImage(resource);
                final Image painted = canvas.snapshot(null, null);
                int checked = 0;
                for (int y = 0; y < 45; y++) {
                    for (int x = 0; x < 45; x++) {
                        if (icon.getPixelReader().getColor(x, y)
                                .getOpacity() == 1) {
                            assertEquals(icon.getPixelReader().getArgb(x, y),
                                    painted.getPixelReader().getArgb(x, y),
                                    "Selected resource picture: "
                                            + resource.name());
                            checked++;
                        }
                    }
                }
                assertTrue(checked > 50,
                        "Reference includes a readable resource silhouette");
            }
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifyReferenceProperties(final String id,
            final String... expectedParts) throws Exception {
        final String text = WaitForAsyncUtils.asyncFx(() -> {
            final VBox cell = getNodeFinder().lookup("#" + id)
                    .queryAs(VBox.class);
            return ((Labeled) cell.lookup(".label")).getText();
        }).get(5, TimeUnit.SECONDS);
        for (final String expected : expectedParts) {
            assertTrue(text.contains(expected),
                    () -> "Reference should contain '" + expected
                            + "' but was: " + text);
        }
    }

    public void verifyFillsGameWindow() {
        getRobot().interact(() -> {
            final Node page = getNodeFinder().lookup("#colonizopediaPage")
                    .query();
            final javafx.scene.Scene scene = getContext().getPrimaryStage()
                    .getScene();
            assertEquals(scene, page.getScene(),
                    "Help belongs to the main game window");
            assertEquals(1,
                    javafx.stage.Window.getWindows().stream()
                            .filter(javafx.stage.Window::isShowing).count(),
                    "Help opens no additional window");
            final Bounds bounds = page.localToScene(page.getLayoutBounds());
            assertEquals(0, bounds.getMinX(), 1);
            assertEquals(0, bounds.getMinY(), 1);
            assertEquals(scene.getWidth(), bounds.getWidth(), 1);
            assertEquals(scene.getHeight(), bounds.getHeight(), 1);
            final Node list = page.lookup("#" + ScreenColonizopedia.TOPICS_ID);
            final Node body = page
                    .lookup("#" + ScreenColonizopedia.BODY_SCROLL_ID);
            final Node status = page.lookup("#statusBar");
            final Bounds listBounds = list.localToScene(list.getLayoutBounds());
            final Bounds bodyBounds = body.localToScene(body.getLayoutBounds());
            final Bounds statusBounds = status
                    .localToScene(status.getLayoutBounds());
            assertEquals(listBounds.getHeight(), bodyBounds.getHeight(), 1);
            assertTrue(bodyBounds.getMaxX() >= scene.getWidth() - 60,
                    "Article fills available width");
            assertTrue(bodyBounds.getMaxY() >= statusBounds.getMinY() - 60,
                    "Article fills available height");
            assertTrue(bodyBounds.getMaxY() <= statusBounds.getMinY(),
                    "Article does not cover status");
        });
    }

    public void verifyDialogOpen() {
        assertTrue(isCssIdVisible(ScreenColonizopedia.SEARCH_ID),
                "Colonizopedia search field is not shown");
        assertTrue(isCssIdVisible(ScreenColonizopedia.TOPICS_ID),
                "Colonizopedia topic list is not shown");
        assertTrue(isCssIdVisible(ScreenColonizopedia.BODY_ID),
                "Colonizopedia text is not shown");
    }

    /** Keyboard users reach the topic list as soon as the screen opens. */
    public void verifyTopicListHasFocus() throws Exception {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(
                WaitForAsyncUtils.asyncFx(() -> getTopicList().isFocused())
                        .get(5, TimeUnit.SECONDS),
                "The topic list must have keyboard focus when opened");
    }

    /** The visible topics in list order, by their localized titles. */
    public void verifyTopicTitles(final String... expectedTitles)
            throws Exception {
        final List<String> actual = WaitForAsyncUtils
                .asyncFx(() -> getTopicList().getItems().stream()
                        .map(topic -> topic.getTitle(getI18n()))
                        .collect(Collectors.toList()))
                .get(5, TimeUnit.SECONDS);
        assertEquals(List.of(expectedTitles), actual,
                "Unexpected topic titles");
    }

    public void verifySelectedTopic(final String expectedTitle,
            final String expectedBodyPart) throws Exception {
        final String selectedTitle = WaitForAsyncUtils.asyncFx(() -> {
            final ColonizopediaTopic selected = getTopicList()
                    .getSelectionModel().getSelectedItem();
            return selected == null ? null : selected.getTitle(getI18n());
        }).get(5, TimeUnit.SECONDS);
        assertEquals(expectedTitle, selectedTitle, "Unexpected selected topic");
        verifyBodyContains(expectedBodyPart);
    }

    public void verifyBodyContains(final String expectedPart) {
        final String text = getLabeledById(ScreenColonizopedia.BODY_ID)
                .getText();
        assertTrue(text.contains(expectedPart), () -> "Body should contain '"
                + expectedPart + "' but was: " + text);
    }

    /**
     * The final sentence is visible whether the prose fits or needs scrolling.
     */
    public void verifyBodyEndVisible() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            final ScrollPane scroll = getNodeFinder()
                    .lookup("#" + ScreenColonizopedia.BODY_SCROLL_ID)
                    .queryAs(ScrollPane.class);
            final Node body = getNodeFinder()
                    .lookup("#" + ScreenColonizopedia.BODY_ID).query();
            final Node viewport = scroll.lookup(".viewport");
            final Bounds textBounds = body
                    .localToScreen(body.getBoundsInLocal());
            final Bounds viewportBounds = viewport
                    .localToScreen(viewport.getBoundsInLocal());
            assertTrue(textBounds.getMaxY() <= viewportBounds.getMaxY() + 1,
                    "The end of the scoring text must be visible");
            assertTrue(textBounds.getMaxY() > viewportBounds.getMinY(),
                    "The text end must not be scrolled past the window");
            return null;
        }).get(5, TimeUnit.SECONDS);
    }

    public void verifyBodyScrolledToEnd() throws Exception {
        final ScrollPane scroll = getNodeFinder()
                .lookup("#" + ScreenColonizopedia.BODY_SCROLL_ID)
                .queryAs(ScrollPane.class);
        final boolean overflows = WaitForAsyncUtils
                .asyncFx(() -> scroll.getContent().getBoundsInLocal()
                        .getHeight() > scroll.getViewportBounds().getHeight())
                .get(5, TimeUnit.SECONDS);
        assertTrue(overflows,
                "The selected text must be taller than its window to scroll");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> scroll.getVvalue()).get(5,
                        TimeUnit.SECONDS) >= 0.99);
    }

    public void verifyNoResults(final String expectedMessage) {
        assertTrue(getTopicList().getItems().isEmpty(),
                "No topic should match the search");
        final Labeled message = getLabeledById(
                ScreenColonizopedia.NO_RESULTS_ID);
        assertTrue(message.isVisible(),
                "The no-results message should be visible");
        assertEquals(expectedMessage, message.getText(),
                "Unexpected no-results message");
    }

    public void verifyNoResultsHidden() {
        assertFalse(
                getLabeledById(ScreenColonizopedia.NO_RESULTS_ID).isVisible(),
                "The no-results message should be hidden");
    }

    public void verifyDialogClosed() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !isCssIdVisible(ScreenColonizopedia.TOPICS_ID));
        assertFalse(isCssIdVisible(ScreenColonizopedia.TOPICS_ID),
                "Colonizopedia should be closed");
    }

    public void verifyGameStateUnchanged(final String expectedState) {
        assertEquals(expectedState, snapshotGameState(),
                "Colonizopedia must not change turn, gold, units or cargo");
    }

    public void verifyTurnAndGold(final int expectedTurn,
            final int expectedGold) {
        assertEquals(expectedTurn,
                getModel().getCalendar().getNumberOfPlayedTurns(),
                "Unexpected number of played turns");
        assertEquals(expectedGold,
                getModel().getPlayerByName("Dutch").getGold(),
                "Unexpected Dutch gold");
    }

    public void verifyNextTurnEnabled() {
        assertFalse(
                getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID)
                        .isDisabled(),
                "Next turn must be available after closing the Colonizopedia");
    }

    @SuppressWarnings("unchecked")
    private ListView<ColonizopediaTopic> getTopicList() {
        return getNodeFinder().lookup("#" + ScreenColonizopedia.TOPICS_ID)
                .queryAs(ListView.class);
    }

    private TextField getSearchField() {
        return getNodeFinder().lookup("#" + ScreenColonizopedia.SEARCH_ID)
                .queryAs(TextField.class);
    }

    private I18n getI18n() {
        return getContext().getClassFromGuice(I18n.class);
    }

}
