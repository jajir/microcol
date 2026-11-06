package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteAssignment;
import org.akropia.model.TradeRouteMode;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * Trade route management with the tabs Stations, Carriers and Goods. Choices
 * are made through the visible tabs, rows, matrix cells, menus and buttons as a
 * player would; a control in another tab is reached by first clicking that tab.
 * The model is only read to verify the outcome.
 */
public final class TradeRouteCargoDialog extends AbstractScreen {

    private static final String STATIONS_TAB = "tradeRouteStationsTab";
    private static final String CARRIERS_TAB = "tradeRouteCarriersTab";
    private static final String GOODS_TAB = "tradeRouteGoodsTab";

    /** Row of the goods matrix that the add/verify goods methods address. */
    private int chosenStop;

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

    public void openStationsTab() {
        clickTab(STATIONS_TAB);
    }

    public void openCarriersTab() {
        clickTab(CARRIERS_TAB);
    }

    public void openGoodsTab() {
        clickTab(GOODS_TAB);
    }

    /**
     * Moves to the next tab with the keyboard: the focus goes to the tab row
     * and the Right arrow selects the following tab.
     */
    public void openNextTabWithKeyboard() {
        getRobot().interact(() -> tabs().requestFocus());
        getRobot().push(KeyCode.RIGHT);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Addresses one stop row of the goods matrix and shows the Goods tab.
     *
     * @param index zero-based stop index of the chosen route
     */
    public void chooseStop(final int index) {
        chosenStop = index;
        openGoodsTab();
    }

    /** Clicks the cell of the chosen stop until it unloads the goods. */
    public void addUnload(final GoodsType type) {
        setCell(chosenStop, type, "−");
    }

    /** Clicks the cell of the chosen stop until it loads the goods. */
    public void addLoad(final GoodsType type) {
        setCell(chosenStop, type, "+");
    }

    /** Clicks the cell of the chosen stop until it ignores the goods. */
    public void ignoreGoods(final GoodsType type) {
        setCell(chosenStop, type, "");
    }

    /** One mouse click on a matrix cell. */
    public void clickCell(final int stop, final GoodsType type) {
        final Button cell = cell(stop, type);
        showTabOf(cell);
        reveal(cell);
        getRobot().clickOn(cell);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Focuses a matrix cell and activates it with the Space key. */
    public void cycleCellWithKeyboard(final int stop, final GoodsType type) {
        final Button cell = cell(stop, type);
        showTabOf(cell);
        reveal(cell);
        getRobot().interact(cell::requestFocus);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void setCell(final int stop, final GoodsType type,
            final String sign) {
        for (int i = 0; i < 3
                && !cell(stop, type).getText().equals(sign); i++) {
            clickCell(stop, type);
        }
    }

    public void chooseWaitForCompletion() {
        clickRadio("tradeRouteModeWait");
    }

    public void chooseTouchAndGo() {
        clickRadio("tradeRouteModeTouch");
    }

    private void clickRadio(final String id) {
        final RadioButton radio = getNodeFinder().lookup("#" + id)
                .queryAs(RadioButton.class);
        showTabOf(radio);
        reveal(radio);
        getRobot().clickOn(radio);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void save() {
        press("tradeRouteCargoSave");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void saveWithKeyboard() {
        pressWithKeyboard("tradeRouteCargoSave");
    }

    public void discardChanges() {
        press("tradeRouteCargoDiscard");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void process() {
        press("tradeRouteCargoProcess");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Presses Back once; unsaved changes need a second press. */
    public void close() {
        press("tradeRouteCargoClose");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** @param index zero-based position among the carriers at the stops */
    public void chooseCarrier(final int index) {
        final ComboBox<?> combo = getNodeFinder()
                .lookup("#tradeRouteCargoCarrier").queryComboBox();
        showTabOf(combo);
        expandSectionOf(combo);
        choose(combo, index);
    }

    // Stations

    public void moveStationUp(final int index) {
        press("tradeRouteStationUp-" + index);
    }

    public void moveStationDown(final int index) {
        press("tradeRouteStationDown-" + index);
    }

    public void moveStationUpWithKeyboard(final int index) {
        pressWithKeyboard("tradeRouteStationUp-" + index);
    }

    public void removeStation(final int index) {
        press("tradeRouteStationRemove-" + index);
    }

    /** @param name the colony name, or the text of Home port, as offered */
    public void addStation(final String name) {
        final ComboBox<TradeRouteStop> combo = addStationBox();
        showTabOf(combo);
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
        press("tradeRouteAddStationButton");
    }

    /**
     * Replaces the stop at {@code index} as a player does in the Stations tab:
     * remove it, add the new destination at the end and move it up to the same
     * place. The change is staged until Save.
     */
    public void replaceStation(final int index, final String name) {
        removeStation(index);
        addStation(name);
        for (int at = stationCount() - 1; at > index; at--) {
            moveStationUp(at);
        }
    }

    private int stationCount() {
        int count = 0;
        while (getNodeFinder().lookup("#tradeRouteStation-" + count).tryQuery()
                .isPresent()) {
            count++;
        }
        return count;
    }

    // Carriers

    /** Opens Add carrier and picks the unit by its ID. */
    public void addCarrier(final int unitId) {
        openAddCarrierList();
        chooseOfferedCarrier(unitId);
    }

    /** Opens the Add carrier list and leaves it open. */
    public void openAddCarrierList() {
        openAddCarrierMenu();
    }

    /** Picks a unit by its ID in the open Add carrier list. */
    public void chooseOfferedCarrier(final int unitId) {
        final Node item = getNodeFinder()
                .lookup("#tradeRouteAddCarrier-" + unitId).query();
        getRobot().clickOn(item);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Closes the open Add carrier list without a choice. */
    public void dismissAddCarrierList() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Opens Add carrier with the keyboard, walks the entries with Down until
     * the unit has the popup focus and takes it with Enter.
     */
    public void addCarrierWithKeyboard(final int unitId) {
        final MenuButton menu = addCarrierMenu();
        showTabOf(menu);
        reveal(menu);
        getRobot().interact(menu::requestFocus);
        getRobot().push(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
        final String itemId = "tradeRouteAddCarrier-" + unitId;
        for (int i = 0; i <= menu.getItems().size()
                && !isPopupItemFocused(itemId); i++) {
            getRobot().push(KeyCode.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void openAddCarrierMenu() {
        final MenuButton menu = addCarrierMenu();
        showTabOf(menu);
        reveal(menu);
        getRobot().clickOn(menu);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void removeCarrier(final int unitId) {
        press("tradeRouteCarrierRemove-" + unitId);
    }

    public void removeCarrierWithKeyboard(final int unitId) {
        pressWithKeyboard("tradeRouteCarrierRemove-" + unitId);
    }

    // Delete

    /** First press: names the affected carriers, deletes nothing yet. */
    public void askToDeleteRoute() {
        press("tradeRouteDelete");
    }

    /** Second press on the same route: now the route is deleted. */
    public void confirmDeleteRoute() {
        press("tradeRouteDelete");
    }

    public void askToDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    public void confirmDeleteRouteWithKeyboard() {
        pressWithKeyboard("tradeRouteDelete");
    }

    /**
     * Closes the screen with the Escape key, as a player abandoning it. The
     * focus is first moved to Back, because a focused choice box may take
     * Escape for itself.
     */
    public void closeWithEscape() {
        final Button button = getButtonById("tradeRouteCargoClose");
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void pressWithKeyboard(final String buttonId) {
        final Button button = getButtonById(buttonId);
        showTabOf(button);
        expandSectionOf(button);
        reveal(button);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Presses a button after showing its tab and scrolling it into view, so a
     * screen taller than a small window is operated as a player would.
     */
    private void press(final String buttonId) {
        final Button button = getButtonById(buttonId);
        showTabOf(button);
        expandSectionOf(button);
        reveal(button);
        requireShown(button);
        clickOnButtonWithId(buttonId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Stops the scenario before a click that would land on another control: the
     * node must be shown and lie inside the window after revealing it.
     */
    private void requireShown(final Node node) {
        final Bounds bounds = sceneBounds(node);
        final double width = getPrimaryStage().getScene().getWidth();
        final double height = getPrimaryStage().getScene().getHeight();
        if (!node.isVisible() || bounds.getMinX() < 0
                || bounds.getMaxX() > width || bounds.getMinY() < 0
                || bounds.getMaxY() > height) {
            throw new IllegalStateException(
                    "Control is not shown in the window: " + node.getId() + " "
                            + bounds);
        }
        // A control in a tab or in the scrolled page must also lie inside
        // the area that shows it, not in a clipped part.
        for (final String clip : List.of("#tradeRouteTabs",
                "#tradeRouteCargoScroll")) {
            getNodeFinder().lookup(clip).tryQuery()
                    .filter(area -> isInside(node, area)).ifPresent(area -> {
                        final Bounds shown = sceneBounds(area);
                        if (bounds.getMinY() < shown.getMinY() - 1
                                || bounds.getMaxY() > shown.getMaxY() + 1) {
                            throw new IllegalStateException(
                                    "Control is clipped: " + node.getId() + " "
                                            + bounds + " in " + shown);
                        }
                    });
        }
    }

    private void clickTab(final String tabId) {
        final Tab tab = tab(tabId);
        if (!tab.isSelected()) {
            final Node header = getNodeFinder().lookup("#" + tabId).query();
            getRobot().clickOn(header);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    /** Clicks the tab whose content holds the node, when not shown yet. */
    private void showTabOf(final Node node) {
        getNodeFinder().lookup("#tradeRouteTabs").tryQuery()
                .ifPresent(found -> {
                    for (final Tab tab : ((TabPane) found).getTabs()) {
                        if (isInside(node, tab.getContent())) {
                            clickTab(tab.getId());
                        }
                    }
                });
    }

    private static boolean isInside(final Node node, final Node ancestor) {
        for (Node at = node; at != null; at = at.getParent()) {
            if (at == ancestor) {
                return true;
            }
        }
        return false;
    }

    private void expandSectionOf(final Node node) {
        for (Parent parent = node.getParent(); parent != null; parent = parent
                .getParent()) {
            if (parent instanceof TitledPane
                    && !((TitledPane) parent).isExpanded()) {
                reveal(parent);
                getRobot().clickOn(parent.lookup(".title"));
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
    }

    /** Scrolls the screen, when it has a scroll bar, until the node shows. */
    private void reveal(final Node node) {
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

    private MenuButton addCarrierMenu() {
        return getNodeFinder().lookup("#tradeRouteAddCarrier")
                .queryAs(MenuButton.class);
    }

    private TabPane tabs() {
        return getNodeFinder().lookup("#tradeRouteTabs").queryAs(TabPane.class);
    }

    private Tab tab(final String id) {
        return tabs().getTabs().stream().filter(tab -> id.equals(tab.getId()))
                .findFirst().orElseThrow();
    }

    private Button cell(final int stop, final GoodsType type) {
        return getButtonById("tradeRouteCell-" + stop + "-" + type.name());
    }

    private ComboBox<TradeRouteStop> addStationBox() {
        return getNodeFinder().lookup("#tradeRouteAddStation").queryComboBox();
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

    private Unit carrier(final int unitId) {
        // A ship on the Atlantic is not on the map, so every unit is searched.
        return getModel().getColonyById(0).orElseThrow().getOwner()
                .getAllUnits().stream().filter(unit -> unit.getId() == unitId)
                .findFirst().orElseThrow();
    }

    private List<TradeRoute> storedRoutes(final String routeName) {
        return getModel()
                .getTradeRoutes(
                        getModel().getColonyById(0).orElseThrow().getOwner())
                .stream().filter(route -> route.getName().equals(routeName))
                .collect(Collectors.toList());
    }

    /** Signs of the chosen stop's row, as the matrix shows them now. */
    private List<GoodsType> rowGoods(final String sign) {
        final List<GoodsType> goods = new ArrayList<>();
        for (final GoodsType type : TradeRouteStop.TRANSPORTABLE_GOODS) {
            if (cell(chosenStop, type).getText().equals(sign)) {
                goods.add(type);
            }
        }
        return goods;
    }

    /**
     * Tabs fill the width under the header; the header keeps Back at the right
     * edge also in a narrow window.
     */
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
            final Bounds summary = sceneBounds(
                    getNodeFinder().lookup("#tradeRouteCargoSummary").query());
            final Bounds tabBounds = sceneBounds(tabs());
            final Bounds actions = sceneBounds(
                    getNodeFinder().lookup("#tradeRouteDraftActions").query());
            assertTrue(summary.getMaxY() <= tabBounds.getMinY() + 1,
                    "The route kind and stop count are above the tabs");
            assertTrue(tabBounds.getMaxY() <= actions.getMinY() + 1,
                    "Save and Cancel changes follow the tabs");
            assertEquals(3, tabs().getTabs().size());
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

    /** @param expected tab titles, Stations, Carriers and Goods, in order */
    public void verifyTabs(final String... expected) {
        assertEquals(List.of(expected), tabs().getTabs().stream()
                .map(Tab::getText).collect(Collectors.toList()));
    }

    /** @param expected title of the tab shown now */
    public void verifySelectedTab(final String expected) {
        assertEquals(expected,
                tabs().getSelectionModel().getSelectedItem().getText());
    }

    /** @param expectedName route shown as chosen when the screen opened */
    public void verifyChosenRoute(final String expectedName) {
        assertEquals(expectedName, routeMenu().getText());
    }

    /** @param expected Unicode code points of the name shown in the menu */
    public void verifyChosenRouteLength(final int expected) {
        final String text = routeMenu().getText();
        assertEquals(expected, text.codePointCount(0, text.length()));
    }

    /** @param expected type and stop count shown above the tabs */
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
        final Button back = getButtonById("tradeRouteCargoClose");
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
        final Button back = getButtonById("tradeRouteCargoClose");
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

    /** @param expected numbered station rows of the draft, in order */
    public void verifyStopChoices(final String... expected) {
        final Pane rows = getNodeFinder().lookup("#tradeRouteStationRows")
                .queryAs(Pane.class);
        final List<String> shown = new ArrayList<>();
        for (int i = 0; i < rows.getChildren().size(); i++) {
            getNodeFinder().lookup("#tradeRouteStationName-" + i).tryQuery()
                    .ifPresent(node -> shown.add(((Label) node).getText()));
        }
        assertEquals(List.of(expected), shown);
    }

    /** @param expected numbered stop rows of the goods matrix, in order */
    public void verifyGoodsRows(final String... expected) {
        final List<String> shown = new ArrayList<>();
        for (int i = 0; getNodeFinder().lookup("#tradeRouteGoodsStop-" + i)
                .tryQuery().isPresent(); i++) {
            shown.add(getLabeledById("tradeRouteGoodsStop-" + i).getText());
        }
        assertEquals(List.of(expected), shown);
    }

    /**
     * Column headers show only icons, in the fixed colony goods order, and name
     * each goods for assistive technology.
     */
    public void verifyGoodsColumns(final List<GoodsType> expectedOrder,
            final List<String> expectedNames) {
        final List<GoodsType> shown = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            getNodeFinder().lookup("#tradeRouteGoodsHeader-" + type.name())
                    .tryQuery().ifPresent(node -> {
                        final Label header = (Label) node;
                        assertEquals("", header.getText());
                        assertNotNull(header.getGraphic());
                        shown.add(type);
                        names.add(header.getAccessibleText());
                    });
        }
        assertEquals(expectedOrder, shown);
        assertEquals(expectedNames, names);
    }

    /**
     * @param expectedSign       {@code +}, {@code −} or empty
     * @param expectedAccessible the text assistive technology reads
     */
    public void verifyCell(final int stop, final GoodsType type,
            final String expectedSign, final String expectedAccessible) {
        final Button cell = cell(stop, type);
        assertEquals(expectedSign, cell.getText());
        assertEquals(expectedAccessible, cell.getAccessibleText());
    }

    /** @param expected the cell with keyboard focus */
    public void verifyCellFocused(final int stop, final GoodsType type) {
        assertSame(cell(stop, type),
                getPrimaryStage().getScene().getFocusOwner());
    }

    /** The route menu takes the keyboard focus when the screen opens. */
    public void verifyRouteMenuFocused() {
        assertSame(routeMenu(), getPrimaryStage().getScene().getFocusOwner());
    }

    public void verifyTitle(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCargoTitle").getText());
    }

    /** @param expected goods the chosen stop unloads, in fixed goods order */
    public void verifyUnloadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), rowGoods("−"));
    }

    /** @param expected goods the chosen stop loads, in fixed goods order */
    public void verifyLoadOrder(final GoodsType... expected) {
        assertEquals(List.of(expected), rowGoods("+"));
    }

    /** @param expectedWait whether the route waits at every stop */
    public void verifyModeChoice(final boolean expectedWait,
            final String expectedTouchText, final String expectedWaitText) {
        final RadioButton touch = getNodeFinder().lookup("#tradeRouteModeTouch")
                .queryAs(RadioButton.class);
        final RadioButton wait = getNodeFinder().lookup("#tradeRouteModeWait")
                .queryAs(RadioButton.class);
        assertEquals(expectedWait, wait.isSelected());
        assertEquals(!expectedWait, touch.isSelected());
        assertEquals(expectedTouchText, touch.getText());
        assertEquals(expectedWaitText, wait.getText());
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

    /** @param expected the model's refusal of the draft, or a pending note */
    public void verifyStationStatus(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteStationStatus").getText());
    }

    /** Which row actions a station offers, so the order stays legal. */
    public void verifyStationButtons(final int index, final boolean expectedUp,
            final boolean expectedDown, final boolean expectedRemove) {
        assertEquals(expectedUp,
                !getButtonById("tradeRouteStationUp-" + index).isDisabled());
        assertEquals(expectedDown,
                !getButtonById("tradeRouteStationDown-" + index).isDisabled());
        assertEquals(expectedRemove,
                !getButtonById("tradeRouteStationRemove-" + index)
                        .isDisabled());
    }

    /** @param expected the destinations Add offers, in order */
    public void verifyAddStationOffers(final String... expected) {
        final ComboBox<TradeRouteStop> combo = addStationBox();
        assertEquals(List.of(expected),
                combo.getItems().stream().map(combo.getConverter()::toString)
                        .collect(Collectors.toList()));
    }

    /**
     * One carrier row: type and ID, the stop it heads to or stands at, and its
     * state with the reason it waits.
     */
    public void verifyCarrierRow(final int unitId, final String expectedName,
            final String expectedDestination, final String expectedState) {
        assertEquals(expectedName,
                getLabeledById("tradeRouteCarrierName-" + unitId).getText());
        assertEquals(expectedDestination,
                getLabeledById("tradeRouteCarrierDestination-" + unitId)
                        .getText());
        assertEquals(expectedState,
                getLabeledById("tradeRouteCarrierState-" + unitId).getText());
    }

    /** @param expected unit IDs of the carrier rows, in order */
    public void verifyCarrierRows(final Integer... expected) {
        final Pane rows = getNodeFinder().lookup("#tradeRouteCarrierRows")
                .queryAs(Pane.class);
        assertEquals(List.of(expected),
                rows.getChildren().stream().map(Node::getId)
                        .filter(id -> id != null
                                && id.startsWith("tradeRouteCarrierRow-"))
                        .map(id -> Integer.valueOf(
                                id.substring("tradeRouteCarrierRow-".length())))
                        .collect(Collectors.toList()));
    }

    /** @param expected the sentence shown when no carrier follows the route */
    public void verifyNoCarrierRow(final String expected) {
        assertEquals(expected,
                getLabeledById("tradeRouteCarrierNone").getText());
    }

    /**
     * One hold of a carrier row: its shown amount and the name read by
     * assistive technology.
     */
    public void verifyCarrierSlot(final int unitId, final int slot,
            final String expectedText, final String expectedAccessible) {
        final Label box = (Label) getLabeledById(
                "tradeRouteCarrierSlot-" + unitId + "-" + slot);
        assertEquals(expectedText, box.getText());
        assertEquals(expectedAccessible, box.getAccessibleText());
    }

    /** @param expected carriers the open Add carrier list offers, in order */
    public void verifyAddCarrierOffers(final String... expected) {
        assertEquals(List.of(expected), addCarrierMenu().getItems().stream()
                .map(MenuItem::getText).collect(Collectors.toList()));
    }

    /** @param name carrier type and ID as the open Add carrier list shows */
    public void verifyAddCarrierOffered(final String name,
            final boolean expected) {
        assertEquals(expected, addCarrierMenu().getItems().stream().anyMatch(
                item -> item.getText().equals(name) && !item.isDisable()));
    }

    public void verifyCarrierRemoveText(final int unitId,
            final String expected) {
        assertEquals(expected,
                getButtonById("tradeRouteCarrierRemove-" + unitId).getText());
    }

    public void verifyAddCarrierText(final String expected) {
        assertEquals(expected, addCarrierMenu().getText());
    }

    /** @param expected the status bar of the route screen, as shown now */
    public void verifyStatusBar(final String expected) {
        final Parent page = getNodeFinder().lookup("#tradeRouteCargoPage")
                .queryAs(Parent.class);
        final Label label = (Label) page.lookup("#"
                + org.akropia.gui.screen.game.components.StatusBarView.STATUS_BAR_LABEL_ID);
        assertEquals(expected, label.getText());
    }

    /**
     * Every column of the goods matrix and the mode choice are inside the
     * window width, so no cell needs horizontal scrolling.
     */
    public void verifyGoodsMatrixFits() {
        layout();
        final double width = getPrimaryStage().getScene().getWidth();
        for (final GoodsType type : TradeRouteStop.TRANSPORTABLE_GOODS) {
            final Bounds header = sceneBounds(getNodeFinder()
                    .lookup("#tradeRouteGoodsHeader-" + type.name()).query());
            assertTrue(header.getMinX() >= 0 && header.getMaxX() <= width,
                    "Goods column outside the window: " + type + " " + header);
        }
        final Bounds wait = sceneBounds(
                getNodeFinder().lookup("#tradeRouteModeWait").query());
        assertTrue(wait.getMaxX() <= width, "Mode choice cut: " + wait);
    }

    /**
     * The carrier row shows Remove and every hold inside the window width.
     *
     * @param expectedHolds number of hold boxes the carrier shows
     */
    public void verifyCarrierRowFits(final int unitId,
            final int expectedHolds) {
        layout();
        final double width = getPrimaryStage().getScene().getWidth();
        final Pane cargo = getNodeFinder()
                .lookup("#tradeRouteCarrierCargo-" + unitId)
                .queryAs(Pane.class);
        assertEquals(expectedHolds, cargo.getChildren().size());
        for (final Node box : cargo.getChildren()) {
            assertTrue(sceneBounds(box).getMaxX() <= width,
                    "Hold outside the window: " + sceneBounds(box));
        }
        final Bounds remove = sceneBounds(
                getButtonById("tradeRouteCarrierRemove-" + unitId));
        assertTrue(remove.getMinX() >= 0 && remove.getMaxX() <= width,
                "Remove outside the window: " + remove);
    }

    /** Up, Down and Remove of a station row are inside the window width. */
    public void verifyStationRowFits(final int index) {
        layout();
        final double width = getPrimaryStage().getScene().getWidth();
        for (final String action : List.of("Up", "Down", "Remove")) {
            final Bounds button = sceneBounds(
                    getButtonById("tradeRouteStation" + action + "-" + index));
            assertTrue(button.getMinX() >= 0 && button.getMaxX() <= width,
                    action + " outside the window: " + button);
        }
    }

    private void layout() {
        getRobot().interact(() -> {
            getPrimaryStage().getScene().getRoot().applyCss();
            getPrimaryStage().getScene().getRoot().layout();
        });
    }

    private Integer rememberedGold;
    private String rememberedCargo;

    /** Keeps the treasury and the carrier's holds for a later comparison. */
    public void rememberGoldAndCargo(final int unitId) {
        rememberedGold = getModel().getColonyById(0).orElseThrow().getOwner()
                .getGold();
        rememberedCargo = cargoText(unitId);
    }

    private String cargoText(final int unitId) {
        return ((org.akropia.model.unit.UnitWithCargo) carrier(unitId))
                .getCargo().getSlots().stream()
                .map(slot -> slot.getGoods().map(Goods::toString)
                        .orElse(slot.getUnit().isPresent() ? "unit" : "-"))
                .collect(Collectors.joining(","));
    }

    /** Nothing was sold or bought again since the remembered state. */
    public void verifyGoldAndCargoUnchanged(final int unitId) {
        assertNotNull(rememberedGold, "Nothing was remembered");
        assertEquals(rememberedGold.intValue(),
                getModel().getColonyById(0).orElseThrow().getOwner().getGold());
        assertEquals(rememberedCargo, cargoText(unitId));
    }

    /** The treasury holds more than when it was remembered. */
    public void verifyGoldIncreased() {
        assertNotNull(rememberedGold, "Nothing was remembered");
        assertTrue(getModel().getColonyById(0).orElseThrow().getOwner()
                .getGold() > rememberedGold);
    }

    /**
     * The hold contains some, but not a full hold, of the goods.
     */
    public void verifyHoldPartlyFilled(final int unitId, final int slotIndex,
            final GoodsType expectedType) {
        final org.akropia.model.CargoSlot slot = ((org.akropia.model.unit.UnitWithCargo) carrier(
                unitId)).getCargo().getSlotByIndex(slotIndex);
        final Goods goods = slot.getGoods().orElseThrow();
        assertEquals(expectedType, goods.getType());
        assertTrue(goods.getAmount() > 0 && slot.getAvailableCapacity() > 0,
                "Not a partly filled hold: " + goods);
    }

    /** The treasury cannot pay for even one more unit of the goods. */
    public void verifyGoldBelowPriceOf(final GoodsType expectedType) {
        final org.akropia.model.Player owner = getModel().getColonyById(0)
                .orElseThrow().getOwner();
        assertTrue(owner.getGold() < owner
                .quoteEuropeTrade(Goods.of(expectedType, 1), true).getTotal(),
                "Gold " + owner.getGold() + " still buys " + expectedType);
    }

    public void verifyStoredCargo(final String routeName, final int stopIndex,
            final List<GoodsType> expectedUnload,
            final List<GoodsType> expectedLoad) {
        final List<TradeRoute> matching = storedRoutes(routeName);
        assertEquals(1, matching.size());
        final TradeRoute route = matching.get(0);
        assertEquals(expectedUnload,
                route.getStops().get(stopIndex).getUnloadGoods());
        assertEquals(expectedLoad,
                route.getStops().get(stopIndex).getLoadGoods());
    }

    public void verifyStoredMode(final String routeName,
            final TradeRouteMode expected) {
        final List<TradeRoute> matching = storedRoutes(routeName);
        assertEquals(1, matching.size());
        assertEquals(expected, matching.get(0).getMode());
    }

    /** @param expected colony names or {@code Home port}, in order */
    public void verifyStoredStops(final String routeName,
            final String... expected) {
        final List<TradeRoute> matching = storedRoutes(routeName);
        assertEquals(1, matching.size());
        assertEquals(List.of(expected),
                matching.get(0).getStops().stream()
                        .map(stop -> stop.isEurope() ? "Home port"
                                : getModel().getColonyById(stop.getColonyId())
                                        .map(colony -> colony.getName())
                                        .orElse("lost " + stop.getColonyId()))
                        .collect(Collectors.toList()));
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

    /** The unit is no longer on the square it left. */
    public void verifyUnitLeft(final int unitId, final Location left) {
        final Unit unit = carrier(unitId);
        assertFalse(unit.isAtPlaceLocation() && left.equals(unit.getLocation()),
                "Unit is still at " + left);
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
