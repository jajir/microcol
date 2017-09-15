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
	private final Cargo cargo;

	Unit(final UnitType type, final Player owner, final Location location) {
		this.type = Preconditions.checkNotNull(type, "UnitType is null");
		this.owner = Preconditions.checkNotNull(owner, "Owner is null");
		this.place = new PlaceLocation(this, Preconditions.checkNotNull(location));
		this.cargo = new Cargo(this, type.getCargoCapacity());
		this.id = IdManager.nextId();
	}

	Unit(final UnitType type, final Player owner, final PlaceBuilder placeBuilder) {
		this.type = Preconditions.checkNotNull(type, "UnitType is null");
		this.owner = Preconditions.checkNotNull(owner, "Owner is null");
		this.place = Preconditions.checkNotNull(placeBuilder.build(this), "PlaceBuilder is null");
		this.cargo = new Cargo(this, type.getCargoCapacity());
		this.id = IdManager.nextId();
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
		validateTerrain();
	}
	
	private void validateTerrain(){
		if(isAtMap()){
			final Terrain t = model.getMap().getTerrainAt(getLocation());
			Preconditions.checkState(isMoveable(getLocation(), true),
					String.format("Unit (%s) is not at valid terrain (%s)", this, t));
		}
	}

	public UnitType getType() {
		return type;
	}

	public Player getOwner() {
		return owner;
	}

	public Location getLocation() {
		Preconditions.checkArgument(isAtPlaceLocation(), "Unit (%s) is not at map. ", this);
		return ((PlaceLocation) place).getLocation();
	}

	public int getAvailableMoves() {
		Preconditions.checkState(isAtMap(),"Moving unit have to be at map");

		return availableMoves;
	}

	public Cargo getCargo() {
		return cargo;
	}

	/**
	 * It's called before turn starts.
	 */
	void startTurn() {
		availableMoves = type.getSpeed();
		if (isAtHighSea()) {
			PlaceHighSea placeHighSea = (PlaceHighSea) place;
			placeHighSea.decreaseRemainingTurns();
			if (placeHighSea.getRemainigTurns() <= 0) {
				if (placeHighSea.isTravelToEurope()) {
					model.getEurope().getPort().placeShipToPort(this);
				} else {
					// XXX ships always come from east side of map
					final List<Location> locations = model.getHighSea()
							.getSuitablePlaceForShipCommingFromEurope(getOwner(), true);
					// TODO random should be class instance
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
			if(isPossibleGoToPort(location)){
				return true;
			}
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
		Preconditions.checkState(isAtMap(),"Moving unit have to be at map");

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
			return getCargo().getSlots().stream()
					.filter(cargoSlot -> canCargoDisembark(cargoSlot, targetLocation, inCurrentTurn)).findAny()
					.isPresent();
		} else {
			return false;
		}
	}

	public boolean isPossibleGoToPort(final Location moveToLocation){
		if(model.getColoniesAt(moveToLocation, owner).isPresent()){
			return true;
		}
		return false;
	}
	
	private boolean canCargoDisembark(final CargoSlot slot, final Location moveToLocation, boolean inCurrentTurn) {
		if (slot.isEmpty() || slot.isLoadedGood()) {
			return false;
		} else {
			// TODO jj express it better slot.getUnit().get().getUnit()
			final Unit holdedUnit = slot.getUnit().get();
			return (!inCurrentTurn || holdedUnit.availableMoves > 0) && holdedUnit.canUnitDisembarkAt(moveToLocation);
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
				&& getCargo().getSlots().stream().filter(cargoSlot -> cargoSlot.isEmpty()).findAny().isPresent();
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
		Preconditions.checkState(isAtMap(),"Moving unit have to be at map");

		return getLocation().getNeighbors().stream().flatMap(neighbor -> owner.getUnitsAt(neighbor).stream())
				.filter(unit -> unit != this).filter(unit -> unit.getCargo().getSlots().stream()
						.filter(slot -> slot.isEmpty()).findAny().isPresent())
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
		Preconditions.checkState(isAtMap(), "Moving unit have to be at map");

		return getPath(destination, false);
	}

	public Optional<List<Location>> getPath(final Location destination, final boolean excludeDestination) {
		Preconditions.checkState(isAtMap(), "Moving unit have to be at map");

		PathFinder finder = new PathFinder(this, getLocation(), destination, excludeDestination);

		return Optional.ofNullable(finder.find());
	}

	public void moveTo(final Path path) {
		Preconditions.checkState(isAtMap(), "Moving unit have to be at map");

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
			if (!isMoveable(newLocation)) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only moveable terrain (%s).",
						newLocation, model.getMap().getTerrainAt(newLocation)));
			}
			locations.add(newLocation);
			((PlaceLocation) place).setLocation(newLocation);
			availableMoves--;
		}
		if (!locations.isEmpty()) {
			final Path reallyExecutedPath = Path.of(locations);
			final Terrain targetTerrain = model.getMap().getTerrainAt(reallyExecutedPath.getTarget());
			if (targetTerrain == Terrain.HIGH_SEA) {
				placeToHighSeas(true);
			}
			model.fireUnitMoved(this, start, reallyExecutedPath);
		}
	}

	public void attack(final Location location) {
		Preconditions.checkState(isAtMap(),"Moving unit have to be at map");

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

	public boolean isAtCargoSlot() {
		return place instanceof PlaceCargoSlot;
	}

	public boolean isAtHighSea() {
		return place instanceof PlaceHighSea;
	}

	public boolean isAtPlaceLocation() {
		return place instanceof PlaceLocation;
	}

	public boolean isAtEuropePort() {
		return place instanceof PlaceEuropePort;
	}

	public boolean isAtMap() {
		return place instanceof PlaceLocation;
	}

	public boolean isAtEuropePier() {
		return place instanceof PlaceEuropePier;
	}

	public boolean isAtPlaceConstruction() {
		return place instanceof PlaceConstructionSlot;
	}

	public boolean isAtPlaceColonyField() {
		return place instanceof PlaceColonyField;
	}

	/**
	 * This unit will be moved to given place cargo slot.
	 * 
	 * @param placeCargoSlot
	 *            required place cargo slot
	 */
	void placeToCargoSlot(final PlaceCargoSlot placeCargoSlot) {
		//Verify that only moving in slots is available.
		if (isAtCargoSlot()) {
			if (!getPlaceCargoSlot().getCargoSlotOwner().equals(placeCargoSlot.getCargoSlotOwner())) {
				throw new IllegalStateException(String.format("This unit (%s) cannot be stored.", this));
			}
		}
		//remove from previous place
		place.destroy();
		// TODO JKA check adjacent location
		// TODO JKA check movement?
		// TODO JKA prazdny naklad?
		place = placeCargoSlot;
		availableMoves = 0;
		// TODO JKA Move to CargoSlot?
		model.fireUnitStored(this, placeCargoSlot.getCargoSlot());
	}

	void placeToEuropePort(final EuropePort port) {
		place.destroy();
		place = new PlaceEuropePort(this, Preconditions.checkNotNull(port));
	}

	void placeToLocation(final Location target) {
		place.destroy();
		place = new PlaceLocation(this, Preconditions.checkNotNull(target));
	}

	public void placeToHighSeas(final boolean isTravelToEurope) {
		Preconditions.checkArgument(UnitType.isShip(type), "Only ships could be placed to high sea.");
		// TODO add some preconditions
		final int requiredTurns = 3;
		place.destroy();
		// XXX choose if it's direction to east or to west (+1 rule to europe)
		place = new PlaceHighSea(this, isTravelToEurope, requiredTurns);
	}

	public void placeToEuropePortPier() {
		Preconditions.checkState(isAtCargoSlot(), "Unit is not in cargo.");
		Preconditions.checkState(getPlaceCargoSlot().isOwnerAtEuropePort(),
				"Holding unit is not at europe port, cant be placed to port pier.");
		getPlaceCargoSlot().getCargoSlot().empty();
		place = new PlaceEuropePier(this);
	}
	
	public void placeToColonyStructureSlot(final ConstructionSlot structureSlot){
		Preconditions.checkNotNull(structureSlot);
		Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
		Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
		Preconditions.checkState(structureSlot.isEmpty(), "Unit can't be placed to non empty colony structure");
		place.destroy();
		place = new PlaceConstructionSlot(this, structureSlot);
		structureSlot.set((PlaceConstructionSlot)place);
	}
	
	public void placeToColonyField(final ColonyField colonyField){
		Preconditions.checkNotNull(colonyField);
		Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
		Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
		Preconditions.checkState(colonyField.isEmpty(), "Unit can't be placed to non empty colony field");
		place.destroy();
		place = new PlaceColonyField(this, colonyField);
		colonyField.setPlaceColonyField((PlaceColonyField)place);
	}

	/**
	 * This method place unit to map. It's not about moving unit. MEthod doesn't
	 * verify if unit can go to target location.
	 * 
	 * @param location Required location where is unit placed.
	 */
	public void placeToMap(final Location location){
		Preconditions.checkNotNull(location);
		Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
		Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
		place.destroy();
		placeToLocation(location);
	}

	void unload(final Location targetLocation) {
		Preconditions.checkNotNull(targetLocation);
		Preconditions.checkState(availableMoves > 0, "Unit (%s) need for unload at least on action point", this);
		Preconditions.checkState(isAtCargoSlot(), "This unit (%s) can't be unload, it's not stored.", this);
		// TODO JKA run "standard" unit location checks

		// TODO JKA empty all moves and attacks?
		this.availableMoves = 0;
		place = new PlaceLocation(this, targetLocation);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("type", type)
				.add("owner", owner)
				.add("place", place.getName())
				.add("availableMoves", availableMoves)
				.add("hold", cargo)
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

	private PlaceCargoSlot getPlaceCargoSlot() {
		Preconditions.checkState(isAtCargoSlot(), "Unit have to be in cargo slot");
		return (PlaceCargoSlot) place;
	}
	
	PlaceConstructionSlot getPlaceConstruction() {
		Preconditions.checkState(isAtPlaceConstruction(), "Unit have to be in colony construction");
		return (PlaceConstructionSlot) place;
	}
	
	PlaceColonyField getPlaceColonyField(){
		Preconditions.checkState(isAtPlaceColonyField(), "Unit have to be in colony field");
		return (PlaceColonyField) place;		
	}

	public int getId() {
		return id;
	}

	Model getModel() {
		return model;
	}

}
