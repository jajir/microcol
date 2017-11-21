package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.event.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javafx.application.Platform;

/**
 * Simple event controller. Class should not be extended.
 * 
 * @param <E>
 *            event object type
 */
public abstract class AbstractEventController<E> {

	private final Logger logger = LoggerFactory.getLogger(AbstractEventController.class);

	/**
	 * Holds all registered listeners.
	 */
	private final List<Wrapper<E>> listeners = new ArrayList<>();

	private final boolean fireEventsAsynchronously;

	public AbstractEventController() {
		this(true);
	}

	public AbstractEventController(final boolean fireEventsAsynchronously) {
		this.fireEventsAsynchronously = fireEventsAsynchronously;
	}

	/**
	 * Method add listener.
	 * 
	 * @param listener
	 *            required listener
	 */
	public void addListener(final Listener<E> listener) {
		addListener(false, listener);
	}

	/**
	 * Method add listener which will started in separate thread. This is
	 * dedicated for operation that changing UI.
	 * 
	 * @param listener
	 *            required listener
	 */
	public void addRunLaterListener(final Listener<E> listener) {
		addListener(true, listener);
	}

	private void addListener(final boolean fireEventsAsynchronously, final Listener<E> listener) {
		Preconditions.checkNotNull(listener);
		if (listeners.contains(listener)) {
			logger.debug("Attempt to register one listener '" + listener + "'more that one time.");
		} else {
			listeners.add(new Wrapper<>(fireEventsAsynchronously, listener));
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
		listeners.stream().forEach(wrapper -> {
			if (fireEventsAsynchronously) {
				/**
				 * Is it correct to call events in Platform.runLater even when
				 * doesn't change UI? Probably yes. Lot of events could flooding
				 * queue and make UI unresponsive. In case of problems with
				 * 'runLatert' than should be used just in case of UI event
				 */
				Platform.runLater(() -> wrapper.listener.onEvent(event));
			} else {
				if (wrapper.fireEventsAsynchronously) {
					Platform.runLater(() -> wrapper.listener.onEvent(event));
				} else {
					wrapper.listener.onEvent(event);
				}
			}
		});
	}

	private class Wrapper<T> {

		private final boolean fireEventsAsynchronously;

		private final Listener<T> listener;

		Wrapper(final boolean fireEventsAsynchronously, final Listener<T> listener) {
			this.fireEventsAsynchronously = fireEventsAsynchronously;
			this.listener = Preconditions.checkNotNull(listener);
		}

	}

}
