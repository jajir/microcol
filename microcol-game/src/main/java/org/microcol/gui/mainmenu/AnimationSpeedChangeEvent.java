package org.microcol.gui.mainmenu;

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
