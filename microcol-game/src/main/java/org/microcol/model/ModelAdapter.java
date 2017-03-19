package org.microcol.model;

import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

public class ModelAdapter implements ModelListener {
	@Override
	public void gameStarted(GameStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void roundStarted(RoundStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void turnStarted(TurnStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void shipMoved(ShipMovedEvent event) {
		// Do nothing.
	}

	@Override
	public void gameFinished(GameFinishedEvent event) {
		// Do nothing.
	}
}
