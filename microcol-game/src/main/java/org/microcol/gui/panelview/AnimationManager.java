package org.microcol.gui.panelview;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Hold and manage planned animation steps.
 * 
 */
public class AnimationManager {

	private final Queue<AnimationPart> animationParts = new LinkedList<>();

	private Optional<AnimationPart> runningPart;

	private boolean hasNextStep = false;

	public AnimationManager() {
		runningPart = Optional.empty();
	}

	public boolean hasNextStep() {
		return hasNextStep;
	}

	public void performStep() {
		Preconditions.checkArgument(hasNextStep, "Can't perform step when there is no next step.");
		Preconditions.checkArgument(runningPart.isPresent(), "Actually running animation was lost.");
		runningPart.get().nextStep();
		if (runningPart.get().hasNextStep()) {
			hasNextStep = true;
		} else {
			if (animationParts.isEmpty()) {
				runningPart = Optional.empty();
				hasNextStep = false;
			} else {
				runningPart = Optional.of(animationParts.remove());
				Preconditions.checkArgument(runningPart.get().hasNextStep(),
						"Just started animation should have at least one step.");
			}
		}
	}

	public void paint(final GraphicsContext graphics, final Area area) {
		Preconditions.checkArgument(hasNextStep, "Can't perform step when there is no next step.");
		runningPart.get().paint(graphics, area);
	}

	public void addAnimationPart(final AnimationPart animationPart) {
		if (runningPart.isPresent()) {
			animationParts.add(Preconditions.checkNotNull(animationPart));
		} else {
			runningPart = Optional.of(animationPart);
			hasNextStep = animationPart.hasNextStep();
		}
	}

}
