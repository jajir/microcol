package org.microcol.model;

import org.microcol.model.event.ActionEndedEvent;
import org.microcol.model.event.ActionStartedEvent;
import org.microcol.model.event.ActionWasStartedEvent;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GameStoppedEvent;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.NewUnitWasBornEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitArrivedToColoniesEvent;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;
import org.microcol.model.event.UnitMovedToHighSeasEvent;
import org.microcol.model.event.UnitMovedToLocationEvent;

public class ModelListenerAdapter implements ModelListener {

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onGameStopped(final GameStoppedEvent event) {
        // Do nothing.
    }

    @Override
    public void onRoundStarted(final RoundStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedStepFinished(final UnitMovedStepFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToConstruction(final UnitMovedToConstructionEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToLocation(UnitMovedToLocationEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToColonyField(final UnitMovedToColonyFieldEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        // Do nothing.
    }

    @Override
    public void onActionStarted(final ActionStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onActionEnded(final ActionEndedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitAttacked(final UnitAttackedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitEmbarked(final UnitEmbarkedEvent event) {
        // Do nothing.
    }

    @Override
    public void onGoldWasChanged(final GoldWasChangedEvent event) {
        // Do nothing.
    }

    @Override
    public void onGoodsWasSoldInEurope(GoodsWasSoldInEuropeEvent event) {
        // Do nothing.
    }

    @Override
    public void onColonyWasFounded(ColonyWasFoundEvent event) {
        // Do nothing.
    }

    @Override
    public void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        // Do nothing.
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        // Do nothing.
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        // Do nothing.
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        // Do nothing.
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        // Do nothing.
    }

    @Override
    public void onTurnFinished(final TurnFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitArraviedToColonies(final UnitArrivedToColoniesEvent event) {
        // Do nothing.
    }

    @Override
    public void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
        // Do nothing.
    }

    @Override
    public void onActionWasStarted(final ActionWasStartedEvent event) {
        // Do nothing.
    }

}
