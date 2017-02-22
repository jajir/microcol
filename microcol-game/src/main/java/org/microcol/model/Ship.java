package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class Ship {
	private final Player owner;
	private Location location;
	private final int maxActionPoints;
	private int currentActionPoints;

	public Ship(final Player owner, final Location location, final int maxActionPoints) {
		// TODO JKA Add not null tests.
		this.owner = owner;
		this.location = location;
		this.maxActionPoints = maxActionPoints;
		this.currentActionPoints = maxActionPoints;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		return location;
	}

	public void moveTo(final Path path) {
		// TODO JKA Implement tests.
		// TODO JKA Komplet předělat.
		final Location startLocation = location;
		List<Location> moves = new ArrayList<>();
		for (Location newLocation : path.getLocations()) {
			if (currentActionPoints <= 0) {
				break;
			}

			if (!location.equals(newLocation)) {
				moves.add(newLocation);
				location = newLocation;
				currentActionPoints--;
			}
		}
		if (!moves.isEmpty()) {
			Game.instance.listenersManager.fireShipMoved(Game.instance, this, startLocation, new Path(moves));
		}
	}

	protected void startTurn() {
		currentActionPoints = maxActionPoints;
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
