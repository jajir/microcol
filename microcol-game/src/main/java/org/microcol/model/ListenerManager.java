package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipAttackedEvent;
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

	void fireGameStarted(final Model model) {
		final GameStartedEvent event = new GameStartedEvent(model);

		listeners.forEach(listener -> listener.gameStarted(event));
	}

	void fireRoundStarted(final Model model, final Calendar calendar) {
		final RoundStartedEvent event = new RoundStartedEvent(model, calendar);

		listeners.forEach(listener -> listener.roundStarted(event));
	}

	void fireTurnStarted(final Model model, final Player player) {
		final TurnStartedEvent event = new TurnStartedEvent(model, player);

		listeners.forEach(listener -> listener.turnStarted(event));
	}

	void fireShipMoved(final Model model, final Ship ship, final Location start, final Path path) {
		final ShipMovedEvent event = new ShipMovedEvent(model, ship, start, path);

		listeners.forEach(listener -> listener.shipMoved(event));
	}

	void fireShipAttacked(final Model model, final Ship attacker, final Ship defender, final Ship destroyed) {
		final ShipAttackedEvent event = new ShipAttackedEvent(model, attacker, defender, destroyed);

		listeners.forEach(listener -> listener.shipAttacked(event));
	}

	void fireGameFinished(final Model model) {
		final GameFinishedEvent event = new GameFinishedEvent(model);

		listeners.forEach(listener -> listener.gameFinished(event));
	}

	void fireDebugRequested(final Model model, final List<Location> locations) {
		final DebugRequestedEvent event = new DebugRequestedEvent(model, locations);

		listeners.forEach(listener -> listener.debugRequested(event));
	}
}
