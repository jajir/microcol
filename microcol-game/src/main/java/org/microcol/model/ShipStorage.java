package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// TODO JKA groupingBy -> ImmutableList::copyOf, Guava verze 21 ma svoje kolektory
class ShipStorage {
	private final List<Ship> ships;

	ShipStorage(final List<Ship> ships) {
		this.ships = ImmutableList.copyOf(ships);
	}

	List<Ship> getShips() {
		return ships;
	}

	Map<Location, List<Ship>> getShipsAt() {
		return ships.stream()
			.collect(Collectors.collectingAndThen(
				Collectors.groupingBy(Ship::getLocation), ImmutableMap::copyOf));
	}

	List<Ship> getShipsAt(final Location location) {
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> ship.getLocation().equals(location))
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	List<Ship> getShips(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> ship.getOwner().equals(player))
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	Map<Location, List<Ship>> getShipsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> ship.getOwner().equals(player))
			.collect(Collectors.collectingAndThen(
				Collectors.groupingBy(Ship::getLocation), ImmutableMap::copyOf));
	}

	List<Ship> getShipsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> ship.getOwner().equals(player) && ship.getLocation().equals(location))
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	List<Ship> getEnemyShips(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> !ship.getOwner().equals(player))
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	Map<Location, List<Ship>> getEnemyShipsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> !ship.getOwner().equals(player))
			.collect(Collectors.collectingAndThen(
				Collectors.groupingBy(Ship::getLocation), ImmutableMap::copyOf));
	}

	List<Ship> getEnemyShipsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> !ship.getOwner().equals(player) && ship.getLocation().equals(location))
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}
}
