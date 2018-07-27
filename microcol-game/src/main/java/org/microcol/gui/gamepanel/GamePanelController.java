package org.microcol.gui.gamepanel;

import org.microcol.gui.util.Listener;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * 
 */
@Listener
public final class GamePanelController {

    private boolean humanIsOnTurn;

    private boolean unitIsMoving;

    @Inject
    GamePanelController() {
    }

    @Subscribe
    private void onColonyWasCaptured(
            @SuppressWarnings("unused") final ColonyWasCapturedEvent event) {
        unitIsMoving = false;
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        humanIsOnTurn = event.getPlayer().isHuman();
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
        return humanIsOnTurn && !unitIsMoving;
    }

}
