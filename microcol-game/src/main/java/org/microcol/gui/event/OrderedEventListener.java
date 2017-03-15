package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class OrderedEventListener<E> {

	/**
	 * Value of priority when it's not specified.
	 */
	private final static int DEFAULT_PRIORITY = 10;

	private final List<ValueWithPriority<E>> listeners = new ArrayList<>();

	/**
	 * It add listener with default priority.
	 * 
	 * @param listener
	 *            required listener
	 */
	public void addListener(final Listener<E> listener) {
		addListener(listener, DEFAULT_PRIORITY);
	}

	public void addListener(final Listener<E> listener, final int priority) {
		Preconditions.checkNotNull(listener);
		listeners.add(new ValueWithPriority<>(listener, priority));
	}

	/**
	 * Synchronously invoke event on all listeners. Listeners are called in
	 * ordered way. listener with lover priority is called before listener with
	 * bigger priority.
	 * 
	 * @param event
	 *            required event object
	 */
	public void fireEvent(final E event) {
		Preconditions.checkNotNull(event);
		listeners.stream().sorted((v1, v2) -> v1.getPriority() - v2.getPriority())
				.forEach(valueWithPriority -> valueWithPriority.getListener().onEvent(event));
	}

	/**
	 * Class helps to hold listener of specific type and it's priority.
	 * 
	 * @param <ET>
	 *            event type
	 */
	private class ValueWithPriority<ET> {

		private final Listener<ET> listener;

		private final int priority;

		public ValueWithPriority(final Listener<ET> listener, final int priority) {
			this.listener = listener;
			this.priority = priority;
		}

		public Listener<ET> getListener() {
			return listener;
		}

		public int getPriority() {
			return priority;
		}

	}

}
