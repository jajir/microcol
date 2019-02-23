package org.microcol.gui.screen.game.gamepanel;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;

/**
 * Hold and manage planned animation steps.
 */
public final class AnimationManager implements AnimationLock {

    private final Logger logger = LoggerFactory.getLogger(AnimationManager.class);

    private final EventBus eventBus;

    private AnimationHolder runningPart;

    private final AnimationLatch latch = new AnimationLatch();

    @Inject
    public AnimationManager(final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        runningPart = null;
    }

    public boolean hasNextStep(final Area area) {
        while (runningPart != null && runningPart.getAnimation().hasNextStep()
                && !runningPart.getAnimation().canBePainted(area)) {
            performStep();
        }
        return runningPart != null;
    }

    private void performStep() {
        Preconditions.checkState(runningPart != null, "Actually running animation was lost.");
        runningPart.getAnimation().nextStep();
        if (!runningPart.getAnimation().hasNextStep()) {
            runningPart.runOnAnimationIsDone();
            runningPart = null;
            eventBus.post(new AnimationIsDoneEvent());
            logger.debug("You are done, unlocking threads");
            latch.unlock();
        }
    }

    public void paint(final GraphicsContext graphics, final Area area) {
        Preconditions.checkState(runningPart != null, "Actually running animation was lost.");
        runningPart.getAnimation().paint(graphics, area);
        performStep();
    }

    /**
     * Schedule animation to be shown. Also allow to lock calling thread with
     * method {@link #waitWhileRunning()}.
     * 
     * @param animation
     *            required animation object
     * @param onAnimationIsDone
     *            optional operation that will be executed when animation is
     *            none
     */
    void addAnimation(final Animation animation, final Consumer<Animation> onAnimationIsDone) {
        Preconditions.checkNotNull(animation);
        Preconditions.checkState(runningPart == null, "There is still runnign animation '%s'",
                runningPart);
        //TODO skip event here all invisible moves
        runningPart = new AnimationHolder(animation, onAnimationIsDone);
        logger.debug("Adding animation {}", animation);
        eventBus.post(new AnimationStartedEvent());
        Preconditions.checkState(runningPart.getAnimation().hasNextStep(),
                "Animation should contain at least one step.");
        latch.lock();
    }

    @Override
    public void waitWhileRunning() {
        latch.waitForUnlock();
    }

    public void addAnimation(final Animation animation) {
        addAnimation(animation, null);
    }

}
