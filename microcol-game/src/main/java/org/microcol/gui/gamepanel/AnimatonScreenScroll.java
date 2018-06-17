package org.microcol.gui.gamepanel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Perform scrolling to some place at map as animation.
 */
public class AnimatonScreenScroll implements Animation {

    private final ScreenScrolling screenScrolling;

    AnimatonScreenScroll(final ScreenScrolling screenScrolling) {
        this.screenScrolling = Preconditions.checkNotNull(screenScrolling);
    }
    
    @Override
    public boolean hasNextStep() {
        return screenScrolling.isNextPointAvailable();
    }

    @Override
    public void nextStep() {
        // nothing to do
    }

    @Override
    public void paint(final GraphicsContext graphics, final Area area) {
        area.scrollToPoint(screenScrolling.getNextPoint());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("screenScrolling", screenScrolling)
                .toString();
    }

}
