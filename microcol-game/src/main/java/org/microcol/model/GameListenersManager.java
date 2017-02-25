package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

class GameListenersManager {
	private final List<GameListener> listeners;

	public GameListenersManager() {
		this.listeners = new ArrayList<>();
	}

	public void addListener(GameListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(GameListener listener) {
		listeners.remove(listener);
	}

	public void fireRoundStarted(final Game game, final Calendar calendar) {
		final RoundStartedEvent event = new RoundStartedEvent(game, calendar);

		listeners.forEach(listener -> {
			listener.roundStarted(event);
		});
	}

	public void fireTurnStarted(final Game game, final Player player) {
		final TurnStartedEvent event = new TurnStartedEvent(game, player);

		listeners.forEach(listener -> {
			listener.turnStarted(event);
		});
	}

	public void fireShipMoved(final Game game, final Ship ship, final Location startLocation, final Path path) {
		final ShipMovedEvent event = new ShipMovedEvent(game, ship, startLocation, path);

		listeners.forEach(listener -> {
			listener.shipMoved(event);
		});
	}
}
