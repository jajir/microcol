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
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Translate model events to GUI events.
 */
public class ModelListenerImpl implements ModelListener {

	private final ModelEventManager modelEventManager;
	
	private final GameModelController gameModelController;
	
	public ModelListenerImpl(final ModelEventManager modelEventManager, final GameModelController gameModelController) {
		this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("modelEventManager", modelEventManager)
				.add("gameModelController", gameModelController).toString();
	}

	@Override
	public void turnStarted(final TurnStartedEvent event) {
		modelEventManager.getTurnStartedController().fireEvent(event);
	}

	@Override
	public void unitMovedStep(final UnitMovedStepEvent event) {
		if (event.canPlayerSeeMove(gameModelController.getCurrentPlayer())) {
			modelEventManager.getUnitMovedController().fireEvent(event);
		}
	}

	@Override
	public void unitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
		if (event.getUnit().getOwner().equals(gameModelController.getCurrentPlayer())) {
			modelEventManager.getUnitMovedToHighSeasController().fireEvent(event);
		}
	}
	
	@Override
	public void unitMoveFinished(final UnitMoveFinishedEvent event) {
		modelEventManager.getUnitMoveFinishedController().fireEvent(event);
	}
	
	@Override
	public void roundStarted(final RoundStartedEvent event) {
		modelEventManager.getRoundStartedController().fireEvent(event);
	}

	@Override
	public void gameStarted(final GameStartedEvent event) {
		modelEventManager.getGameStartedController().fireEvent(event);
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
