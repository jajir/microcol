package org.microcol.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Game;
import org.microcol.model.Location;
import org.microcol.model.ModelListener;
import org.microcol.model.PathBuilder;
import org.microcol.model.Ship;
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
					move();
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

	private void move() {
		game.getShips().forEach(ship -> {
			if (ship.getOwner().isComputer()) {
				move(ship);
			}
		});
	}

	private void move(final Ship ship) {
		if (lastDirections.get(ship) == null) {
			lastDirections.put(ship, directions.get(random.nextInt(8)));
		}

		Location lastLocation = ship.getLocation();
		final PathBuilder pathBuilder = new PathBuilder();
		while (pathBuilder.getLength() < ship.getAvailableMoves()) {
			final Location lastDirection = lastDirections.get(ship);
			final Location newLocation = new Location(lastLocation.getX() + lastDirection.getX(),
				lastLocation.getY() + lastDirection.getY());
			if (game.getMap().isValid(newLocation)) {
				pathBuilder.add(newLocation);
				lastLocation = newLocation;
			} else {
				lastDirections.put(ship, directions.get(random.nextInt(8)));
			}
		}

		ship.moveTo(pathBuilder.build());
	}
}
