package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Unit {

	private final int id;
	private Model model;
	private final UnitType type;
	private final Player owner;
	private Place place;
	private int availableMoves;
	private final CargoHold hold;

	Unit(final UnitType type, final Player owner, final Location location) {
		this.type = Preconditions.checkNotNull(type);
		this.owner = Preconditions.checkNotNull(owner);
		this.place = new PlaceLocation(this, Preconditions.checkNotNull(location));
		this.hold = new CargoHold(this, type.getCargoCapacity());
		this.id = IdManager.nextId();
	}

	Unit(final UnitType type, final Player owner, final PlaceBuilder placeBuilder) {
		this.type = Preconditions.checkNotNull(type);
		this.owner = Preconditions.checkNotNull(owner);
		this.place = Preconditions.checkNotNull(placeBuilder.build(this));
		this.hold = new CargoHold(this, type.getCargoCapacity());
		this.id = IdManager.nextId();
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	public UnitType getType() {
		return type;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		Preconditions.checkArgument(place instanceof PlaceLocation, "Unit (%s) is not at map. ", this);
		return ((PlaceLocation) place).getLocation();
	}

	public int getAvailableMoves() {
		checkNotStored();

		return availableMoves;
	}

	public CargoHold getHold() {
		return hold;
	}
	
	/**
	 * It's called before turn starts.
	 */
	void startTurn() {
		availableMoves = type.getSpeed();
		if (isInHighSea()) {
			PlaceHighSea placeHighSea = (PlaceHighSea) place;
			placeHighSea.decreaseRemainingTurns();
			if (placeHighSea.getRemainigTurns() <= 0) {
				if(placeHighSea.isTravelToEurope()){
					model.getEurope().getPort().placeShipToPort(this);
				}else{
					//XXX ships always come from east side of map
					final List<Location> locations = model.getHighSea().getSuitablePlaceForShipCommingFromEurope(getOwner(), true);
					//TODO random should be class instance
					final Random random = new Random();
					final Location location = locations.get(random.nextInt(locations.size()));
					placeToLocation(location);
				}
			}
		}
	}

	// Netestuje nedosažitelné lokace, pouze jestli je teoreticky možné na danou
	// lokaci vstoupit
	public boolean isMoveable(final Location location) {
		return isMoveable(location, false);
	}

	/**
	 * Get info if unit could move to target location. Method doesn't verify if
	 * target location could be reached in current move.
	 * 
	 * @param location
	 *            required target location
	 * @param ignoreEnemies
	 *            when it's <code>true</code> than enemy unit at target location
	 *            will be ignored
	 * @return return <code>true</code> when unit could move to target location
	 *         otherwise return <code>false</code>
	 */
	public boolean isMoveable(final Location location, final boolean ignoreEnemies) {
		Preconditions.checkNotNull(location);

		if (!model.getMap().isValid(location)) {
			return false;
		}

		if (!type.canMoveAtTerrain(model.getMap().getTerrainAt(location))) {
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
		openSet.add(getLocation());
		Set<Location> closedSet = new HashSet<>();
		Set<Unit> enemies = new HashSet<>();
		for (int i = 0; i < availableMoves + 1; i++) {
			Set<Location> currentSet = new HashSet<>();
			for (Location location : openSet) {
				for (Location neighbor : location.getNeighbors()) {
					if (model.getMap().isValid(neighbor)) {
						if (isMoveable(neighbor, true)) {
							final List<Unit> eee = owner.getEnemyUnitsAt(location);
							if (eee.isEmpty()) {
								currentSet.add(neighbor);
							} else {
								enemies.addAll(eee);
							}
						} else if (isPossibleToDisembarkAt(neighbor, true)) {
							currentSet.add(neighbor);
						} else if (isPossibleToEmbarkAt(neighbor, true)) {
							currentSet.add(neighbor);
						}
					}
				}
				closedSet.add(location);
			}
			openSet.clear();
			openSet.addAll(currentSet);
		}
		closedSet.remove(getLocation());

		if (availableLocations != null) {
			availableLocations.addAll(closedSet);
		}
		if (attackableTargets != null) {
			attackableTargets.addAll(enemies);
		}
	}

	public boolean isPossibleToDisembarkAt(final Location targetLocation, boolean inCurrentTurn) {
		Preconditions.checkNotNull(targetLocation);
		if (getLocation().isNeighbor(targetLocation) && getType().getCargoCapacity() > 0) {
			return getHold().getSlots().stream()
					.filter(cargoSlot -> canCargoDisembark(cargoSlot, targetLocation, inCurrentTurn)).findAny()
					.isPresent();
		} else {
			return false;
		}
	}

	private boolean canCargoDisembark(final CargoSlot slot, final Location moveToLocation, boolean inCurrentTurn) {
		if (slot.isEmpty()) {
			return false;
		} else {
			final Unit unit = slot.getUnit().get();
			return (!inCurrentTurn || unit.availableMoves > 0) && unit.canUnitDisembarkAt(moveToLocation);
		}
	}

	private boolean canUnitDisembarkAt(final Location targeLocation) {
		return getType().canMoveAtTerrain(model.getMap().getTerrainAt(targeLocation));
	}

	public boolean isPossibleToEmbarkAt(final Location targetLocation, boolean inCurrentTurn) {
		if (isStorable() && getLocation().isNeighbor(targetLocation) && (!inCurrentTurn || availableMoves > 0)) {
			final List<Unit> units = model.getUnitsAt(targetLocation);
			return isSameOwner(units)
					&& units.stream().filter(unit -> unit.isAtLeastOneCargoSlotEmpty()).findAny().isPresent();
		} else {
			return false;
		}
	}

	public boolean isPossibleToAttackAt(final Location targetLocation) {
		if (model.getUnitsAt(targetLocation).isEmpty()) {
			return false;
		} else {
			if (type.canMoveAtTerrain(model.getMap().getTerrainAt(targetLocation))) {
				if (isSameOwner(model.getUnitsAt(targetLocation))) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Return true when given unit have free cargo slot for unit.
	 * 
	 * @return return <code>true</code> when at least one cargo slot is empty
	 *         and could be loaded otherwise return <code>false</code>
	 */
	public boolean isAtLeastOneCargoSlotEmpty() {
		return getType().getCargoCapacity() > 0
				&& getHold().getSlots().stream().filter(cargoSlot -> cargoSlot.isEmpty()).findAny().isPresent();
	}

	/**
	 * Verify that all units belongs to same owner.
	 * 
	 * @param units
	 *            required {@link List} of units
	 * @return return <code>true</code> when all units belongs to same owner as
	 *         this unit otherwise return <code>false</code>
	 */
	public boolean isSameOwner(final List<Unit> units) {
		Preconditions.checkNotNull(units);
		// XXX could it be rewritten to stream API?
		return units.size() > 0 && units.get(0).getOwner().equals(getOwner());
	}

	public List<Unit> getStorageUnits() {
		checkNotStored();

		return getLocation().getNeighbors().stream().flatMap(neighbor -> owner.getUnitsAt(neighbor).stream())
				.filter(unit -> unit != this)
				.filter(unit -> unit.getHold().getSlots().stream().filter(slot -> slot.isEmpty()).findAny().isPresent())
				.collect(ImmutableList.toImmutableList());
		/*
		 * final ImmutableList.Builder<Unit> builder = ImmutableList.builder();
		 * 
		 * // TODO JKA Predelat location.getNeighbors().forEach(neighbor -> {
		 * owner.getUnitsAt(neighbor).forEach(unit -> { if (unit != this) {
		 * unit.getHold().getSlots().forEach(slot -> { if (slot.isEmpty()) {
		 * builder.add(unit); } }); } }); });
		 * 
		 * return builder.build();
		 */
	}

	public Optional<List<Location>> getPath(final Location destination) {
		checkNotStored();

		return getPath(destination, false);
	}

	public Optional<List<Location>> getPath(final Location destination, final boolean excludeDestination) {
		checkNotStored();

		PathFinder finder = new PathFinder(this, getLocation(), destination, excludeDestination);

		return Optional.ofNullable(finder.find());
	}

	public void moveTo(final Path path) {
		checkNotStored();

		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getStart().isNeighbor(getLocation()),
				"Path (%s) must be neighbor to current location (%s).", path.getStart(), getLocation());
		Preconditions.checkArgument(model.getMap().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyUnitsAt().keySet()),
				"There is enemy unit on path (%s).", path);

		final Location start = getLocation();
		final List<Location> locations = new ArrayList<>();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				// TODO JKA neumožnit zadat delší cestu, než je povolený počet
				break;
			}
			if (!type.canMoveAtTerrain(model.getMap().getTerrainAt(newLocation))) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only moveable terrain (%s).",
						newLocation, model.getMap().getTerrainAt(newLocation)));
			}
			locations.add(newLocation);
			((PlaceLocation) place).setLocation(newLocation);
			availableMoves--;
		}
		if (!locations.isEmpty()) {
			final Path reallyExecutedPath =  Path.of(locations);
			final Terrain targetTerrain = model.getMap().getTerrainAt(reallyExecutedPath.getTarget());
			if (targetTerrain==Terrain.HIGH_SEA){
				placeToHighSeas(true);
			}
			model.fireUnitMoved(this, start, reallyExecutedPath);
		}
	}

	public void attack(final Location location) {
		checkNotStored();

		model.checkGameRunning();
		model.checkCurrentPlayer(owner);

		Preconditions.checkState(type.canAttack(), "This unit type (%s) cannot attack.", this);
		Preconditions.checkNotNull(location);
		Preconditions.checkArgument(type.canMoveAtTerrain(model.getMap().getTerrainAt(location)),
				"Target location (%s) is not moveable for this unit (%s)", location, this);
		Preconditions.checkArgument(this.getLocation().isNeighbor(location),
				"Unit location (%s) is not neighbor to target location (%s).", this.getLocation(), location);
		Preconditions.checkState(availableMoves > 0, "Unit (%s) cannot attack this turn.", this);
		Preconditions.checkState(!owner.getEnemyUnitsAt(location).isEmpty(),
				"There is not any enemy unit on target location (%s).", location);

		availableMoves = 0;

		final Unit defender = owner.getEnemyUnitsAt(location).get(0);
		final Unit destroyed = Math.random() <= 0.6 ? defender : this;
		model.destroyUnit(destroyed);
		if (this != destroyed && owner.getEnemyUnitsAt(location).isEmpty()) {
			((PlaceLocation) place).setLocation(location);
		}
		model.fireUnitAttacked(this, defender, destroyed);
	}

	public boolean isStorable() {
		return type.isStorable();
	}

	public boolean isStored() {
		return place instanceof PlaceCargoSlot;
	}

	public boolean isInHighSea() {
		return place instanceof PlaceHighSea;
	}

	public boolean isAtPort() {
		return place instanceof PlaceEuropePort;
	}

	public boolean isAtMap() {
		return place instanceof PlaceLocation;
	}

	//TODO rename it to placeToCargo
	void store(final CargoSlot slot) {
		storeWithoutEvent(slot);
		model.fireUnitStored(this, slot); // TODO JKA Move to CargoSlot?
	}
	
	void placeToEuropePort(final EuropePort port) {
		place = new PlaceEuropePort(this, Preconditions.checkNotNull(port));
	}

	void placeToLocation(final Location target) {
		place = new PlaceLocation(this, Preconditions.checkNotNull(target));
	}

	public void placeToHighSeas(final boolean isTravelToEurope) {
		//TODO add some preconditions
		final int requiredTurns = 3;
		//XXX choose if it's direction to east or to west (+1 rule to europe)
		place = new PlaceHighSea(this, isTravelToEurope, requiredTurns);
	}

	void storeWithoutEvent(final CargoSlot slot) {
		Preconditions.checkState(isStorable(), "This unit (%s) cannot be stored.", this);
		checkNotStored();
		// TODO JKA check adjacent location
		// TODO JKA check movement?
		// TODO JKA prazdny naklad?
		place = new PlaceCargoSlot(this, slot);
		availableMoves = 0;
	}

	void unload(final Location targetLocation) {
		Preconditions.checkNotNull(targetLocation);
		Preconditions.checkArgument(availableMoves > 0, "Unit (%s) need for unload at least on action point", this);
		Preconditions.checkState(isStored(), "This unit (%s) can't be unload, it's not stored.", this);
		// TODO JKA run "standard" unit location checks

		// TODO JKA empty all moves and attacks?
		this.availableMoves = 0;
		place = new PlaceLocation(this, targetLocation);
	}

	void checkNotStored() {
		if (isStored()) {
			throw new IllegalStateException(String.format("This unit (%s) is stored in (%s).", this, place));
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("type", type).add("owner", owner)
				.add("place", place.getName()).add("availableMoves", availableMoves).add("hold", hold)
				.add("place", place.getName()).toString();
	}

	void save(final JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("type", type.name());
		generator.write("owner", owner.getName());
		getLocation().save("location", generator);
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
		final Player owner = players.stream().filter(player -> player.getName().equals(ownerName)).findAny()
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

	Place getPlace() {
		return place;
	}

	public int getId() {
		return id;
	}

}
