package org.akropia.gui;

import org.akropia.gui.event.VolumeChangeEvent;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Communicate between GUI and sound and music playing. A music volume change
 * goes only to the music, an effects volume change only to the success cue.
 *
 */
@Listener
public final class MusicController {

    private final MusicPlayer musicPlayer;
    private final SuccessCuePlayer successCuePlayer;
    private final GamePreferences gamePreferences;

    @Inject
    public MusicController(final MusicPlayer musicPlayer,
            final SuccessCuePlayer successCuePlayer,
            final GamePreferences gamePreferences) {
        this.musicPlayer = Preconditions.checkNotNull(musicPlayer);
        this.successCuePlayer = Preconditions.checkNotNull(successCuePlayer);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    private void onVolumeChanged(final VolumeChangeEvent event) {
        if (gamePreferences.isSoundEnabled()) {
            if (event.isEffects()) {
                successCuePlayer.setVolume(event.getVolume());
            } else {
                musicPlayer.setVolume(event.getVolume());
            }
        }
    }

    public void start(final int defaultVolume) {
        if (gamePreferences.isSoundEnabled()) {
            musicPlayer.setVolume(defaultVolume);
            new Thread(
                    () -> musicPlayer.playSound("music/AnnounceMyName_1.wav"))
                    .start();
        }
    }

    public void stop() {
        if (gamePreferences.isSoundEnabled()) {
            musicPlayer.stop();
        }
    }

}
