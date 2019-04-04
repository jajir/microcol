package org.microcol.gui.screen.game.gamepanel;

import java.util.function.Consumer;

import com.google.common.base.MoreObjects;

final class AnimationHolder {

    private final Animation animation;

    private final Consumer<Animation> onAnimationIsDone;

    AnimationHolder(final Animation animation, final Consumer<Animation> onAnimationIsDone) {
        this.animation = animation;
        this.onAnimationIsDone = onAnimationIsDone;
    }

    /**
     * @return the animation
     */
    Animation getAnimation() {
        return animation;
    }

    void runOnAnimationIsDone() {
        if (onAnimationIsDone != null) {
            onAnimationIsDone.accept(animation);
        }
    }

    /**
     * @return the onAnimationIsDone
     */
    Consumer<Animation> getOnAnimationIsDone() {
        return onAnimationIsDone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("animation", animation)
                .add("onAnimationIsDone", onAnimationIsDone).toString();
    }

}