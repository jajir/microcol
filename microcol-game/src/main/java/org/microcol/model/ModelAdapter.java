package org.microcol.model;

import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedStepEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;

public class ModelAdapter implements ModelListener {
	
	@Override
	public void gameStarted(final GameStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void roundStarted(final RoundStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void turnStarted(final TurnStartedEvent event) {
		// Do nothing.
	}

	@Override
	public void unitMovedStep(final UnitMovedStepEvent event) {
		// Do nothing.
	}
	
	@Override
	public void unitMovedToHighSeas(UnitMovedToHighSeasEvent event) {
		// Do nothing.
	}
	
	@Override
	public void unitMoveFinished(final UnitMoveFinishedEvent event){
		// Do nothing.		
	}


	@Override
	public void unitAttacked(final UnitAttackedEvent event) {
		// Do nothing.
	}

	@Override
	public void unitEmbarked(final UnitEmbarkedEvent event) {
		// Do nothing.
	}

	@Override
	public void goldWasChanged(GoldWasChangedEvent event) {
		// Do nothing.
	}
	
	@Override
	public void colonyWasCaptured(ColonyWasCapturedEvent event){
		// Do nothing.		
	}


	@Override
	public void gameFinished(final GameFinishedEvent event) {
		// Do nothing.
	}

	@Override
	public void debugRequested(final DebugRequestedEvent event) {
		// Do nothing.
	}
}
