package org.microcol.gui;

import org.microcol.gui.event.VolumeChangeEvent;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Communicate between GUI and sound and music playing.
 * 
 */
@Listener
public final class MusicController {

    private final MusicPlayer musicPlayer;
    private final GamePreferences gamePreferences;

    @Inject
    public MusicController(final MusicPlayer musicPlayer, final GamePreferences gamePreferences) {
        this.musicPlayer = Preconditions.checkNotNull(musicPlayer);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    private void onVolumeChanged(final VolumeChangeEvent event) {
        if (gamePreferences.isSoundEnabled()) {
            musicPlayer.setVolume(event.getVolume());
        }
    }

    public void start(final int defaultVolume) {
        if (gamePreferences.isSoundEnabled()) {
            new Thread(() -> musicPlayer.playSound("music/AnnounceMyName_1.wav", defaultVolume))
                    .start();
        }
    }

    public void stop() {
        if (gamePreferences.isSoundEnabled()) {
            musicPlayer.stop();
        }
    }

}
