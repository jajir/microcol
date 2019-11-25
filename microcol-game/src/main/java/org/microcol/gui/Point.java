package org.microcol.gui;

import com.google.common.base.MoreObjects;

/**
 * Represents point in on-screen coordinates.
 */
public final class Point {

    /**
     * Point at zero [0,0].
     */
    public static final Point ZERO = Point.of(0, 0);

    /**
     * On screen X coordinate.
     */
    private final int x;

    /**
     * On screen Y coordinate.
     */
    private final int y;

    private Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return x + (y << 16);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Point)) {
            return false;
        }

        final Point location = (Point) object;

        return x == location.x && y == location.y;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString();
    }

    public static Point of(final int x, final int y) {
        return new Point(x, y);
    }

    public static Point of(final double x, final double y) {
        return new Point((int) x, (int) y);
    }

    public Point add(final int addX, final int addY) {
        return new Point(x + addX, y + addY);
    }

    public Point add(final double addX, final double addY) {
        return new Point(x + (int) addX, y + (int) addY);
    }

    public Point add(final Point point) {
        return new Point(x + point.getX(), y + point.getY());
    }

    public Point substract(final Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public Point multiply(final int factor) {
        return new Point(x * factor, y * factor);
    }

    public boolean isGreaterOrEqualsThan(final Point point) {
        return x >= point.x && y >= point.y;
    }

    public boolean isSmallerOrEqualsThan(final Point point) {
        return x <= point.x && y <= point.y;
    }

    /**
     * Divide point by some factor and round down.
     *
     * @param factor
     *            required this number will divide coordinates
     * @return divided point by factor
     */
    public Point divide(final double factor) {
        return new Point(div(x, factor), div(y, factor));
    }

    private int div(final int num, final double factor) {
        if (num < 0) {
            return (int) (num / factor) - 1;
        } else {
            return (int) (num / factor);
        }
    }

    public int distanceSimplified(final Point p) {
        return Math.abs(x - p.getX()) + Math.abs(y - p.getY());
    }
}
