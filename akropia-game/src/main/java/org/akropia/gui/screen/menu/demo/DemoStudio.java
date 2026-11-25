package org.akropia.gui.screen.menu.demo;

import java.nio.file.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.ActionButtons;

/** Developer controls around the same preview used by the menu. */
public final class DemoStudio {
    private final DemoView view;
    private final Path root;
    private final ComboBox<String> scenes = new ComboBox<>();
    private final Slider position = new Slider();
    private final Label status = new Label();
    private final Label error = new Label();
    private final TextArea actors = new TextArea();
    private boolean updating;
    private final Label hovered = new Label();
    private javafx.geometry.Point2D pointer;
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (view.playback() == null)
                return;
            hovered.setText(pointer == null ? ""
                    : view.describeAt(pointer.getX(), pointer.getY()));
            updating = true;
            var playback = view.playback();
            if (!position.isValueChanging())
                position.setValue(playback.time());
            status.setText(String.format("%.1f / %.1f s — step %d%s",
                    playback.time() / 1000, playback.duration() / 1000d,
                    playback.step() + 1, playback.current() == null ? " — end"
                            : " — " + playback.current().action));
            updating = false;
        }
    };

    public DemoStudio(DemoView view, Path root) {
        this.view = view;
        this.root = root;
    }

    public void show(Stage stage) {
        hovered.setId("demo-hover");
        view.setMouseTransparent(false);
        view.setOnMouseMoved(
                e -> pointer = new javafx.geometry.Point2D(e.getX(), e.getY()));
        view.setOnMouseExited(e -> {
            pointer = null;
            hovered.setText("");
        });
        scenes.setId("demo-scenes");
        position.setId("demo-position");
        position.setAccessibleText("Presentation time");
        actors.setEditable(false);
        actors.setPrefRowCount(4);
        actors.setId("demo-actors");
        error.setId("demo-error");
        error.setWrapText(true);
        var play = button(ButtonIcon.PLAY, "Play / pause",
                () -> view.setPaused(!view.isPaused()));
        var stop = button(ButtonIcon.STOP, "Stop", () -> {
            view.setPaused(true);
            view.seek(0);
        });
        var reload = button(ButtonIcon.RECOVER, "Reload", () -> reload(true));
        var previous = button(ButtonIcon.BACK, "Previous step",
                () -> jump(false));
        var next = button(ButtonIcon.NEXT, "Next step", () -> jump(true));
        ComboBox<Double> speed = new ComboBox<>();
        speed.getItems().setAll(0.25, 0.5, 1.0, 2.0);
        speed.setValue(1.0);
        speed.setAccessibleText("Playback speed");
        speed.setOnAction(e -> view.setSpeed(speed.getValue()));
        CheckBox loop = new CheckBox("Loop");
        loop.setSelected(true);
        loop.setOnAction(e -> view.setLooping(loop.isSelected()));
        position.valueProperty().addListener((o, oldValue, newValue) -> {
            if (!updating && view.playback() != null) {
                view.setPaused(true);
                attempt(() -> view.seek(newValue.doubleValue()));
            }
        });
        scenes.valueProperty().addListener(
                (observable, previousScene, selectedScene) -> reload(false));
        view.onError(e -> error.setText(e.getMessage()));
        VBox content = new VBox(10, scenes, new StackPane(view),
                new HBox(8, play, stop, previous, next, reload), position,
                new HBox(8, speed, loop, status), hovered, error, actors);
        content.setStyle("-fx-padding: 16;");
        Scene scene = new Scene(content, 900, 620);
        scene.getStylesheets()
                .add(org.akropia.gui.MainStageBuilder.STYLE_SHEET_AKROPIA);
        stage.setTitle("Akropia — Demo Studio");
        stage.setScene(scene);
        stage.setOnHidden(e -> {
            timer.stop();
            view.stop();
        });
        attempt(() -> {
            try (var entries = Files.list(root)) {
                scenes.getItems()
                        .setAll(entries
                                .filter(p -> Files
                                        .isRegularFile(p.resolve("scene.json")))
                                .map(p -> p.getFileName().toString()).sorted()
                                .toList());
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Cannot read " + root, e);
            }
            if (scenes.getItems().isEmpty())
                throw new IllegalArgumentException("No scenes in " + root);
            scenes.getSelectionModel().selectFirst();
        });
        stage.show();
        timer.start();
    }

    private Button button(ButtonIcon icon, String text, Runnable action) {
        Button button = ActionButtons.button(icon, text);
        button.setOnAction(e -> attempt(action));
        return button;
    }

    private void jump(boolean forward) {
        view.setPaused(true);
        view.seek(view.playback().adjacentBoundary(forward));
    }

    private void reload(boolean keepTime) {
        attempt(() -> {
            double time = keepTime && view.playback() != null
                    ? view.playback().time()
                    : 0;
            Path directory = root.resolve(scenes.getValue());
            view.load(DemoScene.load(directory), time);
            updating = true;
            position.setMax(view.playback().duration());
            position.setValue(view.playback().time());
            updating = false;
            actors.setText(view.playback().scene().actors.toString() + "\n"
                    + view.playback().model().getAllUnits().stream()
                            .map(u -> u.getId() + ": " + u.getType() + " — "
                                    + u.getOwner().getName())
                            .collect(
                                    java.util.stream.Collectors.joining("\n")));
            error.setText("");
        });
    }

    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(DemoStudio.class)
                    .warn("Cannot load or play demo", e);
            error.setText(e.getMessage());
        }
    }
}
