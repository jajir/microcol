package org.microcol.model;

public class Location {
	private final int x;
	private final int y;

	public Location(final int x, final int y) {
		// TODO JKA Add >= 0 test.
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
