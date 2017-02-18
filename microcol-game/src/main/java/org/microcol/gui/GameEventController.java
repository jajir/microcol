package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

public class GameEventController {

	private final Logger logger = Logger.getLogger(GameEventController.class);

	private final List<GameEventListener> listeners = new ArrayList<GameEventListener>();

	public void addGameEventListener(final GameEventListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireGameExit() {
		logger.trace("firing game exit event: ");
		listeners.forEach(listener -> {
			listener.onGameExit();
		});
	}

	public void fireAboutGameEvent() {
		logger.trace("firing about game event: ");
		listeners.forEach(listener -> {
			listener.onAboutGame();
		});
	}
}
