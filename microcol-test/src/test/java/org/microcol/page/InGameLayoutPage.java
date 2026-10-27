package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.testfx.util.WaitForAsyncUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;

/**
 * The same frame contract applies to reports, work screens and modal readers.
 */
public final class InGameLayoutPage extends AbstractScreen {
    public InGameLayoutPage(final TestContext context) {
        super(context);
    }

    private Region page() {
        return getNodeFinder().lookup(".in-game-page").queryAll().stream()
                .filter(node -> node.getScene() != null
                        && node.getScene().getWindow().isShowing())
                .sorted((a, b) -> Boolean.compare(
                        b.getScene().getWindow().isFocused(),
                        a.getScene().getWindow().isFocused()))
                .map(node -> (Region) node).findFirst().orElseThrow();
    }

    public void open(final String id) {
        if (id.endsWith("Tab")) {
            clickOnStatisticsTab(id);
        } else {
            getRobot().clickOn(
                    getNodeFinder().lookup("#" + id).queryAs(Node.class));
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifySelectedTab(final String id) {
        assertTrue(getNodeFinder().lookup("#" + id)
                .queryAs(javafx.scene.control.ToggleButton.class).isSelected(),
                id);
    }

    public void closeWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void scrollToBottom() {
        getRobot().interact(() -> ((ScrollPane) page().lookup(".screen-scroll"))
                .setVvalue(1));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyFrame(final int expectedGold, final int expectedYear) {
        getRobot().interact(() -> {
            final Region root = page();
            final Bounds pageBounds = root.localToScene(root.getLayoutBounds());
            final Node toolbar = root.lookup(".screen-toolbar");
            final Region status = (Region) root.lookup("#statusBar");
            final Node content = root.lookup(".screen-scroll");
            final Bounds top = toolbar.localToScene(toolbar.getLayoutBounds());
            final Bounds middle = content
                    .localToScene(content.getLayoutBounds());
            final Bounds bottom = status.localToScene(status.getLayoutBounds());
            assertEquals(pageBounds.getMinY(), top.getMinY(), 1,
                    "Toolbar is at the top of the screen");
            assertTrue(top.getHeight() >= 45 && top.getHeight() < 180,
                    "Controls reserve a compact, visible toolbar");
            assertTrue(middle.getMinY() >= top.getMaxY() - 1);
            assertTrue(middle.getMaxY() <= bottom.getMinY() + 1);
            assertEquals(pageBounds.getMaxY(), bottom.getMaxY(), 1,
                    "Status remains at the bottom of the screen");
            assertTrue(status.getHeight() >= 72);
            assertTrue(((Label) root.lookup("#labelGold")).getText()
                    .endsWith(" " + expectedGold));
            assertTrue(((Label) root.lookup("#labelEra")).getText()
                    .contains(Integer.toString(expectedYear)));
            for (Node control : ((javafx.scene.Parent) toolbar)
                    .lookupAll(".button")) {
                if (control.isVisible() && control.isManaged()) {
                    final Bounds bounds = control
                            .localToScene(control.getLayoutBounds());
                    assertTrue(bounds.getMinY() >= top.getMinY() - 1);
                    assertTrue(bounds.getMaxY() <= top.getMaxY() + 1);
                    assertTrue(bounds.getMinX() >= pageBounds.getMinX() - 1);
                    assertTrue(bounds.getMaxX() <= pageBounds.getMaxX() + 1);
                }
            }
        });
        new PaperButtonsPage(getContext()).verifyVisibleButtonStyles();
    }

    public void verifyScreenTitle(final String expectedTitle) {
        assertEquals(expectedTitle,
                ((Label) page().lookup(".screen-title")).getText());
    }

    public void verifyMapReturned() {
        getRobot().interact(() -> {
            assertEquals(1, javafx.stage.Window.getWindows().stream()
                    .filter(window -> window instanceof javafx.stage.Stage
                            && window.isShowing())
                    .count(), "No modal reader remains over the map");
            assertNotNull(getContext().getPrimaryStage().getScene()
                    .lookup("#nextTurn"));
        });
    }

    public void verifyColonyNameVisible(final String expectedName) {
        getRobot().interact(() -> {
            final Region root = page();
            final Label name = (Label) root.lookup("#colonyName");
            assertEquals(expectedName, name.getText());
            final Bounds bounds = name.localToScene(name.getLayoutBounds());
            final Node scroll = root.lookup(".screen-scroll");
            final Bounds viewport = scroll
                    .localToScene(scroll.getLayoutBounds());
            assertTrue(bounds.getMinX() >= viewport.getMinX());
            assertTrue(bounds.getMaxX() <= viewport.getMaxX());
            assertTrue(bounds.getMinY() >= viewport.getMinY());
            assertTrue(bounds.getMaxY() <= viewport.getMaxY());
        });
    }

    public void verifyTurnUnchanged(final int expectedTurns,
            final int expectedGold) {
        assertEquals(expectedTurns,
                getModel().getCalendar().getNumberOfPlayedTurns());
        assertEquals(expectedGold,
                getContext().getClassFromGuice(
                        org.microcol.gui.event.model.GameModelController.class)
                        .getHumanPlayer().getGold());
    }

    public void saveEvidence(final String name) throws Exception {
        final javafx.scene.image.WritableImage image = WaitForAsyncUtils
                .asyncFx(() -> snapshotScene(page().getScene()))
                .get(10, TimeUnit.SECONDS);
        final File file = new File("target/warehouse-qa", name);
        file.getParentFile().mkdirs();
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }
}
