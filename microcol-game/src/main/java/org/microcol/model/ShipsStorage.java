package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// TODO JKA Optimalizovat
// TODO JKA Documentation
// TODO JKA Tests
class ShipsStorage {
	private final List<Ship> ships;

	public ShipsStorage(final List<Ship> ships) {
		this.ships = ImmutableList.copyOf(ships);
	}

	public List<Ship> getShips() {
		return ships;
	}

	public Map<Location, List<Ship>> getShipsAt() {
		Map<Location, List<Ship>> shipsAt = new HashMap<>();

		// TODO JKA Use stream and immutable
		ships.forEach(ship -> {
			List<Ship> ships = shipsAt.get(ship.getLocation());
			if (ships == null) {
				ships = new ArrayList<>();
				shipsAt.put(ship.getLocation(), ships);
			}
			ships.add(ship);
		});

		return ImmutableMap.copyOf(shipsAt);
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

	public Map<Location, List<Ship>> getEnemyShipsAt(final Player player) {
		Map<Location, List<Ship>> enemyShipsAt = new HashMap<>();

		// TODO JKA Use stream and immutable
		ships.forEach(ship -> {
			if (!ship.getOwner().equals(player)) {
				List<Ship> ships = enemyShipsAt.get(ship.getLocation());
				if (ships == null) {
					ships = new ArrayList<>();
					enemyShipsAt.put(ship.getLocation(), ships);
				}
				ships.add(ship);
			}
		});

		return ImmutableMap.copyOf(enemyShipsAt);
	}
}
