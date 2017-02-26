package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.Game;

import com.google.common.base.Preconditions;

/**
 * It's called when haman user should start play his move.
 * 
 */
public class NextTurnController {

	private final Logger logger = Logger.getLogger(NextTurnController.class);

	private final List<NextTurnListener> listeners = new ArrayList<NextTurnListener>();

	public void addNextTurnListener(final NextTurnListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireNextTurnEvent(final Game game) {
		Preconditions.checkNotNull(game);
		logger.trace("firing next turn event: " + game);
		listeners.forEach(listener -> {
			listener.onNextTurn(game);
		});
	}

}
