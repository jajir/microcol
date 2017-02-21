package org.microcol.model.event;

import org.microcol.model.Game;
import org.microcol.model.Path;
import org.microcol.model.Ship;

public class ShipMovedEvent extends GameEvent {
	private final Ship ship;
	private final Path path;

	public ShipMovedEvent(final Game game, final Ship ship, final Path path) {
		super(game);

		// TODO JKA Add not null tests.
		this.ship = ship;
		this.path = path;
	}

	public Ship getShip() {
		return ship;
	}

	public Path getPath() {
		return path;
	}
}
