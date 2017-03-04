package org.microcol.gui.event;

import org.microcol.model.event.TurnStartedEvent;

/**
 * Listen to new turn.
 */
public interface TurnStartedListener {

	/**
	 * It's called when turn is triggered. Listener reflect only human players.
	 * 
	 * @param event
	 *            required turn started event
	 */
	void onNewTurn(TurnStartedEvent event);

}
