package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.microcol.model.store.CargoPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

class UnitStorage {
	
	private final List<Unit> units;

	UnitStorage(final List<Unit> units) {
		this.units = new ArrayList<>(units);
		checkUnitLocations(this.units);
		checkDuplicities(units);
	}
	
	void addUnit(final Unit unit) {
		units.add(unit);
	}
	
	void addUnitToPlayer(final UnitType unitType, final Player owner, final Model model){
		UnitPo unitPo = new UnitPo();
		unitPo.setCargo(new CargoPo());
		units.add(new Unit(unitPo, model, IdManager.nextId(),
				unit -> new PlaceEuropePier(unit), unitType, owner, unitType.getSpeed()));
	}

	private void checkUnitLocations(final List<Unit> units) {
		Map<Location, Player> owners = new HashMap<>();
		units.forEach(unit -> {
			if (unit.isAtPlaceLocation()) {
				Player owner = owners.get(unit.getLocation());
				if (owner != null) {
					if (!owner.equals(unit.getOwner())) {
						throw new IllegalArgumentException(
								String.format("There is an enemy unit at the same location (%s).", unit.getLocation()));
					}
				} else {
					owners.put(unit.getLocation(), unit.getOwner());
				}
			}
		});
	}

	/**
	 * Verify that all units are just one time in all unit list.
	 * 
	 * @param units
	 *            required list of units.
	 */
	private void checkDuplicities(final List<Unit> units) {
		final Set<Unit> tmp = new HashSet<>();
		units.forEach(unit -> {
			if (tmp.contains(unit)) {
				throw new IllegalStateException("Unit was registered twice, unit: " + unit);
			} else {
				tmp.add(unit);
			}
		});
	}

	List<Unit> getUnits() {
		return ImmutableList.copyOf(units);
	}

	// TODO JJ rename it, be more specific about function
	//TODO it's function to remove
	List<Unit> getUnits(final boolean includeStored) {
		return units.stream().filter(unit -> includeStored || unit.isAtPlaceLocation()).collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getUnitsAt() {
		return Multimaps.asMap(units.stream().filter(unit -> unit.isAtPlaceLocation())
				.collect(ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getUnitsAt(final Location location) {
		Preconditions.checkNotNull(location);

		return units.stream().filter(unit -> unit.isAtPlaceLocation()).filter(unit -> unit.getLocation().equals(location))
				.collect(ImmutableList.toImmutableList());
	}

	List<Unit> getUnits(final Player player, final boolean includeStored) {
		Preconditions.checkNotNull(player);

		return units.stream().filter(unit -> unit.getOwner().equals(player))
				.filter(unit -> includeStored || unit.isAtPlaceLocation()).collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getUnitsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(
				units.stream().filter(unit -> unit.getOwner().equals(player)).filter(unit -> unit.isAtPlaceLocation()).collect(
						ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getUnitsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return units.stream().filter(unit -> unit.getOwner().equals(player)).filter(unit -> unit.isAtPlaceLocation())
				.filter(unit -> unit.getLocation().equals(location)).collect(ImmutableList.toImmutableList());
	}

	List<Unit> getEnemyUnits(final Player player, final boolean includeStored) {
		Preconditions.checkNotNull(player);

		return units.stream().filter(unit -> !unit.getOwner().equals(player))
				.filter(unit -> includeStored || unit.isAtPlaceLocation()).collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Unit>> getEnemyUnitsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(
				units.stream().filter(unit -> !unit.getOwner().equals(player)).filter(unit -> unit.isAtPlaceLocation()).collect(
						ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation, Function.identity())));
	}

	List<Unit> getEnemyUnitsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return units.stream().filter(unit -> !unit.getOwner().equals(player)).filter(unit -> unit.isAtPlaceLocation())
				.filter(unit -> unit.getLocation().equals(location)).collect(ImmutableList.toImmutableList());
	}

	void remove(final Unit unit) {
		Preconditions.checkNotNull(unit);

		units.remove(unit);
	}

	void save(final ModelPo gamePo) {
		units.forEach(unit -> {
			gamePo.getUnits().add(unit.save());
		});
	}

	Unit getUnitById(int id) {
		return tryGetUnitById(id)
				.orElseThrow(() -> new IllegalArgumentException("There is no unit with id '" + id + "'."));
	}
	
	Optional<Unit> tryGetUnitById(final int id) {
		return units.stream().filter(unit -> unit.getId() == id).findAny();
	}

}
