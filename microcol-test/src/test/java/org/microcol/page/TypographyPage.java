package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.util.FontService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testfx.util.WaitForAsyncUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/** Inspect the fonts actually selected by CSS and the canvas font provider. */
public final class TypographyPage extends AbstractScreen {
    public TypographyPage(final TestContext context) {
        super(context);
    }

    public void resizeGameWindow(final int width, final int height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hoverColonyCaption() throws TimeoutException {
        getRobot().moveTo("#colonyName");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils
                        .asyncFx(() -> getLabeledById("colonyName").getTooltip()
                                .isShowing())
                        .get());
    }

    public void verifyColonyTooltip() {
        getRobot().interact(() -> {
            assertEquals("Playpen Sans Light", getLabeledById("colonyName")
                    .getTooltip().getFont().getName());
        });
    }

    public void verifyVisibleCaption(final String id, final String expected) {
        getRobot().interact(() -> {
            final Labeled label = getLabeledById(id);
            assertEquals(expected, ((Text) label.lookup(".text")).getText());
            final Bounds bounds = label.localToScene(label.getBoundsInLocal());
            assertTrue(bounds.getMinX() >= 0 && bounds.getMinY() >= 0);
            assertTrue(bounds.getMaxX() <= label.getScene().getWidth());
            assertTrue(bounds.getMaxY() <= label.getScene().getHeight());
        });
    }

    public void verifySceneTypography(final String anchorId) {
        final Node anchor = getNodeFinder().lookup("#" + anchorId).query();
        getRobot().interact(() -> {
            final Parent root = anchor.getScene().getRoot();
            root.applyCss();
            int checked = 0;
            for (Node node : root.lookupAll("*")) {
                Font font = null;
                if (node instanceof Labeled) {
                    font = ((Labeled) node).getFont();
                } else if (node instanceof TextInputControl) {
                    font = ((TextInputControl) node).getFont();
                } else if (node instanceof Text) {
                    font = ((Text) node).getFont();
                }
                if (font != null && node.isVisible()) {
                    assertEquals("Playpen Sans Light", font.getName(),
                            node.getClass().getSimpleName() + " #"
                                    + node.getId());
                    checked++;
                }
            }
            assertTrue(checked > 0, "Inspect actual scene text");
        });
    }

    public void verifyTextSize(final String selector, final double expected) {
        getRobot().interact(() -> {
            final Node node = getNodeFinder().lookup(selector).query();
            final Font font = node instanceof Labeled
                    ? ((Labeled) node).getFont()
                    : ((TextInputControl) node).getFont();
            assertEquals(expected, font.getSize(), selector);
            if (node instanceof Labeled) {
                assertEquals(((Labeled) node).getText(),
                        ((Text) node.lookup(".text")).getText(), selector);
            }
        });
    }

    public void verifySettingsDefaultLabels(final double expectedSize,
            final String... prefixes) {
        getRobot().interact(() -> {
            final java.util.List<Labeled> labels = getListOfNodes(
                    ".settings-controls .label");
            for (final String prefix : prefixes) {
                final Labeled label = labels.stream()
                        .filter(item -> item.getText().startsWith(prefix))
                        .findFirst().orElseThrow();
                assertEquals(expectedSize, label.getFont().getSize(), prefix);
                assertEquals(label.getText(),
                        ((Text) label.lookup(".text")).getText(), prefix);
            }
        });
    }

    public void verifyBundledCssFace(final String family,
            final String expectedName) {
        getRobot().interact(() -> {
            final javafx.scene.control.Label label = new javafx.scene.control.Label(
                    "Příliš žluťoučký français Straße €");
            label.setStyle(
                    "-fx-font-family: '" + family + "'; -fx-font-size: 14;");
            final javafx.scene.chart.NumberAxis axis = new javafx.scene.chart.NumberAxis(
                    0, 100, 10);
            axis.setStyle("-fx-tick-label-font: 14px '" + family + "';");
            final javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(
                    label, axis);
            new javafx.scene.Scene(root);
            root.applyCss();
            root.layout();
            assertEquals(expectedName, label.getFont().getName());
            assertEquals(14, label.getFont().getSize());
            assertEquals(expectedName, axis.getTickLabelFont().getName());
            assertEquals(14, axis.getTickLabelFont().getSize());
            assertEquals("Playpen Sans Light",
                    getContext().getClassFromGuice(FontService.class)
                            .getDefault(14).getName());
        });
    }

    public void verifyCanvasTypography() {
        getRobot().interact(() -> {
            final Font font = getContext().getClassFromGuice(FontService.class)
                    .getDefault(16);
            assertEquals("Playpen Sans Light", font.getName());
            assertEquals(16, font.getSize());
            final Canvas canvas = getNodeFinder().lookup(".colony-fields")
                    .queryAs(Parent.class).getChildrenUnmodifiable().stream()
                    .filter(Canvas.class::isInstance).map(Canvas.class::cast)
                    .findFirst().orElseThrow();
            assertEquals("Playpen Sans Light",
                    canvas.getGraphicsContext2D().getFont().getName());
        });
    }
}
