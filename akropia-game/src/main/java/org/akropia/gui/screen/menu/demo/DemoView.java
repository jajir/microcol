package org.akropia.gui.screen.menu.demo;

import com.google.inject.Inject;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.game.gamepanel.DemoMapPainter;
import org.akropia.gui.util.FontService;
import org.akropia.i18n.I18n;
import org.akropia.model.*;

/** Non-interactive presentation with an independent clock and model. */
public final class DemoView extends StackPane {
    private final Canvas canvas = new Canvas();
    private final DemoMapPainter painter;
    private final I18n texts;
    private DemoScene scene;
    private Model model;
    private int step;
    private long started;
    private long lastFrame;
    private boolean paused;
    private long pausedAt;
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (paused)
                return;
            if (now - lastFrame < 33000000)
                return;
            lastFrame = now;
            try {
                frame(now);
            } catch (RuntimeException error) {
                fail(error);
            }
        }
    };

    @Inject
    public DemoView(ImageProvider images, FontService fonts, I18n texts,
            GamePreferences preferences) {
        this.texts = texts;
        painter = new DemoMapPainter(() -> model, images, fonts, texts,
                preferences);
        setId("menu-demo");
        setMouseTransparent(true);
        setFocusTraversable(false);
        setMinSize(260, 220);
        setPrefSize(600, 360);
        setMaxSize(600, 360);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
    }

    public void start() {
        stop();
        paused = false;
        try {
            scene = DemoScene.load();
            reset();
            getChildren().setAll(canvas);
            timer.start();
        } catch (RuntimeException error) {
            fail(error);
        }
    }

    public void setPaused(boolean paused) {
        if (this.paused == paused)
            return;
        this.paused = paused;
        if (paused)
            pausedAt = System.nanoTime();
        else if (started != 0)
            started += System.nanoTime() - pausedAt;
    }

    public void stop() {
        timer.stop();
        model = null;
    }

    private void reset() {
        model = scene.newModel();
        painter.refresh();
        step = 0;
        started = 0;
    }

    private void fail(RuntimeException error) {
        stop();
        org.slf4j.LoggerFactory.getLogger(DemoView.class)
                .warn("Demo failed at step {}", step, error);
        getChildren().setAll(new Label(texts
                .get(org.akropia.gui.screen.menu.GameMenu.demoUnavailable)));
    }

    private void frame(long now) {
        if (getWidth() < 1 || getHeight() < 1)
            return;
        if (started == 0)
            started = now;
        DemoScene.Step current = scene.steps.get(step);
        double fraction = current.durationMs == 0 ? 1
                : Math.min(1,
                        (now - started) / (current.durationMs * 1000000d));
        double elapsedMs = (now - started) / 1000000d;
        canvas.setOpacity(step == 0 ? Math.min(1, elapsedMs / 500)
                : step == scene.steps.size() - 1
                        ? Math.min(1,
                                Math.max(0,
                                        (current.durationMs - elapsedMs) / 500))
                        : 1);
        Unit moving = null;
        Location from = null, to = null;
        if ("move".equals(current.action)) {
            moving = scene.actor(model, current);
            from = moving.getLocation();
            to = scene.destination(current);
        }
        painter.paint(canvas.getGraphicsContext2D(),
                Location.of(scene.camera[0], scene.camera[1]), now / 33000000,
                moving, from, to, fraction);
        if (fraction >= 1) {
            scene.execute(model, current);
            painter.refresh();
            step++;
            started = now;
            if (step == scene.steps.size())
                reset();
        }
    }
}
