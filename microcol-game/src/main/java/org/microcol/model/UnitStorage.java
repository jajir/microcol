package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

class UnitStorage {
	private final List<Unit> units;

	UnitStorage(final List<Unit> units) {
		this.units = new ArrayList<>(units);
		checkUnitLocations(this.units);
	}

	private void checkUnitLocations(final List<Unit> units) {
		Map<Location, Player> owners = new HashMap<>();
		units.forEach(unit -> {
			Player owner = owners.get(unit.getLocation());
			if (owner != null) {
				if (!owner.equals(unit.getOwner())) {
					throw new IllegalArgumentException(String.format("There is an enemy unit at the same location (%s).", unit.getLocation()));
				}
			} else {
				owners.put(unit.getLocation(), unit.getOwner());
			}
		});
	}

	List<Unit> getUnits(final boolean includeStored) {
		return units.stream()
			.filter(unit -> includeStored || !unit.isStored())
			.collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getUnitsAt() {
		return Multimaps.asMap(units.stream()
			.filter(unit -> !unit.isStored())
			.collect(ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getUnitsAt(final Location location) {
		Preconditions.checkNotNull(location);

		return units.stream()
			.filter(unit -> !unit.isStored())
			.filter(unit -> unit.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	List<Unit> getUnits(final Player player, final boolean includeStored) {
		Preconditions.checkNotNull(player);

		return units.stream()
			.filter(unit -> unit.getOwner().equals(player))
			.filter(unit -> includeStored || !unit.isStored())
			.collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getUnitsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(units.stream()
			.filter(unit -> unit.getOwner().equals(player))
			.filter(unit -> !unit.isStored())
			.collect(ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getUnitsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return units.stream()
			.filter(unit -> unit.getOwner().equals(player))
			.filter(unit -> !unit.isStored())
			.filter(unit -> unit.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	List<Unit> getEnemyUnits(final Player player, final boolean includeStored) {
		Preconditions.checkNotNull(player);

		return units.stream()
			.filter(unit -> !unit.getOwner().equals(player))
			.filter(unit -> includeStored || !unit.isStored())
			.collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getEnemyUnitsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(units.stream()
			.filter(unit -> !unit.getOwner().equals(player))
			.filter(unit -> !unit.isStored())
			.collect(ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getEnemyUnitsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return units.stream()
			.filter(unit -> !unit.getOwner().equals(player))
			.filter(unit -> !unit.isStored())
			.filter(unit -> unit.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	void remove(final Unit unit) {
		Preconditions.checkNotNull(unit);

		units.remove(unit);
	}

	void save(final JsonGenerator generator) {
		generator.writeStartArray("units");
		units.forEach(unit -> unit.save(generator));
		generator.writeEnd();
	}

	static List<Unit> load(final JsonParser parser, final List<Player> players) {
		parser.next();  // START_ARRAY
		final List<Unit> units = new ArrayList<>();
		Unit unit = null;
		while ((unit = Unit.load(parser, players)) != null) {
			units.add(unit);
		}

		return units;
	}
}
