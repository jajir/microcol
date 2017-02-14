package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.model.World;

import com.google.common.base.Preconditions;

public class NextTurnController {

	private final Logger logger = Logger.getLogger(NextTurnController.class);

	private final List<NextTurnListener> listeners = new ArrayList<NextTurnListener>();

	public void addNextTurnListener(final NextTurnListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireNextTurnEvent(final World world) {
		Preconditions.checkNotNull(world);
		logger.trace("firing next turn event: " + world);
		listeners.forEach(listener -> {
			listener.onNextTurn(world);
		});
	}

}
