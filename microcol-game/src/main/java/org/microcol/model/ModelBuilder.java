package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class ModelBuilder {
	private final List<Player> players;
	private final List<Ship> ships;

	private Calendar calendar;
	private WorldMap map;

	public ModelBuilder() {
		players = new ArrayList<>();
		ships = new ArrayList<>();
	}

	public ModelBuilder setCalendar(final int startYear, final int endYear) {
		calendar = new Calendar(startYear, endYear);

		return this;
	}

	public ModelBuilder setMap(final String fileName) {
		map = new WorldMap(fileName);

		return this;
	}

	public ModelBuilder addPlayer(final String name, final boolean computer) {
		players.add(new Player(name, computer));

		return this;
	}

	public ModelBuilder addShip(final String ownerName, final ShipType type, final Location location) {
		ships.add(new Ship(getPlayer(ownerName), type, location));

		return this;
	}

	private Player getPlayer(final String name) {
		return players.stream()
			.filter(player -> player.getName().equals(name))
			.findAny()
			.orElse(null);
	}

	public Model build() {
		return new Model(calendar, map, players, ships);
	}
}
