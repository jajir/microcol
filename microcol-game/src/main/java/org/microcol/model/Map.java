package org.microcol.model;

public class Map {
	private final int maxX;
	private final int maxY;

	public Map(final int maxX, final int maxY) {
		// TODO JKA Add > 0 test.
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Map [maxX = ");
		builder.append(maxX);
		builder.append(", maxY = ");
		builder.append(maxY);
		builder.append("]");

		return builder.toString();
	}
}
