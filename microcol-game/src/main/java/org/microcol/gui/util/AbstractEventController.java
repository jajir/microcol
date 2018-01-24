package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public AbstractEventController() {
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
		if (containsListener(listener)) {
			logger.debug("Attempt to register one listener '" + listener + "'more that one time.");
		} else {
			listeners.add(new Wrapper<>(fireEventsAsynchronously, listener));
		}
	}

	/**
	 * If listener with is already added then return <code>true</code> otherwise
	 * return <code>false</code>.
	 * 
	 * @param listener
	 *            required listener
	 * @return return <code>true</code> if given listener is already registered
	 */
	private boolean containsListener(final Listener<E> listener) {
		return listeners.stream().filter(wrapper -> wrapper.listener.equals(listener)).findAny().isPresent();
	}

	/**
	 * Remove listener from controller.
	 * 
	 * @param listener
	 *            required listener instance
	 */
	public void removeListener(final Listener<E> listener) {
		Preconditions.checkNotNull(listener);
		final Optional<Wrapper<E>> oWrap = listeners.stream().filter(wrapper -> wrapper.listener.equals(listener))
				.findFirst();
		if (oWrap.isPresent()) {
			listeners.remove(oWrap.get());
		}
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
			if (wrapper.fireEventsAsynchronously) {
				/**
				 * Is it correct to call events in Platform.runLater even when
				 * doesn't change UI? Probably yes. Lot of events could flooding
				 * queue and make UI unresponsive. In case of problems with
				 * 'runLatert' than should be used just in case of UI event
				 */
				Platform.runLater(() -> wrapper.listener.onEvent(event));
			} else {
				wrapper.listener.onEvent(event);
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
