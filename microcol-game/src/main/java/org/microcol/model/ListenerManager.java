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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

class ListenerManager {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final List<ModelListener> listeners;

	ListenerManager() {
		listeners = new ArrayList<>();
	}

	void addListener(final ModelListener listener) {
		Preconditions.checkNotNull(listener);

		if (!listeners.contains(listener)) {
			logger.info("Added model listener {}.", listener);
			listeners.add(listener);
		} else {
			logger.warn("Model listener {} already added.", listener);
		}
	}

	void removeListener(final ModelListener listener) {
		Preconditions.checkNotNull(listener);

		logger.info("Removed model listener {}.", listener);

		listeners.remove(listener);
	}

	void fireGameStarted(final Model model) {
		final GameStartedEvent event = new GameStartedEvent(model);

		logger.info("Game started: {}.", event);

		listeners.forEach(listener -> listener.gameStarted(event));
	}

	void fireRoundStarted(final Model model, final Calendar calendar) {
		final RoundStartedEvent event = new RoundStartedEvent(model, calendar);

		logger.info("Round started: {}.", event);

		listeners.forEach(listener -> listener.roundStarted(event));
	}

	void fireTurnStarted(final Model model, final Player player) {
		final TurnStartedEvent event = new TurnStartedEvent(model, player);

		logger.info("Turn started: {}.", event);

		listeners.forEach(listener -> listener.turnStarted(event));
	}

	void fireShipMoved(final Model model, final Ship ship, final Location start, final Path path) {
		final ShipMovedEvent event = new ShipMovedEvent(model, ship, start, path);

		logger.info("Ship moved: {}.", event);

		listeners.forEach(listener -> listener.shipMoved(event));
	}

	void fireShipAttacked(final Model model, final Ship attacker, final Ship defender, final Ship destroyed) {
		final ShipAttackedEvent event = new ShipAttackedEvent(model, attacker, defender, destroyed);

		logger.info("Ship attacked: {}.", event);

		listeners.forEach(listener -> listener.shipAttacked(event));
	}

	void fireGameFinished(final Model model) {
		final GameFinishedEvent event = new GameFinishedEvent(model);

		logger.info("Game finished: {}.", event);

		listeners.forEach(listener -> listener.gameFinished(event));
	}

	void fireDebugRequested(final Model model, final List<Location> locations) {
		final DebugRequestedEvent event = new DebugRequestedEvent(model, locations);

		listeners.forEach(listener -> listener.debugRequested(event));
	}
}
