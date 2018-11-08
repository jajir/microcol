package org.microcol.gui;

import org.microcol.gui.mainmenu.VolumeChangeEvent;
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

    @Inject
    public MusicController(final MusicPlayer musicPlayer) {
        this.musicPlayer = Preconditions.checkNotNull(musicPlayer);
    }

    @Subscribe
    private void onVolumeChanged(final VolumeChangeEvent event) {
        musicPlayer.setVolume(event.getVolume());
    }

    public void start(final int defaultVolume) {
        new Thread(() -> musicPlayer.playSound("music/AnnounceMyName_1.wav", defaultVolume))
                .start();
    }

    public void stop() {
        musicPlayer.stop();
    }

}
