package org.microcol.gui.event;

/**
 * Contains new volume value.
 *
 */
public class VolumeChangeEvent {

	private final int volume;

	public VolumeChangeEvent(final int volume) {
		this.volume = volume;
	}

	public int getVolume() {
		return volume;
	}

}
