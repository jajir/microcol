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
import org.akropia.model.event.ImmigrationChoiceRequiredEvent;
import org.akropia.model.event.NewUnitWasBornEvent;
import org.akropia.model.event.ScoreBoundaryRequiredEvent;
import org.akropia.model.event.RoundStartedEvent;
import org.akropia.model.event.MercenaryOfferRequiredEvent;
import org.akropia.model.event.TaxDemandRequiredEvent;
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

/**
 * Evens called from model to registered listeners.
 */
public interface ModelListener {

    /** A free immigrant needs a choice before this owner's turn can begin. */
    default void onImmigrationChoiceRequired(
            final ImmigrationChoiceRequiredEvent event) {
    }

    /**
     * Spring 1800 froze the score of the human nation; the game waits for the
     * unscored continuation (R-0193).
     */
    default void onScoreBoundaryRequired(
            final ScoreBoundaryRequiredEvent event) {
    }

    /** A Crown tax demand needs an answer before this owner's turn begins. */
    default void onTaxDemandRequired(final TaxDemandRequiredEvent event) {
    }

    /** The human owner has a mercenary offer to answer (R-0169). */
    default void onMercenaryOfferRequired(
            final MercenaryOfferRequiredEvent event) {
    }

    void onGameStarted(GameStartedEvent event);

    void onRoundStarted(RoundStartedEvent event);

    /**
     * When player start turn this event is fired. It could notify UI that human
     * player is on turn.
     *
     * @param event required event
     */
    void onTurnStarted(TurnStartedEvent event);

    /**
     * When player finish turn this event is fired.
     * <p>
     * Turn is finished by calling model.endTurn().
     * </p>
     *
     * @param event required event
     */
    void onTurnFinished(TurnFinishedEvent event);

    void onUnitMovedStepStarted(UnitMovedStepStartedEvent event);

    void onUnitMovedStepFinished(UnitMovedStepFinishedEvent event);

    /**
     * It's called when unit move to high seas.
     *
     * @param event required event
     */
    void onUnitMovedToHighSeas(UnitMovedToHighSeasEvent event);

    /**
     * It's called when ship arrive to colonies from Europe.
     *
     * @param event required event object
     */
    void onUnitArraviedToColonies(UnitArrivedToColoniesEvent event);

    /**
     * It's called when unit move to colony field.
     *
     * @param event required event
     */
    void onUnitMovedToColonyField(UnitMovedToColonyFieldEvent event);

    /**
     * It's called when unit move to construction slot in colony.
     *
     * @param event required event
     */
    void onUnitMovedToConstruction(UnitMovedToConstructionEvent event);

    /**
     * It's called when unit move to map location.
     *
     * @param event required event
     */
    void onUnitMovedToLocation(UnitMovedToLocationEvent event);

    void onUnitMoveStarted(UnitMoveStartedEvent event);

    /**
     * It's called when some action started. Action is some action that should
     * be treated as one piece. For example move should be treated as one action
     * even when it's composed from single one step moves.
     *
     * @param event required event
     */
    void onActionStarted(ActionStartedEvent event);

    /**
     * It's called when some action ended.
     *
     * @param event required event
     */
    void onActionEnded(ActionEndedEvent event);

    /**
     * It's called move is finished. No other move step event could come.
     *
     * @param event required event
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
     * @param event required event object
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
     * @param event required event
     */
    void onNewUnitWasBorn(NewUnitWasBornEvent event);

    /**
     * Event is raised when unit starts same action like plow field.
     *
     * @param event required event
     */
    void onActionWasStarted(ActionWasStartedEvent event);
}
