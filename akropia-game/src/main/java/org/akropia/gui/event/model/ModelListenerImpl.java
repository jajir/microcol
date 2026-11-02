package org.akropia.gui.event.model;

import java.util.Optional;

import org.akropia.gui.event.NativeRelationsChangedEvent;
import org.akropia.model.ModelListener;
import org.akropia.model.Player;
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
import org.akropia.model.event.MercenaryOfferRequiredEvent;
import org.akropia.model.event.TaxDemandRequiredEvent;
import org.akropia.model.event.TurnFinishedEvent;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.event.ImmigrationChoiceRequiredEvent;
import org.akropia.model.event.ScoreBoundaryRequiredEvent;
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Translate model events to GUI events.
 */
final class ModelListenerImpl implements ModelListener {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    ModelListenerImpl(final GameModelController gameModelController,
            final EventBus eventBus) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("gameModelController", gameModelController).toString();
    }

    @Override
    public void onImmigrationChoiceRequired(
            final ImmigrationChoiceRequiredEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onScoreBoundaryRequired(
            final ScoreBoundaryRequiredEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onTaxDemandRequired(final TaxDemandRequiredEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onMercenaryOfferRequired(
            final MercenaryOfferRequiredEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        eventBus.post(event);
        // A turn update may have moved native alarm and attitude.
        eventBus.post(new NativeRelationsChangedEvent());
    }

    @Override
    public void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMovedStepFinished(
            final UnitMovedStepFinishedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        if (event.getUnit().getOwner()
                .equals(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onRoundStarted(final RoundStartedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        eventBus.post(event);
        final Optional<Player> human = event.getModel().getPlayers().stream()
                .filter(player -> player.isHuman()).findAny();
        if (human.isPresent()) {
            onGoldWasChanged(new GoldWasChangedEvent(event.getModel(),
                    human.get(), human.get().getGold(), human.get().getGold()));
        }
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitAttacked(final UnitAttackedEvent event) {
        if (event.canPlayerSeeAttack(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
        // An attack changes native relations even when its view is hidden.
        eventBus.post(new NativeRelationsChangedEvent());
    }

    @Override
    public void onGoldWasChanged(final GoldWasChangedEvent event) {
        if (event.getPlayer().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        if (event.getCapturedColony().getOwner().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitEmbarked(final UnitEmbarkedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (event.getColony().getOwner().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onIndependenceWasDeclared(
            final IndependenceWasDeclaredEvent event) {
        if (event.getWhoDecalareIt().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onBeforeDeclaringIndependence(
            final BeforeDeclaringIndependenceEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onActionStarted(final ActionStartedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onActionEnded(final ActionEndedEvent event) {
        eventBus.post(event);
        // A committed action such as a trade or gift may change relations.
        eventBus.post(new NativeRelationsChangedEvent());
    }

    @Override
    public void onGameStopped(final GameStoppedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedToConstruction(
            final UnitMovedToConstructionEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedToColonyField(
            final UnitMovedToColonyFieldEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedToLocation(final UnitMovedToLocationEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onTurnFinished(final TurnFinishedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitArraviedToColonies(
            final UnitArrivedToColoniesEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onActionWasStarted(final ActionWasStartedEvent event) {
        eventBus.post(event);
    }

}
