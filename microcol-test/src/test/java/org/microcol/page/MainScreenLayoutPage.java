package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.testfx.util.WaitForAsyncUtils;
import com.google.common.eventbus.EventBus;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** Readability checks for the real map screen and its status fields. */
public final class MainScreenLayoutPage extends AbstractScreen {
    public MainScreenLayoutPage(final TestContext context) {
        super(context);
    }

    public void resize(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void showContextMessage(final String message) {
        getRobot().interact(() -> getContext().getClassFromGuice(EventBus.class)
                .post(new StatusBarMessageEvent(message,
                        StatusBarMessageEvent.Source.GAME)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Node node(final String id) {
        return getNodeFinder().lookup("#" + id).query();
    }

    private Bounds bounds(final String id) {
        final Node n = node(id);
        return n.localToScene(n.getBoundsInLocal());
    }

    /** Verify the agreed pigments in the actual rendered map, not a mockup. */
    public void verifyMapPalette() {
        getRobot().interact(() -> {
            final javafx.scene.image.WritableImage image = org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                    .captureCanvas(getContext());
            final int[] expected = { 0x649B97, 0xC5C18A, 0x9B9486 };
            for (int pigment : expected) {
                int pixels = 0;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        if ((image.getPixelReader().getArgb(x, y)
                                & 0xFFFFFF) == pigment) {
                            pixels++;
                        }
                    }
                }
                assertTrue(pixels > 100, "Map displays agreed pigment "
                        + Integer.toHexString(pigment));
            }
        });
    }

    public void verifyLayout(final String expectedMessage) {
        getRobot().interact(() -> {
            final Bounds bar = bounds("statusBar");
            final Bounds map = bounds("canvas");
            final Bounds right = bounds("rightPanel");
            assertTrue(bar.getHeight() >= 72,
                    "Status panel reserves vertical space");
            assertTrue(map.getHeight() >= 200, "Map retains usable height");
            assertTrue(map.getMaxY() <= bar.getMinY() + 1,
                    "Map is above status panel");
            assertTrue(map.getMaxX() <= right.getMinX() + 1,
                    "Map does not overlap side panel");
            assertTrue(
                    bar.getMaxY() <= getContext().getPrimaryStage().getScene()
                            .getHeight() + 1,
                    "Bottom panel stays inside the scene");
            final Label description = (Label) node("statusBarLabel");
            assertEquals(expectedMessage, description.getText());
            assertTrue(description.isWrapText());
            assertTrue(
                    description.getHeight() + 1 >= description
                            .prefHeight(description.getWidth()),
                    "Wrapped message has enough height");
            assertTrue(bounds("statusBarLabel")
                    .getMaxX() <= bounds("labelGold").getMinX() + 1);
            assertTrue(bounds("labelGold")
                    .getMaxX() <= bounds("labelEra").getMinX() + 1);
            for (String id : new String[] { "statusBarLabel", "labelGold",
                    "labelEra" }) {
                final Label label = (Label) node(id);
                assertEquals(14, label.getFont().getSize(), 0.01);
                assertEquals("Playpen Sans", label.getFont().getFamily());
                assertEquals("Regular", label.getFont().getStyle());
                assertFalse(label.getText().isEmpty());
                assertTrue(bounds(id).getMaxY() <= bar.getMaxY());
                assertTrue(bounds(id).getMaxX() <= bar.getMaxX());
            }
            for (String id : new String[] { "labelGold", "labelEra" }) {
                final Region label = (Region) node(id);
                assertTrue(label.getWidth() + 1 >= label.prefWidth(-1),
                        "Economic fields are not clipped");
            }
        });
    }

    public void saveEvidence(final String name) throws Exception {
        final javafx.scene.image.WritableImage image = WaitForAsyncUtils
                .asyncFx(() -> snapshotScene(
                        getContext().getPrimaryStage().getScene()))
                .get(10, TimeUnit.SECONDS);
        final File file = new File("target/warehouse-qa", name);
        file.getParentFile().mkdirs();
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }
}
