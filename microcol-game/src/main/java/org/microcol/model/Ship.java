package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Ship {
	private Model model;

	private final ShipType type;
	private final Player owner;

	private Location location;
	private int availableMoves;

	Ship(final ShipType type, final Player owner, final Location location) {
		this.type = Preconditions.checkNotNull(type);
		this.owner = Preconditions.checkNotNull(owner);
		this.location = Preconditions.checkNotNull(location);
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);

		if (!isMoveable(location, true)) {
			throw new IllegalStateException(String.format("Invalid start location of ship (%s).", this));
		}
	}

	public ShipType getType() {
		return type;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		return location;
	}

	public int getAvailableMoves() {
		return availableMoves;
	}

	void startTurn() {
		availableMoves = type.getSpeed();
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
		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		if (availableMoves == 0) {
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
		model.checkGameRunning();
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

	public void attack(final Location location) {
		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(location);
		Preconditions.checkArgument(this.location.isAdjacent(location), "Ship (%s) is not adjacent to target location (%s).", this, location);
		Preconditions.checkState(availableMoves > 0, "Ship (%s) cannot attack this turn.", this);
		Preconditions.checkState(!owner.getEnemyShipsAt(location).isEmpty(), "There is not any enemy ship on target location (%s).", location);

		availableMoves = 0;

		final Ship defender = owner.getEnemyShipsAt(location).get(0);
		final Ship destroyed = Math.random() <= 0.6 ? defender : this;
		model.destroyShip(destroyed);
		if (this != destroyed && owner.getEnemyShipsAt(location).isEmpty()) {
			this.location = location;
		}
		model.fireShipAttacked(this, defender, destroyed);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", type)
			.add("owner", owner)
			.add("location", location)
			.add("availableMoves", availableMoves)
			.toString();
	}

	void save(final JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("type", type.name());
		generator.write("owner", owner.getName());
		location.save("location", generator);
		generator.write("availableMoves", availableMoves);
		generator.writeEnd();
	}

	static Ship load(final JsonParser parser, final List<Player> players) {
		// START_OBJECT or END_ARRAY
		if (parser.next() == JsonParser.Event.END_ARRAY) {
			return null;
		}
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final ShipType type = ShipType.valueOf(parser.getString());
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final String ownerName = parser.getString();
		final Player owner = players.stream()
			.filter(player -> player.getName().equals(ownerName))
			.findAny()
			.orElse(null);
		parser.next(); // KEY_NAME
		final Location location = Location.load(parser);
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_NUMBER
		final int availableMoves = parser.getInt();
		parser.next(); // END_OBJECT

		final Ship ship = new Ship(type, owner, location);
		ship.availableMoves = availableMoves;

		return ship;
	}
}
