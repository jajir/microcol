package org.microcol.gui.event.model;

import java.util.Optional;

import org.microcol.model.ModelListener;
import org.microcol.model.Player;
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
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Translate model events to GUI events.
 */
public final class ModelListenerImpl implements ModelListener {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    public ModelListenerImpl(final GameModelController gameModelController,
            final EventBus eventBus) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("gameModelController", gameModelController).toString();
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMovedStepFinished(final UnitMovedStepFinishedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getHumanPlayer())) {
            eventBus.post(event);
        }
    }

    @Override
    public void onUnitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        if (event.getUnit().getOwner().equals(gameModelController.getHumanPlayer())) {
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
            onGoldWasChanged(new GoldWasChangedEvent(event.getModel(), human.get(),
                    human.get().getGold(), human.get().getGold()));
        }
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitAttacked(final UnitAttackedEvent event) {
        eventBus.post(event);
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
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        if (event.getWhoDecalareIt().isHuman()) {
            eventBus.post(event);
        }
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onActionStarted(final ActionStartedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onActionEnded(final ActionEndedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onGameStopped(final GameStoppedEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedToConstruction(final UnitMovedToConstructionEvent event) {
        eventBus.post(event);
    }

    @Override
    public void onUnitMovedToColonyField(final UnitMovedToColonyFieldEvent event) {
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

}
