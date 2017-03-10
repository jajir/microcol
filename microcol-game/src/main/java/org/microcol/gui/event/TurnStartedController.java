package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;

/**
 * Allows to trigger new game event and manage event listeners.
 */
public class TurnStartedController {

	private final Logger logger = Logger.getLogger(TurnStartedController.class);

	private final List<TurnStartedListener> listeners = new ArrayList<TurnStartedListener>();

	public void addTurnStartedListener(final TurnStartedListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	/**
	 * Controlled accept all user but listeners are called just for human
	 * players.
	 * 
	 * @param event
	 *            required event object
	 */
	public void fireTurnStartedEvent(final TurnStartedEvent event) {
		Preconditions.checkNotNull(event);
		logger.debug("New turn started: " + event);
		listeners.forEach(listener -> {
			listener.onNewTurn(event);
		});
	}
}
