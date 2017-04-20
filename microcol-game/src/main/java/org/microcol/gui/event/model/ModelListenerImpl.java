package org.microcol.gui.event.model;

import org.microcol.model.ModelAdapter;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedEvent;

import com.google.common.base.Preconditions;

/**
 * Translate model events to GUI events.
 */
public class ModelListenerImpl extends ModelAdapter {

	private final ModelEventManager modelEventManager;

	public ModelListenerImpl(final ModelEventManager modelEventManager) {
		this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
	}

	@Override
	public void turnStarted(final TurnStartedEvent event) {
		modelEventManager.getTurnStartedController().fireEvent(event);
	}

	@Override
	public void unitMoved(final UnitMovedEvent event) {
		modelEventManager.getMoveUnitController().fireUnitMovedEvent(event);
	}

	@Override
	public void roundStarted(final RoundStartedEvent event) {
		modelEventManager.getNextTurnController().fireEvent(event);
	}

	@Override
	public void gameStarted(final GameStartedEvent event) {
		modelEventManager.getNewGameController().fireEvent(event);
	}

	@Override
	public void gameFinished(final GameFinishedEvent event) {
		modelEventManager.getGameFinishedController().fireEvent(event);
	}

	@Override
	public void debugRequested(final DebugRequestedEvent event) {
		modelEventManager.getDebugRequestController().fireEvent(event);
	}

	@Override
	public void unitAttacked(final UnitAttackedEvent event) {
		modelEventManager.getUnitAttackedEventController().fireEvent(event);
	}

}