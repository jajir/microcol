package org.microcol.gui.gamepanel;

import java.util.Map;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javafx.scene.canvas.GraphicsContext;

/**
 * Class animate fight between two adjacent units.
 */
public final class AnimationFight implements Animation {

    /**
     * Contains mapping of speed to step size.
     */
    private static final Map<Integer, Integer> SPEED_ANIMATION_STEPS = ImmutableMap.of(0, 200, 1,
            150, 2, 100, 3, 60, 4, 40);

    private int step;

    private final Unit attacker;

    private final Unit defender;

    private final ImageProvider imageProvider;

    private final int animationSpeed;

    AnimationFight(final Unit attacker, final Unit defender, final ImageProvider imageProvider,
            final int animationSpeed) {
        this.attacker = Preconditions.checkNotNull(attacker);
        this.defender = Preconditions.checkNotNull(defender);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.animationSpeed = animationSpeed;
    }

    @Override
    public boolean hasNextStep() {
        return step < SPEED_ANIMATION_STEPS.get(animationSpeed);
    }

    @Override
    public void nextStep() {
        step++;
    }

    @Override
    public void paint(final GraphicsContext graphics, final Area area) {
        final Point aPoint = area.convertToPoint(attacker.getLocation());
        final Point dPoint = area.convertToPoint(defender.getLocation());
        final Point middle = aPoint.add(dPoint.substract(aPoint).divide(2));
        // TODO JJ paint animation just when at least one point is on screen.
        graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS), middle.getX(),
                middle.getY());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("attacker", attacker)
                .add("defender", defender).toString();
    }

}
