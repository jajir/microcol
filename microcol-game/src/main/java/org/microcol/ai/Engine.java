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
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Model model;
	private final Random random;
	private final Map<Unit, Location> lastDirections;

	private boolean running;

	public Engine(final Model model) {
		this.model = model;
		this.random = new Random();
		this.lastDirections = new HashMap<>();
	}

	public boolean isRunning() {
		return running;
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
		if (model.isGameRunning() && model.getCurrentPlayer().isComputer()) {
			turn(model.getCurrentPlayer());
		}
	}

	void turn(final Player player) {
		player.getUnits().forEach(unit -> move(unit));

		if (!running) {
			return;
		}

		player.endTurn();
	}

	void move(final Unit unit) {
		if (unit.getType() == UnitType.COLONIST) {
			unit.getOwner().getUnits().forEach(xxx -> {
				if (xxx.getType() == UnitType.GALLEON) {
					xxx.getHold().getSlots().get(0).store(unit);
				}
			});
		}
	}

	void move2(final Unit unit) {
		if (!running) {
			return;
		}

		if (lastDirections.get(unit) == null) {
			lastDirections.put(unit, Location.DIRECTIONS.get(random.nextInt(Location.DIRECTIONS.size())));
		}

		showDebug(unit);

		final List<Location> directions = new ArrayList<>(Location.DIRECTIONS);
		final List<Location> locations = new ArrayList<>();
		Location lastLocation = unit.getLocation();
		outerloop:
		while (locations.size() < unit.getAvailableMoves()) {
			if (unit.getType().canAttack()) {
				for (final Location location : lastLocation.getNeighbors()) {
					if (!unit.getOwner().getEnemyUnitsAt(location).isEmpty()) {
						break outerloop;
					}
				}
			}
			final Location lastDirection = lastDirections.get(unit);
			final Location newLocation = lastLocation.add(lastDirection);
			if (unit.isMoveable(newLocation)) {
				locations.add(newLocation);
				lastLocation = newLocation;
			} else {
				directions.remove(lastDirection);
				if (directions.isEmpty()) {
					break;
				}
				lastDirections.put(unit, directions.get(random.nextInt(directions.size())));
			}
		}

		if (!locations.isEmpty()) {
			unit.moveTo(Path.of(locations));
		}

		if (unit.getType().canAttack() && unit.getAvailableMoves() > 0) {
			for (final Location location : unit.getLocation().getNeighbors()) {
				final List<Unit> enemies = unit.getOwner().getEnemyUnitsAt(location);
				if (!enemies.isEmpty()) {
					unit.attack(enemies.get(0).getLocation());
					break;
				}
			}
		}
	}

	void showDebug(final Unit unit) {
		final List<Location> locations = new ArrayList<>();
		for (final Unit enemy : unit.getOwner().getEnemyUnits()) {
			locations.addAll(unit.getPath(enemy.getLocation(), true).orElse(Collections.emptyList()));
		}

		/*
		locations.addAll(unit.getAvailableLocations());
		for (final Unit enemy : unit.getAttackableTargets()) {
			locations.add(enemy.getLocation());
		}
		*/

		model.requestDebug(locations);
	}
}
