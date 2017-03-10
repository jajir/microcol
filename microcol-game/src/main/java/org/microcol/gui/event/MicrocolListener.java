package org.microcol.gui.event;

/**
 * This interface helps to avoid creating lot of listeners that just listen to
 * one event.
 * 
 * @param <E>
 *            generic event
 */
public interface MicrocolListener<E> {

	/**
	 * It's called when event occurs.
	 * 
	 * @param event
	 */
	void onEvent(E event);

}
