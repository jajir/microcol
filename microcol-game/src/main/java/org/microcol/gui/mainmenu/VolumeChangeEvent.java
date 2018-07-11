package org.microcol.gui.mainmenu;

/**
 * Contains new volume value.
 */
public final class VolumeChangeEvent {

    private final int volume;

    /**
     * Default constructor.
     *
     * @param volume
     *            chosen volume
     */
    public VolumeChangeEvent(final int volume) {
        this.volume = volume;
    }

    /**
     * Get volume chosen by player.
     * 
     * @return actual volume level
     */
    public int getVolume() {
        return volume;
    }

}
