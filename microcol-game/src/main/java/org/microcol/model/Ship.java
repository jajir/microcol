package org.microcol.model;

public class Ship {
	private final Player owner;
	private Location location;

	public Ship(final Player owner, final Location location) {
		// TODO JKA Add not null tests.
		this.owner = owner;
		this.location = location;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		return location;
	}

	public void moveTo(final Path path) {
		// FIXME JKA Implement.
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Ship [owner = ");
		builder.append(owner);
		builder.append(", location = ");
		builder.append(location);
		builder.append("]");

		return builder.toString();
	}
}
