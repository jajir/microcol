package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Ship {
	private Model model;

	private final Player owner;
	private final ShipType type;

	private Location location;
	private int availableMoves;
	private boolean canAttack;

	Ship(final Player owner, final ShipType type, final Location location) {
		this.owner = Preconditions.checkNotNull(owner);
		this.type = Preconditions.checkNotNull(type);
		this.location = Preconditions.checkNotNull(location);
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);

		final Terrain terrain = this.model.getMap().getTerrainAt(location);
		if (terrain != Terrain.OCEAN) {
			throw new IllegalStateException(String.format("Ship must start on ocen (%s -> %s).", location, terrain));
		}
	}

	public Player getOwner() {
		return owner;
	}

	public ShipType getType() {
		return type;
	}

	public Location getLocation() {
		return location;
	}

	// TODO JKA package access?
	public int getAvailableMoves() {
		return availableMoves;
	}

	void startTurn() {
		availableMoves = type.getSpeed();
		canAttack = true;
	}

	// Netestuje nedosažitelné lokace, pouze jestli je teoreticky možné na danou lokaci vstoupit
	public boolean isMoveable(final Location location) {
		return isMoveable(location, false);
	}

	public boolean isMoveable(final Location location, final boolean ignoreEnemies) {
		Preconditions.checkNotNull(location);

		if (!model.getMap().isValid(location)) {
			return false;
		}

		if (model.getMap().getTerrainAt(location) != Terrain.OCEAN) {
			return false;
		}

		return ignoreEnemies || owner.getEnemyShipsAt(location).isEmpty();
	}

	public List<Location> getAvailableLocations() {
		List<Location> locations = new ArrayList<>();
		aaa(locations, null);

		return ImmutableList.copyOf(locations);
	}

	public List<Ship> getAttackableTargets() {
		List<Ship> enemies = new ArrayList<>();
		aaa(null, enemies);

		return ImmutableList.copyOf(enemies);
	}

	private void aaa(final List<Location> availableLocations, final List<Ship> attackableTargets) {
		model.checkGameActive();
		model.checkCurrentPlayer(owner);

		if (availableMoves == 0 || !canAttack) {
			return;
		}

		Set<Location> openSet = new HashSet<>();
		openSet.add(location);
		Set<Location> closedSet = new HashSet<>();
		Set<Ship> enemies = new HashSet<>();
		for (int i = 0; i < availableMoves + 1; i++) {
			Set<Location> currentSet = new HashSet<>();
			for (Location location : openSet) {
				for (Location neighbor : location.getNeighbors()) {
					if (isMoveable(neighbor, true)) {
						final List<Ship> eee = owner.getEnemyShipsAt(location);
						if (eee.isEmpty()) {
							currentSet.add(neighbor);
						} else {
							enemies.addAll(eee);
						}
					}
				}
				closedSet.add(location);
			}
			openSet.clear();
			openSet.addAll(currentSet);
		}
		closedSet.remove(location);

		if (availableLocations != null) {
			availableLocations.addAll(closedSet);
		}
		if (attackableTargets != null) {
			attackableTargets.addAll(enemies);
		}
	}

	public Optional<List<Location>> getPath(final Location destination) {
		return getPath(destination, false);
	}

	public Optional<List<Location>> getPath(final Location destination, final boolean excludeDestination) {
		PathFinder finder = new PathFinder(this, location, destination, excludeDestination);

		return Optional.ofNullable(finder.find());
	}

	public void moveTo(final Path path) {
		model.checkGameActive();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getStart().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getStart(), location);
		Preconditions.checkArgument(model.getMap().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyShipsAt().keySet()), "There is enemy ship on path (%s).", path);

		final Location start = location;
		final List<Location> locations = new ArrayList<>();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				// TODO JKA neumožnit zadat delší cestu, než je povolený počet
				break;
			}
			if (model.getMap().getTerrainAt(newLocation) != Terrain.OCEAN) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only ocean (%s).", newLocation, model.getMap().getTerrainAt(newLocation)));
			}
			locations.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!locations.isEmpty()) {
			model.fireShipMoved(this, start, Path.of(locations));
		}
	}

	// TODO JKA canAttack

	public void attack(final Ship ship) {
		model.checkGameActive();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(ship);
		Preconditions.checkArgument(!owner.equals(ship.owner), "Cannot attack own ship (%s - %s).", this, ship);
		Preconditions.checkArgument(location.isAdjacent(ship.location), "Ship is not adjacent (%s - %s)", this, ship);
		Preconditions.checkArgument(canAttack, "This ship (%s) already fight this turn.", this);

		availableMoves = 0;
		canAttack = false;

		final Ship destroyed = Math.random() <= 0.6 ? ship : this;
		model.destroyShip(destroyed);
		model.fireShipAttacked(this, ship, destroyed);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("owner", owner)
			.add("type", type)
			.add("location", location)
			.add("availableMoves", availableMoves)
			.add("canAttack", canAttack)
			.toString();
	}
}
