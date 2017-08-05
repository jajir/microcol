package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ModelBuilder {
	private final List<Player> players;
	private final List<Unit> units;
	private final List<Town> towns;
	private final List<HighSeaUnit> highSeas = Lists.newArrayList();
	private Europe europe;

	private Calendar calendar;
	private WorldMap map;

	public ModelBuilder() {
		players = new ArrayList<>();
		units = new ArrayList<>();
		towns = new ArrayList<>();
	}

	void setEurope(final Europe europe) {
		this.europe = Preconditions.checkNotNull(europe);
	}

	public ModelBuilder addUnit(final Unit unit) {
		Preconditions.checkNotNull(unit);
		units.add(unit);
		return this;
	}

	public EuropeBuilder getEuropeBuilder() {
		if (europe == null) {
			return new EuropeBuilder(this);
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
		Preconditions.checkNotNull(europe, "Europe was not builded");
		return new Model(calendar, map, players, towns, units, europe, highSeas);
	}
	

	public ModelBuilder addShipIncomingToEurope(final Unit ship, final int remainingTurns) {
		Preconditions.checkArgument(UnitType.isShip(ship.getType()));
		final HighSeaUnit highSeaUnit = new HighSeaUnit(ship, true, remainingTurns);  
		highSeas.add(highSeaUnit);
		addUnit(ship);

		return this;
	}

	
	public ModelBuilder addShipIncomingToColonies(final Unit ship, final int remainingTurns) {
		Preconditions.checkArgument(UnitType.isShip(ship.getType()));
		final HighSeaUnit highSeaUnit = new HighSeaUnit(ship, false, remainingTurns);  
		highSeas.add(highSeaUnit);
		addUnit(ship);

		return this;
	}


	public UnitBuilder makeUnitBuilder() {
		return new UnitBuilder(this);
	}
}
