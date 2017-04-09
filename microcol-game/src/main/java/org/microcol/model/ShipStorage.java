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

class ShipStorage {
	private final List<Ship> ships;

	ShipStorage(final List<Ship> ships) {
		this.ships = new ArrayList<>(ships);
		checkShipLocations(this.ships);
	}

	private void checkShipLocations(final List<Ship> ships) {
		Map<Location, Player> owners = new HashMap<>();
		ships.forEach(ship -> {
			Player owner = owners.get(ship.getLocation());
			if (owner != null) {
				if (!owner.equals(ship.getOwner())) {
					throw new IllegalArgumentException(String.format("There is an enemy ship at the same location (%s).", ship.getLocation()));
				}
			} else {
				owners.put(ship.getLocation(), ship.getOwner());
			}
		});
	}

	List<Ship> getShips() {
		return ImmutableList.copyOf(ships);
	}

	Map<Location, List<Ship>> getShipsAt() {
		return Multimaps.asMap(ships.stream()
			.collect(ImmutableListMultimap.toImmutableListMultimap(Ship::getLocation, Function.identity())));
	}

	List<Ship> getShipsAt(final Location location) {
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> ship.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	List<Ship> getShips(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> ship.getOwner().equals(player))
			.collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Ship>> getShipsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(ships.stream()
			.filter(ship -> ship.getOwner().equals(player))
			.collect(ImmutableListMultimap.toImmutableListMultimap(Ship::getLocation, Function.identity())));
	}

	List<Ship> getShipsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> ship.getOwner().equals(player) && ship.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	List<Ship> getEnemyShips(final Player player) {
		Preconditions.checkNotNull(player);

		return ships.stream()
			.filter(ship -> !ship.getOwner().equals(player))
			.collect(ImmutableList.toImmutableList());
	}

	Map<Location, List<Ship>> getEnemyShipsAt(final Player player) {
		Preconditions.checkNotNull(player);

		return Multimaps.asMap(ships.stream()
			.filter(ship -> !ship.getOwner().equals(player))
			.collect(ImmutableListMultimap.toImmutableListMultimap(Ship::getLocation, Function.identity())));
	}

	List<Ship> getEnemyShipsAt(final Player player, final Location location) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(location);

		return ships.stream()
			.filter(ship -> !ship.getOwner().equals(player) && ship.getLocation().equals(location))
			.collect(ImmutableList.toImmutableList());
	}

	void remove(final Ship ship) {
		Preconditions.checkNotNull(ship);

		ships.remove(ship);
	}

	void save(final JsonGenerator generator) {
		generator.writeStartArray("ships");
		ships.forEach(ship -> ship.save(generator));
		generator.writeEnd();
	}

	static List<Ship> load(final JsonParser parser, final List<Player> players) {
		parser.next();  // START_ARRAY
		final List<Ship> ships = new ArrayList<>();
		Ship ship = null;
		while ((ship = Ship.load(parser, players)) != null) {
			ships.add(ship);
		}

		return ships;
	}
}
