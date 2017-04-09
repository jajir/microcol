package org.microcol.gui.event;

/**
 * Contains new animation speed value.
 *
 */
public class AnimationSpeedChangeEvent {

	private final int animationSpeed;

	public AnimationSpeedChangeEvent(final int animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public int getAnimationSpeed() {
		return animationSpeed;
	}

}
