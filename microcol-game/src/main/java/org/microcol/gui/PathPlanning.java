package org.microcol.gui;

import java.util.Map;

import org.microcol.gui.preferences.GamePreferences;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * Class contains methods for computing path for units.
 * <p>
 * animation speed 0 is faster and {@link #ANIMATION_SPEED_MAX_VALUE} is slower.
 * </p>
 * 
 * @author jan
 *
 */
public final class PathPlanning {

    private final GamePreferences gamePreferences;

    /**
     * Minimal value of animation speed.
     */
    public static final int ANIMATION_SPEED_MIN_VALUE = 0;

    /**
     * Maximal value of animation speed.
     */
    public static final int ANIMATION_SPEED_MAX_VALUE = 5;

    /**
     * Contains mapping of speed to step size.
     */
    private static final Map<Integer, Float> SPEED_FUNCTION = ImmutableMap.of(0, 0.01F, 1, 0.02F, 2,
            0.05F, 3, 0.08F, 4, 0.15F);

    @Inject
    public PathPlanning(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        Preconditions.checkArgument(
                SPEED_FUNCTION.size() == ANIMATION_SPEED_MAX_VALUE - ANIMATION_SPEED_MIN_VALUE,
                "Animation speed finction is not well defined. It's shorter or longer that required size");
    }

    /**
     * Allows to define step operation.
     * 
     * @author jan
     *
     */
    public interface WhatToDoWithPointInPath {
        void pathPoint(Point point);
    }

    /**
     * Draw steps between two map points. It use naive algorithm. <i>y = ax +
     * b</i>
     * 
     * @param tileFrom
     *            required tile from
     * @param tileTo
     *            required tile to
     * @param whatToDoWithPointInPath
     *            required function that's executed with each found point to
     *            visit
     */
    public void paintPath(final Point tileFrom, final Point tileTo,
            final WhatToDoWithPointInPath whatToDoWithPointInPath) {
        paintPathWithSpeed(tileFrom, tileTo, whatToDoWithPointInPath,
                gamePreferences.getAnimationSpeed());
    }

    public void paintPathWithSpeed(final Point tileFrom, final Point tileTo,
            final WhatToDoWithPointInPath whatToDoWithPointInPath, final int speed) {
        final int diff = Math.abs(tileTo.getY() - tileFrom.getY())
                - Math.abs(tileTo.getX() - tileFrom.getX());
        if (diff < 0) {
            float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
            float b = tileFrom.getY() - tileFrom.getX() * a;
            if (tileFrom.getX() < tileTo.getX()) {
                final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
                for (double x = tileFrom.getX(); x <= tileTo.getX(); x += increment) {
                    int y = (int) Math.round(a * x + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of((int) x, y));
                }
            } else {
                final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
                for (double x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
                    int y = (int) Math.round(a * x + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of((int) x, y));
                }
            }
        } else if (!tileFrom.equals(tileTo)) {
            float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
            float b = tileFrom.getX() - tileFrom.getY() * a;
            if (tileFrom.getY() < tileTo.getY()) {
                final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
                for (double y = tileFrom.getY(); y <= tileTo.getY(); y += increment) {
                    int x = (int) Math.round(a * y + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, (int) y));
                }
            } else {
                final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
                for (double y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
                    int x = (int) Math.round(a * y + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, (int) y));
                }
            }
        }
    }

    public float countStepSize(final int from, final int to, final int speed) {
        return countStepSize(to - from, speed);
    }

    public float countStepSize(final int diff, final int speed) {
        final int sign = (int) Math.signum(diff);
        final int diffAbs = Math.abs(diff);
        return sign * countPositiveStepSize(diffAbs, speed);
    }

    private float countPositiveStepSize(final int diffAbs, final int speed) {
        PathPlanning.checkSpeed(speed);
        return SPEED_FUNCTION.get(speed) * diffAbs;
    }

    public static void checkSpeed(final int speed) {
        Preconditions.checkArgument(speed >= ANIMATION_SPEED_MIN_VALUE, "speed '%s' is to low",
                speed);
        Preconditions.checkArgument(speed < ANIMATION_SPEED_MAX_VALUE, "speed '%s' is to high",
                speed);
    }

    /**
     * Draw steps between two map points. It use naive algorithm. <i>y = ax +
     * b</i>
     * 
     * @param tileFrom
     *            required tile from
     * @param tileTo
     *            required tile to
     * @param whatToDoWithPointInPath
     *            required function that's executed with each found point to
     *            visit
     * @param howManyStepsShouldBeDone
     *            required how many steps should be done to reach target
     */
    public void paintPathWithStepsLimit(final Point tileFrom, final Point tileTo,
            final WhatToDoWithPointInPath whatToDoWithPointInPath,
            final int howManyStepsShouldBeDone) {
        final int diff = Math.abs(tileTo.getY() - tileFrom.getY())
                - Math.abs(tileTo.getX() - tileFrom.getX());
        if (diff < 0) {
            float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
            float b = tileFrom.getY() - tileFrom.getX() * a;
            if (tileFrom.getX() < tileTo.getX()) {
                final int increment = getStepSize(tileFrom.getX(), tileTo.getX(),
                        howManyStepsShouldBeDone);
                for (int x = tileFrom.getX(); x <= tileTo.getX(); x += increment) {
                    int y = Math.round(a * x + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
                }
            } else {
                final int increment = getStepSize(tileFrom.getX(), tileTo.getX(),
                        howManyStepsShouldBeDone);
                for (int x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
                    int y = Math.round(a * x + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
                }
            }
        } else if (!tileFrom.equals(tileTo)) {
            float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
            float b = tileFrom.getX() - tileFrom.getY() * a;
            if (tileFrom.getY() < tileTo.getY()) {
                final int increment = getStepSize(tileFrom.getY(), tileTo.getY(),
                        howManyStepsShouldBeDone);
                for (int y = tileFrom.getY(); y <= tileTo.getY(); y += increment) {
                    int x = Math.round(a * y + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
                }
            } else {
                final int increment = getStepSize(tileFrom.getY(), tileTo.getY(),
                        howManyStepsShouldBeDone);
                for (int y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
                    int x = Math.round(a * y + b);
                    addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
                }
            }
        }
    }

    private int getStepSize(final int from, final int to, final int howManyStepsShouldBeDone) {
        int stepSize = (int) ((to - from) / (float) howManyStepsShouldBeDone);
        if (stepSize == 0) {
            return to - from;
        }
        return stepSize;
    }

    /**
     * Pass found location to callBack function. Just when start point is equals
     * to found call back function is not called.
     * 
     * @param tileFrom
     *            required from location
     * @param whatToDoWithPointInPath
     *            required call back function
     * @param pointToAdd
     *            required to location
     */
    private void addPoint(final Point tileFrom,
            final WhatToDoWithPointInPath whatToDoWithPointInPath, final Point pointToAdd) {
        if (!tileFrom.equals(pointToAdd)) {
            whatToDoWithPointInPath.pathPoint(pointToAdd);
        }
    }

}
