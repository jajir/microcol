package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.WasdController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Hold and manage planned animation steps.
 */
public final class ScrollingManager {

    private final Logger logger = LoggerFactory.getLogger(ScrollingManager.class);

    private final WasdController wasdController;

    private AnimatonScreenScroll runningAnimation;

    private final VisibleArea visibleArea;
    
    private final EventBus eventBus;

    private final AnimationLatch latch = new AnimationLatch();

    @Inject
    public ScrollingManager(final EventBus eventBus,
            final WasdController wasdController, final VisibleArea visibleArea) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.wasdController = Preconditions.checkNotNull(wasdController);
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
        runningAnimation = null;
    }

    public void paint() {
        if (runningAnimation == null) {
            if (wasdController.isScrolling()) {
                visibleArea.addDeltaToTopLeftPoint(wasdController.getDiff());
            }
        } else {
            runningAnimation.paint(visibleArea);
            if (!runningAnimation.hasNextStep()) {
                latch.unlock();
                runningAnimation = null;
            }
        }
    }

    public void addAnimation(final AnimatonScreenScroll animation) {
        logger.debug("Adding animation {}", animation);
        eventBus.post(new AnimationStartedEvent());
        latch.lock();
        runningAnimation = animation;
    }

}
