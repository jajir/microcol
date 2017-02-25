package org.microcol.model;

import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

public interface GameListener {
	void roundStarted(RoundStartedEvent event);

	void turnStarted(TurnStartedEvent event);

	void shipMoved(ShipMovedEvent event);
}
