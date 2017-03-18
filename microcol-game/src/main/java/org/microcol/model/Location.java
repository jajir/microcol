package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Location {
	private final int x;
	private final int y;

	private Location(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	// tranzitivní
	public boolean isAdjacent(final Location location) {
		Preconditions.checkNotNull(location);

		if (equals(location)) {
			return false;
		}

		return Math.abs(x - location.x) <= 1
			&& Math.abs(y - location.y) <= 1; 
	}

	// tranzitivní
	// funguje správně pouze pro čísla menší než MAX_INT
	public Location add(final Location location) {
		Preconditions.checkNotNull(location);

		return Location.of(x + location.x, y + location.y);
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

		if (!(object instanceof Location)) {
			return false;
		}

		final Location location = (Location) object;

		return x == location.x
			&& y == location.y;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("x", x)
			.add("y", y)
			.toString();
	}

	public static Location of(final int x, final int y) {
		return new Location(x, y);
	}
}
