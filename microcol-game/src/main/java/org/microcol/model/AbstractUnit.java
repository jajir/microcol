package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.model.unit.Ship;

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
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int actionPoints, final UnitAction unitAction) {
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

    /**
     * Allows to set action points. It could be called just at this class. It's
     * not exposed.
     *
     * @param newActionPoints required new amount of action points
     */
    public void setActionPoints(final int newActionPoints) {
        Preconditions.checkArgument(newActionPoints <= getSpeed(),
                "Unit %s can't have more action point than is it's speed.",
                this);
        this.actionPoints = newActionPoints;
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
        if (unitAction.startTurn(model, this)) {
            setAction(UnitActionType.noAction.make());
            actionPoints = 0;
            model.fireActionWasStarted(this);
        }
    }

    /**
     * Verify if it's possible to move at give location. Method doesn't verify
     * if unit have enough action point is if location is reachable.
     *
     * @param location required map location.
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
     * @param location      required target location
     * @param ignoreEnemies when it's <code>true</code> than enemy unit at
     *                      target location will be ignored
     * @return return <code>true</code> when unit could move to target location
     *         otherwise return <code>false</code>
     */
    @Override
    public boolean isPossibleToMoveAt(final Location location,
            final boolean ignoreEnemies) {
        Preconditions.checkNotNull(location);

        if (!model.getMap().isValid(location)) {
            return false;
        }
        final Optional<Colony> optColony = model.getColonyAt(location);
        if (optColony.isPresent()) {
            final Colony colony = optColony.get();
            if (!owner.equals(colony.getOwner())
                    && !model.areHostile(owner, colony.getOwner())) {
                return false;
            }
        }

        if (!getType()
                .canMoveAtTerrain(model.getMap().getTerrainTypeAt(location))) {
            if (isPossibleToGoToPort(location)) {
                return ignoreEnemies
                        || owner.getEnemyUnitsAt(location).isEmpty();
            }
            return false;
        }

        return ignoreEnemies || owner.getEnemyUnitsAt(location).isEmpty()
                || isPossibleToCaptureColonyAt(location);
    }

    /**
     * Find all reachable locations. It implements Dijkstra's algorithm.
     *
     * @return Return list of location where unit could move.
     */
    @Override
    public List<Location> getAvailableLocations() {
        Preconditions.checkArgument(isAtPlaceLocation(), "Unit have to at map");
        model.checkGameRunning();
        model.checkCurrentPlayer(owner);

        if (actionPoints == 0) {
            return isShip()
                    ? getLocation().getNeighbors().stream()
                            .filter(location -> ((UnitWithCargo) this)
                                    .isPossibleToDisembarkAt(location, true))
                            .collect(Collectors.toList())
                    : Collections.emptyList();
        }

        /*
         * Set will contains locations which should be inspected if could be
         * visited.
         */
        final Set<Location> openSet = new HashSet<>();
        openSet.add(getLocation());

        /*
         * This set will be returned.
         */
        final Set<Location> returnSet = new HashSet<>();

        /*
         * Location that should be highlighted, but should not be counted as
         * movement.
         */
        final Set<Location> finalSet = new HashSet<>();

        for (int i = 0; i < actionPoints + 1; i++) {
            /*
             * Set of all locations that should be inspected.
             */
            final Set<Location> candidateSet = openSet.stream()
                    .flatMap(location -> location.getNeighbors().stream())
                    .filter(location -> !returnSet.contains(location))
                    .filter(location -> model.getMap().isValid(location))
                    .collect(Collectors.toSet());

            finalSet.addAll(candidateSet.stream()
                    .filter(location -> isPossibleToMakeFinalActionAt(location))
                    .collect(Collectors.toSet()));

            returnSet.addAll(openSet);

            /*
             * Prepare new set of border location for next iteration.
             */
            openSet.clear();
            openSet.addAll(candidateSet.stream()
                    .filter(location -> isPossibleToMoveAt(location))
                    .collect(Collectors.toSet()));
        }

        // Returned set should not contain location where unit actually is.
        returnSet.remove(getLocation());

        // Returned set should contain all final location.
        returnSet.addAll(finalSet);

        return ImmutableList.copyOf(new ArrayList<Location>(returnSet));
    }

    /**
     * Provide information if unit could move at give location. It's final
     * action because no further move in given turn not will be possible. For
     * example when ship disembark units that ship could move in next turn.
     *
     * @param location required location
     * @return Return if it's possible make final action at given location.
     */
    private boolean isPossibleToMakeFinalActionAt(final Location location) {
        return (canHoldCargo() && ((UnitWithCargo) this)
                .isPossibleToDisembarkAt(location, true))
                || isPossibleToEmbarkAt(location);
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
                    .filter(entry -> entry.getValue() == valToCheck)
                    .map(entry -> entry.getKey())
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
        return isShip() ? model.isOwnedCoastalPort(owner, moveToLocation)
                : model.getColoniesAt(moveToLocation, owner).isPresent();
    }

    /**
     * Verify that unit can move at target location and there is colony without
     * units to defend it.
     *
     * @param targetLocation required location where should be captured colony
     * @return Return <code>true</code> when target location contains enemy
     *         colony without military units to defend it.
     */
    @Override
    public boolean isPossibleToCaptureColonyAt(final Location targetLocation) {
        return model.getMap().isValid(targetLocation) && model
                .getColonyAt(targetLocation)
                .map(colony -> colony.canBeCapturedBy(this)).orElse(false);
    }

    private boolean isLiveOwnedUnit(final Unit unit) {
        return unit.getOwner() == owner
                && owner.getAllUnits().stream().anyMatch(live -> live == unit);
    }

    private boolean canTransferPassenger() {
        return model.isGameRunning() && model.getCurrentPlayer() == owner
                && isLiveOwnedUnit(this) && isStorable()
                && getAction().getType() == UnitActionType.noAction;
    }

    private boolean hasValidCarrier() {
        if (!isAtCargoSlot()) {
            return false;
        }
        final CargoSlot slot = getPlaceCargoSlot().getCargoSlot();
        final Unit carrier = slot.getOwnerUnit();
        return carrier.isShip() && isLiveOwnedUnit(carrier)
                && ((UnitWithCargo) carrier).getCargo().getSlots().stream()
                        .anyMatch(actual -> actual == slot)
                && slot.getUnit().orElse(null) == this;
    }

    private boolean isPortTransfer(final Unit carrier) {
        return carrier.isAtPlaceLocation()
                && model.isOwnedCoastalPort(owner, carrier.getLocation());
    }

    @Override
    public boolean canEmbark(final CargoSlot slot) {
        Preconditions.checkNotNull(slot);
        if (!canTransferPassenger() || !slot.isEmpty()) {
            return false;
        }
        final Unit carrier = slot.getOwnerUnit();
        if (!carrier.isShip() || !isLiveOwnedUnit(carrier)
                || !((UnitWithCargo) carrier).getCargo().getSlots().stream()
                        .anyMatch(actual -> actual == slot)) {
            return false;
        }
        if (isAtEuropePier()) {
            return carrier.isAtEuropePort();
        }
        if (isAtCargoSlot()) {
            if (!hasValidCarrier()) {
                return false;
            }
            final Unit source = getPlaceCargoSlot().getOwnerUnit();
            return (source.isAtEuropePort() && carrier.isAtEuropePort())
                    || (isPortTransfer(source) && isPortTransfer(carrier)
                            && source.getLocation()
                                    .equals(carrier.getLocation()));
        }
        if (!isAtPlaceLocation() || !carrier.isAtPlaceLocation()
                || !model.getMap().isValid(getLocation())
                || !model.getMap().getTerrainTypeAt(getLocation()).isLand()
                || !carrier.isPossibleToMoveAt(carrier.getLocation())) {
            return false;
        }
        if (getLocation().equals(carrier.getLocation())) {
            return isPortTransfer(carrier);
        }
        return actionPoints > 0
                && getLocation().isNeighbor(carrier.getLocation());
    }

    @Override
    public boolean canDisembarkToEuropePier() {
        return canTransferPassenger() && hasValidCarrier()
                && getPlaceCargoSlot().getOwnerUnit().isAtEuropePort();
    }

    @Override
    public boolean canUnitDisembarkAt(final Location targetLocation) {
        return canUnitDisembarkAt(targetLocation, true);
    }

    @Override
    public boolean canUnitDisembarkAt(final Location targetLocation,
            final boolean inCurrentTurn) {
        Preconditions.checkNotNull(targetLocation);
        if (!canTransferPassenger() || !hasValidCarrier()
                || !model.getMap().isValid(targetLocation)
                || !model.getMap().getTerrainTypeAt(targetLocation).isLand()
                || !getType().canMoveAtTerrain(
                        model.getMap().getTerrainTypeAt(targetLocation))
                || !owner.getEnemyUnitsAt(targetLocation).isEmpty()) {
            return false;
        }
        final Unit carrier = getPlaceCargoSlot().getOwnerUnit();
        if (!carrier.isAtPlaceLocation()
                || !carrier.isPossibleToMoveAt(carrier.getLocation())) {
            return false;
        }
        if (model.getColonyAt(targetLocation).isPresent()
                && !model.isOwnedCoastalPort(owner, targetLocation)) {
            return false;
        }
        if (carrier.getLocation().equals(targetLocation)) {
            return isPortTransfer(carrier);
        }
        return carrier.getLocation().isNeighbor(targetLocation)
                && (!inCurrentTurn || actionPoints > 0);
    }

    @Override
    public boolean isPossibleToEmbarkAt(final Location targetLocation) {
        return getFirstUnitToEmbarkAt(targetLocation).isPresent();
    }

    @Override
    public Optional<UnitWithCargo> getFirstUnitToEmbarkAt(
            final Location targetLocation) {
        Preconditions.checkNotNull(targetLocation);
        if (!isAtPlaceLocation() || !model.getMap().isValid(targetLocation)
                || !getLocation().isNeighbor(targetLocation)) {
            return Optional.empty();
        }
        return model.getUnitsAt(targetLocation).stream().filter(Unit::isShip)
                .map(unit -> (UnitWithCargo) unit)
                .filter(ship -> ship.getCargo().getSlots().stream()
                        .anyMatch(this::canEmbark))
                .findFirst();
    }

    @Override
    public boolean canAttack() {
        return getType().canAttack();
    }

    @Override
    public boolean isPossibleToAttackAt(final Location targetLocation) {
        return findAttackTarget(targetLocation, false).isPresent();
    }

    @Override
    public Optional<AttackOrder> prepareAttack(final Location targetLocation,
            final boolean allowDeclaration) {
        return findAttackTarget(targetLocation, allowDeclaration)
                .map(defender -> new AttackOrder(this, defender));
    }

    private Optional<Unit> findAttackTarget(final Location targetLocation,
            final boolean allowDeclaration) {
        if (!model.isGameRunning() || model.getCurrentPlayer() != owner
                || !isLiveOwnedUnit(this) || !isAtPlaceLocation()
                || !canAttack() || actionPoints <= 0
                || !getAction().isNoAction() || targetLocation == null
                || !model.getMap().isValid(targetLocation)
                || !getLocation().isNeighbor(targetLocation)
                || !owner.isVisible(targetLocation)
                || !getType().canMoveAtTerrain(
                        model.getMap().getTerrainTypeAt(targetLocation))) {
            return Optional.empty();
        }
        final Optional<Colony> peacefulColony = model
                .getColonyAt(targetLocation)
                .filter(colony -> colony.getOwner() != owner
                        && !model.areHostile(owner, colony.getOwner()));
        if (peacefulColony.isPresent() && !allowDeclaration) {
            return Optional.empty();
        }
        return owner.getEnemyUnitsAt(targetLocation).stream()
                .filter(unit -> getType().getAttackableUnitType()
                        .contains(unit.getType()))
                .filter(unit -> allowDeclaration
                        || model.areHostile(owner, unit.getOwner()))
                .filter(unit -> peacefulColony.isEmpty()
                        || peacefulColony.get().getOwner() == unit.getOwner())
                // An existing enemy takes priority over breaking another
                // treaty.
                .sorted(Comparator.comparing(
                        unit -> !model.areHostile(owner, unit.getOwner())))
                .findFirst();
    }

    /**
     * Verify that all units belongs to same owner.
     *
     * @param units required {@link List} of units
     * @return return <code>true</code> when there is not unit in list belonging
     *         to different owner otherwise return <code>false</code>
     */
    @Override
    public boolean isSameOwner(final List<Unit> units) {
        Preconditions.checkNotNull(units);
        return !units.stream()
                .filter(unit -> !getOwner().equals(unit.getOwner())).findFirst()
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
                .map(unit -> (UnitWithCargo) unit)
                .filter(unit -> unit.getCargo().getSlots().stream()
                        .anyMatch(this::canEmbark))
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

        PathFinder finder = new PathFinder(this, getLocation(), destination,
                excludeDestination);

        return Optional.ofNullable(finder.find());
    }

    /**
     * Move one step to adjacent tile.
     *
     * @param moveTo required location when unit should be moved
     */
    @Override
    public void moveOneStep(final Location moveTo) {
        verifyThatUnitIsAtMap();

        model.checkGameRunning();
        model.checkCurrentPlayer(owner);

        Preconditions.checkNotNull(moveTo);
        Preconditions.checkArgument(getLocation().isNeighbor(moveTo),
                "Location (%s) must be neighbor to current location (%s).",
                moveTo, getLocation());
        Preconditions.checkArgument(model.getMap().isValid(moveTo),
                "Location (%s) must be valid.", moveTo);
        Preconditions.checkArgument(isPossibleToMoveAt(moveTo),
                "It's not possible to move at (%s).", moveTo);
        Preconditions.checkState(actionPoints > 0,
                "There is not enough available action points (%s)", this);
        Preconditions.checkState(
                UnitActionType.noAction.equals(getAction().getType()),
                "Unit can't move because already doing something else. Unit %s",
                this);

        if (isShip() && model.getHighSea().isDepartureMove(this, moveTo)) {
            placeToHighSeas(true);
            model.fireUnitMovedToHighSeas(this);
        } else {
            actionPoints--;
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
     * @param moveTo required location where is unit moving
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
     * @param newLocation required new location
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
     * Attacks a currently hostile adjacent target using the shared live checks.
     *
     * @param attackAt required battlefield
     */
    @Override
    public void attack(final Location attackAt) {
        prepareAttack(attackAt, false)
                .orElseThrow(() -> new IllegalStateException(
                        "There is no legal hostile attack at " + attackAt))
                .execute();
    }

    void executeAttack(final AttackOrder order) {
        Preconditions.checkState(
                order.getAttacker() == this && order.matchesParticipants(),
                "The selected battle changed");
        final Unit selected = findAttackTarget(order.getTarget(),
                order.isDeclarationRequired())
                .orElseThrow(() -> new IllegalStateException(
                        "The selected attack is no longer legal"));
        Preconditions.checkState(selected == order.getDefender(),
                "The selected defender changed");
        Preconditions
                .checkState(
                        order.isDeclarationRequired() == !model
                                .areHostile(owner, selected.getOwner()),
                        "The diplomatic state changed");
        final AbstractUnit defender = (AbstractUnit) selected;
        if (order.isDeclarationRequired()) {
            model.declareHostilities(owner, defender.getOwner());
        }
        actionPoints = 0;
        defender.actionPoints = 0;
        final Unit winner = isWinnerAttacker() ? this : defender;
        if (winner == this) {
            model.fireUnitAttacked(this, defender, defender);
            model.destroyUnit(defender);
            if (owner.getEnemyUnitsAt(order.getTarget()).isEmpty()) {
                placeToLocation(order.getTarget());
                tryToCaptureColony(order.getTarget());
            }
        } else {
            model.fireUnitAttacked(this, defender, this);
            model.destroyUnit(this);
        }
    }

    /**
     * Allows to change unit owner.
     *
     * @param player required player will be new unit's owner
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
     * @param placeCargoSlot required place cargo slot
     */
    @Override
    public void placeToCargoSlot(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkNotNull(placeCargoSlot, "Place cargo slot is null");
        Preconditions.checkArgument(
                owner.equals(placeCargoSlot.getCargoSlotOwner()),
                "Cargo slot belongs to different player %s than unit %s",
                placeCargoSlot.getCargoSlotOwner(), owner);
        final CargoSlot slot = placeCargoSlot.getCargoSlot();
        Preconditions.checkState(
                placeCargoSlot.getUnit() == this && canEmbark(slot),
                "Passenger cannot board this cargo slot");
        final int cost = isAtPlaceLocation()
                && !getLocation().equals(slot.getOwnerUnit().getLocation()) ? 1
                        : 0;
        place.destroy();
        place = placeCargoSlot;
        actionPoints -= cost;
        slot.store(placeCargoSlot);
        model.fireUnitEmbarked(this, slot);
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
    public void placeToLocation(final Location target,
            final Direction orientation) {
        if (place instanceof PlaceLocation) {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target),
                    orientation);
        } else {
            place.destroy();
            place = new PlaceLocation(this, Preconditions.checkNotNull(target),
                    orientation);
            model.fireUnitMovedToLocation(this);
        }
    }

    @Override
    public void placeToHighSeas(final boolean isTravelToEurope) {
        Preconditions.checkArgument(getType().isShip(),
                "Only ships could be placed to high sea.");
        model.checkGameRunning();
        model.checkCurrentPlayer(owner);
        Preconditions.checkState(
                owner.getAllUnits().stream().anyMatch(unit -> unit == this),
                "Ship is no longer in this game");
        final Ship ship = (Ship) this;
        final Location origin;
        final Direction direction;
        if (isTravelToEurope) {
            Preconditions.checkState(
                    isAtPlaceLocation() && model.getMap().getTerrainTypeAt(
                            getLocation()) == TerrainType.HIGH_SEA,
                    "Departure requires a ship in a sea lane");
            Preconditions.checkState(
                    actionPoints > 0 && UnitActionType.noAction
                            .equals(getAction().getType()),
                    "Ship cannot depart without an available movement action");
            origin = getLocation();
            direction = model.getHighSea().getDepartureDirection(origin);
        } else {
            Preconditions.checkState(isAtEuropePort(),
                    "Return departure requires the European port");
            origin = ship.getDepartureLocation().orElseGet(() -> model
                    .getHighSea().getDefaultArrivalLocation()
                    .orElseThrow(() -> new IllegalStateException(
                            "No eastern sea-lane arrival is available")));
            direction = model.getHighSea().getDepartureDirection(origin);
        }
        final int requiredTurns = isTravelToEurope
                && direction == Direction.west ? TRAVEL_TO_EUROPE_FROM_WEST
                        : TRAVEL_TO_EUROPE;
        ship.rememberDeparture(origin, direction);
        if (isTravelToEurope) {
            actionPoints--;
        }
        place.destroy();
        place = new PlaceHighSea(this, isTravelToEurope, requiredTurns);
    }

    private final static int TRAVEL_TO_EUROPE = 3;

    private final static int TRAVEL_TO_EUROPE_FROM_WEST = 4;

    @Override
    public void placeToEuropePortPier() {
        Preconditions.checkState(canDisembarkToEuropePier(),
                "Passenger cannot disembark to the European pier");
        place.destroy();
        place = new PlaceEuropePier(this);
    }

    @Override
    public void placeToColonyStructureSlot(
            final ConstructionSlot structureSlot) {
        validateColonyStructureSlot(structureSlot);
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
        validateColonyField(colonyField, producedGoodsType);
        if (isAlreadyInSameColony(colonyField.getColony())) {
            place.destroySimple();
        } else {
            place.destroy();
        }
        place = new PlaceColonyField(this, colonyField, producedGoodsType);
        colonyField.setPlaceColonyField((PlaceColonyField) place);
    }

    protected void validateColonyStructureSlot(
            final ConstructionSlot structureSlot) {
        model.checkTurnPreparationComplete();
        Preconditions.checkNotNull(structureSlot);
        Preconditions.checkState(!isAtEuropePort(),
                "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(),
                "Unit can't skip from europe port pier to map");
        Preconditions.checkState(structureSlot.isEmpty(),
                "Unit can't be placed to non empty colony structure");
        Preconditions.checkState(structureSlot.isValid(),
                "Unit can't be placed to already destroyed colony");
        Preconditions.checkState(structureSlot.canAccept(this),
                "Unit can't be placed to structure that cannot accept worker");
    }

    protected void validateColonyField(final ColonyField colonyField,
            final GoodsType producedGoodsType) {
        model.checkTurnPreparationComplete();
        Preconditions.checkNotNull(colonyField);
        Preconditions.checkState(!isAtEuropePort(),
                "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(),
                "Unit can't skip from europe port pier to map");
        Preconditions.checkState(colonyField.isEmpty(),
                "Unit can't be placed to non empty colony field");
        Preconditions.checkState(colonyField.isValid(),
                "Unit can't be placed to already destroyed colony");
        Preconditions.checkNotNull(producedGoodsType);
        Preconditions.checkArgument(colonyField.canProduce(producedGoodsType),
                "Field cannot produce requested goods");
        Preconditions.checkState(colonyField.canAccept(this, producedGoodsType),
                "Worker or colony field is no longer available");
    }

    private boolean isAlreadyInSameColony(
            final Colony colonyWhereWillBeUnitPlaced) {
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
     * @return colony where the unit works, or empty if it works in no colony
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
     * @param location Required location where is unit placed.
     */
    @Override
    public void placeToMap(final Location location) {
        model.checkTurnPreparationComplete();
        Preconditions.checkNotNull(location);
        if (isAtCargoSlot()) {
            disembarkToLocation(location);
            return;
        }
        Preconditions.checkState(!isAtEuropePort(),
                "Unit can't skip from europe port to map");
        Preconditions.checkState(!isAtEuropePier(),
                "Unit can't skip from europe port pier to map");
        Preconditions.checkState(
                getType().canMoveAtTerrain(
                        model.getMap().getTerrainTypeAt(location)),
                "Unit '%s' can't be placed at '%s' because can't move on terrain '%s'",
                this, location, model.getMap().getTerrainTypeAt(location));

        placeToLocation(location, getDefaultOrintation());
    }

    @Override
    public void disembarkToLocation(final Location targetLocation) {
        Preconditions.checkNotNull(targetLocation);
        Preconditions.checkState(canUnitDisembarkAt(targetLocation),
                "Passenger cannot disembark at %s", targetLocation);
        final Location startLocation = getPlaceCargoSlot().getOwnerUnit()
                .getLocation();
        if (startLocation.equals(targetLocation)) {
            placeToLocation(targetLocation, getDefaultOrintation());
            return;
        }

        final Direction orientation = findOrintationForMove(targetLocation);
        final Path path = Path
                .of(Lists.newArrayList(startLocation, targetLocation));
        if (model.fireUnitMoveStarted(this, path)) {
            Preconditions.checkState(canUnitDisembarkAt(targetLocation),
                    "Passenger transfer changed before landing");
            model.fireUnitMovedStepStarted(this, startLocation, targetLocation,
                    orientation);
            this.actionPoints--;
            placeToLocation(targetLocation, orientation);
            owner.revealMapForUnit(this);
            model.fireUnitMovedStepFinished(this, startLocation,
                    targetLocation);
            model.fireUnitMovedFinished(this, path);
        }
    }

    @Override
    public void embarkFromLocation(final CargoSlot cargoSlot) {
        Preconditions.checkNotNull(cargoSlot);
        Preconditions.checkState(isAtPlaceLocation() && canEmbark(cargoSlot),
                "Passenger cannot embark from this location");
        final Location startLocation = getLocation();
        final Location targetLocation = cargoSlot.getOwnerUnit().getLocation();
        final PlaceCargoSlot placeCargoSlot = new PlaceCargoSlot(this,
                cargoSlot);
        if (startLocation.equals(targetLocation)) {
            placeToCargoSlot(placeCargoSlot);
            return;
        }

        final Direction orientation = findOrintationForMove(targetLocation);
        final Path path = Path
                .of(Lists.newArrayList(startLocation, targetLocation));

        if (model.fireUnitMoveStarted(this, path)) {
            Preconditions.checkState(
                    isAtPlaceLocation() && canEmbark(cargoSlot),
                    "Passenger transfer changed before boarding");
            model.fireUnitMovedStepStarted(this, startLocation, targetLocation,
                    orientation);

            placeToCargoSlot(placeCargoSlot);

            model.fireUnitMovedStepFinished(this, startLocation,
                    targetLocation);
            model.fireUnitMovedFinished(this, path);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id)
                .add("type", getType()).add("owner", owner)
                .add("place", place == null ? place : place.toString())
                .add("availableMoves", actionPoints)
                .add("unitAction", unitAction).toString();
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
        Preconditions.checkState(isAtCargoSlot(),
                "Unit have to be in cargo slot");
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
        Preconditions.checkState(isAtPlaceColonyField(),
                "Unit have to be in colony field");
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
        this.unitAction = Preconditions.checkNotNull(unitAction,
                "Unit action is null");
        Preconditions.checkState(
                getSupportedActions().contains(unitAction.getType()),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(),
                this);
    }

    /**
     * Define action supported by unit.
     *
     * @return Return list of action types.
     */
    abstract protected List<UnitActionType> getSupportedActions();

    @Override
    public void setActionType(final UnitActionType actionType) {
        model.checkTurnPreparationComplete();
        Preconditions.checkNotNull(actionType, "Unit action is null");
        Preconditions.checkState(getSupportedActions().contains(actionType),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(),
                this);
        setAction(actionType.make());
        actionPoints = 0;
        model.fireActionWasStarted(this);
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
        if (getSupportedActions().contains(UnitActionType.plowField)
                && actionPoints > 0 && isAtPlaceLocation()) {
            final TerrainType targetTerrain = model.getMap()
                    .getTerrainTypeAt(getLocation());
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
