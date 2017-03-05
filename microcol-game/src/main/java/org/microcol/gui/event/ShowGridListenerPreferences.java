package org.microcol.gui.event;

import org.microcol.gui.GamePreferences;

import com.google.inject.Inject;

/**
 * Class just pass changed showing of grid to preferences.
 */
public class ShowGridListenerPreferences {

	@Inject
	public ShowGridListenerPreferences(final ShowGridController showGridController,
			final GamePreferences gamePreferences) {
		showGridController.addShowGridListener(e -> gamePreferences.setShowGrid(e.isGridShown()));
	}

}
