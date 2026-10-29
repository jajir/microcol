package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.testfx.util.WaitForAsyncUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    public void selectNextReportWithKeyboard() {
        getRobot().push(KeyCode.RIGHT);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyAtlasLayout(final String expectedTitle) {
        getRobot().interact(() -> {
            final Region root = page();
            final ScrollPane sidebar = (ScrollPane) root
                    .lookup("#statsSidebar");
            final Region detail = (Region) root.lookup(".atlas-detail");
            final Bounds left = sidebar.localToScene(sidebar.getLayoutBounds());
            final Bounds right = detail.localToScene(detail.getLayoutBounds());
            final Node body = root.lookup(".screen-scroll");
            final Bounds viewport = body.localToScene(body.getLayoutBounds());
            assertEquals(12, root.lookupAll(".atlas-tab").size());
            assertTrue(right.getMinX() >= left.getMaxX() + 16,
                    "Atlas index and detail remain separate");
            assertTrue(right.getMaxX() <= viewport.getMaxX() + 1);
            assertTrue(right.getWidth() >= 400, "Report has readable width");
            assertEquals(expectedTitle,
                    ((Label) root.lookup("#atlasReportTitle")).getText());
            final ToggleButton selected = root.lookupAll(".atlas-tab").stream()
                    .map(node -> (ToggleButton) node)
                    .filter(ToggleButton::isSelected).findFirst().orElseThrow();
            final Bounds tab = selected
                    .localToScene(selected.getLayoutBounds());
            final Node clip = sidebar.lookup(".viewport");
            final Bounds visible = clip.localToScene(clip.getLayoutBounds());
            assertTrue(tab.getMinY() >= visible.getMinY() - 1);
            assertTrue(tab.getMaxY() <= visible.getMaxY() + 1);
            assertTrue(selected.isUnderline(), "Selection has a non-color cue");
            assertEquals("Playpen Sans Regular", selected.getFont().getName());
            assertEquals(14, selected.getFont().getSize());
            assertTrue(root.getBackground().getFills().stream().anyMatch(
                    fill -> Color.web("#FFF9EC").equals(fill.getFill())));
        });
    }

    public void verifyAtlasHistory(final int expectedCount,
            final int expectedLastValue, final int expectedLastYear,
            final String expectedSummary) {
        getRobot().interact(() -> {
            final LineChart<?, ?> chart = (LineChart<?, ?>) page()
                    .lookup("#statisticsChart");
            assertNotNull(chart);
            assertFalse(chart.getAnimated());
            assertEquals(1, chart.getData().size());
            final java.util.List<? extends XYChart.Data<?, ?>> points = chart
                    .getData().get(0).getData();
            assertEquals(expectedCount, points.size());
            assertEquals(expectedSummary,
                    ((Label) page().lookup("#atlasHistorySummary")).getText());
            if (expectedCount > 0) {
                final XYChart.Data<?, ?> last = points.get(expectedCount - 1);
                assertEquals(expectedLastValue,
                        ((Number) last.getYValue()).intValue());
                assertEquals(expectedLastYear,
                        ((Number) last.getXValue()).intValue());
                final NumberAxis axis = (NumberAxis) chart.getXAxis();
                assertTrue(axis.getUpperBound() >= expectedLastYear,
                        "Every recorded year remains in the chart");
                assertEquals(Integer.toString(expectedLastYear), axis
                        .getTickLabelFormatter().toString(expectedLastYear));
                assertNotNull(last.getNode(), "Recorded points stay visible");
            }
        });
    }

    /**
     * Load a saved fixture with zero or one historical point, before
     * navigation.
     */
    public void loadAtlasHistoryFixture(final int records) throws Exception {
        final Gson gson = new Gson();
        final JsonObject fixture = gson.fromJson(
                Files.readString(Path
                        .of("src/test/scenarios/F221-game-toolbar.microcol")),
                JsonObject.class);
        final JsonObject statistics = fixture.getAsJsonObject("model")
                .getAsJsonObject("statistics");
        final JsonArray history = new JsonArray();
        if (records == 1) {
            history.add(statistics.getAsJsonArray("turnStatistics").get(0));
        }
        statistics.add("turnStatistics", history);
        final Path file = Files.createTempFile("atlas-history-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(fixture));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
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
            final Region toolbar = (Region) root.lookup(".screen-toolbar");
            final Region status = (Region) root.lookup("#statusBar");
            final Node content = root.lookup(".screen-scroll");
            final Bounds top = toolbar.localToScene(toolbar.getLayoutBounds());
            final Bounds middle = content
                    .localToScene(content.getLayoutBounds());
            final Bounds bottom = status.localToScene(status.getLayoutBounds());
            assertEquals(pageBounds.getMinY(), top.getMinY(), 1,
                    "Toolbar is at the top of the screen");
            assertEquals(72, top.getHeight(), 0.01,
                    "Every game view uses the map's fixed toolbar height");
            assertEquals(72, toolbar.getMinHeight());
            assertEquals(72, toolbar.getPrefHeight());
            assertEquals(72, toolbar.getMaxHeight());
            assertEquals(16, toolbar.getPadding().getLeft());
            assertEquals(16, toolbar.getPadding().getRight());
            assertEquals(toolbar.getPadding().getLeft(),
                    status.getPadding().getLeft());
            assertEquals(toolbar.getPadding().getRight(),
                    status.getPadding().getRight());
            assertEquals(0,
                    ((Label) root.lookup("#labelEra")).getPadding().getRight(),
                    "The year adds no second inset at the right edge");
            assertEquals(1, toolbar.getBorder().getImages().size());
            assertEquals(1, status.getBorder().getImages().size());
            assertEquals(toolbar.getBorder().getImages().get(0).getImage(),
                    status.getBorder().getImages().get(0).getImage(),
                    "Both bars use the shared hand-drawn frame");
            assertTrue(middle.getMinY() >= top.getMaxY() - 1);
            assertTrue(middle.getMaxY() <= bottom.getMinY() + 1);
            assertEquals(pageBounds.getMaxY(), bottom.getMaxY(), 1,
                    "Status remains at the bottom of the screen");
            assertTrue(status.getHeight() >= 72);
            assertEquals(Integer.toString(expectedGold),
                    ((Label) root.lookup("#labelGold")).getText()
                            .replace("\u00a0", ""));
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
