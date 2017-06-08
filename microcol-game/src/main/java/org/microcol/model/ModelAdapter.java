package org.microcol.model;

import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedEvent;
import org.microcol.model.event.UnitStoredEvent;

public class ModelAdapter implements ModelListener {
	@Override
	public void gameStarted(final GameStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void roundStarted(final RoundStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void turnStarted(final TurnStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void unitMoved(final UnitMovedEvent event) {
		// Do nothing.
	}

	@Override
	public void unitAttacked(final UnitAttackedEvent event) {
		// Do nothing.
	}

	@Override
	public void unitStored(final UnitStoredEvent event) {
		// Do nothing.
	}

	@Override
	public void gameFinished(final GameFinishedEvent event) {
		// Do nothing.
	}

	@Override
	public void debugRequested(final DebugRequestedEvent event) {
		// Do nothing.
	}
}
