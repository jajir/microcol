package org.microcol.ai;

import java.util.ArrayList;
import java.util.Collections;
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
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Model model;
	private final Random random;
	private final Map<Ship, Location> lastDirections;

	private boolean running;

	public Engine(final Model model) {
		this.model = model;
		this.random = new Random();
		this.lastDirections = new HashMap<>();
	}

	public void start() {
		running = true;
		model.addListener(new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().isComputer()) {
					turn(event.getPlayer());
				}
			}
		});
		logger.info("AI engine started.");
	}

	public void suspend() {
		running = false;
	}

	public void resume() {
		running = true;
		// TODO JKA Check if game is running.
		if (model.getCurrentPlayer().isComputer()) {
			turn(model.getCurrentPlayer());
		}
	}

	void turn(final Player player) {
		player.getShips().forEach(ship -> move(ship));

		if (!running) {
			return;
		}

		player.endTurn();
	}

	void move(final Ship ship) {
		if (!running) {
			return;
		}

		if (lastDirections.get(ship) == null) {
			lastDirections.put(ship, Location.DIRECTIONS.get(random.nextInt(Location.DIRECTIONS.size())));
		}

		showDebug(ship);

		final List<Location> directions = new ArrayList<>(Location.DIRECTIONS);
		final List<Location> locations = new ArrayList<>();
		Location lastLocation = ship.getLocation();
		outerloop:
		while (locations.size() < ship.getAvailableMoves()) {
			for (final Location location : lastLocation.getNeighbors()) {
				if (!ship.getOwner().getEnemyShipsAt(location).isEmpty()) {
					break outerloop;
				}
			}
			final Location lastDirection = lastDirections.get(ship);
			final Location newLocation = lastLocation.add(lastDirection);
			if (ship.isMoveable(newLocation)) {
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

		if (ship.getAvailableMoves() > 0) {
			for (final Location location : ship.getLocation().getNeighbors()) {
				final List<Ship> enemies = ship.getOwner().getEnemyShipsAt(location);
				if (!enemies.isEmpty()) {
					ship.attack(enemies.get(0).getLocation());
					break;
				}
			}
		}
	}

	void showDebug(final Ship ship) {
		final List<Location> locations = new ArrayList<>();
		for (final Ship enemy : ship.getOwner().getEnemyShips()) {
			locations.addAll(ship.getPath(enemy.getLocation(), true).orElse(Collections.emptyList()));
		}
		/*
		locations.addAll(ship.getAvailableLocations());
		for (final Ship enemy : ship.getAttackableTargets()) {
			locations.add(enemy.getLocation());
		}
		*/

		model.requestDebug(locations);
	}
}
