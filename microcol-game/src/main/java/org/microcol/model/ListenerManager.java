package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;

class ListenerManager {
	private final List<ModelListener> listeners;

	ListenerManager() {
		listeners = new ArrayList<>();
	}

	void addListener(final ModelListener listener) {
		Preconditions.checkNotNull(listener);

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	void removeListener(final ModelListener listener) {
		Preconditions.checkNotNull(listener);

		listeners.remove(listener);
	}

	void fireGameStarted(final Game game) {
		final GameStartedEvent event = new GameStartedEvent(game);

		listeners.forEach(listener -> listener.gameStarted(event));
	}

	void fireRoundStarted(final Game game, final Calendar calendar) {
		final RoundStartedEvent event = new RoundStartedEvent(game, calendar);

		listeners.forEach(listener -> listener.roundStarted(event));
	}

	void fireTurnStarted(final Game game, final Player player) {
		final TurnStartedEvent event = new TurnStartedEvent(game, player);

		listeners.forEach(listener -> listener.turnStarted(event));
	}

	void fireShipMoved(final Game game, final Ship ship, final Location start, final Path path) {
		final ShipMovedEvent event = new ShipMovedEvent(game, ship, start, path);

		listeners.forEach(listener -> listener.shipMoved(event));
	}

	void fireGameFinished(final Game game) {
		final GameFinishedEvent event = new GameFinishedEvent(game);

		listeners.forEach(listener -> listener.gameFinished(event));
	}
}
