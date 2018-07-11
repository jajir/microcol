package org.microcol.gui.gamepanel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;

/**
 * Hold and manage planned animation steps.
 */
public final class AnimationManager implements AnimationLock {

    private final Logger logger = LoggerFactory.getLogger(AnimationManager.class);

    private final Queue<AnimationHolder> animationsQueue = new LinkedList<>();

    private final AnimationStartedController animationStartedController;
    
    private final AnimationIsDoneController animationIsDoneController;

    private AnimationHolder runningPart;

    private boolean hasNextStep = false;

    private final AnimationLatch latch = new AnimationLatch();

    @Inject
    public AnimationManager(final AnimationStartedController animationStartedController,
            final AnimationIsDoneController animationIsDoneController) {
        this.animationStartedController = Preconditions.checkNotNull(animationStartedController);
        this.animationIsDoneController = Preconditions.checkNotNull(animationIsDoneController);
        runningPart = null;
    }

    public boolean hasNextStep() {
        return hasNextStep;
    }

    public void performStep() {
        Preconditions.checkState(hasNextStep, "Can't perform step when there is no next step.");
        Preconditions.checkState(runningPart != null, "Actually running animation was lost.");
        runningPart.getAnimation().nextStep();
        if (!runningPart.getAnimation().hasNextStep()) {
            runningPart.runOnAnimationIsDone();
            if (animationsQueue.isEmpty()) {
                runningPart = null;
                hasNextStep = false;
                latch.unlock();
                animationIsDoneController.fireEvent(new AnimationIsDoneEvent());
                logger.debug("You are done, unlocking threads");
            } else {
                runningPart = animationsQueue.remove();
                animationStartedController.fireEvent(new AnimationStartedEvent());
                Preconditions.checkState(runningPart.getAnimation().hasNextStep(),
                        "Just started animation should have at least one step.");
            }
        }
    }

    void paint(final GraphicsContext graphics, final Area area) {
        Preconditions.checkArgument(hasNextStep, "Can't perform step when there is no next step.");
        runningPart.getAnimation().paint(graphics, area);
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
    void addAnimation(final Animation animation, Consumer<Animation> onAnimationIsDone) {
        Preconditions.checkNotNull(animation);
        final AnimationHolder holder = new AnimationHolder(animation, onAnimationIsDone);
        logger.debug("Adding animation {}", animation);
        if (runningPart == null) {
            runningPart = holder;
            animationStartedController.fireEvent(new AnimationStartedEvent());
            hasNextStep = holder.getAnimation().hasNextStep();
            Preconditions.checkState(hasNextStep, "Animation should contain at least one step.");
        } else {
            animationsQueue.add(holder);
        }
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
