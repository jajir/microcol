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
import org.microcol.model.event.UnitMovedStepEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;

public class ModelListenerAdapter implements ModelListener {

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
    public void unitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        // Do nothing.
    }

    @Override
    public void unitMoveFinished(final UnitMoveFinishedEvent event) {
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
    public void goldWasChanged(final GoldWasChangedEvent event) {
        // Do nothing.
    }

    @Override
    public void goodsWasSoldInEurope(GoodsWasSoldInEuropeEvent event) {
        // Do nothing.
    }

    @Override
    public void colonyWasFounded(ColonyWasFoundEvent event) {
        // Do nothing.
    }

    @Override
    public void colonyWasCaptured(final ColonyWasCapturedEvent event) {
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

    @Override
    public void unitMoveStarted(final UnitMoveStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void beforeEndTurn(final BeforeEndTurnEvent event) {
        // Do nothing.
    }

    @Override
    public void independenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        // Do nothing.
    }

    @Override
    public void beforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        // Do nothing.
    }
}
