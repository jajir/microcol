package org.akropia.gui.screen.menu.demo;

import org.akropia.model.Model;

/** Reconstructs presentation time without undoing gameplay operations. */
public final class DemoPlayback {
    private final DemoScene scene;
    private Model model;
    private int step;
    private double time;
    private long boundary;

    public DemoPlayback(DemoScene scene) {
        this.scene = scene;
        seek(0);
    }

    public void seek(double target) {
        if (!Double.isFinite(target))
            throw new IllegalArgumentException("Invalid presentation time");
        target = Math.max(0, Math.min(duration(), target));
        if (model == null || target < time) {
            model = scene.newModel();
            step = 0;
            boundary = 0;
        }
        while (step < scene.steps.size()
                && boundary + scene.steps.get(step).durationMs <= target) {
            DemoScene.Step current = scene.steps.get(step);
            try {
                scene.execute(model, current);
            } catch (RuntimeException error) {
                throw new IllegalStateException("Step " + (step + 1) + ": "
                        + current.action + " (" + current.actor + ")", error);
            }
            boundary += current.durationMs;
            step++;
        }
        time = target;
    }

    public long duration() {
        return scene.steps.stream().mapToLong(s -> s.durationMs).sum();
    }

    public Model model() {
        return model;
    }

    public int step() {
        return step;
    }

    public double time() {
        return time;
    }

    public DemoScene scene() {
        return scene;
    }

    public DemoScene.Step current() {
        return step < scene.steps.size() ? scene.steps.get(step) : null;
    }

    public double fraction() {
        return current() == null ? 1 : (time - boundary) / current().durationMs;
    }

    public long adjacentBoundary(boolean forward) {
        long position = 0, previous = 0;
        for (DemoScene.Step item : scene.steps) {
            position += item.durationMs;
            if (forward && position > time)
                return position;
            if (!forward && position >= time)
                return previous;
            previous = position;
        }
        return forward ? duration() : previous;
    }
}
