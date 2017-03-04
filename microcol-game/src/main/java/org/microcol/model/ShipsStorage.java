package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

class ShipsStorage {
	private final List<Ship> ships;

	public ShipsStorage(final List<Ship> ships) {
		this.ships = ImmutableList.copyOf(ships);
	}

	public List<Ship> getShips() {
		return ships;
	}

	public List<Ship> getShipsAt(final Location location) {
		return ships.stream()
			.filter(ship -> {
				return ship.getLocation().equals(location);
			})
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	public List<Ship> getShips(final Player player) {
		return ships.stream()
			.filter(ship -> {
				return ship.getOwner().equals(player);
			})
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	public List<Ship> getShipsAt(final Player player, final Location location) {
		return ships.stream()
			.filter(ship -> {
				return ship.getOwner().equals(player)
					&& ship.getLocation().equals(location);
			})
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}

	public List<Ship> getEnemyShips(final Player player) {
		return ships.stream()
			.filter(ship -> {
				return !ship.getOwner().equals(player);
			})
			.collect(Collectors.collectingAndThen(
				Collectors.toList(), ImmutableList::copyOf));
	}
}
