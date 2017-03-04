package org.microcol.gui;

import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.VolumeChangeEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Communicate between GUI and sound and music playing.
 * 
 */
public class MusicController {

	private final MusicPlayer musicPlayer;

	@Inject
	public MusicController(final MusicPlayer musicPlayer, final VolumeChangeController volumeChangeController) {
		this.musicPlayer = Preconditions.checkNotNull(musicPlayer);
		volumeChangeController.addFocusedTileListener(e -> onVolumeChanged(e));
	}

	private void onVolumeChanged(final VolumeChangeEvent event) {
		musicPlayer.setVolume(event.getVolume());
	}

	public void start(int defaultVolume) {
		new Thread(() -> musicPlayer.playSound("src/main/resources/music/AnnounceMyName_1.wav", defaultVolume)).start();
	}

	public void stop() {
		musicPlayer.stop();
	}

}
