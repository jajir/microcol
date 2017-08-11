package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class ModelBuilder {
	private final List<Player> players;
	private final List<Unit> units;
	private final List<Town> towns;
	private EuropeBuilder europeBuilder;

	private Calendar calendar;
	private WorldMap map;

	public ModelBuilder() {
		players = new ArrayList<>();
		units = new ArrayList<>();
		towns = new ArrayList<>();
	}

	public ModelBuilder addUnit(final Unit unit) {
		Preconditions.checkNotNull(unit);
		if (units.contains(unit)) {
			throw new IllegalArgumentException("Unit was already registered. Unit: " + unit);
		}
		units.add(unit);
		return this;
	}

	public EuropeBuilder getEuropeBuilder() {
		if (europeBuilder == null) {
			europeBuilder = new EuropeBuilder(this);
			return europeBuilder;
		} else {
			throw new IllegalStateException("Europe is already build");
		}
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

	Player getPlayer(final String name) {
		return players.stream().filter(player -> player.getName().equals(name)).findAny()
				.orElseThrow(() -> new IllegalStateException("Invalid owner name '" + name + "'"));
	}

	public Model build() {
		Preconditions.checkNotNull(europeBuilder == null, "Europe was not builded");
		return new Model(calendar, map, players, towns, units, europeBuilder.getUnitsInEuropePort());
	}

	public UnitBuilder makeUnitBuilder() {
		return new UnitBuilder(this);
	}
}
