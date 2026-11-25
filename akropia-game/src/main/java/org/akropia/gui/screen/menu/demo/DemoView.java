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
    private Model model;
    private DemoPlayback playback;
    private long lastFrame;
    private boolean paused;
    private boolean looping = true;
    private double speed = 1;
    private java.util.function.Consumer<RuntimeException> errorHandler = error -> {
    };
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (paused) {
                lastFrame = now;
                return;
            }
            if (now - lastFrame < 33000000)
                return;
            double delta = lastFrame == 0 ? 0 : (now - lastFrame) / 1000000d;
            lastFrame = now;
            try {
                double target = playback.time() + delta * speed;
                if (target >= playback.duration() && looping) {
                    playback = new DemoPlayback(playback.scene());
                    target = 0;
                }
                seek(target);
                if (playback.time() >= playback.duration())
                    paused = true;
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
        canvas.widthProperty().addListener((o, a, b) -> {
            if (playback != null && model != null)
                paint();
        });
        canvas.heightProperty().addListener((o, a, b) -> {
            if (playback != null && model != null)
                paint();
        });
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
    }

    public void start() {
        try {
            load(DemoScene.load(), 0);
            setPaused(false);
        } catch (RuntimeException error) {
            fail(error);
        }
    }

    public void load(DemoScene scene, double time) {
        DemoPlayback candidate = new DemoPlayback(scene);
        candidate.seek(time);
        playback = candidate;
        model = candidate.model();
        paused = true;
        lastFrame = 0;
        painter.refresh();
        getChildren().setAll(canvas);
        paint();
        timer.start();
    }

    public DemoPlayback playback() {
        return playback;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean value) {
        paused = value;
        lastFrame = 0;
    }

    public void setLooping(boolean value) {
        looping = value;
    }

    public void setSpeed(double value) {
        if (!Double.isFinite(value) || value <= 0)
            throw new IllegalArgumentException("Invalid speed");
        speed = value;
    }

    public void onError(java.util.function.Consumer<RuntimeException> handler) {
        errorHandler = handler;
    }

    public void seek(double time) {
        int previousStep = playback.step();
        playback.seek(time);
        boolean changed = model != playback.model()
                || previousStep != playback.step();
        model = playback.model();
        if (changed)
            painter.refresh();
        paint();
    }

    public void stop() {
        timer.stop();
        model = null;
    }

    private void fail(RuntimeException error) {
        paused = true;
        org.slf4j.LoggerFactory.getLogger(DemoView.class).warn("Demo failed",
                error);
        errorHandler.accept(error);
        getChildren().setAll(new Label(texts
                .get(org.akropia.gui.screen.menu.GameMenu.demoUnavailable)));
    }

    private void paint() {
        if (getWidth() < 1 || getHeight() < 1)
            return;
        DemoScene scene = playback.scene();
        DemoScene.Step current = playback.current();
        canvas.setOpacity(Math.max(0, Math.min(1,
                Math.min(playback.time(), playback.duration() - playback.time())
                        / 500)));
        Unit moving = null;
        Location from = null, to = null;
        if (current != null && ("move".equals(current.action)
                || "disembark".equals(current.action))) {
            moving = scene.actor(model, current);
            from = scene.movementOrigin(model, current);
            to = scene.destination(current);
        }
        painter.paint(canvas.getGraphicsContext2D(),
                Location.of(scene.camera[0], scene.camera[1]),
                (long) (playback.time() / 33), moving, from, to,
                playback.fraction());
    }
}
