package org.microcol.gui;

import org.microcol.gui.gamepanel.AnimationIsDoneEvent;
import org.microcol.gui.gamepanel.AnimationStartedEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.event.ActionEndedEvent;
import org.microcol.model.event.ActionStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Enable and disable 'Next turn' button based on running animation.
 * 
 * 
 * 
 * FIXME button is moved to buttoon menu
 */
@Listener
public final class RightPanelNextButtonControll {

    private final RightPanelView rightPanelView;

    @Inject
    RightPanelNextButtonControll(final RightPanelView rightPanelView) {
        this.rightPanelView = Preconditions.checkNotNull(rightPanelView);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onActionStarted(final ActionStartedEvent event) {
        rightPanelView.setNextTurnButtonDisable(true);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onActionEnded(final ActionEndedEvent event) {
        rightPanelView.setNextTurnButtonDisable(false);
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onAnimationStarted(final AnimationStartedEvent event) {
        // rightPanelView.setNextTurnButtonDisable(true);
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onAnimationIsDone(final AnimationIsDoneEvent event) {
        // rightPanelView.setNextTurnButtonDisable(false);
    }

}
