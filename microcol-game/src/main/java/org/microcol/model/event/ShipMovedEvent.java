package org.microcol.model.event;

import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.Path;
import org.microcol.model.Ship;

public class ShipMovedEvent extends GameEvent {
	private final Ship ship;
	private final Location startLocation;
	private final Path path;

	public ShipMovedEvent(final Game game, final Ship ship, final Location startLocation, final Path path) {
		super(game);

		// TODO JKA Add not null tests.
		this.ship = ship;
		this.startLocation = startLocation;
		this.path = path;
	}

	public Ship getShip() {
		return ship;
	}

	public Location getStartLocation() {
		return startLocation;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("ShipMovedEvent [ship = ");
		builder.append(ship);
		builder.append(", startLocation = ");
		builder.append(startLocation);
		builder.append(", path = ");
		builder.append(path);
		builder.append("]");

		return builder.toString();
	}
}
