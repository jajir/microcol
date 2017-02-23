package org.microcol.model;

import com.google.common.base.Preconditions;

public class Location {
	private final int x;
	private final int y;

	public Location(final int x, final int y) {
		Preconditions.checkArgument(x >= 0, "X-axis cannot be negative: %s", x);
		Preconditions.checkArgument(y >= 0, "Y-axis cannot be negative: %s", y);

		// TODO JKA Check x <= map.getMaxX()
		// TODO JKA Check y <= map.getMaxY()

		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	// TODO JKA Rename to AdjacentOrEquals?
	public boolean isAdjacent(final Location location) {
		Preconditions.checkNotNull(location);

		return Math.abs(x - location.x) <= 1
			&& Math.abs(y - location.y) <= 1; 
	}

	@Override
	public int hashCode() {
		return x + (y << 16);
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (!(object instanceof Location)) {
			return false;
		}

		Location location = (Location) object;

		return x == location.x && y == location.y;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Location [x = ");
		builder.append(x);
		builder.append(", y = ");
		builder.append(y);
		builder.append("]");

		return builder.toString();
	}
}
