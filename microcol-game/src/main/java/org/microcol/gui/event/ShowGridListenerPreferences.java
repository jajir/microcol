package org.microcol.gui.event;

import org.microcol.gui.mainmenu.ShowGridController;
import org.microcol.gui.util.GamePreferences;

import com.google.inject.Inject;

/**
 * Class just pass changed showing of grid to preferences.
 */
public final class ShowGridListenerPreferences {

    @Inject
    public ShowGridListenerPreferences(final ShowGridController showGridController,
            final GamePreferences gamePreferences) {
        showGridController.addListener(e -> gamePreferences.setShowGrid(e.isGridShown()));
    }

}
