package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
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

		executeInSeparateThread(listener -> listener.roundStarted(event));
	}

	void fireTurnStarted(final Model model, final Player player) {
		final TurnStartedEvent event = new TurnStartedEvent(model, player);

		logger.info("Turn started: {}.", event);

		executeInSeparateThread(listener -> listener.turnStarted(event));
	}

	void fireUnitMoved(final Model model, final Unit unit, final Location start, final Path path) {
		final UnitMovedEvent event = new UnitMovedEvent(model, unit, start, path);

		logger.info("Unit moved: {}.", event);

		executeInSeparateThread(listener -> listener.unitMoved(event));
	}

	void fireUnitAttacked(final Model model, final Unit attacker, final Unit defender, final Unit destroyed) {
		final UnitAttackedEvent event = new UnitAttackedEvent(model, attacker, defender, destroyed);

		logger.info("Unit attacked: {}.", event);

		executeInSeparateThread(listener -> listener.unitAttacked(event));
	}

	void fireUnitEmbarked(final Model model, final Unit unit, final CargoSlot slot) {
		final UnitEmbarkedEvent event = new UnitEmbarkedEvent(model, unit, slot);

		logger.info("Unit embarked: {}.", event);

		listeners.forEach(listener -> listener.unitEmbarked(event));
	}
	
	void fireGoldWasChanged(final Model model,final Player player, final int oldValue, final int newValue) {
		final GoldWasChangedEvent event = new GoldWasChangedEvent(model, player, oldValue, newValue);

		logger.info("Gold amount changed: {}.", event);
		
		listeners.forEach(listener -> listener.goldWasChanged(event));
	}
	
	void fireColonyWasCaptured(final Model model, final Unit capturingUnit, final Colony capturedColony) {
		final ColonyWasCapturedEvent event = new ColonyWasCapturedEvent(model, capturingUnit, capturedColony);

		logger.info("Colony was captured: {}.", event);
		
		listeners.forEach(listener -> listener.colonyWasCaptured(event));
	}


	void fireGameFinished(final Model model, final GameOverResult gameOverResult) {
		final GameFinishedEvent event = new GameFinishedEvent(model, gameOverResult);

		logger.info("Game finished: {}.", event);

		listeners.forEach(listener -> listener.gameFinished(event));
	}

	void fireDebugRequested(final Model model, final List<Location> locations) {
		final DebugRequestedEvent event = new DebugRequestedEvent(model, locations);

		listeners.forEach(listener -> listener.debugRequested(event));
	}

	private void executeInSeparateThread(Consumer<ModelListener> action) {
		listeners.forEach(listener -> Executors.newSingleThreadExecutor().execute(() -> action.accept(listener)));
	}
	
}
