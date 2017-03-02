package org.microcol.gui.event;

import org.microcol.model.event.RoundStartedEvent;

/**
 * Listener is called when new round starts.
 * 
 */
public interface NextTurnListener {

	void onNextTurn(RoundStartedEvent game);
}
