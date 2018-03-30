package org.microcol.model;

import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.event.UnitMovedStepEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;

/**
 * Evens called from model to registered listeners.
 */
public interface ModelListener {

    void gameStarted(GameStartedEvent event);

    void roundStarted(RoundStartedEvent event);

    void turnStarted(TurnStartedEvent event);

    void unitMovedStep(UnitMovedStepEvent event);

    void unitMovedToHighSeas(UnitMovedToHighSeasEvent event);

    void unitMoveStarted(UnitMoveStartedEvent event);

    /**
     * It's called move is finished. No other move step event could come.
     *
     * @param event
     *            required event
     */
    void unitMoveFinished(UnitMoveFinishedEvent event);

    void beforeEndTurn(BeforeEndTurnEvent event);

    void unitAttacked(UnitAttackedEvent event);

    void unitEmbarked(UnitEmbarkedEvent event);

    void colonyWasFounded(ColonyWasFoundEvent event);

    void colonyWasCaptured(ColonyWasCapturedEvent event);

    void gameFinished(GameFinishedEvent event);

    void debugRequested(DebugRequestedEvent event);

    void goldWasChanged(GoldWasChangedEvent event);

    void goodsWasSoldInEurope(GoodsWasSoldInEuropeEvent event);
}
