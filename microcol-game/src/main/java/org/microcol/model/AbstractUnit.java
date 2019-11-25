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
public abstract class AbstractUnit implements Unit {

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

    /**
     * Immutable and unique unit id.
     */
    private final int id;

    /**
     * Immutable model.
     */
    protected final Model model;

    private Player owner;
    private Place place;
    private int actionPoints;
    private UnitAction unitAction;

    public AbstractUnit(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner, final int actionPoints,
            final UnitAction unitAction) {
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
     * <p>
     * This method is better than unitType method getMoveableTerrains().
     * </p>
     *
     * @return list terrain types
     */
    @Override
    public List<TerrainType> getMoveableTerrainTypes() {
        return getType().getMoveableTerrains();
    }

    @Override
    public boolean canMount() {
        return this instanceof UnitFreeColonist;
    }

    @Override
    public boolean canHoldGuns() {
        return this instanceof UnitFreeColonist;
    }

    @Override
    public boolean canHoldTools() {
        return this instanceof UnitFreeColonist;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public PlaceLocation getPlaceLocation() {
        verifyThatUnitIsAtMap();

        return ((PlaceLocation) place);
    }

    @Override
    public Location getLocation() {
        return getPlaceLocation().getLocation();
    }

    /**
     * Get number of available actions points.
     *
     * @return available action points.
     */
    @Override
    public int getActionPoints() {
        return actionPoints;
    }

    private void verifyThatUnitIsAtMap() {
        Preconditions.checkState(isAtPlaceLocation(),
                "Unit have to be at map. Unit (%s) is at (%s)", this, place);
    }

    /**
     * Get number of action point in each turn. Generally should be called this
     * method because speed could change depending on unit status.
     *
     * @return Return number of action point per turn.
     */
    @Override
    public int getSpeed() {
        return getType().getSpeed();
    }

    /**
     * It's called before turn starts.
     */
    public void startTurn() {
        actionPoints = getSpeed();
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
                    model.getTurnEventStore().add(TurnEventProvider.getShipComeToHighSeas(owner));
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
    @Override
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
    @Override
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

    @Override
    public List<Location> getAvailableLocations() {
        Preconditions.checkArgument(isAtPlaceLocation(), "Unit have to at map");
        final List<Location> locations = new ArrayList<>();
        findLocations(locations, null);
        return ImmutableList.copyOf(locations);
    }

    /**
     * Find all reachable locations. It implements Dijkstra's algorithm.
     *
     * @param availableLocations
     *            optional list that will contains list of reachable locations
     * @param attackableTargets
     *            optional list that will contain all reachable enemies.
     */
    private void findLocations(final List<Location> availableLocations,
            final List<Unit> attackableTargets) {
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

    @Override
    public List<Location> getVisibleLocations() {
        Preconditions.checkState(isAtPlaceLocation(),
                "Visible location can be determined just when unit is on map. Unit is at '%s'",
                place.getName());
        int maxMoves = getSpeed() + VISIBILITY_INCREASE;
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

    @Override
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
    @Override
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

    @Override
    public boolean canUnitDisembarkAt(final Location targeLocation) {
        return getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(targeLocation));
    }

    @Override
    public boolean isPossibleToEmbarkAt(final Location targetLocation) {
        return getFirstUnitToEmbarkAt(targetLocation).isPresent();
    }

    @Override
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

    @Override
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
    @Override
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
    @Override
    public List<UnitWithCargo> getNeighborUnitsWithFreeSlot() {
        verifyThatUnitIsAtMap();

        return getLocation().getNeighbors().stream()
                .flatMap(neighbor -> owner.getUnitsAt(neighbor).stream())
                .filter(unit -> unit != this && unit.canHoldCargo())
                .map(unit -> (UnitWithCargo) unit).filter(unit -> unit.getCargo().getSlots()
                        .stream().filter(slot -> slot.isEmpty()).findAny().isPresent())
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public Optional<List<Location>> getPath(final Location destination) {
        verifyThatUnitIsAtMap();

        return getPath(destination, false);
    }

    @Override
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
    @Override
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
        Preconditions.checkState(actionPoints > 0, "There is not enough available action points (%s)", this);

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

    /**
     * Find direction where unit is pointed based on location where is moving.
     * 
     * @param moveTo
     *            required location where is unit moving
     * @return direction where unit will face
     */
    protected Direction findOrintationForMove(final Location moveTo) {
        if (isAtPlaceLocation()) {
            return getPlaceLocation().getOrientation();
        } else {
            return getDefaultOrintation();
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
                col.captureColony(this);
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
    @Override
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
    @Override
    public void takeOver(final Player player) {
        this.owner = Preconditions.checkNotNull(player);
    }

    @Override
    public boolean isStorable() {
        return getType().isStorable();
    }

    @Override
    public boolean isAtCargoSlot() {
        return place instanceof PlaceCargoSlot;
    }

    @Override
    public boolean isAtHighSea() {
        return place instanceof PlaceHighSea;
    }

    @Override
    public boolean isAtPlaceLocation() {
        return place instanceof PlaceLocation;
    }

    @Override
    public boolean isAtEuropePort() {
        return place instanceof PlaceEuropePort;
    }

    @Override
    public boolean isAtEuropePier() {
        return place instanceof PlaceEuropePier;
    }

    @Override
    public boolean isAtPlaceConstructionSlot() {
        return place instanceof PlaceConstructionSlot;
    }

    @Override
    public boolean isAtPlaceColonyField() {
        return place instanceof PlaceColonyField;
    }

    /**
     * This unit will be moved to given place cargo slot.
     * 
     * @param placeCargoSlot
     *            required place cargo slot
     */
    @Override
    public void placeToCargoSlot(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkNotNull(placeCargoSlot, "Place cargo slot is null");
        Preconditions.checkArgument(owner.equals(placeCargoSlot.getCargoSlotOwner()),
                "Cargo slot belongs to different player %s than unit %s",
                placeCargoSlot.getCargoSlotOwner(), owner);
        // remove from previous place
        place.destroy();
        place = placeCargoSlot;
        actionPoints = 0;
        model.fireUnitEmbarked(this, placeCargoSlot.getCargoSlot());
    }

    @Override
    public void placeToEuropePort(final EuropePort port) {
        place.destroy();
        place = new PlaceEuropePort(this, Preconditions.checkNotNull(port));
    }

    /**
     * Provide orientation where unit facing when is placed to map.
     *
     * @return return default orientation value
     */
    @Override
    public Direction getDefaultOrintation() {
        return Direction.east;
    }

    @Override
    public void placeToLocation(final Location target) {
        placeToLocation(target, getDefaultOrintation());
    }

    @Override
    public void placeToLocation(final Location target, final Direction orientation) {
        if (place instanceof PlaceLocation) {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target), orientation);
        } else {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target), orientation);
            model.fireUnitMovedToLocation(this);
        }
    }

    @Override
    public void placeToHighSeas(final boolean isTravelToEurope) {
        Preconditions.checkArgument(getType().isShip(), "Only ships could be placed to high sea.");
        Preconditions.checkArgument(isAtPlaceLocation() || isAtEuropePort(),
                "Unit %s have to at map or in Europe port.", this);
        final int requiredTurns = countRequiredTurnsToSail(isTravelToEurope);
        place.destroy();
        place = new PlaceHighSea(this, isTravelToEurope, requiredTurns);
    }

    private final static int TRAVEL_TO_EUROPE = 3;

    private final static int TRAVEL_TO_EUROPE_FROM_WEST = 4;

    /**
     * Count turn required to travel to Europe. When player send ship from west
     * part of map than travel took more time. It's like send ships to Europe at
     * west direction.
     * 
     * @param isTravelToEurope
     *            required if ship travel to Europe or back.
     * @return required number turns
     */
    private int countRequiredTurnsToSail(final boolean isTravelToEurope) {
        if (isTravelToEurope) {
            int x = getLocation().getX();
            if (x < getModel().getMap().getMaxLocationX() / 2) {
                return TRAVEL_TO_EUROPE_FROM_WEST;
            } else {
                return TRAVEL_TO_EUROPE;
            }
        } else {
            return TRAVEL_TO_EUROPE;
        }
    }

    @Override
    public void placeToEuropePortPier() {
        Preconditions.checkState(isAtCargoSlot(), "Unit is not in cargo.");
        Preconditions.checkState(getPlaceCargoSlot().isOwnerAtEuropePort(),
                "Holding unit is not at europe port, cant be placed to port pier.");
        getPlaceCargoSlot().getCargoSlot().empty();
        place = new PlaceEuropePier(this);
    }

    @Override
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

    @Override
    public void placeToColonyField(final ColonyField colonyField,
            final GoodsType producedGoodsType) {
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
        place = new PlaceColonyField(this, colonyField, producedGoodsType);
        colonyField.setPlaceColonyField((PlaceColonyField) place);
    }

    private boolean isAlreadyInSameColony(final Colony colonyWhereWillBeUnitPlaced) {
        final Optional<Colony> oColony = getColony();
        if (oColony.isPresent()) {
            final Colony colony = oColony.get();
            if (colony.getUnitsInsideColony().size() == 1) {
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
    @Override
    public void placeToMap(final Location location) {
        Preconditions.checkNotNull(location);
        Preconditions.checkState(!isAtEuropePort(), "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(), "Unit can't skip from europe port pier to map");
        Preconditions.checkState(
                getType().canMoveAtTerrain(model.getMap().getTerrainTypeAt(location)),
                "Unit '%s' can't be placed at '%s' because can't move on terrain '%s'", this,
                location, model.getMap().getTerrainTypeAt(location));

        placeToLocation(location, getDefaultOrintation());
    }

    @Override
    public void disembarkToLocation(final Location targetLocation) {
        Preconditions.checkNotNull(targetLocation);
        Preconditions.checkState(actionPoints > 0,
                "Unit (%s) need for unload at least one action point", this);
        Preconditions.checkState(isAtCargoSlot(),
                "This unit (%s) can't be unload, it's not in cargo slot.", this);
        final TerrainType terrainType = model.getMap().getTerrainTypeAt(targetLocation);
        Preconditions.checkState(getType().getMoveableTerrains().contains(terrainType),
                "This unit (%s) can't move at target terrain %s.", this, terrainType);
        final Location startLocation = getPlaceCargoSlot().getOwnerUnit().getLocation();
        Preconditions.checkState(startLocation.isNeighbor(targetLocation),
                "Start location '%s' have to neighbour of target location '%s'", startLocation,
                targetLocation);
        Preconditions.checkArgument(model.getColonyAt(targetLocation).isEmpty(),
                "Target location %s can't be colony.", targetLocation);

        final Direction orientation = findOrintationForMove(targetLocation);
        final Path path = Path.of(Lists.newArrayList(startLocation, targetLocation));
        if (model.fireUnitMoveStarted(this, path)) {
            model.fireUnitMovedStepStarted(this, startLocation, targetLocation, orientation);
            this.actionPoints = 0;
            placeToLocation(targetLocation, orientation);
            owner.revealMapForUnit(this);
            model.fireUnitMovedStepFinished(this, startLocation, targetLocation);
            model.fireUnitMovedFinished(this, path);
        }
    }

    @Override
    public void embarkFromLocation(final CargoSlot cargoSlot) {
        Preconditions.checkNotNull(cargoSlot);
        Preconditions.checkArgument(cargoSlot.isEmpty(), "Cargo slot (%s) is already loaded.",
                cargoSlot);
        Preconditions.checkState(isAtPlaceLocation(), "Unit have to be at map, it's at '%s'",
                place);
        Preconditions.checkState(cargoSlot.getOwnerUnit().isAtPlaceLocation(),
                "Unit '%s' where other unit would like to embark have to be at map",
                cargoSlot.getOwnerUnit());
        Preconditions.checkArgument(owner.equals(cargoSlot.getOwnerPlayer()),
                "Cargo slot belongs to different player %s than unit %s",
                cargoSlot.getOwnerPlayer(), owner);
        final PlaceCargoSlot placeCargoSlot = new PlaceCargoSlot(this, cargoSlot);
        final Location startLocation = getLocation();
        final Location targetLocation = placeCargoSlot.getCargoSlot().getOwnerUnit().getLocation();
        Preconditions.checkArgument(startLocation.isNeighbor(targetLocation),
                "Start location %s and target locations %s have to be neighbours",
                cargoSlot.getOwnerPlayer(), owner);
        
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

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public PlaceCargoSlot getPlaceCargoSlot() {
        Preconditions.checkState(isAtCargoSlot(), "Unit have to be in cargo slot");
        return (PlaceCargoSlot) place;
    }

    @Override
    public PlaceConstructionSlot getPlaceConstruction() {
        Preconditions.checkState(isAtPlaceConstructionSlot(),
                "Unit have to be in colony construction");
        return (PlaceConstructionSlot) place;
    }

    @Override
    public PlaceColonyField getPlaceColonyField() {
        Preconditions.checkState(isAtPlaceColonyField(), "Unit have to be in colony field");
        return (PlaceColonyField) place;
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * Get basic unit military strength. It could be overridden in unit specific
     * classes.
     *
     * @return Return military strength.
     */
    @Override
    public double getMilitaryStrenght() {
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

    /**
     * Define action supported by unit.
     *
     * @return Return list of action types.
     */
    abstract protected List<UnitActionType> getSupportedActions();

    @Override
    public void setActionType(final UnitActionType actionType) {
        Preconditions.checkNotNull(actionType, "Unit action is null");
        Preconditions.checkState(getSupportedActions().contains(actionType),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(), this);
        setAction(actionType.make());
        actionPoints = 0;
    }

    @Override
    public UnitAction getAction() {
        return unitAction;
    }

    @Override
    public boolean canHoldCargo() {
        return this instanceof UnitWithCargo;
    }

    @Override
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
