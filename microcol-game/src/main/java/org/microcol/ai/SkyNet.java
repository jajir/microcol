package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.ModelListener;
import org.microcol.model.PathBuilder;
import org.microcol.model.Player;
import org.microcol.model.Ship;
import org.microcol.model.Terrain;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.ShipMovedEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.collect.ImmutableList;

public class SkyNet {
	private static final ImmutableList<Location> directions = ImmutableList.of(
		new Location(1, 1),
		new Location(0, 1),
		new Location(-1, 1),
		new Location(-1, 0),
		new Location(-1, -1),
		new Location(0, -1),
		new Location(1, -1),
		new Location(1, 0)
	);

	private final Game game;
	private final Random random;
	private final Map<Ship, Location> lastDirections;

	private Map<Location, List<Ship>> enemyShipsAt;

	public SkyNet(final Game game) {
		this.game = game;
		this.random = new Random();
		this.lastDirections = new HashMap<>();
	}

	public void searchAndDestroy() {
		game.addListener(new ModelListener() {
			@Override
			public void gameStarted(GameStartedEvent event) {
				// Do nothing.
			}

			@Override
			public void roundStarted(RoundStartedEvent event) {
				// Do nothing.
			}

			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().isComputer()) {
					turn(event.getPlayer());
				}
			}

			@Override
			public void shipMoved(ShipMovedEvent event) {
				// Do nothing.
			}

			@Override
			public void gameFinished(GameFinishedEvent event) {
				// Do nothing.
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
		final PathBuilder pathBuilder = new PathBuilder();
		while (pathBuilder.getLength() < ship.getAvailableMoves()) {
			final Location lastDirection = lastDirections.get(ship);
			final Location newLocation = lastLocation.add(lastDirection);
			if (game.getMap().isValid(newLocation) && game.getMap().getTerrainAt(newLocation) == Terrain.OCEAN && !isEnemyShipAt(newLocation)) {
				pathBuilder.add(newLocation);
				lastLocation = newLocation;
			} else {
				directions.remove(lastDirection);
				if (directions.isEmpty()) {
					break;
				}
				lastDirections.put(ship, directions.get(random.nextInt(directions.size())));
			}
		}

		if (!pathBuilder.isEmpty()) {
			ship.moveTo(pathBuilder.build());
		}
	}

	private boolean isEnemyShipAt(final Location location) {
		List<Ship> ships = enemyShipsAt.get(location);

		return ships != null && ships.size() > 0;
	}
}
