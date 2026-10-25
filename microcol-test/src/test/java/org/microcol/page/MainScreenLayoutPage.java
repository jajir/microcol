package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.gamepanel.GamePanelComponent;
import org.testfx.util.WaitForAsyncUtils;
import com.google.common.eventbus.EventBus;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Readability checks for the real map screen and its status fields. */
public final class MainScreenLayoutPage extends AbstractScreen {
    public MainScreenLayoutPage(final TestContext context) {
        super(context);
    }

    public void verifyPaperWindowConfiguration(final StageStyle expectedStyle) {
        getRobot().interact(() -> {
            final Stage stage = getContext().getPrimaryStage();
            assertEquals(Color.web("#FFF9EC"), stage.getScene().getFill());
            assertEquals(expectedStyle, stage.getStyle());
            assertEquals("MicroCol", stage.getTitle());
            assertTrue(stage.isResizable());
            assertTrue(stage.isShowing());
        });
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

    public void verifyStatusBar() {
        getRobot().interact(() -> {
            final Region bar = (Region) node("statusBar");
            assertTrue(bar.getHeight() >= 72);
            assertEquals(new Insets(12, 16, 12, 16), bar.getPadding());
            assertEquals(Color.web("#FFF9EC"),
                    bar.getBackground().getFills().get(0).getFill());
            assertEquals(new BorderWidths(1, 0, 0, 0),
                    bar.getBorder().getStrokes().get(0).getWidths());
            assertTrue(bounds("statusBar")
                    .getMaxY() <= bar.getScene().getHeight() + 1,
                    "Status panel remains inside the scene");
            for (String id : new String[] { "description", "labelGold" }) {
                final Region field = (Region) node(id);
                assertEquals(new BorderWidths(0, 1, 0, 0),
                        field.getBorder().getStrokes().get(0).getWidths(), id);
            }
            final Region year = (Region) node("labelEra");
            assertTrue(
                    year.getBorder() == null
                            || year.getBorder().getStrokes().stream()
                                    .allMatch(stroke -> BorderWidths.EMPTY
                                            .equals(stroke.getWidths())),
                    "Year has no frame");
            for (String id : new String[] { "statusBarLabel", "labelGold",
                    "labelEra" }) {
                final Label label = (Label) node(id);
                assertEquals("Playpen Sans Regular", label.getFont().getName(),
                        id);
                assertEquals(14, label.getFont().getSize(), 0.01, id);
                assertTrue(
                        bounds(id).getMaxX() <= bounds("statusBar").getMaxX());
                assertTrue(
                        bounds(id).getMaxY() <= bounds("statusBar").getMaxY());
            }
            assertTrue(bounds("labelGold")
                    .getMaxX() <= bounds("labelEra").getMinX() + 1);
        });
    }

    public void verifyMainScreenSeparators() {
        getRobot().interact(() -> {
            final Region button = getNodeFinder()
                    .lookup(".game-toolbar-control").queryAs(Region.class);
            final BorderImage frame = button.getBorder().getImages().get(0);
            final double width = frame.getWidths().getTop();
            final String[] selectors = { ".buttonPanel", "#rightPanel",
                    "#statusBar", "#statusBar #description",
                    "#statusBar #labelGold" };
            final BorderWidths[] edges = { new BorderWidths(0, 0, width, 0),
                    new BorderWidths(0, 0, 0, width),
                    new BorderWidths(width, 0, 0, 0),
                    new BorderWidths(0, width, 0, 0),
                    new BorderWidths(0, width, 0, 0) };
            for (int i = 0; i < selectors.length; i++) {
                final Region panel = getNodeFinder().lookup(selectors[i])
                        .queryAs(Region.class);
                assertEquals(1, panel.getBorder().getImages().size(),
                        selectors[i]);
                final BorderImage edge = panel.getBorder().getImages().get(0);
                assertEquals(frame.getImage(), edge.getImage(), selectors[i]);
                assertEquals(frame.getSlices(), edge.getSlices(), selectors[i]);
                assertEquals(edges[i], edge.getWidths(), selectors[i]);
                assertEquals(frame.getRepeatX(), edge.getRepeatX());
                assertEquals(frame.getRepeatY(), edge.getRepeatY());
                panel.getBorder().getStrokes().forEach(stroke -> {
                    assertEquals(Color.TRANSPARENT, stroke.getTopStroke());
                    assertEquals(Color.TRANSPARENT, stroke.getRightStroke());
                    assertEquals(Color.TRANSPARENT, stroke.getBottomStroke());
                    assertEquals(Color.TRANSPARENT, stroke.getLeftStroke());
                });
            }
        });
    }

    public void verifyMainScreenTypography() {
        getRobot().interact(() -> {
            final Parent screen = getNodeFinder().lookup(".game-screen")
                    .queryAs(Parent.class);
            int checked = 0;
            for (Node child : screen.lookupAll("*")) {
                final Font font = child instanceof Labeled
                        ? ((Labeled) child).getFont()
                        : child instanceof Text ? ((Text) child).getFont()
                                : null;
                if (font != null && child.isVisible()) {
                    assertEquals("Playpen Sans Regular", font.getName(),
                            child.toString());
                    assertEquals(14, font.getSize(), 0.01, child.toString());
                    checked++;
                }
            }
            assertTrue(checked > 0, "Inspect visible main-screen text");
            final Font mapFont = getContext()
                    .getClassFromGuice(GamePanelComponent.class).getCanvas()
                    .getGraphicsContext2D().getFont();
            assertEquals("Playpen Sans Regular", mapFont.getName());
            assertEquals(14, mapFont.getSize(), 0.01);
        });
    }

    /** Verify the agreed pigments in the actual rendered map, not a mockup. */
    public void verifyMapPalette() {
        getRobot().interact(() -> {
            final javafx.scene.image.WritableImage image = org.microcol.gui.screen.game.gamepanel.FogFrameProbe
                    .captureCanvas(getContext());
            final int[] expected = { 0x649B97, 0xA9B77A, 0x9B9486 };
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
