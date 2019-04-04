package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Perform scrolling to some place at map as animation.
 */
public final class AnimatonScreenScroll {

    private final ScreenScrolling screenScrolling;

    public AnimatonScreenScroll(final ScreenScrolling screenScrolling) {
        this.screenScrolling = Preconditions.checkNotNull(screenScrolling);
    }

    public boolean hasNextStep() {
        return screenScrolling.isNextPointAvailable();
    }

    public void paint(final VisibleArea visibleArea) {
        final Point point = screenScrolling.getNextPoint();
        visibleArea.setX(point.getX());
        visibleArea.setY(point.getY());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("screenScrolling", screenScrolling)
                .toString();
    }

}
