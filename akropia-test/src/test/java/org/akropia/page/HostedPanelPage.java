package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.gui.screen.menu.GameMenuBackground;
import org.akropia.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import com.google.inject.name.Names;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

/**
 * Settings and Load in the menu sea or the in-game frame (R-0377): player keys,
 * scrolling and explicit checks of host, bounds and origin.
 */
public final class HostedPanelPage extends AbstractScreen {

    private final Map<Labeled, Double> fonts = new IdentityHashMap<>();
    private Optional<Unit> selectedUnit;
    private double viewX;
    private double viewY;

    public HostedPanelPage(final TestContext context) {
        super(context);
    }

    private Scene scene() {
        return getContext().getPrimaryStage().getScene();
    }

    private ScrollPane scroll() {
        final Node sea = scene().lookup(".sea-scroll");
        if (sea != null) {
            return (ScrollPane) sea;
        }
        return (ScrollPane) scene().lookup(".in-game-page .screen-scroll");
    }

    /** Press the platform shortcut: Command on macOS, Ctrl elsewhere. */
    public void pressShortcut(final KeyCode key) {
        getRobot().push(isMacPlatform() ? KeyCode.COMMAND : KeyCode.CONTROL,
                key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void press(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void click(final String id) {
        clickOnButtonWithId(id);
    }

    /** Choose one entry of the saved-games filter menu. */
    public void chooseFilter(final String itemId) {
        clickOnButtonWithId("savedGamesFilter");
        getRobot().clickOn("#" + itemId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Choose one entry of the saved-games filter menu with the keyboard only:
     * Shift+Tab from the list to the filter, Down opens it, further Down
     * presses focus the item and Enter chooses it.
     */
    public void chooseFilterWithKeyboard(final String itemId) {
        final javafx.scene.control.MenuButton filter = (javafx.scene.control.MenuButton) scene()
                .lookup("#savedGamesFilter");
        for (int i = 0; i < 10 && !filter.isFocused(); i++) {
            getRobot().push(KeyCode.SHIFT, KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        // Down opens the popup without focusing an entry; later Down presses
        // move through the entries until the wanted one owns the popup focus.
        getRobot().push(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
        for (int i = 0; i <= filter.getItems().size()
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

    /**
     * Show the main menu through the shared screen transition while the loaded
     * game stays in memory, the state that used to select the in-game host.
     */
    public void showMainMenuOverLiveGame() {
        getRobot().interact(() -> getContext()
                .getClassFromGuice(com.google.common.eventbus.EventBus.class)
                .post(new org.akropia.gui.screen.ShowScreenEvent(
                        org.akropia.gui.screen.Screen.MENU)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Scroll the panel content to its end with the mouse wheel. */
    public void scrollContentToEnd() {
        final ScrollPane scroll = scroll();
        getRobot().moveTo(scroll);
        for (int step = 0; step < 40 && scroll.getVvalue() < 1; step++) {
            getRobot().scroll(3, VerticalDirection.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void rememberFonts() {
        fonts.clear();
        getRobot().interact(() -> labels(scroll().getContent()).forEach(
                labeled -> fonts.put(labeled, labeled.getFont().getSize())));
    }

    public void rememberMapView() {
        selectedUnit = getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit();
        viewX = mapView().getTopLeft().getX();
        viewY = mapView().getTopLeft().getY();
    }

    private VisibleAreaService mapView() {
        return getAkropia().getInjector().getInstance(com.google.inject.Key
                .get(VisibleAreaService.class, Names.named("game")));
    }

    private static List<Labeled> labels(final Node root) {
        final List<Labeled> found = new ArrayList<>();
        if (root instanceof Labeled) {
            found.add((Labeled) root);
        }
        if (root instanceof Parent) {
            ((Parent) root).getChildrenUnmodifiable()
                    .forEach(child -> found.addAll(labels(child)));
        }
        return found;
    }

    /**
     * The panel lies inside the menu's sea stripe, with its title above and the
     * return control below the scrolled content.
     */
    public void verifyMenuHosted(final String title, final String backId,
            final String back) {
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            final Scene scene = scene();
            assertNull(scene.lookup(".in-game-page"),
                    "A menu origin never shows the in-game frame");
            final Node panel = scene.lookup(".sea-panel");
            assertNotNull(panel);
            final Labeled heading = (Labeled) scene
                    .lookup(".sea-page .main-title");
            assertEquals(title, heading.getText());
            final Bounds sea = panel.localToScene(panel.getBoundsInLocal());
            final double seaTop = scene.getHeight() / 2
                    + GameMenuBackground.SEA_SHIFT
                    - GameMenuBackground.SEA_HEIGHT / 2d;
            assertEquals(seaTop, sea.getMinY(), 1.5, "Panel starts at the sea");
            assertEquals(seaTop + GameMenuBackground.SEA_HEIGHT, sea.getMaxY(),
                    1.5, "Panel ends at the sea");
            assertTrue(sea.getMinX() >= 0 && sea.getMaxX() <= scene.getWidth(),
                    "Panel fits the window width");
            final Bounds head = heading
                    .localToScene(heading.getBoundsInLocal());
            assertTrue(head.getMinY() >= 0 && head.getMaxY() <= sea.getMinY(),
                    "Title stays visible above the sea");
            final Button button = (Button) scene.lookup("#" + backId);
            assertEquals(back, button.getText());
            assertEquals(back, ((Text) button.lookup(".text")).getText(),
                    "Return label must not be truncated");
            final Bounds backBounds = button
                    .localToScene(button.getBoundsInLocal());
            final Bounds viewport = scroll()
                    .localToScene(scroll().getBoundsInLocal());
            assertTrue(sea.contains(backBounds), "Return stays in the sea");
            assertTrue(backBounds.getMinY() >= viewport.getMaxY() - 1,
                    "Return is outside the scrolled content");
        });
    }

    /** The panel uses the in-game frame without the menu sea. */
    public void verifyInGameHosted(final String title) {
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            assertNull(scene().lookup(".sea-panel"));
            final Node page = scene().lookup(".in-game-page");
            assertNotNull(page, "A game origin uses the in-game frame");
            assertEquals(title,
                    ((Labeled) page.lookup(".screen-title")).getText());
        });
    }

    /**
     * Only the content scrolls vertically: the bar appears exactly when the
     * content's minimum height exceeds the viewport, and nothing scrolls
     * horizontally.
     */
    public void verifyScrollBarFollowsOverflow() {
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            final ScrollPane scroll = scroll();
            final Node content = scroll.getContent();
            final Bounds viewport = scroll.getViewportBounds();
            // The content cannot shrink below its minimum height.
            final double needed = content.minHeight(viewport.getWidth());
            final boolean overflow = needed > viewport.getHeight() + 1;
            assertTrue(
                    content.getLayoutBounds()
                            .getHeight() >= Math.min(needed,
                                    viewport.getHeight()) - 1,
                    "Content keeps the height it needs");
            assertTrue(content.getLayoutBounds()
                    .getWidth() <= viewport.getWidth() + 1,
                    "No horizontal overflow");
            for (final Node node : scroll.getChildrenUnmodifiable()) {
                // Nested lists keep their own bars inside the content.
                if (!(node instanceof ScrollBar)) {
                    continue;
                }
                final ScrollBar bar = (ScrollBar) node;
                if (bar.getOrientation() == Orientation.HORIZONTAL) {
                    assertFalse(bar.isVisible(), "No horizontal scroll bar");
                } else {
                    assertEquals(overflow, bar.isVisible(),
                            "Vertical bar exactly on overflow: needed=" + needed
                                    + " viewport=" + viewport);
                }
            }
        });
    }

    /**
     * A session-menu item names the platform shortcut: Command on macOS, Ctrl
     * elsewhere.
     */
    public void verifyMenuShortcut(final String id, final String label,
            final KeyCode key) {
        final String shortcut = isMacPlatform() ? "⌘" + key.getName()
                : "Ctrl+" + key.getName();
        getRobot().interact(() -> {
            final Node item = getNodeFinder().lookup("#" + id).query();
            assertTrue(
                    labels(item).stream().map(Labeled::getText)
                            .anyMatch((label + " (" + shortcut + ")")::equals),
                    id + " shows " + shortcut);
        });
    }

    /**
     * Every visible label in the content shows its whole text within the
     * viewport width.
     */
    public void verifyCompleteTexts() {
        getRobot().interact(() -> {
            final ScrollPane scroll = scroll();
            final Bounds viewport = scroll
                    .localToScene(scroll.getBoundsInLocal());
            for (final Labeled labeled : labels(scroll.getContent())) {
                final Node text = labeled.lookup(".text");
                if (!labeled.isVisible() || labeled.getText() == null
                        || labeled.getText().isEmpty()
                        || !(text instanceof Text)) {
                    continue;
                }
                assertEquals(labeled.getText(), ((Text) text).getText(),
                        "Text must not be truncated");
                final Bounds bounds = labeled
                        .localToScene(labeled.getBoundsInLocal());
                assertTrue(bounds.getMaxX() <= viewport.getMaxX() + 1,
                        labeled.getText() + " fits the panel width");
            }
        });
    }

    /** Fonts keep the sizes remembered in another window size. */
    public void verifyFontsUnchanged() {
        getRobot().interact(() -> {
            assertFalse(fonts.isEmpty());
            fonts.forEach((labeled, size) -> assertEquals(size,
                    labeled.getFont().getSize(), 0.01, labeled.getText()));
        });
    }

    /** The common Settings appear in the same top-to-bottom order. */
    public void verifySettingsOrder(final List<String> ids) {
        getRobot().interact(() -> {
            double previousTop = Double.NEGATIVE_INFINITY;
            double previousLeft = Double.NEGATIVE_INFINITY;
            for (final String id : ids) {
                final Node node = scene().lookup("#" + id);
                assertNotNull(node, id);
                final javafx.geometry.Bounds bounds = node
                        .localToScene(node.getBoundsInLocal());
                final double top = bounds.getMinY();
                final double left = bounds.getMinX();
                assertTrue(
                        top > previousTop + 0.5
                                || Math.abs(top - previousTop) <= 0.5
                                        && left > previousLeft,
                        id + " keeps its reading order");
                previousTop = top;
                previousLeft = left;
            }
        });
    }

    /** Title and return remain visible after scrolling the content. */
    public void verifyTitleAndBackVisible(final String backId) {
        getRobot().interact(() -> {
            final Scene scene = scene();
            final Node title = Optional
                    .ofNullable(scene.lookup(".sea-page .main-title"))
                    .orElse(scene.lookup(".in-game-page .screen-title"));
            final Node back = scene.lookup("#" + backId);
            for (final Node node : List.of(title, back)) {
                final Bounds bounds = node
                        .localToScene(node.getBoundsInLocal());
                assertTrue(
                        node.isVisible() && bounds.getMinY() >= 0
                                && bounds.getMaxY() <= scene.getHeight(),
                        node + " stays visible");
            }
            final ScrollPane scroll = scroll();
            final boolean overflow = scroll.getContent()
                    .minHeight(scroll.getViewportBounds().getWidth()) > scroll
                            .getViewportBounds().getHeight() + 1;
            assertEquals(overflow, scroll.getVvalue() > 0,
                    "Only overflowing content scrolls");
        });
    }

    /** The map shows the same view and selection as before. */
    public void verifyMapViewUnchanged() {
        assertEquals(selectedUnit,
                getContext().getClassFromGuice(SelectedUnitManager.class)
                        .getSelectedUnit());
        assertEquals(viewX, mapView().getTopLeft().getX(), 0.01);
        assertEquals(viewY, mapView().getTopLeft().getY(), 0.01);
    }

    /** No Load page was opened. */
    public void verifyLoadClosed() {
        WaitForAsyncUtils.waitForFxEvents();
        assertNull(scene().lookup("#savedGamesList"));
    }

    /** The Load list keeps the given save selected. */
    public void verifySelectedSave(final String name) {
        @SuppressWarnings("unchecked")
        final javafx.scene.control.ListView<org.akropia.model.campaign.po.SavedGames.Entry> list = (javafx.scene.control.ListView<org.akropia.model.campaign.po.SavedGames.Entry>) scene()
                .lookup("#savedGamesList");
        assertNotNull(list.getSelectionModel().getSelectedItem());
        assertEquals(name,
                list.getSelectionModel().getSelectedItem().getName());
    }

    /** Exactly one Load page exists, inside the main window. */
    public void verifySingleLoadPage() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1,
                getNodeFinder().lookup("#savedGamesList").queryAll().size());
        assertNotNull(scene().lookup("#savedGamesList"));
    }

    /** The saved-games filter shows the given view name. */
    public void verifyFilterLabel(final String label) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(label,
                ((Labeled) scene().lookup("#savedGamesFilter")).getText());
    }

    /** The filtered list is empty and explains it. */
    public void verifyEmptyListExplained(final String explanation) {
        WaitForAsyncUtils.waitForFxEvents();
        final javafx.scene.control.ListView<?> list = (javafx.scene.control.ListView<?>) scene()
                .lookup("#savedGamesList");
        assertTrue(list.getItems().isEmpty());
        assertEquals(explanation, ((Labeled) list.getPlaceholder()).getText());
        assertTrue(list.getPlaceholder().isVisible());
    }

    /** A confirmation window stays open over the main window. */
    public void verifyConfirmationOpen(final String id) {
        WaitForAsyncUtils.waitForFxEvents();
        final Node button = getNodeFinder().lookup("#" + id).query();
        assertNotNull(button);
        assertNotSame(scene(), button.getScene());
        assertTrue(button.getScene().getWindow().isShowing());
    }
}
