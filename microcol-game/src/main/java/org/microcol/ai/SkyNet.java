package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Ship;
import org.microcol.model.Terrain;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.ImmutableList;

public class SkyNet {
	private static final ImmutableList<Location> directions = ImmutableList.of(
		Location.of(1, 1),
		Location.of(0, 1),
		Location.of(-1, 1),
		Location.of(-1, 0),
		Location.of(-1, -1),
		Location.of(0, -1),
		Location.of(1, -1),
		Location.of(1, 0)
	);

	private final Model model;
	private final Random random;
	private final Map<Ship, Location> lastDirections;

	private Map<Location, List<Ship>> enemyShipsAt;

	public SkyNet(final Model model) {
		this.model = model;
		this.random = new Random();
		this.lastDirections = new HashMap<>();
	}

	public void searchAndDestroy() {
		model.addListener(new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().isComputer()) {
					turn(event.getPlayer());
				}
			}
		});
	}

	private void turn(final Player player) {
		enemyShipsAt = player.getEnemyShipsAt();

		player.getShips().forEach(ship -> {
			move(ship);
		});
		player.endTurn();
	}

	private void move(final Ship ship) {
		if (lastDirections.get(ship) == null) {
			lastDirections.put(ship, directions.get(random.nextInt(directions.size())));
		}

		final List<Location> directions = new ArrayList<>(SkyNet.directions);
		Location lastLocation = ship.getLocation();
		final List<Location> locations = new ArrayList<>();
		while (locations.size() < ship.getAvailableMoves()) {
			final Location lastDirection = lastDirections.get(ship);
			final Location newLocation = lastLocation.add(lastDirection);
			// TODO JKA Tohle je spatne XXX == Terrain.OCEAN - dat na Ship
			if (model.getWorld().isValid(newLocation) && model.getWorld().getTerrainAt(newLocation) == Terrain.OCEAN && !isEnemyShipAt(newLocation)) {
				locations.add(newLocation);
				lastLocation = newLocation;
			} else {
				directions.remove(lastDirection);
				if (directions.isEmpty()) {
					break;
				}
				lastDirections.put(ship, directions.get(random.nextInt(directions.size())));
			}
		}

		if (!locations.isEmpty()) {
			ship.moveTo(Path.of(locations));
		}
	}

	private boolean isEnemyShipAt(final Location location) {
		List<Ship> ships = enemyShipsAt.get(location);

		return ships != null && ships.size() > 0;
	}
}
