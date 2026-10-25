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
