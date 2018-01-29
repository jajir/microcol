package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

class UnitStorage {
	
	/**
	 * Ordered list of all units.
	 */
	private final List<Unit> units;

	UnitStorage() {
		this.units = new ArrayList<>();
	}
	
	void addUnit(final Unit unit) {
		Preconditions.checkNotNull(unit);
		Preconditions.checkArgument(!units.contains(unit), "unit %s was already added.", unit);
		Preconditions.checkArgument(!tryGetUnitById(unit.getId()).isPresent(), "Unit with id %s was already added.",
				unit, unit.getId());
		//TODO add some preconditions
		//TODO add test
		units.add(unit);
		//TODO change following that will check just added unit 
		checkUnitLocations(units);
		checkDuplicities(units);
	}
	
	void addUnitToPlayer(final UnitType unitType, final Player owner, final Model model){
		units.add(new Unit(unit -> new Cargo(unit, unitType.getCargoCapacity()), model, IdManager.nextId(),
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
	
	Optional<Unit> getNextUnitForCurrentUser(final Player currentPlayer, final Unit currentUnit){
		final List<Unit> list = units.stream()
				.filter(unit -> currentPlayer.equals(unit.getOwner()))
				.filter(unit -> unit.isAtPlaceLocation() && unit.getAvailableMoves() > 0)
				.collect(Collectors.toList());
		if(list.size()==0){
			return Optional.empty();
		}
		int pos = list.indexOf(currentUnit);
		if (pos >= 0) {
			if (pos + 1 < list.size()) {
				return Optional.of(list.get(pos + 1));
			} else {
				return Optional.of(list.get(0));
			}
		} else {
			return Optional.of(list.get(0));
		}
	}

	Optional<Unit> getFirstSelectableUnit(final Player currentPlayer){
		return units.stream()
				.filter(unit -> currentPlayer.equals(unit.getOwner()))
				.filter(unit -> unit.isAtPlaceLocation())
				.filter(unit -> unit.getAvailableMoves() > 0)
				.findFirst();		
	}

	Optional<Unit> getFirstSelectableUnitAt(final Player currentPlayer, final Location location){
		return units.stream()
				.filter(unit -> currentPlayer.equals(unit.getOwner()))
				.filter(unit -> unit.isAtPlaceLocation())
				.filter(unit -> unit.getLocation().equals(location))
				.filter(unit -> unit.getAvailableMoves() > 0)
				.findFirst();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("hashcode", hashCode())
				.add("units", units.size())
				.toString();
	}	
	
}
