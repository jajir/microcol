package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

//TODO rename it to AboutGameController
//TODO extends from some generic event controller
public class GameEventController {

	private final Logger logger = Logger.getLogger(GameEventController.class);

	private final List<GameEventListener> listeners = new ArrayList<GameEventListener>();

	public void addGameEventListener(final GameEventListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireAboutGameEvent() {
		logger.trace("firing about game event: ");
		listeners.forEach(listener -> {
			listener.onAboutGame();
		});
	}
}
