package org.microcol.gui.gamepanel;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;

/**
 * Hold and manage planned animation steps.
 * 
 */
public class AnimationManager implements AnimationLock {

	private final Logger logger = LoggerFactory.getLogger(AnimationManager.class);

	private final Queue<AnimationHolder> animationParts = new LinkedList<>();
	
	private final AnimationIsDoneController animationIsDoneController;

	private Optional<AnimationHolder> runningPart;

	private boolean hasNextStep = false;

	private final AnimationLatch latch = new AnimationLatch();

	@Inject
	public AnimationManager(final AnimationIsDoneController animationIsDoneController) {
		this.animationIsDoneController = Preconditions.checkNotNull(animationIsDoneController);
		runningPart = Optional.empty();
	}

	public boolean hasNextStep() {
		return hasNextStep;
	}

	public void performStep() {
		Preconditions.checkState(hasNextStep, "Can't perform step when there is no next step.");
		Preconditions.checkState(runningPart.isPresent(), "Actually running animation was lost.");
		runningPart.get().animation.nextStep();
		if (!runningPart.get().animation.hasNextStep()) {
			if (runningPart.get().onAnimationIsDone != null) {
				runningPart.get().onAnimationIsDone.accept(runningPart.get().animation);
			}
			if (animationParts.isEmpty()) {
				runningPart = Optional.empty();
				hasNextStep = false;
				latch.unlock();
				animationIsDoneController.fireEvent(new AnimationIsDoneEvent());
				logger.debug("You are done, unlocking threads");
			} else {
				runningPart = Optional.of(animationParts.remove());
				Preconditions.checkState(runningPart.get().animation.hasNextStep(),
						"Just started animation should have at least one step.");
			}
		}
	}

	public void paint(final GraphicsContext graphics, final Area area) {
		Preconditions.checkArgument(hasNextStep, "Can't perform step when there is no next step.");
		runningPart.get().animation.paint(graphics, area);
	}

	public void addAnimation(final Animation animation, Consumer<Animation> onAnimationIsDone) {
		Preconditions.checkNotNull(animation);
		final AnimationHolder holder = new AnimationHolder(animation, onAnimationIsDone);
		logger.debug("Adding animation {}", animation);
		if (runningPart.isPresent()) {
			animationParts.add(holder);
		} else {
			runningPart = Optional.of(holder);
			hasNextStep = holder.animation.hasNextStep();
			//TODO Calling thread should be blocked until animations is done.
			if (hasNextStep) {
				latch.lock();
			}
		}
	}

	@Override
	public void waitWhileRunning() {
		latch.waitForUnlock();
	}

	public void addAnimation(final Animation animation) {
		addAnimation(animation, null);
	}

	private static class AnimationHolder {

		private final Animation animation;

		private final Consumer<Animation> onAnimationIsDone;

		AnimationHolder(final Animation animation, final Consumer<Animation> onAnimationIsDone) {
			this.animation = animation;
			this.onAnimationIsDone = onAnimationIsDone;
		}

	}

}
