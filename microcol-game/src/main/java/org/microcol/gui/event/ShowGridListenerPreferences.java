package org.microcol.gui.event;

import org.microcol.gui.GamePreferences;
import org.microcol.gui.mainmenu.ShowGridController;

import com.google.inject.Inject;

/**
 * Class just pass changed showing of grid to preferences.
 */
public class ShowGridListenerPreferences {

	@Inject
	public ShowGridListenerPreferences(final ShowGridController showGridController,
			final GamePreferences gamePreferences) {
		showGridController.addListener(e -> gamePreferences.setShowGrid(e.isGridShown()));
	}

}
