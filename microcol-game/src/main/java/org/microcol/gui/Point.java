package org.microcol.gui;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Point {

	private final int x;

	private final int y;

	private Point(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public static Point make(int x, int y) {
		return new Point(x, y);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(x, y);

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Point other = (Point) obj;
		return Objects.equal(x, other.x) && Objects.equal(y, other.y);
	}

	public Point substract(final Point p) {
		return new Point(x - p.x, y - p.y);
	}

	public Point multiply(final int factor) {
		return new Point(x * factor, y * factor);
	}

	public Point add(final int value) {
		return new Point(x + value, y + value);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Point.class).add("x", x).add("y", y).toString();
	}

}
