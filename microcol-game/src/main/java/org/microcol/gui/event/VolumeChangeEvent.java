package org.microcol.gui.event;

/**
 * Contains new volume value of music or of effects.
 */
public final class VolumeChangeEvent {

    private final boolean effects;

    private final int volume;

    /**
     * Default constructor.
     *
     * @param effects <code>true</code> for the effects volume,
     *                <code>false</code> for the music volume
     * @param volume  chosen volume
     */
    public VolumeChangeEvent(final boolean effects, final int volume) {
        this.effects = effects;
        this.volume = volume;
    }

    /**
     * @return <code>true</code> when the effects volume changed, otherwise the
     *         music volume changed
     */
    public boolean isEffects() {
        return effects;
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
