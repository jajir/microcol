package org.microcol.gui.event;

import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Class just pass changed volume to preferences.
 */
@Listener
public final class VolumeChangedListenerPreferences {

    private final GamePreferences gamePreferences;

    @Inject
    public VolumeChangedListenerPreferences(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    private void onVolumeChange(final VolumeChangeEvent event) {
        gamePreferences.setVolume(event.getVolume());
    }

}
