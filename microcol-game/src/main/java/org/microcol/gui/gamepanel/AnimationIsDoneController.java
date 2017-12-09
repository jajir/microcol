package org.microcol.gui.gamepanel;

import org.microcol.gui.util.AbstractEventController;

/**
 * Control event when all animations are done.
 */
public class AnimationIsDoneController extends AbstractEventController<AnimationIsDoneEvent> {

	/**
	 * Make listeners synchronous by default.
	 */
	AnimationIsDoneController() {
		super(false);
	}

}
