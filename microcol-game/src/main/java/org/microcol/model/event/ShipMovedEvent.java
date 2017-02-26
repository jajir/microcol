package org.microcol.model.event;

import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.Path;
import org.microcol.model.Ship;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ShipMovedEvent extends ModelEvent {
	private final Ship ship;
	private final Location startLocation;
	private final Path path;

	public ShipMovedEvent(final Game game, final Ship ship, final Location startLocation, final Path path) {
		super(game);

		this.ship = Preconditions.checkNotNull(ship);
		this.startLocation = Preconditions.checkNotNull(startLocation);
		this.path = Preconditions.checkNotNull(path);
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
		return MoreObjects.toStringHelper(this)
			.add("ship", ship)
			.add("startLocation", startLocation)
			.add("path", path)
			.toString();
	}
}
