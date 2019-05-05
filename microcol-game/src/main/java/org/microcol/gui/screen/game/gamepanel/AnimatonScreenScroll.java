package org.microcol.gui.screen.game.gamepanel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Perform scrolling to some place at map as animation.
 */
final class AnimatonScreenScroll {

    private final ScreenScrolling screenScrolling;

    AnimatonScreenScroll(final ScreenScrolling screenScrolling) {
        this.screenScrolling = Preconditions.checkNotNull(screenScrolling);
    }

    boolean hasNextStep() {
        return screenScrolling.isNextPointAvailable();
    }

    void paint(final VisibleAreaService visibleArea) {
        visibleArea.setTopLeftPosionOfCanvas(screenScrolling.getNextPoint());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("screenScrolling", screenScrolling)
                .toString();
    }

}
