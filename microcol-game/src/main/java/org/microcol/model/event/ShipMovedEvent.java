package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Location;
import org.microcol.model.Path;
import org.microcol.model.Ship;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ShipMovedEvent extends ModelEvent {
	private final Ship ship;
	private final Location start;
	private final Path path;

	public ShipMovedEvent(final Model model, final Ship ship, final Location start, final Path path) {
		super(model);

		this.ship = Preconditions.checkNotNull(ship);
		this.start = Preconditions.checkNotNull(start);
		this.path = Preconditions.checkNotNull(path);
	}

	public Ship getShip() {
		return ship;
	}

	public Location getStart() {
		return start;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("ship", ship)
			.add("start", start)
			.add("path", path)
			.toString();
	}
}
