package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Labeled;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import org.microcol.model.Calendar.Season;
import org.testfx.util.WaitForAsyncUtils;

/** Interactions and observations of the real 07B game toolbar. */
public final class GameToolbarPage extends AbstractScreen {
    private static final List<String> IDS = List.of("coloniesMenu", "congress",
            "center", "statistics", "turnReport", "goals", "help", "europe",
            "nextTurn", "retire", "exit");

    public GameToolbarPage(final TestContext context) {
        super(context);
    }

    public void verifyNoHoverPopup() {
        getRobot().interact(() -> {
            final javafx.scene.Parent root = getLabeledById("retire").getScene()
                    .getRoot();
            for (final javafx.scene.Node node : root.lookupAll("*")) {
                if (node instanceof javafx.scene.control.Control) {
                    assertNull(
                            ((javafx.scene.control.Control) node).getTooltip(),
                            node.getClass().getSimpleName() + " #"
                                    + node.getId());
                }
            }
            assertTrue(javafx.stage.Window.getWindows().stream().noneMatch(
                    window -> window instanceof javafx.scene.control.Tooltip
                            && window.isShowing()));
        });
    }

    public void hoverRetire() {
        hover("retire");
        getRobot().sleep(1500);
    }

    public void hover(final String id) {
        getRobot().moveTo("#" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void focusCenterWithTab() {
        getRobot().push(KeyCode.O);
        getRobot().push(KeyCode.ESCAPE);
        getRobot().push(KeyCode.TAB);
        getRobot().push(KeyCode.TAB);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void focusMapHeadingWithKeyboard() {
        getRobot().moveTo("#canvas");
        getRobot().push(KeyCode.O, KeyCode.ESCAPE);
        getRobot().press(KeyCode.SHIFT).push(KeyCode.TAB)
                .release(KeyCode.SHIFT);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void leaveMapHeading() {
        getRobot().interact(
                () -> getNodeFinder().lookup("#canvas").query().requestFocus());
        moveOutsideMapHeading();
    }

    public void moveOutsideMapHeading() {
        final Bounds title = getLabeledById("mapHeading")
                .localToScreen(getLabeledById("mapHeading").getLayoutBounds());
        getRobot().moveTo(title.getMaxX() + 8, title.getCenterY());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMapHeading(final boolean focused) {
        getRobot().interact(() -> {
            final Labeled heading = getLabeledById("mapHeading");
            assertEquals("Mappa Mundi", heading.getText());
            assertEquals(focused, heading.isFocused());
            verifyHeading(getNodeFinder().lookup(".screen-toolbar")
                    .queryAs(javafx.scene.layout.Region.class));
        });
    }

    public void verifyHeading(final javafx.scene.layout.Region toolbar) {
        final javafx.scene.control.Label heading = (javafx.scene.control.Label) toolbar
                .lookup(".screen-title");
        if (heading.getText().isEmpty()) {
            assertFalse(heading.isVisible());
            assertFalse(heading.isManaged());
            return;
        }
        assertTrue(heading.isVisible());
        assertTrue(heading.isManaged());
        assertTrue(
                heading.getWidth() + 1 >= Math.min(heading.prefWidth(-1),
                        heading.maxWidth(-1)),
                "The heading retains space for its text");
        assertEquals("Playpen Sans", heading.getFont().getFamily());
        assertEquals("Regular", heading.getFont().getStyle());
        assertEquals(28, heading.getFont().getSize());
        final Bounds frame = toolbar.localToScene(toolbar.getLayoutBounds());
        final Bounds title = heading.localToScene(heading.getLayoutBounds());
        final Node actions = toolbar.lookup(".screen-actions");
        final Bounds actionBounds = actions
                .localToScene(actions.getLayoutBounds());
        final Node content = toolbar.lookup(".game-bar-content");
        final Bounds inner = content.localToScene(content.getLayoutBounds());
        assertEquals(inner.getMinX(), title.getMinX(), 1);
        assertTrue(title.getMaxX() <= actionBounds.getMinX());
        assertTrue(title.getMinY() >= frame.getMinY());
        assertTrue(title.getMaxY() <= frame.getMaxY());
        final double rightmost = toolbar.lookupAll(".button").stream()
                .filter(node -> node.isVisible() && node.isManaged())
                .mapToDouble(node -> node.localToScene(node.getLayoutBounds())
                        .getMaxX())
                .max().orElse(actionBounds.getMaxX());
        assertEquals(inner.getMaxX(), rightmost, 1);
    }

    public void activateFocusedButton() {
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyUnitUnchanged(final int id, final int movement,
            final int turn) {
        assertEquals(id, getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.SelectedUnitManager.class)
                .getSelectedUnit().orElseThrow().getId());
        assertEquals(movement, getModel().getUnitById(id).getActionPoints());
        assertEquals(turn, getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void openColoniesWithKeyboard() {
        getRobot().moveTo("#labelEra");
        getRobot().push(KeyCode.O);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dismissColonies(final boolean outside) {
        if (outside) {
            getRobot().clickOn("#labelEra");
        } else {
            getRobot().push(KeyCode.ESCAPE);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseFirstColony() {
        getRobot().push(KeyCode.DOWN, KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public double getCurrentRowWindowWidth() throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            final FlowPane bar = getNodeFinder().lookup(".buttonPanel")
                    .queryAs(FlowPane.class);
            final List<Node> controls = bar.getChildren().stream()
                    .filter(Node::isManaged).collect(Collectors.toList());
            final org.microcol.gui.util.GameScreenHeader header = getNodeFinder()
                    .lookup(".screen-toolbar")
                    .queryAs(org.microcol.gui.util.GameScreenHeader.class);
            return header.getHeadingWidth() + header.getInsets().getLeft()
                    + header.getInsets().getRight()
                    + controls.stream()
                            .mapToDouble(
                                    node -> node.getBoundsInParent().getWidth())
                            .sum()
                    + (controls.size() - 1) * bar.getHgap()
                    + bar.getInsets().getLeft() + bar.getInsets().getRight()
                    + ((javafx.scene.layout.Region) bar.getParent()).getInsets()
                            .getLeft()
                    + ((javafx.scene.layout.Region) bar.getParent()).getInsets()
                            .getRight()
                    + getPrimaryStage().getWidth() - bar.getScene().getWidth();
        }).get();
    }

    public void verifyControls(final boolean czech, final boolean iconsOnly) {
        getRobot().interact(() -> {
            final String[] labels = czech
                    ? new String[] { "Kolonie", "Kongres", "Vycentrovat",
                            "Statistiky", "Hlášení", "Cíle", "Nápověda",
                            "Evropa", "Další tah", "Ukončit", "Nabídka" }
                    : new String[] { "Colonies", "Congress", "Center",
                            "Statistics", "Report", "Goals", "Help", "Europe",
                            "Next turn", "Retire", "Menu" };
            final FlowPane bar = getNodeFinder().lookup(".buttonPanel")
                    .queryAs(FlowPane.class);
            for (int i = 0; i < IDS.size(); i++) {
                final Labeled control = getLabeledById(IDS.get(i));
                assertEquals(labels[i], control.getText(), control.getId());
                assertEquals("Playpen Sans Regular",
                        control.getFont().getName());
                assertEquals(14, control.getFont().getSize(), 0.01);
                assertTrue(control.getGraphic() instanceof ImageView);
                assertTrue(control.getStyleClass()
                        .contains("game-toolbar-control"));
            }
            final double rowY = getLabeledById("coloniesMenu")
                    .getBoundsInParent().getCenterY();
            for (final Node node : bar.getChildren()) {
                if (!node.isManaged()) {
                    continue;
                }
                final Labeled control = (Labeled) node;
                assertEquals(
                        iconsOnly ? ContentDisplay.GRAPHIC_ONLY
                                : ContentDisplay.LEFT,
                        control.getContentDisplay(),
                        control.getId() + " within " + bar.getWidth()
                                + " px of toolbar content");
                assertEquals(control.getText(),
                        control.queryAccessibleAttribute(
                                AccessibleAttribute.TEXT),
                        control.getId() + " keeps its accessible name");
                final Node text = control.lookup(".text");
                if (iconsOnly) {
                    assertTrue(text == null || !text.isVisible(),
                            control.getId() + " hides its name");
                } else {
                    final Bounds iconBounds = control.getGraphic().localToScene(
                            control.getGraphic().getBoundsInLocal());
                    final Bounds textBounds = text
                            .localToScene(text.getBoundsInLocal());
                    assertTrue(iconBounds.getMaxX() < textBounds.getMinX(),
                            control.getId() + " name must follow its icon");
                    assertEquals(iconBounds.getCenterY(),
                            textBounds.getCenterY(), 2, control.getId()
                                    + " icon and name must share one row");
                }
                assertEquals(rowY, control.getBoundsInParent().getCenterY(), 1,
                        control.getId() + " remains in the only row");
                assertTrue(control.getHeight() <= 64, control.getId());
                assertTrue(control.getWidth() + 1 >= control.prefWidth(-1),
                        control.getId());
                final Bounds bounds = control
                        .localToScene(control.getBoundsInLocal());
                assertTrue(bounds.getMinX() >= 0
                        && bounds.getMaxX() <= bar.getScene().getWidth(),
                        control.getId());
                assertTrue(bounds.getMaxY() <= bar
                        .localToScene(bar.getLayoutBounds()).getMaxY() + 1);
            }
            final Node map = getNodeFinder().lookup("#canvas").query();
            assertTrue(
                    map.localToScene(map.getBoundsInLocal()).getMinY() >= bar
                            .localToScene(bar.getLayoutBounds()).getMaxY(),
                    "Toolbar controls stay above the map; only ink may overlap");
        });
    }

    public void verifySeason(final Season expected, final int accent) {
        getRobot().interact(() -> {
            assertEquals(expected,
                    getModel().getCalendar().getCurrentSeason().orElse(null));
            for (final String id : IDS) {
                final Image icon = ((ImageView) getLabeledById(id).getGraphic())
                        .getImage();
                assertEquals(96, icon.getWidth());
                assertEquals(96, icon.getHeight());
                int inkPixels = 0;
                int accentPixels = 0;
                for (int y = 0; y < 96; y++) {
                    for (int x = 0; x < 96; x++) {
                        final int pixel = icon.getPixelReader().getArgb(x, y);
                        inkPixels += pixel == 0xff302d27 ? 1 : 0;
                        accentPixels += pixel == accent ? 1 : 0;
                    }
                }
                assertTrue(inkPixels > 0, id + " retains ink");
                assertTrue(accentPixels > 0, id + " has the current accent");
                assertEquals(0, icon.getPixelReader().getArgb(0, 0));
            }
        });
    }

    public void verifyExplanation(final String expected,
            final String focusedId) {
        assertEquals(expected, getLabeledById("statusBarLabel").getText());
        if (focusedId != null) {
            assertEquals(focusedId,
                    getPrimaryStage().getScene().getFocusOwner().getId());
            final Labeled focused = getLabeledById(focusedId);
            assertFalse(focused.getBorder().getImages().isEmpty(),
                    "Keyboard focus must preserve the freehand frame");
            assertTrue(focused.isUnderline(),
                    "Keyboard focus also uses an underline, not color alone");
        }
    }

    public void verifyColonies(final boolean showing, final boolean empty,
            final String firstLabel) {
        final MenuButton menu = (MenuButton) getLabeledById("coloniesMenu");
        assertEquals(showing, menu.isShowing());
        assertEquals(firstLabel, menu.getItems().get(0).getText());
        assertEquals(empty, menu.getItems().get(0).isDisable());
        if (empty) {
            assertEquals(1, menu.getItems().size());
        }
    }
}
