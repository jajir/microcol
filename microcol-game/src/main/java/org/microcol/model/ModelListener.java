package org.microcol.model;

import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedEvent;
import org.microcol.model.event.UnitStoredEvent;

public interface ModelListener {
	void gameStarted(GameStartedEvent event);

	void roundStarted(RoundStartedEvent event);

	void turnStarted(TurnStartedEvent event);

	void unitMoved(UnitMovedEvent event);

	void unitAttacked(UnitAttackedEvent event);

	void unitStored(UnitStoredEvent event);
	
	void colonyWasCaptured(ColonyWasCapturedEvent event);

	void gameFinished(GameFinishedEvent event);

	void debugRequested(DebugRequestedEvent event);

	void goldWasChanged(GoldWasChangedEvent event);
}
