package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.event.GameStartedEvent;

import com.google.common.base.Preconditions;

/**
 * Allows to trigger new game event and manage event listeners.
 */
public class NewGameController {

	private final Logger logger = Logger.getLogger(NewGameController.class);

	private final List<NewGameListener> listeners = new ArrayList<NewGameListener>();

	public void addNewGameListener(final NewGameListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireNewGameStartedEvent(final GameStartedEvent event) {
		Preconditions.checkNotNull(event);
		logger.debug("New game started: " + event);
		listeners.forEach(listener -> {
			listener.onNewGame(event.getGame());
		});
	}
}
