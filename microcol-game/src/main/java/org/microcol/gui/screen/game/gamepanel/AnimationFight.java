package org.microcol.gui.screen.game.gamepanel;

import java.util.Map;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javafx.scene.canvas.GraphicsContext;

/**
 * Class animate fight between two adjacent units.
 */
final class AnimationFight implements Animation {

    /**
     * Contains mapping of speed to step size.
     */
    private static final Map<Integer, Integer> SPEED_ANIMATION_STEPS = ImmutableMap.of(0, 200, 1,
            150, 2, 100, 3, 60, 4, 40);

    private int step;

    private final Location attackerLocation;

    private final Location defenderLocation;

    private final ImageProvider imageProvider;

    private final int animationSpeed;

    /**
     * Default constructor.
     *
     * @param attackerLocation
     *            required location of attacking unit
     * @param defenderLocation
     *            required location of defending unit
     * @param imageProvider
     *            required image provider
     * @param animationSpeed
     *            require animation speed from preferences
     */
    AnimationFight(final Location attackerLocation, final Location defenderLocation,
            final ImageProvider imageProvider, final int animationSpeed) {
        this.attackerLocation = Preconditions.checkNotNull(attackerLocation);
        this.defenderLocation = Preconditions.checkNotNull(defenderLocation);
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
        final Point aPoint = area.convertToCanvasPoint(attackerLocation);
        final Point dPoint = area.convertToCanvasPoint(defenderLocation);
        final Point middle = aPoint.add(dPoint.substract(aPoint).divide(2));
        if (area.isVisibleCanvasPoint(aPoint) || area.isVisibleCanvasPoint(dPoint)) {
            graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS),
                    middle.getX(), middle.getY());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("attackerLocation", attackerLocation)
                .add("defenderLocation", defenderLocation).toString();
    }

    @Override
    public boolean canBePainted(final Area area) {
        if (hasNextStep()) {
            final Point aPoint = area.convertToCanvasPoint(attackerLocation);
            final Point dPoint = area.convertToCanvasPoint(defenderLocation);
            return area.isVisibleCanvasPoint(aPoint) || area.isVisibleCanvasPoint(dPoint);
        }
        return false;
    }

}
