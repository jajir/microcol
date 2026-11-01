package org.microcol.gui;

import com.google.inject.ImplementedBy;

/**
 * Plays the short success cue of a won mission (R-0228). It is independent of
 * the background music. Implementations return at once, play at most one cue at
 * a time and never let an audio problem reach the caller.
 */
@ImplementedBy(JavaSoundSuccessCuePlayer.class)
public interface SuccessCuePlayer {

    /**
     * Start the cue, unless sound is off, the effects volume is zero or a cue
     * is already playing; such a request is dropped, not queued.
     */
    void play();

    /**
     * Apply a changed effects volume to a playing cue; a later cue reads the
     * saved effects volume.
     *
     * @param volume from 0, silence, to 100
     */
    void setVolume(int volume);

    /**
     * Stop a playing cue, release its audio resources and play no further cue.
     */
    void stop();

}
