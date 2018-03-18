package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.ColonyWasCapturedController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.event.model.UnitMoveFinishedController;
import org.microcol.gui.event.model.UnitMovedController;
import org.microcol.model.event.ColonyWasCapturedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMovedStepEvent;

import com.google.inject.Inject;

/**
 * 
 */
public class GamePanelController {

    private boolean humanIsOnTurn;

    private boolean unitIsMoving;

    @Inject
    GamePanelController(final TurnStartedController turnStartedController,
            final UnitMovedController unitMovedStepController,
            final UnitMoveFinishedController unitMoveFinishedController,
            final ColonyWasCapturedController colonyWasCapturedController) {
        turnStartedController.addListener(this::onTurnStarted);
        unitMovedStepController.addListener(this::onUnitMovedStep);
        unitMoveFinishedController.addListener(this::onUnitMovedFinished);
        colonyWasCapturedController.addListener(this::onColonyWasCaptured);
    }

    @SuppressWarnings("unused")
    private void onColonyWasCaptured(final ColonyWasCapturedEvent event) {
        unitIsMoving = false;
    }

    private void onTurnStarted(final TurnStartedEvent event) {
        humanIsOnTurn = event.getPlayer().isHuman();
    }

    @SuppressWarnings("unused")
    private void onUnitMovedStep(final UnitMovedStepEvent event) {
        unitIsMoving = true;
    }

    @SuppressWarnings("unused")
    private void onUnitMovedFinished(final UnitMoveFinishedEvent event) {
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
