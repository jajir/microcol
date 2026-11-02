package org.akropia.model;

import org.akropia.model.event.ActionEndedEvent;
import org.akropia.model.event.ActionStartedEvent;
import org.akropia.model.event.ActionWasStartedEvent;
import org.akropia.model.event.BeforeDeclaringIndependenceEvent;
import org.akropia.model.event.BeforeEndTurnEvent;
import org.akropia.model.event.ColonyWasCapturedEvent;
import org.akropia.model.event.ColonyWasFoundEvent;
import org.akropia.model.event.GameFinishedEvent;
import org.akropia.model.event.GameStartedEvent;
import org.akropia.model.event.GameStoppedEvent;
import org.akropia.model.event.GoldWasChangedEvent;
import org.akropia.model.event.GoodsWasSoldInEuropeEvent;
import org.akropia.model.event.IndependenceWasDeclaredEvent;
import org.akropia.model.event.NewUnitWasBornEvent;
import org.akropia.model.event.RoundStartedEvent;
import org.akropia.model.event.TurnFinishedEvent;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.event.UnitArrivedToColoniesEvent;
import org.akropia.model.event.UnitAttackedEvent;
import org.akropia.model.event.UnitEmbarkedEvent;
import org.akropia.model.event.UnitMoveFinishedEvent;
import org.akropia.model.event.UnitMoveStartedEvent;
import org.akropia.model.event.UnitMovedStepFinishedEvent;
import org.akropia.model.event.UnitMovedStepStartedEvent;
import org.akropia.model.event.UnitMovedToColonyFieldEvent;
import org.akropia.model.event.UnitMovedToConstructionEvent;
import org.akropia.model.event.UnitMovedToHighSeasEvent;
import org.akropia.model.event.UnitMovedToLocationEvent;

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
    public void onUnitMovedStepFinished(
            final UnitMovedStepFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToConstruction(
            final UnitMovedToConstructionEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToLocation(UnitMovedToLocationEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitMovedToColonyField(
            final UnitMovedToColonyFieldEvent event) {
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
    public void onIndependenceWasDeclared(
            final IndependenceWasDeclaredEvent event) {
        // Do nothing.
    }

    @Override
    public void onBeforeDeclaringIndependence(
            final BeforeDeclaringIndependenceEvent event) {
        // Do nothing.
    }

    @Override
    public void onTurnFinished(final TurnFinishedEvent event) {
        // Do nothing.
    }

    @Override
    public void onUnitArraviedToColonies(
            final UnitArrivedToColoniesEvent event) {
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
