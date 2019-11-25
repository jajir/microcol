package org.microcol.gui.image;

import org.microcol.gui.util.AnimationScheduler;

/**
 * Holds information when tile animation should be done.
 */
class TileAnimation {

    private final long tickWhenIsDone;

    static TileAnimation makeForSeconds(final long currentGameTick, final int durationInSeconds) {
        return new TileAnimation(currentGameTick + durationInSeconds * AnimationScheduler.FPS);
    }

    TileAnimation(final long tickWhenIsDone) {
        this.tickWhenIsDone = tickWhenIsDone;
    }

    /**
     * Provide information if animation should be done.
     *
     * @param currentTick
     * @return When animation should be done return <code>true</code> otherwise
     *         return <code>false</code>.
     */
    boolean isDone(final long currentTick) {
        return currentTick >= tickWhenIsDone;
    }

}
