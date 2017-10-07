package org.microcol.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Model model;
	private final Directions unitDirections;

	private boolean running;

	public Engine(final Model model) {
		this.model = model;
		unitDirections = new Directions();
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
		player.getUnits().stream().filter(unit->unit.isAtPlaceLocation()).forEach(unit -> move(unit));

		if (!running) {
			return;
		}

		player.endTurn();
	}

	void move(final Unit unit) {
		if (!running) {
			return;
		}

		showDebug(unit);

		final List<Location> locations = computeMoveLocation(unit);

		if (!locations.isEmpty()) {
			unit.moveTo(Path.of(locations));
		}

		tryToFight(unit);
		tryToEmbark(unit);
	}
	
	private List<Location> computeMoveLocation(final Unit unit){
		final List<Location> locations = new ArrayList<>();
		Location lastLocation = unit.getLocation();
		while (locations.size() < unit.getAvailableMoves()) {
			if(isPossibleToAttack(unit, lastLocation)){
				return locations;				
			}
			final Location newLocation = lastLocation.add(unitDirections.getLastDirection(unit));
			if (canMoveAt(unit, newLocation)) {
				locations.add(newLocation);
				lastLocation = newLocation;
			} else {
				unitDirections.resetDirection(unit);
			}
		}
		return locations;
	}

	/**
	 * Prevent move unit to wrong place.
	 * <p>
	 * Ships are blocked to go to high-seas
	 * </p>
	 * .
	 * 
	 * @param unit
	 *            required unit
	 * @param location
	 *            required location
	 * @return return <code>true</code> when unit can move to given location
	 */
	private boolean canMoveAt(final Unit unit, final Location location) {
		return unit.isMoveable(location) && !model.getMap().getTerrainTypeAt(location).equals(TerrainType.HIGH_SEA);
	}
	
	private boolean isPossibleToAttack(final Unit unit, final Location lastLocation){
		if (unit.getType().canAttack()) {
			for (final Location location : lastLocation.getNeighbors()) {
				if (unit.isPossibleToAttackAt(location)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void tryToFight(final Unit unit){
		if (unit.getType().canAttack() && unit.getAvailableMoves() > 0 && unit.isAtPlaceLocation()) {
			for (final Location location : unit.getLocation().getNeighbors()) {
				if(unit.isPossibleToAttackAt(location)){
					final List<Unit> enemies = unit.getOwner().getEnemyUnitsAt(location);
					if (!enemies.isEmpty()) {
						unit.attack(enemies.get(0).getLocation());
						break;
					}
				}
			}
		}		
	}
	
	private void tryToEmbark(final Unit unit){
		if (unit.isStorable() && unit.getAvailableMoves() > 0 && unit.isAtPlaceLocation()) {
			unit.getStorageUnits().stream()
				.flatMap(u -> u.getCargo().getSlots().stream())
				.filter(slot -> slot.isEmpty())
				.findAny()
				.get().store(unit);
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
