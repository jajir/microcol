package org.microcol.gui.event;

import org.microcol.gui.GamePreferences;
import org.microcol.gui.mainmenu.AnimationSpeedChangeController;

import com.google.inject.Inject;

/**
 * Class just pass changed animation speed to preferences.
 */
public class AnimationSpeedChangedListenerPreferences {

	@Inject
	public AnimationSpeedChangedListenerPreferences(final AnimationSpeedChangeController controller,
			final GamePreferences gamePreferences) {
		controller.addListener(e -> gamePreferences.setAnimationSpeed(e.getAnimationSpeed()));
	}

}
