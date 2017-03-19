package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.event.RoundStartedEvent;

import com.google.common.base.Preconditions;

/**
 * It's called when human user should start play his move.
 * 
 */
//FIXME JJ extends from AbstractEventController
public class NextTurnController {

	private final Logger logger = Logger.getLogger(NextTurnController.class);

	private final List<NextTurnListener> listeners = new ArrayList<NextTurnListener>();

	public void addNextTurnListener(final NextTurnListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireNextTurnEvent(final RoundStartedEvent event) {
		Preconditions.checkNotNull(event);
		logger.trace("firing next turn event: " + event);
		listeners.forEach(listener -> {
			listener.onNextTurn(event);
		});
	}

}
