package org.microcol.gui.event.model;

import java.util.Optional;

import org.microcol.model.ModelListener;
import org.microcol.model.Player;
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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Translate model events to GUI events.
 */
public final class ModelListenerImpl implements ModelListener {

    private final ModelEventManager modelEventManager;

    private final GameModelController gameModelController;

    public ModelListenerImpl(final ModelEventManager modelEventManager,
            final GameModelController gameModelController) {
        this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("modelEventManager", modelEventManager)
                .add("gameModelController", gameModelController).toString();
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        modelEventManager.getTurnStartedController().fireEvent(event);
    }

    @Override
    public void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getCurrentPlayer())) {
            modelEventManager.getUnitMovedStepStartedController().fireEvent(event);
        }
    }

    @Override
    public void onUnitMovedStepFinished(final UnitMovedStepFinishedEvent event) {
        if (event.canPlayerSeeMove(gameModelController.getCurrentPlayer())) {
            modelEventManager.getUnitMovedStepFinishedController().fireEvent(event);
        }
    }

    @Override
    public void onUnitMovedToHighSeas(final UnitMovedToHighSeasEvent event) {
        if (event.getUnit().getOwner().equals(gameModelController.getCurrentPlayer())) {
            modelEventManager.getUnitMovedToHighSeasController().fireEvent(event);
        }
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        modelEventManager.getUnitMoveFinishedController().fireEvent(event);
    }

    @Override
    public void onRoundStarted(final RoundStartedEvent event) {
        modelEventManager.getRoundStartedController().fireEvent(event);
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        modelEventManager.getGameStartedController().fireEvent(event);
        final Optional<Player> human = event.getModel().getPlayers().stream()
                .filter(player -> player.isHuman()).findAny();
        if (human.isPresent()) {
            onGoldWasChanged(new GoldWasChangedEvent(event.getModel(), human.get(),
                    human.get().getGold(), human.get().getGold()));
        }
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        modelEventManager.getGameFinishedController().fireEvent(event);
    }

    @Override
    public void onDebugRequested(final DebugRequestedEvent event) {
        modelEventManager.getDebugRequestController().fireEvent(event);
    }

    @Override
    public void onUnitAttacked(final UnitAttackedEvent event) {
        modelEventManager.getUnitAttackedEventController().fireEvent(event);
    }

    @Override
    public void onGoldWasChanged(final GoldWasChangedEvent event) {
        if (event.getPlayer().isHuman()) {
            modelEventManager.getGoldWasChangedController().fireEvent(event);
        }
    }

    @Override
    public void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        if (event.getCapturedColony().getOwner().isHuman()) {
            modelEventManager.getColonyWasCapturedController().fireEvent(event);
        }
    }

    @Override
    public void onUnitEmbarked(final UnitEmbarkedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            modelEventManager.getUnitEmbarkController().fireEvent(event);
        }
    }

    @Override
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        if (event.getColony().getOwner().isHuman()) {
            modelEventManager.getColonyWasFoundController().fireEvent(event);
        }
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        // Front-end doesn't care about this event.
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        if (event.getWhoDecalareIt().isHuman()) {
            modelEventManager.getIndependenceWasDeclaredColntroller().fireEvent(event);
        }
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        // Front-end doesn't care about this event.
    }

}
