package org.microcol.model;

import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.DebugRequestedEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;

/**
 * Evens called from model to registered listeners.
 */
public interface ModelListener {

    void onGameStarted(GameStartedEvent event);

    void onRoundStarted(RoundStartedEvent event);

    void onTurnStarted(TurnStartedEvent event);

    void onUnitMovedStepStarted(UnitMovedStepStartedEvent event);

    void onUnitMovedStepFinished(UnitMovedStepFinishedEvent event);

    void onUnitMovedToHighSeas(UnitMovedToHighSeasEvent event);

    void onUnitMoveStarted(UnitMoveStartedEvent event);

    /**
     * It's called move is finished. No other move step event could come.
     *
     * @param event
     *            required event
     */
    void onUnitMoveFinished(UnitMoveFinishedEvent event);

    void onBeforeEndTurn(BeforeEndTurnEvent event);

    void onUnitAttacked(UnitAttackedEvent event);

    void onUnitEmbarked(UnitEmbarkedEvent event);

    void onColonyWasFounded(ColonyWasFoundEvent event);

    void onColonyWasCaptured(ColonyWasCapturedEvent event);

    void onGameFinished(GameFinishedEvent event);

    void onDebugRequested(DebugRequestedEvent event);

    void onGoldWasChanged(GoldWasChangedEvent event);

    void onGoodsWasSoldInEurope(GoodsWasSoldInEuropeEvent event);

    void onBeforeDeclaringIndependence(BeforeDeclaringIndependenceEvent event);

    void onIndependenceWasDeclared(IndependenceWasDeclaredEvent event);
}
