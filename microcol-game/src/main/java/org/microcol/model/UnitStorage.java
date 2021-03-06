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
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitCreateRequest;
import org.microcol.model.unit.UnitFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

public final class UnitStorage {

    /**
     * Ordered list of all units.
     */
    private final List<Unit> units;

    private final IdManager idManager;

    private final UnitFactory unitFactory;

    UnitStorage(final IdManager idManager) {
        this.idManager = Preconditions.checkNotNull(idManager);
        this.units = new ArrayList<>();
        this.unitFactory = new UnitFactory();
    }

    void addUnit(final Unit unit) {
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(!units.contains(unit), "unit %s was already added.", unit);
        Preconditions.checkArgument(!tryGetUnitById(unit.getId()).isPresent(),
                "Unit with id %s was already added.", unit, unit.getId());
        units.add(unit);
        checkUnitLocations(units);
        checkDuplicities(units);
    }

    Unit createUnit(final Function<Unit, Cargo> cargoBuilder, final Model model,
            final Function<Unit, Place> placeBuilder, final UnitType unitType, final Player owner,
            final int availableMoves, final UnitAction unitAction) {

        final UnitCreateRequest unitCreateRequest = new UnitCreateRequest(model, placeBuilder,
                idManager.nextId(), unitType, owner, unitAction, availableMoves);
        unitCreateRequest.setCargoBuilder(cargoBuilder);
        
        final Unit unit = unitFactory.createUnit(unitCreateRequest);
        units.add(unit);
        return unit;
    }

    private void checkUnitLocations(final List<Unit> units) {
        Map<Location, Player> owners = new HashMap<>();
        units.forEach(unit -> {
            if (unit.isAtPlaceLocation()) {
                Player owner = owners.get(unit.getLocation());
                if (owner != null) {
                    if (!owner.equals(unit.getOwner())) {
                        throw new IllegalArgumentException(
                                String.format("There is an enemy unit at the same location (%s).",
                                        unit.getLocation()));
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
                .collect(ImmutableListMultimap.toImmutableListMultimap(Unit::getLocation,
                        Function.identity())));
    }

    List<Unit> getUnitsAt(final Location location) {
        Preconditions.checkNotNull(location);

        return units.stream().filter(unit -> unit.isAtPlaceLocation())
                .filter(unit -> unit.getLocation().equals(location))
                .collect(ImmutableList.toImmutableList());
    }

    List<Unit> getUnitsOwnedBy(final Player player, final boolean includeStored) {
        Preconditions.checkNotNull(player);

        return units.stream().filter(unit -> unit.getOwner().equals(player))
                .filter(unit -> includeStored || unit.isAtPlaceLocation())
                .collect(ImmutableList.toImmutableList());
    }

    Map<Location, List<Unit>> getUnitsAt(final Player player) {
        Preconditions.checkNotNull(player);

        return Multimaps.asMap(units.stream().filter(unit -> unit.getOwner().equals(player))
                .filter(unit -> unit.isAtPlaceLocation()).collect(ImmutableListMultimap
                        .toImmutableListMultimap(Unit::getLocation, Function.identity())));
    }

    List<Unit> getUnitsAt(final Player player, final Location location) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(location);

        return units.stream().filter(unit -> unit.getOwner().equals(player))
                .filter(unit -> unit.isAtPlaceLocation())
                .filter(unit -> unit.getLocation().equals(location))
                .collect(ImmutableList.toImmutableList());
    }

    List<Unit> getEnemyUnits(final Player player, final boolean includeStored) {
        Preconditions.checkNotNull(player);

        return units.stream().filter(unit -> !unit.getOwner().equals(player))
                .filter(unit -> includeStored || unit.isAtPlaceLocation())
                .collect(ImmutableList.toImmutableList());
    }

    Map<Location, List<Unit>> getEnemyUnitsAt(final Player player) {
        Preconditions.checkNotNull(player);

        return Multimaps.asMap(units.stream().filter(unit -> !unit.getOwner().equals(player))
                .filter(unit -> unit.isAtPlaceLocation()).collect(ImmutableListMultimap
                        .toImmutableListMultimap(Unit::getLocation, Function.identity())));
    }

    List<Unit> getEnemyUnitsAt(final Player player, final Location location) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(location);

        return units.stream().filter(unit -> !unit.getOwner().equals(player))
                .filter(unit -> unit.isAtPlaceLocation())
                .filter(unit -> unit.getLocation().equals(location))
                .collect(ImmutableList.toImmutableList());
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
        return tryGetUnitById(id).orElseThrow(
                () -> new IllegalArgumentException("There is no unit with id '" + id + "'."));
    }

    Optional<Unit> tryGetUnitById(final int id) {
        return units.stream().filter(unit -> unit.getId() == id).findAny();
    }

    Optional<Unit> getNextUnitForPlayer(final Player currentPlayer, final Unit currentUnit) {
        final List<Unit> list = units.stream().filter(unit -> currentPlayer.equals(unit.getOwner()))
                .filter(unit -> unit.isAtPlaceLocation() && unit.getActionPoints() > 0)
                .collect(Collectors.toList());
        if (list.size() == 0) {
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

    /**
     * Find first unit that could be selected.
     *
     * @param currentPlayer
     *            required player for whom will be unit selected
     * @return Optional object with unit
     */
    Optional<Unit> getFirstSelectableUnit(final Player currentPlayer) {
        Preconditions.checkNotNull(currentPlayer, "Player is null");

        return units.stream().filter(unit -> currentPlayer.equals(unit.getOwner()))
                .filter(unit -> unit.isAtPlaceLocation()).filter(unit -> unit.getActionPoints() > 0)
                .findFirst();
    }

    /**
     * Find first unit that could be selected. If there is unit that could move
     * than such unit is returned. If there is not unit that could move than
     * first unit is returned.
     *
     * @param currentPlayer
     *            required player for whom will be unit selected
     * @param location
     *            required location where should be selected unit
     * @return Optional object with unit
     */
    Optional<Unit> getFirstSelectableUnitAt(final Player currentPlayer, final Location location) {
        Preconditions.checkNotNull(currentPlayer, "Player is null");
        Preconditions.checkNotNull(location, "Location is null");

        final List<Unit> playersUnitAtLocation = units.stream()
                .filter(unit -> currentPlayer.equals(unit.getOwner()))
                .filter(unit -> unit.isAtPlaceLocation())
                .filter(unit -> unit.getLocation().equals(location)).collect(Collectors.toList());

        final Optional<Unit> moveableUnit = playersUnitAtLocation.stream()
                .filter(unit -> unit.getActionPoints() > 0).findFirst();
        if (moveableUnit.isPresent()) {
            return moveableUnit;
        } else {
            return playersUnitAtLocation.stream().findFirst();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .add("units", units.size()).toString();
    }

}
