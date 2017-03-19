package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Simple event controller. Class should not be extended.
 * 
 * @param <E>
 *            event object type
 */
public abstract class AbstractEventController<E> {

	private final Logger logger = Logger.getLogger(AbstractEventController.class);

	/**
	 * Holds all registered listeners.
	 */
	private final List<Listener<E>> listeners = new ArrayList<>();

	/**
	 * Method add listener.
	 * 
	 * @param listener
	 *            required listener
	 */
	public void addListener(final Listener<E> listener) {
		Preconditions.checkNotNull(listener);
		if (listeners.contains(listener)) {
			logger.debug("Attempt to register one listener '" + listener + "'more that one time.");
		} else {
			listeners.add(listener);
		}
	}

	/**
	 * Remove listener from controller.
	 * 
	 * @param listener
	 *            required listener instance
	 */
	public void removeListener(final Listener<E> listener) {
		Preconditions.checkNotNull(listener);
		listeners.remove(listener);
	}

	/**
	 * Synchronously invoke event on all listeners. Listeners are called in
	 * random order.
	 * 
	 * @param event
	 *            required event object
	 */
	public void fireEvent(final E event) {
		Preconditions.checkNotNull(event);
		logger.debug("Event " + event + " was triggered.");
		listeners.stream().forEach(listener -> listener.onEvent(event));
	}

}
