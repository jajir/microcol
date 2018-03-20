package org.microcol.gui.gamepanel;

import java.util.function.Consumer;

class AnimationHolder {

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

}