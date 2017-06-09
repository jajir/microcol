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

public class Unit {
	private Model model;

	private final UnitType type;
	private final Player owner;

	private Location location;
	private int availableMoves;
	private final CargoHold hold;
	private CargoSlot slot; // TODO JKA RENAME

	Unit(final UnitType type, final Player owner, final Location location) {
		this.type = Preconditions.checkNotNull(type);
		this.owner = Preconditions.checkNotNull(owner);
		this.location = Preconditions.checkNotNull(location);
		this.hold = new CargoHold(this, type.getCargoCapacity());
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);

		if (!isMoveable(location, true)) {
			throw new IllegalStateException(String.format("Invalid start location of unit (%s).", this));
		}
	}

	public UnitType getType() {
		return type;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		checkNotStored();

		return location;
	}

	public int getAvailableMoves() {
		checkNotStored();

		return availableMoves;
	}

	public CargoHold getHold() {
		return hold;
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

		if (model.getMap().getTerrainAt(location) != type.getMoveableTerrain()) {
			return false;
		}

		return ignoreEnemies || owner.getEnemyUnitsAt(location).isEmpty();
	}

	public List<Location> getAvailableLocations() {
		// TODO JKA IMPLEMENTATION FOR STORED UNIT?

		List<Location> locations = new ArrayList<>();
		aaa(locations, null);

		return ImmutableList.copyOf(locations);
	}

	// TODO JKA VRACET LOCATION?
	public List<Unit> getAttackableTargets() {
		checkNotStored();

		if (!type.canAttack()) {
			return ImmutableList.of();
		}

		List<Unit> enemies = new ArrayList<>();
		aaa(null, enemies);

		return ImmutableList.copyOf(enemies);
	}

	private void aaa(final List<Location> availableLocations, final List<Unit> attackableTargets) {
		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		if (availableMoves == 0) {
			return;
		}

		Set<Location> openSet = new HashSet<>();
		openSet.add(location);
		Set<Location> closedSet = new HashSet<>();
		Set<Unit> enemies = new HashSet<>();
		for (int i = 0; i < availableMoves + 1; i++) {
			Set<Location> currentSet = new HashSet<>();
			for (Location location : openSet) {
				for (Location neighbor : location.getNeighbors()) {
					if (isMoveable(neighbor, true)) {
						final List<Unit> eee = owner.getEnemyUnitsAt(location);
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

	public List<Unit> getStorageUnits() {
		checkNotStored();

		return location.getNeighbors().stream()
			.flatMap(neighbor -> owner.getUnitsAt(neighbor).stream())
			.filter(unit -> unit != this)
			.filter(unit -> unit.getHold().getSlots().stream()
				.filter(slot -> slot.isEmpty())
				.findAny().isPresent())
			.collect(ImmutableList.toImmutableList());
		/*
		final ImmutableList.Builder<Unit> builder = ImmutableList.builder();

		// TODO JKA Predelat
		location.getNeighbors().forEach(neighbor -> {
			owner.getUnitsAt(neighbor).forEach(unit -> {
				if (unit != this) {
					unit.getHold().getSlots().forEach(slot -> {
						if (slot.isEmpty()) {
							builder.add(unit);
						}
					});
				}
			});
		});

		return builder.build();
		*/
	}

	public Optional<List<Location>> getPath(final Location destination) {
		checkNotStored();

		return getPath(destination, false);
	}

	public Optional<List<Location>> getPath(final Location destination, final boolean excludeDestination) {
		checkNotStored();

		PathFinder finder = new PathFinder(this, location, destination, excludeDestination);

		return Optional.ofNullable(finder.find());
	}

	public void moveTo(final Path path) {
		checkNotStored();

		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getStart().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getStart(), location);
		Preconditions.checkArgument(model.getMap().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyUnitsAt().keySet()), "There is enemy unit on path (%s).", path);

		final Location start = location;
		final List<Location> locations = new ArrayList<>();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				// TODO JKA neumožnit zadat delší cestu, než je povolený počet
				break;
			}
			if (model.getMap().getTerrainAt(newLocation) != type.getMoveableTerrain()) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only moveable terrain (%s).", newLocation, model.getMap().getTerrainAt(newLocation)));
			}
			locations.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!locations.isEmpty()) {
			model.fireUnitMoved(this, start, Path.of(locations));
		}
	}

	public void attack(final Location location) {
		checkNotStored();

		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		Preconditions.checkState(type.canAttack(), "This unit type (%s) cannot attack.", this);
		Preconditions.checkNotNull(location);
		Preconditions.checkArgument(model.getMap().getTerrainAt(location) == type.getMoveableTerrain(), "Target location (%s) is not moveable for this unit (%s)", location, this);
		Preconditions.checkArgument(this.location.isAdjacent(location), "Unit location (%s) is not adjacent to target location (%s).", this.location, location);
		Preconditions.checkState(availableMoves > 0, "Unit (%s) cannot attack this turn.", this);
		Preconditions.checkState(!owner.getEnemyUnitsAt(location).isEmpty(), "There is not any enemy unit on target location (%s).", location);

		availableMoves = 0;

		final Unit defender = owner.getEnemyUnitsAt(location).get(0);
		final Unit destroyed = Math.random() <= 0.6 ? defender : this;
		model.destroyUnit(destroyed);
		if (this != destroyed && owner.getEnemyUnitsAt(location).isEmpty()) {
			this.location = location;
		}
		model.fireUnitAttacked(this, defender, destroyed);
	}

	public boolean isStorable() {
		return type.isStorable();
	}

	public boolean isStored() {
		return slot != null;
	}

	void store(final CargoSlot slot) {
		Preconditions.checkState(isStorable(), "This unit (%s) cannot be stored.", this);
		checkNotStored();
		// TODO JKA check adjacent location
		// TODO JKA check movement?
		// TODO JKA prazdny naklad?

		this.slot = slot;

		model.fireUnitStored(slot, this); // TODO JKA Move to CargoSlot?
	}

	void unload(final Location location) {
		// TODO JKA check adjacent location
		// TODO JKA run "standard" unit location checks

		// TODO JKA empty all moves and attacks?

		this.location = location;
		this.slot = null;
	}

	void checkNotStored() {
		if (isStored()) {
			throw new IllegalStateException(String.format("This unit (%s) is stored in (%s).", this, slot));
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", type)
			.add("owner", owner)
			.add("location", location)
			.add("availableMoves", availableMoves)
			.add("hold", hold)
			// TODO JKA SLOT
			.toString();
	}

	void save(final JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("type", type.name());
		generator.write("owner", owner.getName());
		location.save("location", generator);
		generator.write("availableMoves", availableMoves);
		// TODO JKA Implement save/load
		generator.writeEnd();
	}

	static Unit load(final JsonParser parser, final List<Player> players) {
		// START_OBJECT or END_ARRAY
		if (parser.next() == JsonParser.Event.END_ARRAY) {
			return null;
		}
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final UnitType type = UnitType.valueOf(parser.getString());
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

		// TODO JKA Implement save/load

		final Unit unit = new Unit(type, owner, location);
		unit.availableMoves = availableMoves;

		return unit;
	}
}
