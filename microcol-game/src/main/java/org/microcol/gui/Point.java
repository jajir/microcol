package org.microcol.gui;

import com.google.common.base.MoreObjects;

/**
 * Represents point in on-screen coordinates.
 */
public class Point {

	private final int x;
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

	public Point add(final int value) {
		return new Point(x + value, y + value);
	}

	public Point substract(final Point p) {
		return new Point(x - p.x, y - p.y);
	}

	public Point multiply(final int factor) {
		return new Point(x * factor, y * factor);
	}
}