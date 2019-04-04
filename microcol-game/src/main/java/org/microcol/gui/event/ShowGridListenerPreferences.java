package org.microcol.gui.event;

import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Class just pass changed showing of grid to preferences.
 */
@Listener
public final class ShowGridListenerPreferences {

    private final GamePreferences gamePreferences;

    @Inject
    public ShowGridListenerPreferences(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    private void onShowGrid(ShowGridEvent event) {
        gamePreferences.setShowGrid(event.isGridShown());
    }

}
