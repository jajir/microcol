package org.microcol.model;

import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipAttackedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

public interface ModelListener {
	void gameStarted(GameStartedEvent event);

	void roundStarted(RoundStartedEvent event);

	void turnStarted(TurnStartedEvent event);

	void shipMoved(ShipMovedEvent event);

	void shipAttacked(ShipAttackedEvent event);

	void gameFinished(GameFinishedEvent event);

	void debugRequested(DebugRequestedEvent event);
}
