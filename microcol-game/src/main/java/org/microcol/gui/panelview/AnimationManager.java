package org.microcol.gui.panelview;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Hold and manage planned animation steps.
 * 
 */
public class AnimationManager {

	private final Queue<AnimationHolder> animationParts = new LinkedList<>();

	private Optional<AnimationHolder> runningPart;

	private boolean hasNextStep = false;

	public AnimationManager() {
		runningPart = Optional.empty();
	}

	public boolean hasNextStep() {
		return hasNextStep;
	}

	public void performStep() {
		Preconditions.checkState(hasNextStep, "Can't perform step when there is no next step.");
		Preconditions.checkState(runningPart.isPresent(), "Actually running animation was lost.");
		runningPart.get().animation.nextStep();
		if (runningPart.get().animation.hasNextStep()) {
			hasNextStep = true;
		} else {
			if (runningPart.get().onAnimationIsDone != null) {
				runningPart.get().onAnimationIsDone.accept(runningPart.get().animation);
			}
			if (animationParts.isEmpty()) {
				runningPart = Optional.empty();
				hasNextStep = false;
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

		if (runningPart.isPresent()) {
			animationParts.add(holder);
		} else {
			runningPart = Optional.of(holder);
			hasNextStep = holder.animation.hasNextStep();
		}

	}

	public void addAnimation(final Animation animation) {
		addAnimation(animation, null);
	}

	private class AnimationHolder {

		private final Animation animation;

		private final Consumer<Animation> onAnimationIsDone;

		AnimationHolder(final Animation animation, final Consumer<Animation> onAnimationIsDone) {
			this.animation = animation;
			this.onAnimationIsDone = onAnimationIsDone;
		}

	}

}
