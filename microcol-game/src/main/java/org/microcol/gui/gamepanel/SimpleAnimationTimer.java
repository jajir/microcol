package org.microcol.gui.gamepanel;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import javafx.animation.AnimationTimer;

/**
 * Allows to call consume animation repaint event.
 */
public final class SimpleAnimationTimer extends AnimationTimer {

    private final Consumer<Long> handler;

    SimpleAnimationTimer(final Consumer<Long> handler) {
        this.handler = Preconditions.checkNotNull(handler);
    }

    @Override
    public void handle(final long now) {
        handler.accept(now);
    }

}
