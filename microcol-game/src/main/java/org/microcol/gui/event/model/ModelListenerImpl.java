package org.microcol.gui.event.model;

import java.util.Optional;

import org.microcol.model.ModelListener;
import org.microcol.model.Player;
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

import com.google.common.base.Preconditions;

/**
 * Translate model events to GUI events.
 */
public class ModelListenerImpl implements ModelListener {

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
		modelEventManager.getUnitMovedController().fireEvent(event);
	}

	@Override
	public void roundStarted(final RoundStartedEvent event) {
		modelEventManager.getNextTurnController().fireEvent(event);
	}

	@Override
	public void gameStarted(final GameStartedEvent event) {
		modelEventManager.getNewGameController().fireEvent(event);
		final Optional<Player> human = event.getModel().getPlayers().stream().filter(player -> player.isHuman())
				.findAny();
		if (human.isPresent()) {
			goldWasChanged(new GoldWasChangedEvent(event.getModel(), human.get(), human.get().getGold(),
					human.get().getGold()));
		}
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

	@Override
	public void goldWasChanged(final GoldWasChangedEvent event) {
		if (event.getPlayer().isHuman()) {
			modelEventManager.getGoldWasChangedController().fireEvent(event);
		}
	}
	
	@Override
	public void colonyWasCaptured(final ColonyWasCapturedEvent event) {
		if (event.getCapturedColony().getOwner().isHuman()) {
			modelEventManager.getColonyWasCapturedController().fireEvent(event);
		}
	}

	@Override
	public void unitEmbarked(final UnitEmbarkedEvent event) {
		if(event.getUnit().getOwner().isHuman()){
			modelEventManager.getUnitEmbarkController().fireEvent(event);
		}
	}
	
}
