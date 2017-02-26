package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

class ModelListenersManager {
	private final List<ModelListener> listeners;

	public ModelListenersManager() {
		this.listeners = new ArrayList<>();
	}

	public void addListener(final ModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(final ModelListener listener) {
		listeners.remove(listener);
	}

	public void fireGameStarted(final Game game) {
		final GameStartedEvent event = new GameStartedEvent(game);

		listeners.forEach(listener -> {
			listener.gameStarted(event);
		});
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

	public void fireGameFinished(final Game game) {
		final GameFinishedEvent event = new GameFinishedEvent(game);

		listeners.forEach(listener -> {
			listener.gameFinished(event);
		});
	}
}
