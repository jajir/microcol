package org.akropia.page;

import java.nio.file.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.akropia.gui.screen.menu.demo.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Driver for the developer preview; assertions stay in verification methods.
 */
public final class DemoStudioPage {
    private final TestContext context;
    private final Path root;
    private final String configuration;
    private DemoView view;
    private Stage stage;

    public DemoStudioPage(TestContext context) throws Exception {
        this.context = context;
        root = Files.createTempDirectory("akropia-studio-");
        for (String name : java.util.List.of("first", "second")) {
            Path directory = Files.createDirectory(root.resolve(name));
            for (String file : java.util.List.of("scene.json",
                    "initial-state.json")) {
                try (var input = DemoScene.class
                        .getResourceAsStream("/demos/first-colony/" + file)) {
                    Files.copy(input, directory.resolve(file));
                }
            }
        }
        Path second = root.resolve("second");
        Files.copy(PlayerSaveFixture
                .asPlayerSave(second.resolve("initial-state.json").toFile())
                .toPath(), second.resolve("initial-state.akropia"));
        Files.writeString(second.resolve("scene.json"),
                Files.readString(second.resolve("scene.json")).replace(
                        "initial-state.json", "initial-state.akropia"));
        configuration = Files.readString(root.resolve("first/scene.json"));
    }

    public void open() {
        context.getRobot().interact(() -> {
            view = context.getClassFromGuice(DemoView.class);
            stage = new Stage();
            new DemoStudio(view, root).show(stage);
        });
    }

    public void seek(double milliseconds) {
        context.getRobot().interact(
                () -> ((Slider) stage.getScene().lookup("#demo-position"))
                        .setValue(milliseconds));
    }

    public void click(String text) {
        context.getRobot().clickOn(text);
    }

    public void selectSecond() {
        context.getRobot().interact(
                () -> ((ComboBox<?>) stage.getScene().lookup("#demo-scenes"))
                        .getSelectionModel().select(1));
    }

    public void corruptConfiguration() throws Exception {
        Files.writeString(root.resolve("first/scene.json"), "{");
    }

    public void restoreConfiguration() throws Exception {
        Files.writeString(root.resolve("first/scene.json"), configuration);
    }

    public void seekFounderWalk() {
        context.getRobot().interact(() -> {
            long time = 0;
            for (var step : view.playback().scene().steps) {
                if ("animate".equals(step.action))
                    break;
                time += step.durationMs;
            }
            ((Slider) stage.getScene().lookup("#demo-position"))
                    .setValue(time + 600);
        });
    }

    public void hoverFounder() {
        javafx.geometry.Point2D[] point = new javafx.geometry.Point2D[1];
        context.getRobot().interact(() -> {
            for (int y = 0; y < view.getHeight(); y += 8) {
                for (int x = 0; x < view.getWidth(); x += 8) {
                    if (view.describeAt(x, y).contains("founder · ID 0")) {
                        point[0] = view.localToScreen(x, y);
                        return;
                    }
                }
            }
        });
        context.getRobot().moveTo(java.util.Objects.requireNonNull(point[0],
                "Founder not rendered"));
        context.getRobot().sleep(150);
    }

    public void verifyFounderHover() {
        context.getRobot().interact(() -> {
            assertTrue(((Label) stage.getScene().lookup("#demo-hover"))
                    .getText().contains("founder · ID 0"));
            assertEquals("animate", view.playback().current().action);
            assertEquals(0.5, view.playback().fraction(), 0.0001);
        });
    }

    public void captureEvidence() throws Exception {
        var image = org.testfx.util.WaitForAsyncUtils
                .asyncFx(() -> stage.getScene().snapshot(null)).get();
        Path destination = Path.of("target", "warehouse-qa", "demo-studio.png");
        Files.createDirectories(destination.getParent());
        javax.imageio.ImageIO.write(
                javafx.embed.swing.SwingFXUtils.fromFXImage(image, null), "png",
                destination.toFile());
    }

    public void verifyPosition(double time, int colonies) {
        context.getRobot().interact(() -> {
            assertNotNull(view.playback(),
                    ((Label) stage.getScene().lookup("#demo-error")).getText());
            assertEquals(time, view.playback().time(), 0.01);
            assertEquals(colonies,
                    view.playback().model().getColonies().size());
            assertTrue(view.isPaused());
        });
    }

    public void verifyReloadError(boolean expected) {
        context.getRobot()
                .interact(() -> assertEquals(expected,
                        !((Label) stage.getScene().lookup("#demo-error"))
                                .getText().isEmpty()));
    }

    public void verifyAvailableScenes(int count) {
        context.getRobot()
                .interact(() -> assertEquals(count,
                        ((ComboBox<?>) stage.getScene().lookup("#demo-scenes"))
                                .getItems().size()));
    }

    public void close() throws Exception {
        context.getRobot().interact(stage::close);
        try (var files = Files.walk(root)) {
            for (Path file : files.sorted(java.util.Comparator.reverseOrder())
                    .toList())
                Files.delete(file);
        }
    }
}
