package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

public class ModelBuilder {
	private final List<Player> players;
	private final List<Unit> units;
	private final List<Town> towns;
	
	private Calendar calendar;
	private WorldMap map;

	public ModelBuilder() {
		players = new ArrayList<>();
		units = new ArrayList<>();
		towns = new ArrayList<>();		
	}

	public ModelBuilder setCalendar(final int startYear, final int endYear) {
		calendar = new Calendar(startYear, endYear);

		return this;
	}

	public ModelBuilder setMap(final String fileName) {
		map = new WorldMap(fileName);

		return this;
	}

	public ModelBuilder addPlayer(final String name, final boolean computer, final int initialGold) {
		players.add(new Player(name, computer, initialGold));

		return this;
	}
	
	public ModelBuilder addTown(final String name, final String ownerName, final Location location) {
		towns.add(new Town(name, getPlayer(ownerName), location));
		return this;
	}

	public ModelBuilder addUnit(final UnitType type, final String ownerName, final Location location) {
		units.add(new Unit(type, getPlayer(ownerName), location));

		return this;
	}

	private Player getPlayer(final String name) {
		return players.stream()
			.filter(player -> player.getName().equals(name))
			.findAny()
			.orElse(null);
	}

	public Model build() {
		return new Model(calendar, map, players, towns, units);
	}
}
