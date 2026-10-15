package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 *
 */
@Listener
public final class GamePanelController {

    private final GameModelController gameModelController;

    private volatile boolean unitIsMoving;

    @Inject
    GamePanelController(final GameModelController gameModelController) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
    }

    @Subscribe
    private void onColonyWasCaptured(
            @SuppressWarnings("unused") final ColonyWasCapturedEvent event) {
        unitIsMoving = false;
    }

    @Subscribe
    private void onUnitMovedStepStarted(
            @SuppressWarnings("unused") final UnitMovedStepStartedEvent event) {
        unitIsMoving = true;
    }

    @Subscribe
    private void onUnitMovedFinished(
            @SuppressWarnings("unused") final UnitMoveFinishedEvent event) {
        unitIsMoving = false;
    }

    /**
     * If Game panel is enabled. For example when animation is in progress than
     * mouse should be disabled.
     *
     * @return the mouseEnabled
     */
    public boolean isMouseEnabled() {
        // Turn notifications can arrive out of order.
        return gameModelController.isGameModelReady()
                && gameModelController.getModel().isGameRunning()
                && gameModelController.getRealCurrentPlayer().isHuman();
    }

    public boolean isUnitMoving() {
        return unitIsMoving;
    }

}
