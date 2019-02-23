package org.microcol.gui.screen.game.gamepanel;

import com.google.common.base.MoreObjects;

/**
 * Event is send when animation starts. Where there are more than one animation
 * that it's called for each animation.
 */
public final class AnimationStartedEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).toString();
    }

}
