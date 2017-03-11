package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Process events related to view of game.
 */
public class ViewController {

	private final List<MicrocolListenerVoid> listeners = new ArrayList<>();

	public void addCenterViewListener(final MicrocolListenerVoid listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireCenterView() {
		listeners.forEach(listener -> listener.onEvent());
	}

}
