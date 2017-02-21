package org.microcol.model.event;

public interface GameListener {
	void roundStarted(RoundStartedEvent event);

	void turnStarted(TurnStartedEvent event);

	void shipMoved(ShipMovedEvent event);
}
