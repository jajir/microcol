package org.microcol.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.microcol.gui.Point;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * Class contains methods for computing path for units.
 * <p>
 * animation speed 0 is faster and {@link #ANIMATION_SPEED_MAX_VALUE} is slower.
 * </p>
 */
public class PathPlanning {

    /**
     * Contains mapping of speed to step size.
     */
    private static final Map<Integer, Float> SPEED_FUNCTION = ImmutableMap.of(0, 0.01F, 1, 0.02F, 2,
            0.05F, 3, 0.08F, 4, 0.15F);

    @Inject
    public PathPlanning() {
    }

    /**
     * Compute path and limit number of point by length of step.
     *
     * @param tileFrom
     *            required point from
     * @param tileTo
     *            required point to
     * @param speed
     *            how many points is walked in each step
     * @return
     */
    public List<Point> getPathLimitSpeed(final Point tileFrom, final Point tileTo,
            final int speed) {
        final List<Point> out = new ArrayList<>();
        final int diff = Math.abs(tileTo.getY() - tileFrom.getY())
                - Math.abs(tileTo.getX() - tileFrom.getX());
        if (diff < 0) {
            float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
            float b = tileFrom.getY() - tileFrom.getX() * a;
            if (tileFrom.getX() < tileTo.getX()) {
                final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
                for (double x = tileFrom.getX(); x <= tileTo.getX(); x += increment) {
                    int y = (int) Math.round(a * x + b);
                    addPoint(tileFrom, out, Point.of((int) x, y));
                }
            } else {
                final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
                for (double x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
                    int y = (int) Math.round(a * x + b);
                    addPoint(tileFrom, out, Point.of((int) x, y));
                }
            }
        } else if (!tileFrom.equals(tileTo)) {
            float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
            float b = tileFrom.getX() - tileFrom.getY() * a;
            if (tileFrom.getY() < tileTo.getY()) {
                final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
                for (double y = tileFrom.getY(); y <= tileTo.getY(); y += increment) {
                    int x = (int) Math.round(a * y + b);
                    addPoint(tileFrom, out, Point.of(x, (int) y));
                }
            } else {
                final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
                for (double y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
                    int x = (int) Math.round(a * y + b);
                    addPoint(tileFrom, out, Point.of(x, (int) y));
                }
            }
        }
        return out;
    }

    float countStepSize(final int from, final int to, final int speed) {
        final int diff = to - from;
        final int sign = (int) Math.signum(diff);
        final int diffAbs = Math.abs(diff);
        return sign * countPositiveStepSize(diffAbs, speed);
    }

    private float countPositiveStepSize(final int diffAbs, final int speed) {
        return SPEED_FUNCTION.get(speed) * diffAbs;
    }

    /**
     * Get path between two points. It use naive algorithm. <i>y = ax + b</i>
     * 
     * @param tileFrom
     *            required tile from
     * @param tileTo
     *            required tile to
     * @param howManyStepsShouldBeDone
     *            required how many steps should be done to reach target
     * @return Return list of point.
     */
    public List<Point> getPathLimitSteps(final Point tileFrom, final Point tileTo,
            final int howManyStepsShouldBeDone) {
        final List<Point> out = new ArrayList<>();
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
                    addPoint(tileFrom, out, Point.of(x, y));
                }
            } else {
                final int increment = getStepSize(tileFrom.getX(), tileTo.getX(),
                        howManyStepsShouldBeDone);
                for (int x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
                    int y = Math.round(a * x + b);
                    addPoint(tileFrom, out, Point.of(x, y));
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
                    addPoint(tileFrom, out, Point.of(x, y));
                }
            } else {
                final int increment = getStepSize(tileFrom.getY(), tileTo.getY(),
                        howManyStepsShouldBeDone);
                for (int y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
                    int x = Math.round(a * y + b);
                    addPoint(tileFrom, out, Point.of(x, y));
                }
            }
        }
        return out;
    }

    private int getStepSize(final int from, final int to, final int howManyStepsShouldBeDone) {
        int stepSize = (int) ((to - from) / (float) howManyStepsShouldBeDone);
        if (stepSize == 0) {
            return to - from;
        }
        return stepSize;
    }

    private void addPoint(final Point tileFrom, final List<Point> out, final Point pointToAdd) {
        if (!tileFrom.equals(pointToAdd)) {
            out.add(pointToAdd);
        }
    }

}
