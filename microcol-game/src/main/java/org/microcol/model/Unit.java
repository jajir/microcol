package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * All units can attack with muskets except wagon.
 */
public abstract class Unit {

    /**
     * Unit will see all tiles accessible by it's speed. This define how many
     * tiles further will unit see.
     */
    private final static int VISIBILITY_INCREASE = 1;

    /**
     * Number is probability than attacker will win. When it's bigger than 0.5
     * than attacker will have bigger probability that will win.
     */
    private final static double PROBABILITY_OF_ATTACKER_WIN = 0.6d;

    private final Random random = new Random();
    private final int id;
    private final Model model;
    private Player owner;
    private Place place;
    private int actionPoints;
    private UnitAction unitAction;

    public Unit(final Model model, final Integer id, final Function<Unit, Place> placeBuilder,
            final Player owner, final int actionPoints, final UnitAction unitAction) {
        Preconditions.checkNotNull(placeBuilder, "PlaceBuilder is null");
        this.owner = Preconditions.checkNotNull(owner);
        this.id = Preconditions.checkNotNull(id, "Id is null");
        this.model = Preconditions.checkNotNull(model, "Model is null");
        this.actionPoints = actionPoints;
        this.place = Preconditions.checkNotNull(placeBuilder.apply(this),
                "Place builder didn't created cargo");
        setAction(unitAction);
    }

    /**
     * Get list of terrain types at which unit can move.
     *
     * @return list terrain types
     */
    public List<TerrainType> getMoveableTerrainTypes() {
        // TODO use this method instead of type.
        return getType().getMoveableTerrains();
    }

    public boolean canMount() {
        return this instanceof UnitFreeColonist;
    }

    public boolean canHoldGuns() {
        return this instanceof UnitFreeColonist;
    }

    public boolean canHoldTools() {
        return this instanceof UnitFreeColonist;
    }

    /**
     * Get unit type.
     *
     * @return unit type
     */
    abstract public UnitType getType();
    
    /**
     * Get unit owner.
     *
     * @return owner player object
     */
    public Player getOwner() {
        return owner;
    }

    public PlaceLocation getPlaceLocation() {
        verifyThatUnitIsAtMap();

        return ((PlaceLocation) place);
    }

    public Location getLocation() {
        return getPlaceLocation().getLocation();
    }

    /**
     * Get number of available actions points.
     *
     * @return available action points.
     */
    public int getActionPoints() {
        return actionPoints;
    }

    private void verifyThatUnitIsAtMap() {
        Preconditions.checkState(isAtPlaceLocation(),
                "Unit have to be at map. Unit (%s) is at (%s)", this, place);
    }

    /**
     * It's called before turn starts.
     */
    void startTurn() {
        actionPoints = getType().getSpeed();
        if (isAtHighSea()) {
            PlaceHighSea placeHighSea = (PlaceHighSea) place;
            placeHighSea.decreaseRemainingTurns();
            if (placeHighSea.getRemainigTurns() <= 0) {
                if (placeHighSea.isTravelToEurope()) {
                    model.getEurope().getPort().placeShipToPort(this);
                    model.getTurnEventStore().add(TurnEventProvider.getShipComeEuropePort(owner));
                } else {
                    /*
                     * Ships always come from east side of map.
                     */
                    final List<Location> locations = model.getHighSea()
                            .getSuitablePlaceForShipCommingFromEurope(getOwner(), true);
                    placeToLocation(locations.get(random.nextInt(locations.size())),
                            Direction.west);
                    model.getTurnEventStore().add(TurnEventProvider.getShipComeHighSeas(owner));
                }
            }
        }
        unitAction.startTurn(model, this);
    }

