package org.microcol.model;

import org.microcol.model.event.ActionEndedEvent;
import org.microcol.model.event.ActionStartedEvent;
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

/**
 * Evens called from model to registered listeners.
 */
public interface ModelListener {

    void onGameStarted(GameStartedEvent event);

    void onRoundStarted(RoundStartedEvent event);

    /**
     * When player start turn this event is fired. It could notify UI that human
     * player is on turn.
     * 
     * @param event
     *            required event
     */
    void onTurnStarted(TurnStartedEvent event);

    /**
     * When player finish turn this event is fired.
     * <p>
     * Turn is finished by calling model.endTurn().
     * </p>
     *
     * @param event
     *            required event
     */
    void onTurnFinished(TurnFinishedEvent event);

    void onUnitMovedStepStarted(UnitMovedStepStartedEvent event);

    void onUnitMovedStepFinished(UnitMovedStepFinishedEvent event);

    /**
     * It's called when unit move to high seas.
     *
     * @param event
     *            required event
     */
    void onUnitMovedToHighSeas(UnitMovedToHighSeasEvent event);

    /**
     * It's called when ship arrive to colonies from Europe.
     * 
     * @param event
     *            required event object
     */
    void onUnitArraviedToColonies(UnitArrivedToColoniesEvent event);

    /**
     * It's called when unit move to colony field.
     *
     * @param event
     *            required event
     */
    void onUnitMovedToColonyField(UnitMovedToColonyFieldEvent event);

    /**
     * It's called when unit move to construction slot in colony.
     *
     * @param event
     *            required event
     */
    void onUnitMovedToConstruction(UnitMovedToConstructionEvent event);

    /**
     * It's called when unit move to map location.
     *
     * @param event
     *            required event
     */
    void onUnitMovedToLocation(UnitMovedToLocationEvent event);

    void onUnitMoveStarted(UnitMoveStartedEvent event);

    /**
     * It's called when some action started. Action is some action that should
     * be treated as one piece. For example move should be treated as one action
     * even when it's composed from single one step moves.
     *
     * @param event
     *            required event
     */
    void onActionStarted(ActionStartedEvent event);

    /**
     * It's called when some action ended.
     *
     * @param event
     *            required event
     */
    void onActionEnded(ActionEndedEvent event);

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

    /**
     * Event is raised when game is stopped. This is last event send by model.
     *
     * @param event
     *            required event object
     */
    void onGameStopped(GameStoppedEvent event);

    void onGameFinished(GameFinishedEvent event);

    void onGoldWasChanged(GoldWasChangedEvent event);

    void onGoodsWasSoldInEurope(GoodsWasSoldInEuropeEvent event);

    void onBeforeDeclaringIndependence(BeforeDeclaringIndependenceEvent event);

    void onIndependenceWasDeclared(IndependenceWasDeclaredEvent event);

    /**
     * Event is raised when new unit is born in colony.
     *
     * @param event
     *            required event
     */
    void onNewUnitWasBorn(NewUnitWasBornEvent event);
}
