package org.microcol.gui;

import org.microcol.gui.event.model.ActionEndedController;
import org.microcol.gui.event.model.ActionStartedController;
import org.microcol.gui.gamepanel.AnimationIsDoneController;
import org.microcol.gui.gamepanel.AnimationIsDoneEvent;
import org.microcol.gui.gamepanel.AnimationStartedController;
import org.microcol.gui.gamepanel.AnimationStartedEvent;
import org.microcol.model.event.ActionEndedEvent;
import org.microcol.model.event.ActionStartedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Enable and disable 'Next turn' button based on running animation.
 *
 * TODO Animation is stored from multiple parts so button blinks.
 * 
 * TODO UnitMovedFinished is not used correctly
 */
public final class RightPanelNextButtonControll {

    private final RightPanelView rightPanelView;

    @Inject
    RightPanelNextButtonControll(final AnimationStartedController animationStartedController,
            final AnimationIsDoneController animationIsDoneController,
            final RightPanelView rightPanelView,
            final ActionStartedController actionStartedController,
            final ActionEndedController actionEndedController) {
        this.rightPanelView = Preconditions.checkNotNull(rightPanelView);
        animationStartedController.addListener(this::onAnimationStarted);
        animationIsDoneController.addListener(this::onAnimationIsDone);
        actionStartedController.addListener(this::onActionStarted);
        actionEndedController.addListener(this::onActionEnded);
    }

    @SuppressWarnings("unused")
    private void onActionStarted(final ActionStartedEvent event) {
        rightPanelView.setNextTurnButtonDisable(true);
    }

    @SuppressWarnings("unused")
    private void onActionEnded(final ActionEndedEvent event) {
        rightPanelView.setNextTurnButtonDisable(false);
    }

    @SuppressWarnings("unused")
    private void onAnimationStarted(final AnimationStartedEvent event) {
        // rightPanelView.setNextTurnButtonDisable(true);
    }

    @SuppressWarnings("unused")
    private void onAnimationIsDone(final AnimationIsDoneEvent event) {
        // rightPanelView.setNextTurnButtonDisable(false);
    }

}
