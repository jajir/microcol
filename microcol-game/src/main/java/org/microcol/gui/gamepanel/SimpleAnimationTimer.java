package org.microcol.gui.gamepanel;

import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import javafx.animation.AnimationTimer;

/**
 * Allows to call consume animation repaint event.
 */
public class SimpleAnimationTimer extends AnimationTimer {

	private final Consumer<Long> handler;

	SimpleAnimationTimer(final Consumer<Long> handler) {
		this.handler = Preconditions.checkNotNull(handler);
	}

	@Override
	public void handle(long now) {
		handler.accept(now);
	}

}