    /**
     * Verify if it's possible to move at give location. Method doesn't verify
     * if unit have enough action point is if location is reachable.
     *
     * @param location
     *            required map location.
     * @return Return <code>true</code> when unit could move at given location
     *         otherwise return <code>false</code>.
     */
    public boolean isPossibleToMoveAt(final Location location) {
        return isPossibleToMoveAt(location, false);
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
    public boolean isPossibleToMoveAt(final Location location, final boolean ignoreEnemies) {
        Preconditions.checkNotNull(location);

        if (!model.getMap().isValid(location)) {
            return false;
        }

        if (!getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(location))) {
            if (isPossibleToGoToPort(location)) {
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

    private void aaa(final List<Location> availableLocations, final List<Unit> attackableTargets) {
        model.checkGameRunning();
        model.checkCurrentPlayer(owner);

        if (actionPoints == 0) {
            return;
        }

        Set<Location> openSet = new HashSet<>();
        openSet.add(getLocation());
        Set<Location> closedSet = new HashSet<>();
        Set<Unit> enemies = new HashSet<>();
        for (int i = 0; i < actionPoints + 1; i++) {
            Set<Location> currentSet = new HashSet<>();
            for (Location location : openSet) {
                for (Location neighbor : location.getNeighbors()) {
                    if (model.getMap().isValid(neighbor)) {
                        if (isPossibleToMoveAt(neighbor, true)) {
                            final List<Unit> eee = owner.getEnemyUnitsAt(location);
                            if (eee.isEmpty()) {
                                currentSet.add(neighbor);
                            } else {
                                enemies.addAll(eee);
                            }
                        } else if (canHoldCargo()
                                && ((UnitWithCargo) this).isPossibleToDisembarkAt(neighbor, true)) {
                            currentSet.add(neighbor);
                        } else if (isPossibleToEmbarkAt(neighbor)) {
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

    public List<Location> getVisibleLocations() {
        Preconditions.checkState(isAtPlaceLocation(),
                "Visible location can be determined just when unit is on map. Unit is at '%s'",
                place.getName());
        int maxMoves = getType().getSpeed() + VISIBILITY_INCREASE;
        Map<Location, Integer> movePrice = new HashMap<>();
        movePrice.put(getLocation(), maxMoves);
        while (maxMoves > 0) {
            final Integer valToCheck = maxMoves;
            List<Location> toCheck = movePrice.entrySet().stream()
                    .filter(entry -> entry.getValue() == valToCheck).map(entry -> entry.getKey())
                    .collect(ImmutableList.toImmutableList());
            maxMoves--;
            final Integer valToAdd = maxMoves;
            toCheck.forEach(location -> location.getNeighbors()
                    .forEach(loc -> movePrice.put(loc, valToAdd)));
        }
        return movePrice.entrySet().stream().map(entry -> entry.getKey())
                .collect(ImmutableList.toImmutableList());
    }

    public boolean isPossibleToGoToPort(final Location moveToLocation) {
        if (model.getColoniesAt(moveToLocation, owner).isPresent()) {
            return true;
        }
        return false;
    }

    /**
     * Verify that unit can move at target location and there is colony without
     * units to defend it.
     * 
     * @param targetLocation
     *            required location where should be captured colony
     * @return Return <code>true</code> when target location contains enemy
     *         colony without military units to defend it.
     */
    public boolean isPossibleToCaptureColonyAt(final Location targetLocation) {
        if (getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(targetLocation))
                && model.getColonyAt(targetLocation).isPresent()) {
            final Colony c = model.getColonyAt(targetLocation).get();
            if (!c.getOwner().equals(owner) && !isPossibleToAttackAt(targetLocation)) {
                return true;
            }
        }
        return false;
    }

    public boolean canUnitDisembarkAt(final Location targeLocation) {
        return getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(targeLocation));
    }

    public boolean isPossibleToEmbarkAt(final Location targetLocation) {
        return getFirstUnitToEmbarkAt(targetLocation).isPresent();
    }
    
    public Optional<UnitWithCargo> getFirstUnitToEmbarkAt(final Location targetLocation) {
        if (isStorable() && getLocation().isNeighbor(targetLocation) && actionPoints > 0
                && !model.getColonyAt(targetLocation).isPresent()) {
            final List<Unit> units = model.getUnitsAt(targetLocation);
            return units.stream().filter(unit -> unit.canHoldCargo())
                    .map(unit -> (UnitWithCargo) unit).filter(unit -> unit.getOwner().equals(owner))
                    .filter(unit -> unit.isAtLeastOneCargoSlotEmpty()).findFirst();
        } else {
            return Optional.empty();
        }
    }

    public boolean isPossibleToAttackAt(final Location targetLocation) {
        if (model.getUnitsAt(targetLocation).isEmpty()) {
            return false;
        } else {
            if (getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(targetLocation))) {
                List<Unit> units = model.getEnemyUnitsAt(owner, targetLocation).stream()
                        .filter(unit -> getType().getAttackableUnitType().contains(unit.getType()))
                        .collect(ImmutableList.toImmutableList());
                if (isSameOwner(units)) {
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
     * Verify that all units belongs to same owner.
     * 
     * @param units
     *            required {@link List} of units
     * @return return <code>true</code> when there is not unit in list belonging
     *         to different owner otherwise return <code>false</code>
     */
    public boolean isSameOwner(final List<Unit> units) {
        Preconditions.checkNotNull(units);
        return !units.stream().filter(unit -> !getOwner().equals(unit.getOwner())).findFirst()
                .isPresent();
    }

    /**
     * Get list of units where could this unit go to storage.
     *
     * @return list of units with at least one cargo slot free
     */
    public List<UnitWithCargo> getNeighborUnitsWithFreeSlot() {
        verifyThatUnitIsAtMap();

        return getLocation().getNeighbors().stream()
                .flatMap(neighbor -> owner.getUnitsAt(neighbor).stream())
                .filter(unit -> unit != this && unit.canHoldCargo())
                .map(unit -> (UnitWithCargo) unit).filter(unit -> unit.getCargo().getSlots()
                        .stream().filter(slot -> slot.isEmpty()).findAny().isPresent())
                .collect(ImmutableList.toImmutableList());
    }

    public Optional<List<Location>> getPath(final Location destination) {
        verifyThatUnitIsAtMap();

        return getPath(destination, false);
    }

    public Optional<List<Location>> getPath(final Location destination,
            final boolean excludeDestination) {
        verifyThatUnitIsAtMap();

        PathFinder finder = new PathFinder(this, getLocation(), destination, excludeDestination);

        return Optional.ofNullable(finder.find());
    }

    /**
     * Move one step to adjacent tile.
     * 
     * @param moveTo
     *            required location when unit should be moved
     */
    public void moveOneStep(final Location moveTo) {
        verifyThatUnitIsAtMap();

        model.checkGameRunning();
        model.checkCurrentPlayer(owner);

        Preconditions.checkNotNull(moveTo);
        Preconditions.checkArgument(getLocation().isNeighbor(moveTo),
                "Location (%s) must be neighbor to current location (%s).", moveTo, getLocation());
        Preconditions.checkArgument(model.getMap().isValid(moveTo), "Location (%s) must be valid.",
                moveTo);
        Preconditions.checkArgument(isPossibleToMoveAt(moveTo),
                "It's not possible to move at (%s).", moveTo);
        Preconditions.checkState(actionPoints > 0, "There is not enough avilable moves (%s)", this);

        actionPoints--;
        final TerrainType targetTerrain = model.getMap().getTerrainTypeAt(moveTo);
        if (targetTerrain == TerrainType.HIGH_SEA) {
            placeToHighSeas(true);
            model.fireUnitMovedToHighSeas(this);
        } else {
            final Location start = getLocation();
            final Direction orientation = findOrintationForMove(moveTo);
            model.fireUnitMovedStepStarted(this, start, moveTo, orientation);
            placeToLocation(moveTo, orientation);
            owner.revealMapForUnit(this);
            // if it's necessary fire event about captured city
            tryToCaptureColony(moveTo);
            model.fireUnitMovedStepFinished(this, start, moveTo);
        }
    }

    private Direction findOrintationForMove(final Location moveTo) {
        // TODO list of ifs is stupid aproach. Refactor it.
        if (UnitType.GALLEON.equals(getType())) {
            /*
             * Find direction when is moved performed.
             */
            final Location vector = moveTo.sub(getLocation());
            final Direction direction = Direction.valueOf(vector);
            if (direction.isOrientedWest()) {
                return Direction.west;
            } else if (direction.isOrientedEast()) {
                return Direction.east;
            } else {
                /**
                 * Keep original orientation.
                 */
                return getPlaceLocation().getOrientation();
            }
        } else {
            /**
             * Keep original orientation.
             */
            if (isAtPlaceLocation()) {
                return getPlaceLocation().getOrientation();
            } else {
                return getDefaultOrintation();
            }
        }
    }

    /**
     * When unit moves or attack than unit could capture colony.
     * 
     * @param newLocation
     *            required new location
     */
    private void tryToCaptureColony(final Location newLocation) {
        final Optional<Colony> oColony = model.getColonyAt(newLocation);
        if (oColony.isPresent()) {
            final Colony col = oColony.get();
            if (!col.getOwner().equals(owner)) {
                col.captureColony(owner, this);
            }
        }
    }

    private boolean isWinnerAttacker() {
        return random.nextDouble() < PROBABILITY_OF_ATTACKER_WIN;
    }

    /**
     * This unit attack unit at given location.
     * <p>
     * Attack is composed from two steps:
     * </p>
     * <ul>
     * <li>Move at attacked place. Show moving one step animation.</li>
     * <li>Perform attack.</li>
     * </ul>
     *
     * @param attackAt
     *            Required attacked location.
     */
    public void attack(final Location attackAt) {
        canAttackValidation(attackAt);
        Preconditions.checkState(!getAttackableUnitsAt(attackAt).isEmpty(),
                "There is not attackable unit on target location (%s).", attackAt);
        Preconditions.checkState(getLocation().isNeighbor(attackAt),
                "Attacked location '%s' should be neighbor of attacking unit '%s'", attackAt, this);

        actionPoints = 0;
        final Unit defender = getAttackableUnitsAt(attackAt).get(0);
        final Unit winner = isWinnerAttacker() ? this : defender;
        if (winner == this) {
            final Unit destroyed = defender;
            model.fireUnitAttacked(this, defender, destroyed);
            model.destroyUnit(destroyed);
            if (owner.getEnemyUnitsAt(attackAt).isEmpty()) {
                placeToLocation(attackAt);
                tryToCaptureColony(attackAt);
            }
        } else {
            final Unit destroyed = this;
            model.fireUnitAttacked(this, defender, destroyed);
            model.destroyUnit(destroyed);
        }
    }

    private void canAttackValidation(final Location attackAt) {
        verifyThatUnitIsAtMap();

        model.checkGameRunning();
        model.checkCurrentPlayer(owner);

        Preconditions.checkState(getType().canAttack(), "This unit type (%s) cannot attack.", this);
        Preconditions.checkNotNull(attackAt);
        Preconditions.checkArgument(
                getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(attackAt)),
                "Target location (%s) is not moveable for this unit (%s)", attackAt, this);
        Preconditions.checkArgument(this.getLocation().isNeighbor(attackAt),
                "Unit location (%s) is not neighbor to target location (%s).", this.getLocation(),
                attackAt);
        Preconditions.checkState(actionPoints > 0, "Unit (%s) cannot attack this turn.", this);
    }

    /**
     * Get list of units that could be attacked by this unit at some location.
     * 
     * @param at
     *            required examined location
     * @return Return list of units that could be attacked.
     */
    private List<Unit> getAttackableUnitsAt(final Location at) {
        return owner.getEnemyUnitsAt(at).stream()
                .filter(unit -> getType().getAttackableUnitType().contains(unit.getType()))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Allows to change unit owner.
     * 
     * @param player
     *            required player will be new unit's owner
     */
    void takeOver(final Player player) {
        this.owner = Preconditions.checkNotNull(player);

    }

    public boolean isStorable() {
        return getType().isStorable();
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

    public boolean isAtEuropePier() {
        return place instanceof PlaceEuropePier;
    }

    public boolean isAtPlaceConstructionSlot() {
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
        // TODO could be called on last unit in colony
        // Verify that only moving in slots is available.
        if (isAtCargoSlot() && !getPlaceCargoSlot().getCargoSlotOwner()
                .equals(placeCargoSlot.getCargoSlotOwner())) {
            throw new IllegalStateException(
                    String.format("This unit (%s) cannot be stored.", this));
        }
        // remove from previous place
        place.destroy();
        // TODO JKA check adjacent location, why?
        // TODO JKA check movement?
        // TODO JKA prazdny naklad?
        place = placeCargoSlot;
        actionPoints = 0;
        // TODO JKA Move to CargoSlot?
        model.fireUnitEmbarked(this, placeCargoSlot.getCargoSlot());
    }

    void placeToEuropePort(final EuropePort port) {
        place.destroy();
        place = new PlaceEuropePort(this, Preconditions.checkNotNull(port));
    }

    /**
     * Provide orientation where unit facing when is placed to map.
     *
     * @return return default orientation value
     */
    Direction getDefaultOrintation() {
        // TODO it should be unit based.
        return Direction.east;
    }

    void placeToLocation(final Location target) {
        placeToLocation(target, getDefaultOrintation());
    }

    void placeToLocation(final Location target, final Direction orientation) {
        if (place instanceof PlaceLocation) {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target), orientation);
        } else {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target), orientation);
            model.fireUnitMovedToLocation(this);
        }
    }

    public void placeToHighSeas(final boolean isTravelToEurope) {
        Preconditions.checkArgument(getType().isShip(), "Only ships could be placed to high sea.");
        final int requiredTurns = 3;
        place.destroy();
        // TODO choose if it's direction to east or to west (+1 rule to Europe)
        place = new PlaceHighSea(this, isTravelToEurope, requiredTurns);
    }

    public void placeToEuropePortPier() {
        Preconditions.checkState(isAtCargoSlot(), "Unit is not in cargo.");
        Preconditions.checkState(getPlaceCargoSlot().isOwnerAtEuropePort(),
                "Holding unit is not at europe port, cant be placed to port pier.");
        getPlaceCargoSlot().getCargoSlot().empty();
        place = new PlaceEuropePier(this);
    }

    public void placeToColonyStructureSlot(final ConstructionSlot structureSlot) {
        Preconditions.checkNotNull(structureSlot);
        Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
        Preconditions.checkState(structureSlot.isEmpty(),
                "Unit can't be placed to non empty colony structure");
        Preconditions.checkState(structureSlot.isValid(),
                "Unit can't be placed to already destroyed colony");
        if (isAlreadyInSameColony(structureSlot.getColony())) {
            place.destroySimple();
        } else {
            place.destroy();
        }
        place = new PlaceConstructionSlot(this, structureSlot);
        structureSlot.set((PlaceConstructionSlot) place);
    }

    public void placeToColonyField(final ColonyField colonyField, final GoodType producedGoodType) {
        Preconditions.checkNotNull(colonyField);
        Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
        Preconditions.checkState(colonyField.isEmpty(),
                "Unit can't be placed to non empty colony field");
        Preconditions.checkState(colonyField.isValid(),
                "Unit can't be placed to already destroyed colony");
        if (isAlreadyInSameColony(colonyField.getColony())) {
            place.destroySimple();
        } else {
            place.destroy();
        }
        place = new PlaceColonyField(this, colonyField, producedGoodType);
        colonyField.setPlaceColonyField((PlaceColonyField) place);
    }

    private boolean isAlreadyInSameColony(final Colony colonyWhereWillBeUnitPlaced) {
        final Optional<Colony> oColony = getColony();
        if (oColony.isPresent()) {
            final Colony colony = oColony.get();
            if (colony.getUnitsInColony().size() == 1) {
                return colony.equals(colonyWhereWillBeUnitPlaced);
            }
        }
        return false;
    }

    /**
     * If it's possible return colony where is unit placed.
     * 
     * @return
     */
    private Optional<Colony> getColony() {
        if (isAtPlaceColonyField()) {
            return Optional.of(getPlaceColonyField().getColony());
        }
        if (isAtPlaceConstructionSlot()) {
            return Optional.of(getPlaceConstruction().getColony());
        }
        return Optional.empty();
    }

    /**
     * This method place unit to map. It's not about moving unit. MEthod doesn't
     * verify if unit can go to target location.
     * 
     * @param location
     *            Required location where is unit placed.
     */
    public void placeToMap(final Location location) {
        Preconditions.checkNotNull(location);
        Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
        Preconditions.checkState(getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(location)),
                "Unit '%s' can't be placed at '%s' because can't move on terrain '%s'", this,
                location, model.getMap().getTerrainTypeAt(location));

        placeToLocation(location, getDefaultOrintation());
    }

    void disembark(final Location targetLocation) {
        Preconditions.checkNotNull(targetLocation);
        Preconditions.checkState(actionPoints > 0,
                "Unit (%s) need for unload at least on action point", this);
        Preconditions.checkState(isAtCargoSlot(),
                "This unit (%s) can't be unload, it's not stored.", this);
        final TerrainType terrainType = model.getMap().getTerrainTypeAt(targetLocation);
        Preconditions.checkState(getType().getMoveableTerrains().contains(terrainType),
                "This unit (%s) can't move at target terrain %s.", this, terrainType);
        final Location startLocation = getPlaceCargoSlot().getOwnerUnit().getLocation();
        Preconditions.checkState(startLocation.isNeighbor(targetLocation),
                "Start location '%s' have to neighbobor of target location '%s'", startLocation,
                targetLocation);

        this.actionPoints = 0;

        final Direction orientation = findOrintationForMove(targetLocation);
        final Path path = Path.of(Lists.newArrayList(startLocation, targetLocation));
        if (model.fireUnitMoveStarted(this, path)) {
            model.fireUnitMovedStepStarted(this, startLocation, targetLocation, orientation);
            placeToLocation(targetLocation, orientation);
            owner.revealMapForUnit(this);
            model.fireUnitMovedStepFinished(this, startLocation, targetLocation);
            model.fireUnitMovedFinished(this, path);
        }
    }

    /**
     * This put unit to cargo slot from map and paint nice animation of
     * movement.
     *
     * @param placeCargoSlot
     *            required placeCargoSlot
     */
    void embark(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkState(isAtPlaceLocation(), "Unit have to be at map, it's at '%s'",
                place);
        Preconditions.checkState(placeCargoSlot.getCargoSlot().getOwnerUnit().isAtPlaceLocation(),
                "Unit '%s' have to be at map", placeCargoSlot.getCargoSlot().getOwnerUnit());
        final Location startLocation = getLocation();
        final Location targetLocation = placeCargoSlot.getCargoSlot().getOwnerUnit().getLocation();
        final Direction orientation = findOrintationForMove(targetLocation);
        final Path path = Path.of(Lists.newArrayList(startLocation, targetLocation));

        if (model.fireUnitMoveStarted(this, path)) {
            model.fireUnitMovedStepStarted(this, startLocation, targetLocation, orientation);

            placeToCargoSlot(placeCargoSlot);

            model.fireUnitMovedStepFinished(this, startLocation, targetLocation);
            model.fireUnitMovedFinished(this, path);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("type", getType())
                .add("owner", owner).add("place", place == null ? place : place.toString())
                .add("availableMoves", actionPoints).add("unitAction", unitAction).toString();
    }

    public UnitPo save() {
        final UnitPo unitPo = new UnitPo();
        unitPo.setId(id);
        unitPo.setAvailableMoves(actionPoints);
        unitPo.setOwnerId(owner.getName());
        unitPo.setType(getType());
        unitPo.setAction(unitAction.save());
        place.save(unitPo);
        return unitPo;
    }

    Place getPlace() {
        return place;
    }

    public PlaceCargoSlot getPlaceCargoSlot() {
        Preconditions.checkState(isAtCargoSlot(), "Unit have to be in cargo slot");
        return (PlaceCargoSlot) place;
    }

    public PlaceConstructionSlot getPlaceConstruction() {
        Preconditions.checkState(isAtPlaceConstructionSlot(),
                "Unit have to be in colony construction");
        return (PlaceConstructionSlot) place;
    }

    PlaceColonyField getPlaceColonyField() {
        Preconditions.checkState(isAtPlaceColonyField(), "Unit have to be in colony field");
        return (PlaceColonyField) place;
    }

    public int getId() {
        return id;
    }

    public int getMilitaryStrenght() {
        return 1;
    }

    /*
     * Following methods are experiment how to better implement action.
     */

    private void setAction(final UnitAction unitAction) {
        this.unitAction = Preconditions.checkNotNull(unitAction, "Unit action is null");
        Preconditions.checkState(getSupportedActions().contains(unitAction.getType()),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(), this);
    }

    public void setAction(final UnitActionType actionType) {
        Preconditions.checkNotNull(actionType, "Unit action is null");
        Preconditions.checkState(getSupportedActions().contains(actionType),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(), this);
        setAction(actionType.make());
        actionPoints = 0;
    }

    public List<UnitActionType> getSupportedActions() {
        /*
         * Following code will be part on special unit definition.
         */
        if (getType().isShip()) {
            return ImmutableList.of(UnitActionType.noAction);
        } else {
            return ImmutableList.of(UnitActionType.plowField, UnitActionType.noAction);
        }
    }

    public UnitAction getUnitAction() {
        return unitAction;
    }
    
    public boolean canHoldCargo() {
        return this instanceof UnitWithCargo;
    }

    public boolean canPlowFiled() {
        if (getSupportedActions().contains(UnitActionType.plowField) && actionPoints > 0
                && isAtPlaceLocation()) {
            final TerrainType targetTerrain = model.getMap().getTerrainTypeAt(getLocation());
            return TerrainType.GRASSLAND.equals(targetTerrain)
                    || TerrainType.PRAIRIE.equals(targetTerrain)
                    || TerrainType.SAVANNAH.equals(targetTerrain);
        } else {
            return false;
        }
    }

    /**
     * @return the model
     */
    protected Model getModel() {
        return model;
    }

}
