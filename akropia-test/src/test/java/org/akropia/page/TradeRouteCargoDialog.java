package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteAssignment;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * Cargo dialog of trade route stops, opened from the colony screen. Choices are
 * made through the visible combo boxes, lists and buttons as a player would;
 * the model is only read to verify the outcome.
 */
public final class TradeRouteCargoDialog extends AbstractScreen {

    private static final String UNLOAD = "tradeRouteUnload";
    private static final String LOAD = "tradeRouteLoad";

    public TradeRouteCargoDialog(final TestContext context) {
        super(context);
    }

    public void open() {
        press("tradeRouteCargoOpen");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Picks the route from the header menu with the mouse. */
    public void chooseRoute(final String name) {
        final int index = routeIndex(name);
        getRobot().clickOn(routeMenu());
        WaitForAsyncUtils.waitForFxEvents();
        final Node item = getNodeFinder()
                .lookup("#" + routeMenu().getItems().get(index).getId())
                .query();
        getRobot().clickOn(item);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Picks the route from the header menu with the keyboard: Down opens the
     * menu without focusing an entry, further Down presses walk the entries
     * until the wanted one owns the popup focus, and Enter takes it.
     */
    public void chooseRouteWithKeyboard(final String name) {
        final String itemId = routeMenu().getItems().get(routeIndex(name))
                .getId();
        getRobot().interact(() -> routeMenu().requestFocus());
        getRobot().push(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
        for (int i = 0; i <= routeMenu().getItems().size()
                && !isPopupItemFocused(itemId); i++) {
            getRobot().push(KeyCode.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** The popup entry shown for the menu item has keyboard focus. */
    private boolean isPopupItemFocused(final String itemId) {
        return getNodeFinder().lookup("#" + itemId).queryAll().stream()
                .anyMatch(node -> node.getScene() != null
                        && node.getScene().getFocusOwner() == node);
    }

    /** Presses Back with the keyboard. */
    public void closeWithKeyboard() {
        getRobot().interact(
                () -> getButtonById("tradeRouteCargoClose").requestFocus());
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** @param index zero-based stop index of the chosen route */
    public void chooseStop(final int index) {
        choose(stopBox(), index);
    }

    public void addUnload(final GoodsType type) {
        addGoods(UNLOAD, type);
    }

    public void addLoad(final GoodsType type) {
        addGoods(LOAD, type);
    }

    public void selectUnload(final int index) {
        select(UNLOAD, index);
    }

    public void selectLoad(final int index) {
        select(LOAD, index);
    }

    public void moveUnloadUp() {
        press(UNLOAD + "Up");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveUnloadDown() {
        press(UNLOAD + "Down");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveLoadUp() {
        press(LOAD + "Up");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveLoadDown() {
        press(LOAD + "Down");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void removeLoad() {
        press(LOAD + "Remove");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void save() {
        press("tradeRouteCargoSave");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void process() {
        press("tradeRouteCargoProcess");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void close() {
        press("tradeRouteCargoClose");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** @param index zero-based position in the list of carriers at the stop */
    public void chooseCarrier(final int index) {
        choose(getNodeFinder().lookup("#tradeRouteCargoCarrier")
                .queryComboBox(), index);
    }

    public void assign() {
        press("tradeRouteAssign");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Moves the focus to the assign button and presses it with the Space key.
     */
    public void assignWithKeyboard() {
        pressWithKeyboard("tradeRouteAssign");
    }

    /**
     * Picks the carrier to release among every carrier that follows the chosen
     * route, wherever it is, not only among those at the stop.
     *
     * @param index zero-based position in the list of assigned carriers
     */
    public void chooseAssignedCarrier(final int index) {
        expandSection("tradeRouteAssignedPane");
        choose(getNodeFinder().lookup("#tradeRouteAssignedCarrier")
                .queryComboBox(), index);
    }

    /** @param name the colony name, or the text of Europe, as listed */
    public void chooseReplacement(final String name) {
        final ComboBox<TradeRouteStop> combo = replacementBox();
        int index = -1;
        for (int i = 0; i < combo.getItems().size(); i++) {
            if (combo.getConverter().toString(combo.getItems().get(i))
                    .equals(name)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException("Stop is not offered: " + name);
        }
        choose(combo, index);
    }

    public void replaceStop() {
        press("tradeRouteReplaceButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void replaceStopWithKeyboard() {
        pressWithKeyboard("tradeRouteReplaceButton");
    }

    public void cancelAssignment() {
        press("tradeRouteCancelAssignment");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelAssignmentWithKeyboard() {
        pressWithKeyboard("tradeRouteCancelAssignment");
    }

    /** First press: names the affected carriers, deletes nothing yet. */
    public void askToDeleteRoute() {
        press("tradeRouteDelete");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Second press on the same route: now the route is deleted. */
    public void confirmDeleteRoute() {
        press("tradeRouteDelete");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void askToDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    public void confirmDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    /**
     * Closes the dialog with the Escape key, as a player abandoning it. The
     * focus is first moved off the last used choice box to the Close button,
     * because an open or focused choice box may take Escape for itself.
     */
    public void closeWithEscape() {
        final javafx.scene.control.Button button = getButtonById(
                "tradeRouteCargoClose");
        reveal(button);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void pressWithKeyboard(final String buttonId) {
        if (buttonId.equals("tradeRouteDelete")) {
            expandSection("tradeRouteDeletePane");
        }
        if (buttonId.equals("tradeRouteCancelAssignment")) {
            expandSection("tradeRouteAssignedPane");
        }
        final javafx.scene.control.Button button = getButtonById(buttonId);
        reveal(button);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void addGoods(final String prefix, final GoodsType type) {
        final ComboBox<GoodsType> combo = getNodeFinder()
                .lookup("#" + prefix + "Choice").queryComboBox();
        final int index = combo.getItems().indexOf(type);
        if (index < 0) {
            throw new IllegalStateException("Goods is not offered: " + type);
        }
        choose(combo, index);
        press(prefix + "Add");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Focuses the list, goes to its top entry and steps down to the index. */
    private void select(final String prefix, final int index) {
        reveal(list(prefix));
        getRobot().clickOn(list(prefix)).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Presses a button of the dialog after scrolling it into view, so a dialog
     * taller than a small screen is operated the same way a player would.
     */
    private void press(final String buttonId) {
        if (buttonId.equals("tradeRouteDelete")) {
            expandSection("tradeRouteDeletePane");
        }
        if (buttonId.equals("tradeRouteCancelAssignment")) {
            expandSection("tradeRouteAssignedPane");
        }
        reveal(getButtonById(buttonId));
        clickOnButtonWithId(buttonId);
    }

    /** Scrolls the dialog, when it has a scroll bar, until the node shows. */
    private void expandSection(final String id) {
        final TitledPane section = getNodeFinder().lookup("#" + id)
                .queryAs(TitledPane.class);
        if (!section.isExpanded()) {
            reveal(section);
            getRobot().clickOn(section.lookup(".title"));
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private void reveal(final Node node) {
        for (Parent parent = node.getParent(); parent != null; parent = parent
                .getParent()) {
            if (parent instanceof TitledPane
                    && !((TitledPane) parent).isExpanded()) {
                final TitledPane section = (TitledPane) parent;
                getRobot().clickOn(section.lookup(".title"));
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
        getNodeFinder().lookup("#tradeRouteCargoScroll").tryQuery()
                .ifPresent(found -> {
                    final ScrollPane scroll = (ScrollPane) found;
                    getRobot().interact(() -> {
                        final Node content = scroll.getContent();
                        final double whole = content.getLayoutBounds()
                                .getHeight();
                        final double view = scroll.getViewportBounds()
                                .getHeight();
                        if (whole > view) {
                            final Bounds inside = content.sceneToLocal(
                                    node.localToScene(node.getBoundsInLocal()));
                            final double top = inside.getMinY()
                                    - (view - inside.getHeight()) / 2;
                            scroll.setVvalue(Math.max(0,
                                    Math.min(1, top / (whole - view))));
                        }
                    });
                });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private <T> void choose(final ComboBox<T> combo, final int index) {
        reveal(combo);
        getRobot().clickOn(combo).push(KeyCode.HOME);
        for (int i = 0; i < index; i++) {
            getRobot().push(KeyCode.DOWN);
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private MenuButton routeMenu() {
        return getNodeFinder().lookup("#tradeRouteCargoRoute")
                .queryAs(MenuButton.class);
    }

    private int routeIndex(final String name) {
        final List<MenuItem> items = routeMenu().getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getText().equals(name)
                    && !items.get(i).isDisable()) {
                return i;
            }
        }
        throw new IllegalStateException("Route is not offered: " + name);
    }

    private Bounds sceneBounds(final Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    private ComboBox<Integer> stopBox() {
        return getNodeFinder().lookup("#tradeRouteCargoStop").queryComboBox();
    }

    private ComboBox<TradeRouteStop> replacementBox() {
        return getNodeFinder().lookup("#tradeRouteReplaceStop").queryComboBox();
    }

    @SuppressWarnings("unchecked")
    private ListView<GoodsType> list(final String prefix) {
        return getNodeFinder().lookup("#" + prefix + "List")
                .queryAs(ListView.class);
    }

    private ComboBox<GoodsType> choice(final String prefix) {
        return getNodeFinder().lookup("#" + prefix + "Choice").queryComboBox();
    }

    private Unit carrier(final int unitId) {
        // A ship on the Atlantic is not on the map, so every unit is searched.
        return getModel().getColonyById(0).orElseThrow().getOwner()
                .getAllUnits().stream().filter(unit -> unit.getId() == unitId)
                .findFirst().orElseThrow();
    }

    public void verifyWorkbenchLayout(final String screenshot)
            throws Exception {
        final javafx.stage.Stage stage = getContext().getPrimaryStage();
        final double width = stage.getWidth();
        final double height = stage.getHeight();
        final double x = stage.getX();
        final double y = stage.getY();
        try {
            getRobot().interact(() -> {
                stage.setX(0);
                stage.setY(0);
                stage.setWidth(1200);
                stage.setHeight(900);
            });
            WaitForAsyncUtils.waitForFxEvents();
            getRobot().interact(() -> {
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
            });
            final Node stops = getNodeFinder().lookup("#tradeRouteStopsColumn")
                    .query();
            final javafx.scene.control.Label summary = (javafx.scene.control.Label) ((javafx.scene.layout.VBox) stops)
                    .getChildren().get(1);
            assertTrue(
                    summary.getHeight() >= summary
                            .prefHeight(summary.getWidth()) - 1,
                    "The ordered stop summary must display its complete text");
            final Node goods = getNodeFinder().lookup("#tradeRouteGoodsColumn")
                    .query();
            final Node carriers = getNodeFinder()
                    .lookup("#tradeRouteCarriersColumn").query();
            final Bounds left = stops.localToScene(stops.getBoundsInLocal());
            final Bounds middle = goods.localToScene(goods.getBoundsInLocal());
            final Bounds right = carriers
                    .localToScene(carriers.getBoundsInLocal());
            assertEquals(left.getMinY(), middle.getMinY(), 1);
            assertEquals(left.getMinY(), right.getMinY(), 1);
            assertTrue(left.getMaxX() <= middle.getMinX());
            assertTrue(middle.getMaxX() <= right.getMinX());
            captureDialog("tradeRouteCargoPage", screenshot);
            getRobot().interact(() -> stage.setWidth(900));
            WaitForAsyncUtils.waitForFxEvents();
            final Node back = getButtonById("tradeRouteCargoClose");
            final Bounds backBounds = back
                    .localToScene(back.getBoundsInLocal());
            assertTrue(backBounds.getMinY() >= 0
                    && backBounds.getMaxY() <= stage.getScene().getHeight());
            assertTrue(backBounds.getMinX() >= 0
                    && backBounds.getMaxX() <= stage.getScene().getWidth());
        } finally {
            getRobot().interact(() -> {
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setX(x);
                stage.setY(y);
            });
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifyOpened() {
        assertTrue(getNodeFinder().lookup("#tradeRouteCargoPage").tryQuery()
                .isPresent());
        for (final String column : List.of("tradeRouteStopsColumn",
                "tradeRouteGoodsColumn", "tradeRouteCarriersColumn")) {
            assertTrue(getNodeFinder().lookup("#" + column).tryQuery()
                    .isPresent());
        }
        assertTrue(getNodeFinder().lookup("#tradeRouteCargoDialog").tryQuery()
                .isPresent());
    }

    public void verifyDialogClosed() {
        assertTrue(getNodeFinder().lookup("#tradeRouteCargoDialog").tryQuery()
                .isEmpty());
    }

    /** @param expectedName route shown as chosen when the dialog opened */
    public void verifyChosenRoute(final String expectedName) {
        assertEquals(expectedName, routeMenu().getText());
    }

    /** @param expected Unicode code points of the name shown in the menu */
    public void verifyChosenRouteLength(final int expected) {
        final String text = routeMenu().getText();
        assertEquals(expected, text.codePointCount(0, text.length()));
    }

    /** @param expected type and stop count shown above the columns */
    public void verifyRouteSummary(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoSummary").getText());
    }

    /**
     * The heading is on the left; the route menu and Back follow on the right
     * in this order, Back last, inside the window and without overlapping.
     */
    public void verifyHeaderLayout() {
        getRobot().interact(() -> {
            getPrimaryStage().getScene().getRoot().applyCss();
            getPrimaryStage().getScene().getRoot().layout();
        });
        final Node title = getNodeFinder().lookup("#tradeRouteCargoTitle")
                .query();
        final MenuButton menu = routeMenu();
        final javafx.scene.control.Button back = getButtonById(
                "tradeRouteCargoClose");
        final Parent actions = getNodeFinder().lookup("#tradeRouteCargoActions")
                .queryAs(Parent.class);
        assertEquals(List.of(menu, back), actions.getChildrenUnmodifiable(),
                "Header actions must be the route menu, then Back");
        assertTrue(title.getParent() == actions.getParent(),
                "Heading and actions must share the header row");
        final Bounds titleBounds = sceneBounds(title);
        final Bounds menuBounds = sceneBounds(menu);
        final Bounds backBounds = sceneBounds(back);
        final double sceneWidth = getPrimaryStage().getScene().getWidth();
        assertTrue(titleBounds.getMinX() < sceneWidth / 2,
                "The heading must be on the left: " + titleBounds);
        assertTrue(titleBounds.getMaxX() <= menuBounds.getMinX(),
                "The heading overlaps the route menu");
        assertTrue(menuBounds.getMaxX() <= backBounds.getMinX(),
                "The route menu overlaps Back");
        assertTrue(menuBounds.getWidth() >= menu.minWidth(-1) - 1,
                "The route menu is cut below its minimum");
        assertTrue(backBounds.getWidth() >= back.prefWidth(-1) - 1,
                "Back must keep its full width");
        assertTrue(
                backBounds.getMinX() >= 0 && backBounds.getMaxX() <= sceneWidth,
                "Back must be inside the window: " + backBounds);
        assertTrue(backBounds.getMinY() >= 0 && backBounds
                .getMaxY() <= getPrimaryStage().getScene().getHeight());
        assertTrue(sceneWidth - backBounds.getMaxX() < 40,
                "Back must be at the right edge: " + backBounds);
    }

    /**
     * The route menu has the paper face and arrow of the map's colony menu,
     * with the trade icon; Back has the standard return icon and text.
     */
    public void verifyHeaderControls(final String expectedBackText) {
        final MenuButton menu = routeMenu();
        assertTrue(menu.getStyleClass().contains("toolbar-menu"));
        assertTrue(menu.getHeight() >= 57 - 1,
                "Route menu is lower than the colony menu: "
                        + menu.getHeight());
        verifyIcon(menu.getGraphic(), org.akropia.gui.image.ButtonIcon.TRADE);
        final javafx.scene.control.Button back = getButtonById(
                "tradeRouteCargoClose");
        assertEquals(expectedBackText, back.getText());
        assertTrue(back.isCancelButton());
        assertTrue(back.getStyleClass().contains("toolbar-action"));
        verifyIcon(back.getGraphic(), org.akropia.gui.image.ButtonIcon.BACK);
    }

    private void verifyIcon(final Node graphic,
            final org.akropia.gui.image.ButtonIcon expected) {
        final javafx.scene.image.ImageView view = (javafx.scene.image.ImageView) graphic;
        final javafx.scene.image.ImageView reference = expected.createView();
        assertSame(reference.getImage(), view.getImage());
        assertEquals(reference.getViewport(), view.getViewport());
    }

    /** @param expected route names in the header menu, in order */
    public void verifyRouteChoices(final String... expected) {
        assertEquals(List.of(expected), routeMenu().getItems().stream()
                .map(MenuItem::getText).collect(Collectors.toList()));
    }

    /** @param expected stop entries offered for the chosen route, in order */
    public void verifyStopChoices(final String... expected) {
        final ComboBox<Integer> combo = stopBox();
        assertEquals(List.of(expected),
                combo.getItems().stream().map(combo.getConverter()::toString)
                        .collect(Collectors.toList()));
    }

    /** The route menu takes the keyboard focus when the dialog opens. */
    public void verifyRouteMenuFocused() {
        assertSame(routeMenu(), getPrimaryStage().getScene().getFocusOwner());
    }

    public void verifyTitle(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoTitle").getText());
    }

    public void verifyUnloadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), list(UNLOAD).getItems());
    }

    public void verifyLoadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), list(LOAD).getItems());
    }

    public void verifyUnloadAddEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById(UNLOAD + "Add").isDisabled());
    }

    public void verifyLoadAddEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById(LOAD + "Add").isDisabled());
    }

    /** A goods already in the list is no longer offered, so no duplicate. */
    public void verifyUnloadChoiceOffers(final GoodsType type,
            final boolean expected) {
        assertEquals(expected, choice(UNLOAD).getItems().contains(type));
    }

    public void verifySaveEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCargoSave").isDisabled());
    }

    public void verifyProcessEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCargoProcess").isDisabled());
    }

    public void verifyCarrierStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoCarrierStatus").getText());
    }

    public void verifyMessage(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoMessage").getText());
    }

    /** @param expected the result lines, one per configured goods entry */
    public void verifyResult(final String... expected) {
        assertEquals(String.join("\n", expected),
                getLabeledById("tradeRouteCargoResult").getText());
    }

    public void verifyStoredCargo(final String routeName, final int stopIndex,
            final List<GoodsType> expectedUnload,
            final List<GoodsType> expectedLoad) {
        final List<TradeRoute> matching = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(route -> route.getName().equals(routeName))
                .collect(Collectors.toList());
        assertEquals(1, matching.size());
        assertEquals(expectedUnload,
                matching.get(0).getStops().get(stopIndex).getUnloadGoods());
        assertEquals(expectedLoad,
                matching.get(0).getStops().get(stopIndex).getLoadGoods());
    }

    public void verifyHold(final int unitId, final int slotIndex,
            final Goods expected) {
        assertEquals(expected,
                ((org.akropia.model.unit.UnitWithCargo) carrier(unitId))
                        .getCargo().getSlotByIndex(slotIndex).getGoods()
                        .orElse(null));
    }

    public void verifyHoldEmpty(final int unitId, final int slotIndex) {
        assertTrue(((org.akropia.model.unit.UnitWithCargo) carrier(unitId))
                .getCargo().getSlotByIndex(slotIndex).getGoods().isEmpty());
    }

    public void verifyWarehouse(final int colonyId, final Goods expected) {
        assertEquals(expected.getAmount(),
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(expected.getType()).getAmount());
    }

    public void verifyVisitProcessed(final int unitId, final boolean expected) {
        assertEquals(expected,
                carrier(unitId).getProcessedTradeRouteVisit().isPresent());
    }

    public void verifyAssignEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("tradeRouteAssign").isDisabled());
    }

    /** @param expected the model's refusal text, or empty when accepted */
    public void verifyAssignStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteAssignStatus").getText());
    }

    /** @param expected the list of assigned carriers, one line per carrier */
    public void verifyAssigned(final String... expected) {
        expandSection("tradeRouteAssignedPane");
        assertEquals(String.join("\n", expected),
                getLabeledById("tradeRouteAssigned").getText());
    }

    public void verifyAssignmentHeading(final String expected) {
        assertEquals(expected, getNodeFinder().lookup("#tradeRouteAssignedPane")
                .queryAs(TitledPane.class).getText());
    }

    public void verifyAssignButtonText(final String expected) {
        assertEquals(expected, getButtonById("tradeRouteAssign").getText());
    }

    /**
     * Reads the saved assignment of a carrier: its route, the stop it heads to
     * and whether it waits.
     */
    public void verifyAssignment(final int unitId, final String routeName,
            final int stopIndex, final boolean paused) {
        final TradeRouteAssignment assignment = getModel()
                .getTradeRouteAssignment(carrier(unitId)).orElseThrow();
        final TradeRoute route = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(stored -> stored.getName().equals(routeName))
                .findFirst().orElseThrow();
        assertEquals(route.getId(), assignment.getRouteId());
        assertEquals(stopIndex, assignment.getStopIndex());
        assertEquals(paused, assignment.getPause().isPresent());
    }

    public void verifyNotAssigned(final int unitId) {
        assertTrue(
                getModel().getTradeRouteAssignment(carrier(unitId)).isEmpty());
    }

    public void verifyReplaceEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteReplaceButton").isDisabled());
    }

    /** @param expected the model's refusal, or the carriers it will reach */
    public void verifyReplaceStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteReplaceStatus").getText());
    }

    public void verifyCancelAssignmentEnabled(final boolean expected) {
        assertEquals(expected,
                !getButtonById("tradeRouteCancelAssignment").isDisabled());
    }

    public void verifyDeleteEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("tradeRouteDelete").isDisabled());
    }

    /** The button asks for confirmation after its first press. */
    public void verifyDeleteButtonText(final String expected) {
        assertEquals(expected, getButtonById("tradeRouteDelete").getText());
    }

    /** @param expected the carriers the delete would release or released */
    public void verifyDeleteInfo(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteDeleteInfo").getText());
    }

    public void verifyCancelAssignmentButtonText(final String expected) {
        assertEquals(expected,
                getButtonById("tradeRouteCancelAssignment").getText());
    }

    public void verifyReplaceButtonText(final String expected) {
        assertEquals(expected,
                getButtonById("tradeRouteReplaceButton").getText());
    }

    /**
     * @param expected the carriers offered for release, as the combo box shows
     *                 them, wherever those carriers are
     */
    public void verifyAssignedChoices(final String... expected) {
        final ComboBox<org.akropia.model.unit.UnitWithCargo> combo = getNodeFinder()
                .lookup("#tradeRouteAssignedCarrier").queryComboBox();
        assertEquals(List.of(expected),
                combo.getItems().stream()
                        .map(item -> combo.getConverter().toString(item))
                        .collect(Collectors.toList()));
    }

    /** @param expected the name as shown, empty when nothing is picked */
    public void verifyAssignedChoice(final String expected) {
        final ComboBox<org.akropia.model.unit.UnitWithCargo> combo = getNodeFinder()
                .lookup("#tradeRouteAssignedCarrier").queryComboBox();
        assertEquals(expected, combo.getConverter().toString(combo.getValue()));
    }

    public void verifyRouteOffered(final String name, final boolean expected) {
        assertEquals(expected, routeMenu().getItems().stream().anyMatch(
                item -> item.getText().equals(name) && !item.isDisable()));
    }

    public void verifyRouteStored(final String name, final boolean expected) {
        assertEquals(expected, getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().anyMatch(route -> route.getName().equals(name)));
    }

    /**
     * @param expected the colony name, {@code Europe}, or {@code lost <id>} for
     *                 a stop whose colony no longer exists
     */
    public void verifyStoredStop(final String routeName, final int stopIndex,
            final String expected) {
        final TradeRouteStop stop = getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(route -> route.getName().equals(routeName))
                .findFirst().orElseThrow().getStops().get(stopIndex);
        assertEquals(expected,
                stop.isEurope() ? "Home port"
                        : getModel().getColonyById(stop.getColonyId())
                                .map(colony -> colony.getName())
                                .orElse("lost " + stop.getColonyId()));
    }

    /** The stop the carrier accepted at its last boundary, not the route's. */
    public void verifyAcceptedStop(final int unitId, final int stopIndex,
            final String expected) {
        final TradeRouteStop stop = getModel()
                .getTradeRouteAssignment(carrier(unitId)).orElseThrow()
                .getStops().get(stopIndex);
        assertEquals(expected,
                stop.isEurope() ? "Home port"
                        : getModel().getColonyById(stop.getColonyId())
                                .map(colony -> colony.getName())
                                .orElse("lost " + stop.getColonyId()));
    }

    public void verifyPauseReason(final int unitId,
            final org.akropia.model.TradeRoutePause expected) {
        assertEquals(java.util.Optional.of(expected),
                getModel().getTradeRouteAssignment(carrier(unitId))
                        .orElseThrow().getPause());
    }

    public void verifyNotPaused(final int unitId) {
        assertTrue(getModel().getTradeRouteAssignment(carrier(unitId))
                .orElseThrow().getPause().isEmpty());
    }

    /** The ship is on the Atlantic with this many turns of voyage left. */
    public void verifyAtSea(final int unitId, final int remainingTurns) {
        final Unit ship = carrier(unitId);
        assertTrue(ship.isAtHighSea());
        assertEquals(remainingTurns,
                ((org.akropia.model.PlaceHighSea) ship.getPlace())
                        .getRemainigTurns());
    }

    public void verifyNotAtSea(final int unitId) {
        assertFalse(carrier(unitId).isAtHighSea());
    }

    public void verifyGold(final int expected) {
        assertEquals(expected,
                getModel().getColonyById(0).orElseThrow().getOwner().getGold());
    }

    /** The unit obeys its owner again: no route or go-to order holds it. */
    public void verifyNoStandingOrder(final int unitId) {
        assertTrue(
                getModel().getTradeRouteAssignment(carrier(unitId)).isEmpty());
        assertTrue(getModel().getGoToDestination(carrier(unitId)).isEmpty());
    }

    /**
     * @param expected movement left this turn, in thirds of a point; a release
     *                 neither refunds nor takes away movement
     */
    public void verifyMovementThirds(final int unitId, final int expected) {
        assertEquals(expected, carrier(unitId).getMovementThirds());
    }

    /** The whole dialog is inside the screen's usable area. */
    public void verifyDialogFitsScreen() {
        final Window window = getNodeFinder().lookup("#tradeRouteCargoDialog")
                .query().getScene().getWindow();
        final javafx.geometry.Rectangle2D screen = Screen.getPrimary()
                .getVisualBounds();
        assertTrue(window.getHeight() <= screen.getHeight(),
                "Dialog is taller than the screen: " + window.getHeight());
        assertTrue(window.getWidth() <= screen.getWidth(),
                "Dialog is wider than the screen: " + window.getWidth());
        assertTrue(window.getY() >= screen.getMinY() - 1,
                "Dialog starts above the screen: " + window.getY());
        assertTrue(window.getY() + window.getHeight() <= screen.getMaxY() + 1,
                "Dialog ends below the screen: " + window.getY());
    }

    public void verifyUnitAt(final int unitId, final Location expected) {
        assertEquals(expected, carrier(unitId).getLocation());
    }

    private Location rememberedLocation;

    /** Reads and keeps the unit's square, so a later check can compare it. */
    public void rememberUnitLocation(final int unitId) {
        rememberedLocation = carrier(unitId).getLocation();
    }

    public void verifyUnitAtRememberedLocation(final int unitId) {
        assertNotNull(rememberedLocation, "No location was remembered");
        assertEquals(rememberedLocation, carrier(unitId).getLocation());
    }

    /**
     * Several squares between two stops can cost the same, so this checks that
     * the unit took exactly one step: it stands next to both squares.
     */
    public void verifyUnitBetween(final int unitId, final Location from,
            final Location to) {
        final Location at = carrier(unitId).getLocation();
        assertNotEquals(from, at);
        assertNotEquals(to, at);
        assertTrue(at.isNeighbor(from), "Not next to " + from + ": " + at);
        assertTrue(at.isNeighbor(to), "Not next to " + to + ": " + at);
    }
}
