package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/** Observations of the menu actually opened by a player gesture. */
public final class ContextMenuPage extends AbstractScreen {
    public ContextMenuPage(final TestContext context) {
        super(context);
    }

    private ContextMenu menu() {
        return (ContextMenu) Window.getWindows().stream().filter(
                window -> window instanceof ContextMenu && window.isShowing())
                .findFirst().orElseThrow();
    }

    public void verifyPaperMenuWithoutShortcuts(final String firstAction) {
        WaitForAsyncUtils.waitForAsyncFx(5000, () -> {
            final Region panel = (Region) menu().getSkin().getNode();
            assertEquals(Color.web("#fff9ec"),
                    panel.getBackground().getFills().get(0).getFill());
            assertNotNull(panel.getShape());
            assertEquals(firstAction, menu().getItems().get(0).getText());
            assertTrue(panel.lookupAll(".accelerator-text").isEmpty());
            for (final javafx.scene.Node node : panel.lookupAll(".label")) {
                final Label label = (Label) node;
                assertEquals("Playpen Sans", label.getFont().getFamily());
                assertEquals(14, label.getFont().getSize());
                assertFalse(label.getFont().getStyle().contains("Bold"));
                assertTrue(label.getWidth() >= label.prefWidth(-1) - 1);
            }
            return null;
        });
    }

    public void hoverItem(final String selector) {
        getRobot().moveTo(menu().getSkin().getNode().lookup(selector));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void clickUnitHeading() {
        getRobot().clickOn("#unitContextTitle");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyCompactUnitMenu(final String expectedTitle,
            final double maximumActionSpacing) throws Exception {
        WaitForAsyncUtils.waitForAsyncFx(5000, () -> {
            final ContextMenu popup = menu();
            assertEquals(expectedTitle, popup.getItems().get(0).getText());
            assertTrue(popup.getItems().get(0).isDisable(),
                    "The unit heading cannot activate a command");
            assertTrue(popup.getItems()
                    .get(1) instanceof javafx.scene.control.SeparatorMenuItem);
            assertEquals(1, popup.getItems().stream().filter(
                    item -> item instanceof javafx.scene.control.SeparatorMenuItem)
                    .count());
            final Region panel = (Region) popup.getSkin().getNode();
            final Region heading = (Region) panel.lookup("#unitContextTitle");
            assertEquals(Color.TRANSPARENT,
                    heading.getBackground().getFills().get(0).getFill(),
                    "The heading must not acquire a selection fill");
            assertEquals(Color.TRANSPARENT,
                    heading.getBorder().getStrokes().get(0).getTopStroke());
            assertEquals(Color.BLACK,
                    ((Label) heading.lookup(".label")).getTextFill());
            final javafx.scene.Node move = panel.lookup("#move");
            final javafx.scene.Node goTo = panel.lookup("#goToUnit");
            final javafx.geometry.Bounds first = move
                    .localToScreen(move.getBoundsInLocal());
            final javafx.geometry.Bounds next = goTo
                    .localToScreen(goTo.getBoundsInLocal());
            assertTrue(next.getMinY() >= first.getMaxY(),
                    "Command rows must not overlap");
            assertTrue(next.getMinY() - first.getMinY() <= maximumActionSpacing,
                    "Ordinary commands use the selected compact spacing");
            return null;
        });
    }

    public void verifyOpenMenuCount(final int expected) throws Exception {
        WaitForAsyncUtils.waitForAsyncFx(5000, () -> {
            assertEquals(expected,
                    Window.getWindows().stream()
                            .filter(window -> window instanceof ContextMenu
                                    && window.isShowing())
                            .count());
            return null;
        });
    }

    public void verifyDrawnSelection(final String selector) throws Exception {
        WaitForAsyncUtils.waitForAsyncFx(5000, () -> {
            final Region row = (Region) menu().getSkin().getNode()
                    .lookup(selector);
            assertEquals(Color.web("#dfbd91"),
                    row.getBackground().getFills().get(0).getFill());
            assertEquals(Color.BLACK,
                    row.getBorder().getStrokes().get(0).getTopStroke());
            for (final javafx.scene.Node node : row.lookupAll(".label")) {
                assertEquals(Color.BLACK, ((Label) node).getTextFill(),
                        "Focused action stays readable in black ink");
            }
            assertTrue(row.getShape() instanceof javafx.scene.shape.SVGPath);
            assertTrue(
                    ((javafx.scene.shape.SVGPath) row.getShape()).getContent()
                            .contains("Q"),
                    "Selection has curved freehand edges");
            assertTrue(row.getBorder().getStrokes().get(0).getTopStyle()
                    .getDashArray().isEmpty());
            return null;
        });
    }

    public void verifyShortcut(final String selector, final String expectedKey)
            throws Exception {
        WaitForAsyncUtils.waitForAsyncFx(5000, () -> {
            final javafx.scene.control.Button button = (javafx.scene.control.Button) menu()
                    .getSkin().getNode().lookup(selector);
            final Label key = (Label) button.lookup(".unit-action-shortcut");
            assertEquals(expectedKey, key.getText());
            assertTrue(key.isVisible());
            assertNotNull(key.getShape());
            final Label caption = (Label) ((javafx.scene.layout.HBox) button
                    .getGraphic()).getChildren().get(0);
            assertFalse(caption.getText().contains("("));
            assertTrue(caption.localToScreen(caption.getBoundsInLocal())
                    .getMaxX() < key.localToScreen(key.getBoundsInLocal())
                            .getMinX());
            assertTrue(caption
                    .getHeight() >= caption.prefHeight(caption.getWidth()) - 1);
            assertTrue(key.getWidth() >= key.prefWidth(-1) - 1);
            assertTrue(
                    button.getHeight() <= button.prefHeight(button.getWidth())
                            + 1,
                    "Command height is measured at the available menu width");
            return null;
        });
    }

    public void captureMenu(final String fileName) throws Exception {
        final Path output = Path.of("target", "warehouse-qa", fileName);
        Files.createDirectories(output.getParent());
        getRobot().robotContext().getCaptureSupport().saveImage(
                getRobot().capture(menu().getSkin().getNode()).getImage(),
                output);
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyClosed() {
        assertFalse(Window.getWindows().stream().anyMatch(
                window -> window instanceof ContextMenu && window.isShowing()));
    }
}
