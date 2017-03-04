package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Map {
	private final int maxX;
	private final int maxY;

	public Map(final int maxX, final int maxY) {
		Preconditions.checkArgument(maxX > 0, "MaxX (%s) must be positive.", maxX);
		Preconditions.checkArgument(maxY > 0, "MaxY (%s) must be positive.", maxY);

		this.maxX = maxX;
		this.maxY = maxY;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public boolean isValid(final Location location) {
		Preconditions.checkNotNull(location);

		return location.getX() >= 0
			&& location.getX() <= maxX
			&& location.getY() >= 0
			&& location.getY() <= maxY;
	}

	public boolean isValid(final Path path) {
		Preconditions.checkNotNull(path);

		// TODO JKA Use streams
		for (Location location : path.getLocations()) {
			if (!isValid(location)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("maxX", maxX)
			.add("maxY", maxY)
			.toString();
	}
}
