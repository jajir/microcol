package org.microcol.gui.event;

import org.microcol.gui.mainmenu.VolumeChangeController;
import org.microcol.gui.util.GamePreferences;

import com.google.inject.Inject;

/**
 * Class just pass changed volume to preferences.
 */
public class VolumeChangedListenerPreferences {

    @Inject
    public VolumeChangedListenerPreferences(final VolumeChangeController volumeChangeController,
            final GamePreferences gamePreferences) {
        volumeChangeController.addListener(e -> gamePreferences.setVolume(e.getVolume()));
    }

}
