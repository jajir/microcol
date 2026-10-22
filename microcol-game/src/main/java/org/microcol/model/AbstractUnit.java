package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.microcol.model.store.TradeRouteStopPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventGoTo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionFortify;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitActionBuildRoad;
import org.microcol.model.unit.UnitActionClearForest;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;
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
    /** Exact remaining movement; see {@link MovementPoints}. */
    private int movementThirds;
    private UnitAction unitAction;
    /** Persistent go-to order, or {@code null} when the unit has none. */
    private GoToOrder goTo;

    /** Route stop processed by this carrier; cleared when it leaves. */
    private TradeRouteVisit processedTradeRouteVisit;

    /** Persistent route assignment; it excludes go-to and manual orders. */
    private TradeRouteAssignment tradeRouteAssignment;
    /** Waiting immigrant on the European pier; cleared when it boards. */
    private boolean immigrant;
    /** Player withdrew this waiting immigrant from departure boarding. */
    private boolean boardingOptOut;
    /**
     * Player's single current front-of-queue choice among willing immigrants.
     */
    private boolean boardingPriority;

    public AbstractUnit(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int movementThirds, final UnitAction unitAction) {
        Preconditions.checkNotNull(placeBuilder, "PlaceBuilder is null");
        this.owner = Preconditions.checkNotNull(owner);
        this.id = Preconditions.checkNotNull(id, "Id is null");
        this.model = Preconditions.checkNotNull(model, "Model is null");
        Preconditions.checkArgument(movementThirds >= 0,
                "Movement cannot be negative (%s)", movementThirds);
        this.movementThirds = movementThirds;
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
     * Get number of whole action points left. A fraction of a point is not
     * counted; whole-point actions need at least one point.
     *
     * @return available whole action points.
     */
    @Override
    public int getActionPoints() {
        return movementThirds / MovementPoints.THIRDS_PER_POINT;
    }

    /**
     * Get exact remaining movement.
     *
     * @return remaining movement in thirds of a point
     */
    @Override
    public int getMovementThirds() {
        return movementThirds;
    }

    /**
     * Allows to set whole action points, for example when a unit is prepared in
     * a test or spends its allowance.
     *
     * @param newActionPoints required new amount of whole action points
     */
    public void setActionPoints(final int newActionPoints) {
        setMovementThirds(MovementPoints.ofPoints(newActionPoints));
    }

    /**
     * Allows to set exact remaining movement, for example when a saved unit is
     * loaded.
     *
     * @param newMovementThirds required new movement in thirds of a point
     */
    public void setMovementThirds(final int newMovementThirds) {
        Preconditions.checkArgument(newMovementThirds >= 0
                && newMovementThirds <= MovementPoints.ofPoints(getSpeed()),
                "Unit %s can't have more movement than is it's speed.", this);
        this.movementThirds = newMovementThirds;
    }

    /**
     * @return whether this unit still has its full allowance for the turn, so
     *         that one step beyond that allowance is permitted
     */
    boolean hasFullMovement() {
        return movementThirds > 0
                && movementThirds == MovementPoints.ofPoints(getSpeed());
    }

    /**
     * Whether the remaining movement pays this entry cost. A unit at its full
     * allowance may take one step that costs more and finish at zero.
     *
     * @param costThirds required entry cost in thirds
     * @return {@code true} when the step may be made
     */
    private boolean canPayEntryCost(final int costThirds) {
        return costThirds <= movementThirds || hasFullMovement();
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
        return owner.getEffectiveSpeed(getType());
    }

    /**
     * It's called before turn starts.
     */
    public void startTurn() {
        if (this instanceof Ship && ((Ship) this).isDamaged()) {
            movementThirds = 0;
            return;
        }
        movementThirds = MovementPoints.ofPoints(getSpeed());
        if (unitAction.startTurn(model, this)) {
            setAction(UnitActionType.noAction.make());
            movementThirds = 0;
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

        if (this instanceof Ship && ((Ship) this).isDamaged()) {
            return false;
        }
        return MovementPolicy.isPossibleToEnter(model, getType(), isShip(),
                owner, location, ignoreEnemies);
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

        if (movementThirds == 0) {
            return isShip()
                    ? getLocation().getNeighbors().stream()
                            .filter(location -> ((UnitWithCargo) this)
                                    .isPossibleToDisembarkAt(location, true))
                            .collect(Collectors.toList())
                    : Collections.emptyList();
        }

        /*
         * Terrain reach uses the same entry costs as moving, so the highlighted
         * squares are the squares a move can actually end on.
         */
        final Set<Location> returnSet = new PathFinder(this).findReachable();

        /*
         * Final actions such as embarking are checked from the current place.
         */
        returnSet.addAll(getLocation().getNeighbors().stream()
                .filter(location -> model.getMap().isValid(location))
                .filter(location -> isPossibleToMakeFinalActionAt(location))
                .collect(Collectors.toSet()));

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
        final Location center = getLocation();
        final int radius = getSightRadius();
        final List<Location> visible = new ArrayList<>();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY()
                    + radius; y++) {
                final Location candidate = Location.of(x, y);
                if (model.getMap().isValid(candidate)) {
                    visible.add(candidate);
                }
            }
        }
        return ImmutableList.copyOf(visible);
    }

    @Override
    public int getSightRadius() {
        return withFounderSight(isShip() ? 2 : 1);
    }

    /** Apply the owner's current exploration bonus to an ordinary role. */
    protected final int withFounderSight(final int ordinaryRadius) {
        return owner.getCongressMembers().contains(Founder.HERNANDO_DE_SOTO)
                ? Math.max(ordinaryRadius, 2)
                : ordinaryRadius;
    }

    @Override
    public boolean isPossibleToGoToPort(final Location moveToLocation) {
        return isShip() ? model.isOwnedCoastalPort(owner, moveToLocation)
                : model.getColoniesAt(moveToLocation, owner).isPresent();
    }

    private boolean isLiveOwnedUnit(final Unit unit) {
        return unit.getOwner() == owner
                && owner.getAllUnits().stream().anyMatch(live -> live == unit);
    }

    private boolean canTransferPassenger() {
        return canTransferPassenger(false);
    }

    private boolean canTransferPassenger(final boolean departureSentry) {
        if (isAtCargoSlot()
                && getPlaceCargoSlot().getOwnerUnit() instanceof Ship
                && ((Ship) getPlaceCargoSlot().getOwnerUnit()).isDamaged()) {
            return false;
        }
        return model.isGameRunning() && model.getCurrentPlayer() == owner
                && isLiveOwnedUnit(this) && isStorable() && !hasStandingOrder()
                && (getAction().getType() == UnitActionType.noAction
                        || departureSentry && getAction()
                                .getType() == UnitActionType.sentry);
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

    private List<Unit> getPassengersSnapshot() {
        return ((UnitWithCargo) this).getCargo().getSlots().stream()
                .flatMap(slot -> slot.getUnit().stream())
                .collect(Collectors.toList());
    }

    /**
     * Verify before any mutation that the complete arrival plan can land at the
     * owned coastal port this ship is about to enter. The plan contains the
     * passengers already aboard and departure sentries that would board in the
     * same step. Boarding sentries are not in a cargo slot yet, so only their
     * terrain eligibility is checked here.
     *
     * @param port     required destination of the ship
     * @param boarding required departure sentries planned for this step
     */
    private void verifyArrivalPlan(final Location port,
            final List<Boarding> boarding) {
        for (final Unit passenger : getPassengersSnapshot()) {
            Preconditions.checkState(passenger.canUnitDisembarkAt(port, false),
                    "Passenger %s cannot land at port %s", passenger, port);
        }
        final TerrainType terrain = model.getMap().getTerrainTypeAt(port);
        for (final Boarding assignment : boarding) {
            Preconditions.checkState(
                    assignment.passenger.getType().canMoveAtTerrain(terrain),
                    "Passenger %s cannot land at port %s", assignment.passenger,
                    port);
        }
    }

    /**
     * Unload all passengers of the ship that has just entered its owned coastal
     * port. Passengers keep their identity, equipment and action points.
     *
     * @param port required port where the ship is located
     */
    private void unloadPassengersAt(final Location port) {
        for (final Unit passenger : getPassengersSnapshot()) {
            passenger.disembarkToLocation(port);
        }
    }

    @Override
    public boolean canEmbark(final CargoSlot slot) {
        return canEmbark(slot, false);
    }

    /**
     * Whether this unit is a departure passenger candidate. A waiting immigrant
     * depends only on its willingness, so an opt-out is not revived by a sentry
     * order; other units need the sentry order.
     */
    private boolean isDepartureBoardingCandidate() {
        return immigrant ? isWillingToBoard()
                : getAction().getType() == UnitActionType.sentry;
    }

    boolean canEmbarkAsDeparture(final CargoSlot slot) {
        return isDepartureBoardingCandidate() && canEmbark(slot, true);
    }

    private boolean canEmbark(final CargoSlot slot,
            final boolean departureSentry) {
        Preconditions.checkNotNull(slot);
        if (!canTransferPassenger(departureSentry) || !slot.isEmpty()) {
            return false;
        }
        final Unit carrier = slot.getOwnerUnit();
        if (!carrier.isShip()
                || carrier instanceof Ship && ((Ship) carrier).isDamaged()
                || !isLiveOwnedUnit(carrier)
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
        return getActionPoints() > 0
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
                && (!inCurrentTurn || getActionPoints() > 0);
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
        return findBattle(targetLocation, false).isPresent();
    }

    @Override
    public Optional<AttackOrder> prepareAttack(final Location targetLocation,
            final boolean allowDeclaration) {
        return findBattle(targetLocation, allowDeclaration)
                .map(battle -> new AttackOrder(this, battle.defender,
                        battle.defense));
    }

    /** The selected defender and, for a colony tile, its frozen defense. */
    private static final class Battle {
        private final Unit defender;
        private final Optional<ColonyDefense> defense;

        private Battle(final Unit defender,
                final Optional<ColonyDefense> defense) {
            this.defender = defender;
            this.defense = defense;
        }
    }

    private Optional<Battle> colonyBattle(final ColonyDefense defense) {
        return CombatStrengths.forColonyBattle(model, this, defense).isPresent()
                ? Optional.of(
                        new Battle(defense.getDefender(), Optional.of(defense)))
                : Optional.empty();
    }

    private Optional<Battle> findBattle(final Location targetLocation,
            final boolean allowDeclaration) {
        if (!model.isGameRunning() || model.getCurrentPlayer() != owner
                || !isLiveOwnedUnit(this) || !isAtPlaceLocation()
                || !canAttack() || getActionPoints() <= 0
                || !getAction().isNoAction() || hasStandingOrder()
                || targetLocation == null
                || !model.getMap().isValid(targetLocation)
                || !getLocation().isNeighbor(targetLocation)
                || !owner.isVisible(targetLocation)
                || !owner.isCurrentlyVisible(targetLocation)
                || !getType().canMoveAtTerrain(
                        model.getMap().getTerrainTypeAt(targetLocation))) {
            return Optional.empty();
        }
        // The target tile is a foreign colony center, or the field tile one of
        // its workers works; the worker has no map location of its own.
        final Optional<Colony> centerColony = model.getColonyAt(targetLocation)
                .filter(colony -> colony.getOwner() != owner);
        final Optional<Colony> fieldColony = centerColony.isPresent()
                ? Optional.empty()
                : model.getColonies().stream()
                        .filter(colony -> colony.getOwner() != owner
                                && colony.hasWorkerAt(targetLocation))
                        .findFirst();
        // A native raid has no colonial treaty to honour; it fights the unit
        // standing on the tile instead of the colony roster.
        final Optional<Colony> peacefulColony = owner.isNative()
                ? Optional.empty()
                : centerColony.or(() -> fieldColony).filter(
                        colony -> !model.areHostile(owner, colony.getOwner()));
        if (peacefulColony.isPresent() && (!allowDeclaration || model
                .isAttackRestrained(owner, peacefulColony.get().getOwner()))) {
            return Optional.empty();
        }
        final List<Unit> eligible = owner.getEnemyUnitsAt(targetLocation)
                .stream()
                .filter(unit -> getType().getAttackableUnitType()
                        .contains(unit.getType()))
                // Natives raid only colonial units. A colonial European attacks
                // a native only as an explicit decision (allowDeclaration), so
                // the passive hostility query and the European AI never start
                // one; the Crown never fights natives.
                .filter(unit -> owner.isNative()
                        ? unit.getOwner().isColonialNation()
                        : !unit.getOwner().isNative()
                                || owner.isColonialNation() && allowDeclaration)
                // A native battle always has quoted strengths; it never falls
                // back to the legacy even-odds fight.
                .filter(unit -> !(owner.isNative()
                        || unit.getOwner().isNative())
                        || CombatStrengths.forBattle(model, this, unit)
                                .isPresent())
                // Allies, and former allies during the war wait, are never
                // attacked, not even by declaration or privateer exception.
                .filter(unit -> !model.isAttackRestrained(owner,
                        unit.getOwner()))
                .filter(unit -> allowDeclaration
                        || !AttackOrder.isDeclarationNeeded(model, owner,
                                unit.getOwner(),
                                model.isPrivateerNavalException(this, unit)))
                .filter(unit -> peacefulColony.isEmpty()
                        || peacefulColony.get().getOwner() == unit.getOwner())
                .collect(Collectors.toList());
        if (centerColony.isPresent() && !isShip() && !owner.isNative()) {
            // Residents of fields and buildings defend too, so the colony
            // roster, not the map-only occupant list, selects the defender. A
            // peaceful colony needs the ordinary declaration review first.
            return centerColony.get().planDefense(this, eligible)
                    .flatMap(this::colonyBattle);
        }
        if (fieldColony.isPresent() && !isShip() && !owner.isNative()
                && eligible.isEmpty()) {
            return fieldColony.get().planFieldDefense(this, targetLocation)
                    .flatMap(this::colonyBattle);
        }
        return eligible.stream()
                // An existing enemy takes priority over breaking another
                // treaty.
                .sorted((left, right) -> {
                    final int hostility = Boolean.compare(
                            !model.areHostile(owner, left.getOwner()),
                            !model.areHostile(owner, right.getOwner()));
                    if (hostility != 0) {
                        return hostility;
                    }
                    final Optional<CombatStrengths> leftValues = CombatStrengths
                            .forBattle(model, this, left);
                    final Optional<CombatStrengths> rightValues = CombatStrengths
                            .forBattle(model, this, right);
                    if (leftValues.isPresent() != rightValues.isPresent()) {
                        return leftValues.isPresent() ? -1 : 1;
                    }
                    final int strength = leftValues.isPresent()
                            ? Double.compare(
                                    rightValues.get().getDefender().getTotal(),
                                    leftValues.get().getDefender().getTotal())
                            : 0;
                    return strength != 0 ? strength
                            : Integer.compare(left.getId(), right.getId());
                }).findFirst()
                .map(defender -> new Battle(defender, Optional.empty()));
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

        return Optional.ofNullable(
                new PathFinder(this).find(destination, excludeDestination));
    }

    @Override
    public Optional<List<Location>> getKnownPath(final Location destination,
            final boolean excludeDestination) {
        verifyThatUnitIsAtMap();

        return Optional.ofNullable(new PathFinder(this, false, owner::isVisible)
                .find(destination, excludeDestination));
    }

    @Override
    public int getEntryCostThirds(final Location from, final Location to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        // Ships keep one whole point for every sea-lane, lake or port step.
        if (isShip()) {
            return MovementPoints.THIRDS_PER_POINT;
        }
        // A connected road needs a road on both squares of the step; a river
        // course needs its own segment. Either one gives the same third.
        final boolean connectedRoad = model.getMap().hasRoadAt(from)
                && model.getMap().hasRoadAt(to);
        final boolean connectedRiver = model.getMap().hasRiverSegment(from, to);
        return model.getMap().getTerrainTypeAt(to).getMoveCostThirds(
                model.getMap().hasTreesAt(to), connectedRoad || connectedRiver);
    }

    @Override
    public boolean canAffordStepTo(final Location location) {
        Preconditions.checkNotNull(location);
        return isAtPlaceLocation() && model.getMap().isValid(location)
                && getLocation().isNeighbor(location)
                && canPayEntryCost(getEntryCostThirds(getLocation(), location));
    }

    @Override
    public void verifyPathIsLegal(final List<Location> path) {
        Preconditions.checkNotNull(path);
        verifyThatUnitIsAtMap();
        model.checkGameRunning();
        model.checkCurrentPlayer(owner);
        Location at = getLocation();
        for (final Location step : path) {
            Preconditions.checkNotNull(step);
            Preconditions.checkArgument(at.isNeighbor(step),
                    "Location (%s) must be neighbor to current location (%s).",
                    step, at);
            Preconditions.checkArgument(model.getMap().isValid(step),
                    "Location (%s) must be valid.", step);
            Preconditions.checkArgument(isPossibleToMoveAt(step),
                    "It's not possible to move at (%s).", step);
            at = step;
        }
    }

    @Override
    public boolean canAffordPath(final List<Location> path) {
        Preconditions.checkNotNull(path);
        if (!isAtPlaceLocation()) {
            return false;
        }
        int remaining = movementThirds;
        boolean firstStep = true;
        Location at = getLocation();
        for (final Location step : path) {
            if (!at.isNeighbor(step) || !isPossibleToMoveAt(step)) {
                return false;
            }
            final int cost = getEntryCostThirds(at, step);
            if (cost <= remaining) {
                remaining -= cost;
            } else if (firstStep && hasFullMovement()) {
                // The one expensive step of a full allowance ends the turn.
                remaining = 0;
            } else {
                return false;
            }
            firstStep = false;
            at = step;
        }
        return true;
    }

    /**
     * Move one step to adjacent tile.
     *
     * @param moveTo required location when unit should be moved
     */
    @Override
    public void moveOneStep(final Location moveTo) {
        verifyCanMoveOneStep(moveTo);

        if (isShip() && model.getHighSea().isDepartureMove(this, moveTo)) {
            placeToHighSeas(true);
            model.fireUnitMovedToHighSeas(this);
        } else {
            final Location start = getLocation();
            final Direction orientation = findOrintationForMove(moveTo);
            model.fireUnitMovedStepStarted(this, start, moveTo, orientation);
            // Pre-move listeners may change this unit, its cargo, its owner's
            // colonies or the destination. Validate the live step again before
            // the first mutation, including boarding and passenger landing.
            Preconditions.checkState(getLocation().equals(start),
                    "Unit %s changed location before its step", this);
            verifyCanMoveOneStep(moveTo);
            final List<Boarding> boarding = new ArrayList<>();
            if (isShip()) {
                boarding.addAll(collectDepartureSentries((Ship) this));
            }
            final boolean arrivesAtOwnedPort = isShip()
                    && model.isOwnedCoastalPort(owner, moveTo);
            if (arrivesAtOwnedPort) {
                verifyArrivalPlan(moveTo, boarding);
            }
            boardDepartureSentries(boarding);
            // The full-turn exception may cost more than the allowance; it
            // then finishes at zero.
            movementThirds = Math.max(0,
                    movementThirds - getEntryCostThirds(start, moveTo));
            placeToLocation(moveTo, orientation);
            owner.revealMapForUnit(this);
            model.wakeSentriesForMovement(this, moveTo);
            if (arrivesAtOwnedPort) {
                unloadPassengersAt(moveTo);
            }
            model.fireUnitMovedStepFinished(this, start, moveTo);
            // A committed scout step may consume a lost-city rumor; the unit
            // can be lost there, which stops the rest of its path.
            model.resolveLostCityRumor(this, moveTo);
        }
    }

    private void verifyCanMoveOneStep(final Location moveTo) {
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
        Preconditions.checkState(
                canPayEntryCost(getEntryCostThirds(getLocation(), moveTo)),
                "There is not enough movement (%s) to enter (%s)", this,
                moveTo);
        Preconditions.checkState(
                UnitActionType.noAction.equals(getAction().getType()),
                "Unit can't move because already doing something else. Unit %s",
                this);
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
        // A confirmed order is the explicit decision a native target needs.
        final Battle battle = findBattle(order.getTarget(),
                order.isDeclarationRequired()
                        || order.getDefender().getOwner().isNative())
                .orElseThrow(() -> new IllegalStateException(
                        "The selected attack is no longer legal"));
        final Unit selected = battle.defender;
        Preconditions.checkState(selected == order.getDefender(),
                "The selected defender changed");
        // The live roster, actual places, Revere inputs and ship escapes must
        // equal the quote; nothing has changed yet when this rejects.
        Preconditions.checkState(
                battle.defense.equals(order.getColonyDefense()),
                "The colony defense changed");
        Preconditions.checkState(order.matchesPromotionInputs(),
                "The combat experience inputs changed");
        final boolean privateerException = model.isPrivateerNavalException(this,
                selected);
        final EuropeanRelationState currentRelation = AttackOrder
                .isEuropeanPair(owner, selected.getOwner())
                        ? model.getEuropeanRelation(owner, selected.getOwner())
                        : null;
        Preconditions.checkState(
                order.isPrivateerException() == privateerException
                        && order.getOriginalRelation() == currentRelation
                        && order.isDeclarationRequired() == AttackOrder
                                .isDeclarationNeeded(model, owner,
                                        selected.getOwner(),
                                        privateerException),
                "The diplomatic state changed");
        // Actual aggression, not the declaration alone, is recorded and
        // obliges the victim's allies. Plan it before any random draw, combat
        // or action-point change; it is applied where the declaration was.
        final Set<Player> victims = new HashSet<>();
        victims.add(selected.getOwner());
        model.getColonyAt(order.getTarget())
                .filter(colony -> colony.getOwner() != owner
                        && model.areHostile(owner, colony.getOwner()))
                .ifPresent(colony -> victims.add(colony.getOwner()));
        final AggressionPlan aggression = model.planAggression(owner, victims,
                privateerException);
        final Optional<CombatStrengths> strengths = battle.defense.isPresent()
                ? CombatStrengths.forColonyBattle(model, this,
                        battle.defense.get())
                : CombatStrengths.forBattle(model, this, selected);
        Preconditions.checkState(strengths.equals(order.getCombatStrengths()),
                "The battle strength changed");
        Preconditions.checkState(
                strengths.isEmpty()
                        || strengths.get().getAttacker().getTotal() > 0,
                "Attacker has no combat strength");
        final Optional<NavalCombatPreview> naval = isShip() && selected.isShip()
                ? Optional.of(new NavalCombatPreview(model, (Ship) this,
                        (Ship) selected))
                : Optional.empty();
        Preconditions.checkState(naval.equals(order.getNavalPreview()),
                "The naval cargo, market or repair destination changed");
        final AbstractUnit defender = (AbstractUnit) selected;
        if (battle.defense.isPresent()) {
            executeColonyAttack(battle.defense.get(), strengths.orElseThrow(),
                    aggression);
            return;
        }
        if (naval.isPresent()) {
            executeNavalAttack(order, (Ship) selected, strengths.orElseThrow(),
                    naval.get(), aggression);
            return;
        }
        // A native brave's relation effect is planned before the draw, applied
        // once with the committed battle, and no later check can refuse it.
        final Optional<NativeRelationOutcome> nativeEffect = selected.getOwner()
                .isNative()
                        ? Optional
                                .of(model.planNativeUnitAttack(owner, selected))
                        : Optional.empty();
        final boolean nativeFight = owner.isNative()
                || selected.getOwner().isNative();
        final boolean winnerAttacker = strengths.isPresent()
                ? strengths.get().attackerWins(model.nextCombatRandom())
                : isWinnerAttacker();
        model.applyAggression(aggression);
        movementThirds = 0;
        defender.movementThirds = 0;
        nativeEffect.ifPresent(
                outcome -> model.applyNativeRelationOutcome(owner, outcome));
        if (strengths.isPresent()) {
            final AbstractUnit loser = winnerAttacker ? defender : this;
            if (isDestroyedByDefeat(loser, nativeFight)) {
                final Unit winner = winnerAttacker ? this : defender;
                model.fireUnitAttacked(this, defender, loser, true,
                        promoteWinningColonist(winner));
                model.destroyUnit(loser);
                if (loser == defender && canOccupyWonTile(order.getTarget())) {
                    placeToLocation(order.getTarget());
                    model.wakeSentriesForMovement(this, order.getTarget());
                }
                return;
            }
            loser.loseCombatEquipment();
            final Unit winner = winnerAttacker ? this : defender;
            model.fireUnitAttacked(this, defender, loser, false,
                    promoteWinningColonist(winner));
            return;
        }
        final Unit winner = winnerAttacker ? this : defender;
        if (winner == this) {
            model.fireUnitAttacked(this, defender, defender, true,
                    promoteWinningColonist(this));
            model.destroyUnit(defender);
            if (canOccupyWonTile(order.getTarget())) {
                placeToLocation(order.getTarget());
                model.wakeSentriesForMovement(this, order.getTarget());
            }
        } else {
            if (this instanceof UnitArtillery
                    && !((UnitArtillery) this).isDamaged()) {
                loseCombatEquipment();
                model.fireUnitAttacked(this, defender, this, false);
                return;
            }
            model.fireUnitAttacked(this, defender, this, true,
                    promoteWinningColonist(defender));
            model.destroyUnit(this);
        }
    }

    /**
     * One battle of a native settlement attack (R-0174). The caller validated
     * the terms and the strengths before this call; the only draw happens here,
     * once. A defeated native brave is destroyed, and a loser of the attacker's
     * side keeps the ordinary native loss rule. Both sides spend their
     * movement, which is the one attack budget of the whole settlement attack.
     * The winning colonist gets the ordinary promotion roll of one victory, and
     * the promotion travels with the battle event so the player sees it. The
     * strengths are the ones the player confirmed, so a promotion won here does
     * not change the terms of the next battle of the same attack.
     *
     * @param defender  required native brave on the settlement square
     * @param strengths required confirmed strengths of this battle
     * @return the outcome of this battle
     */
    NativeBattle fightNativeDefender(final Unit defender,
            final CombatStrengths strengths) {
        final boolean winnerAttacker = strengths
                .attackerWins(model.nextCombatRandom());
        movementThirds = 0;
        ((AbstractUnit) defender).movementThirds = 0;
        final AbstractUnit loser = winnerAttacker ? (AbstractUnit) defender
                : this;
        final MilitaryPromotion promotion = promoteWinningColonist(
                winnerAttacker ? this : defender);
        if (isDestroyedByDefeat(loser, true)) {
            model.fireUnitAttacked(this, defender, loser, true, promotion);
            model.destroyUnit(loser);
        } else {
            loser.loseCombatEquipment();
            model.fireUnitAttacked(this, defender, loser, false, promotion);
        }
        return new NativeBattle(winnerAttacker, promotion);
    }

    /** Outcome of one battle of a native settlement attack. */
    static final class NativeBattle {
        private final boolean won;
        private final MilitaryPromotion promotion;

        private NativeBattle(final boolean won,
                final MilitaryPromotion promotion) {
            this.won = won;
            this.promotion = promotion;
        }

        /** @return whether the attacker won this battle */
        boolean isWon() {
            return won;
        }

        /** @return the attacker's promotion earned here, or null for none */
        MilitaryPromotion getPromotion() {
            return won ? promotion : null;
        }
    }

    /**
     * Enter the square of a conquered native settlement once its last defender
     * was overcome (R-0174). The settlement has been left empty of defenders.
     *
     * @param square required square of the conquered settlement
     */
    void occupyNativeSquare(final Location square) {
        placeToLocation(square);
        model.wakeSentriesForMovement(this, square);
    }

    /**
     * Resolves a battle for a colony tile after every precondition, the ship
     * escape plan included, was validated against the frozen quote. Nothing
     * before the first draw changes the model.
     */
    private void executeColonyAttack(final ColonyDefense defense,
            final CombatStrengths strengths, final AggressionPlan aggression) {
        final Colony colony = defense.getColony();
        final Location battlefield = defense.getBattlefield();
        final ColonyDefense.Kind kind = defense.getKind();
        final AbstractUnit defender = (AbstractUnit) defense.getDefender();
        final boolean winnerAttacker = strengths
                .attackerWins(model.nextCombatRandom());
        model.applyAggression(aggression);
        movementThirds = 0;
        defender.movementThirds = 0;
        if (kind == ColonyDefense.Kind.REVERE) {
            // The emergency militia costs stored muskets only now that the
            // battle is confirmed; it is never free equipment.
            colony.getWarehouse().removeGoods(
                    Goods.of(GoodsType.MUSKET, defense.getRevereMuskets()));
            ((UnitFreeColonist) defender).setHoldingGuns(true);
        }
        final MilitaryPromotion promotion = promoteWinningColonist(
                winnerAttacker ? this : defender);
        if (winnerAttacker && defense.capturesColony()) {
            // The civilian group or the emergency militia lost: the whole
            // colony, every job included, changes hands once.
            if (kind == ColonyDefense.Kind.REVERE) {
                defender.loseCombatEquipment();
            }
            model.fireUnitAttacked(this, defender, defender, false, promotion,
                    new ColonyBattleOutcome(colony, battlefield, kind,
                            ColonyBattleOutcome.Result.COLONY_CAPTURED, false));
            placeToLocation(battlefield);
            model.wakeSentriesForMovement(this, battlefield);
            colony.captureAfterWonBattle(this, defense);
            return;
        }
        if (winnerAttacker && kind == ColonyDefense.Kind.FIELD
                && !CombatStrengths.isArmedLand(defender)) {
            // An unarmed field worker is captured on its own; the colony and
            // every other job stay. If it was the last resident, the colony
            // is removed by the shared completed-exit rules.
            model.fireUnitAttacked(this, defender, defender, false, promotion,
                    new ColonyBattleOutcome(colony, battlefield, kind,
                            ColonyBattleOutcome.Result.WORKER_CAPTURED, false));
            defender.takeOver(owner);
            defender.leaveJobAfterBattle(getLocation(), false);
            return;
        }
        final AbstractUnit loser = winnerAttacker ? defender : this;
        final boolean militiaExits = !winnerAttacker
                && kind == ColonyDefense.Kind.REVERE && defense.isRevereExit();
        final boolean destroyed = isDestroyedByDefeat(loser);
        final ColonyBattleOutcome.Result result = winnerAttacker
                ? destroyed ? ColonyBattleOutcome.Result.DEFENDER_DESTROYED
                        : ColonyBattleOutcome.Result.DEFENDER_EQUIPMENT_LOST
                : destroyed ? ColonyBattleOutcome.Result.ATTACKER_DESTROYED
                        : ColonyBattleOutcome.Result.ATTACKER_EQUIPMENT_LOST;
        if (destroyed) {
            model.fireUnitAttacked(this, defender, loser, true, promotion,
                    new ColonyBattleOutcome(colony, battlefield, kind, result,
                            false));
            model.destroyUnit(loser);
            return;
        }
        loser.loseCombatEquipment();
        model.fireUnitAttacked(this, defender, loser, false, promotion,
                new ColonyBattleOutcome(colony, battlefield, kind, result,
                        militiaExits));
        if (militiaExits) {
            // A victory over an attacker is a forced, completed exit of the
            // armed worker (liberty cap applied once); only the last resident
            // stays inside with its muskets.
            defender.leaveJobAfterBattle(colony.getLocation(), true);
        }
    }

    private MilitaryPromotion promoteWinningColonist(final Unit winner) {
        // A resident in a job fights and is promoted like a unit on the map.
        if (!(winner instanceof UnitFreeColonist)) {
            return null;
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) winner;
        if (!colonist.isHoldingGuns() || colonist.isVeteran()
                || colonist.getMilitaryRole().isSpecial()) {
            return null;
        }
        final boolean washington = winner.getOwner().getCongressMembers()
                .contains(Founder.GEORGE_WASHINGTON);
        if (!washington && model.nextCombatRandom() >= 0.25) {
            return null;
        }
        return colonist.promoteAfterCombatVictory();
    }

    private void executeNavalAttack(final AttackOrder order,
            final Ship defender, final CombatStrengths strengths,
            final NavalCombatPreview preview, final AggressionPlan aggression) {
        // Both saved draws are consumed exactly once after all preflight
        // checks.
        final boolean winnerAttacker = strengths
                .attackerWins(model.nextCombatRandom());
        final boolean drawSinks = model.nextCombatRandom() < 0.5;
        final Ship winner = winnerAttacker ? (Ship) this : defender;
        final Ship loser = winnerAttacker ? defender : (Ship) this;
        final boolean noRepair = (winnerAttacker
                ? preview.getDefenderRepairDestination()
                : preview.getAttackerRepairDestination()).isEmpty();
        final boolean sunk = drawSinks || noRepair;
        model.applyAggression(aggression);
        movementThirds = 0;
        ((AbstractUnit) defender).movementThirds = 0;
        final List<Goods> captured = new ArrayList<>();
        for (final NavalCombatPreview.Transfer transfer : preview
                .loot(winnerAttacker)) {
            final CargoSlot source = loser.getCargo()
                    .getSlotByIndex(transfer.source);
            final CargoSlot target = winner.getCargo()
                    .getSlotByIndex(transfer.destination);
            source.removeCargo(transfer.goods);
            target.addGoods(transfer.goods);
            captured.add(transfer.goods);
        }
        final NavalCombatOutcome outcome = new NavalCombatOutcome(model,
                preview, winner, loser, sunk, captured);
        if (sunk) {
            if (noRepair && !drawSinks) {
                model.damageShip(loser);
            } else {
                model.destroyUnit(loser);
            }
        } else {
            model.damageShip(loser);
        }
        model.fireUnitAttacked(this, defender, loser, outcome);
    }

    /**
     * A won ordinary battle may be followed by stepping onto the emptied tile.
     * A foreign colony tile is never entered that way: its defense battle has
     * its own capture.
     */
    private boolean canOccupyWonTile(final Location target) {
        // Taking a native settlement square is conquest (R-0174), not a win.
        return owner.getEnemyUnitsAt(target).isEmpty()
                && model.getNativeSettlements().stream().noneMatch(
                        settlement -> settlement.getLocation().equals(target))
                && model.getColonyAt(target)
                        .map(colony -> colony.getOwner() == owner).orElse(true);
    }

    /**
     * Completed exit of a resident from its job after a colony battle. The
     * place is replaced through the shared completed-exit accounting (liberty
     * cap, teaching, removal of a colony left without residents), without the
     * voluntary fortified minimum, because a battle result is not a voluntary
     * choice.
     *
     * @param target               required tile the resident stands on
     *                             afterwards
     * @param requireOtherResident whether another resident must stay behind
     */
    private void leaveJobAfterBattle(final Location target,
            final boolean requireOtherResident) {
        Preconditions.checkState(
                isAtPlaceColonyField() || isAtPlaceConstructionSlot(),
                "Only a resident leaves a job");
        Preconditions.checkState(
                !requireOtherResident
                        || getColony().orElseThrow().getPopulation() > 1,
                "The last resident stays in the colony");
        place.destroy();
        place = new PlaceLocation(this, Preconditions.checkNotNull(target),
                getDefaultOrintation());
        model.fireUnitMovedToLocation(this);
    }

    protected void loseCombatEquipment() {
        throw new IllegalStateException("Unsupported combat equipment loss");
    }

    /**
     * @return whether losing a land battle removes this unit with its equipment
     *         instead of the ordinary equipment loss
     */
    protected boolean isDestroyedWhenDefeated() {
        return false;
    }

    private static boolean isDestroyedByDefeat(final AbstractUnit loser) {
        return loser instanceof UnitArtillery
                && ((UnitArtillery) loser).isDamaged()
                || loser.isDestroyedWhenDefeated();
    }

    /**
     * A native battle destroys an unarmed European loser, such as a colonist or
     * a wagon, instead of capturing it; armed soldiers and artillery keep their
     * ordinary equipment loss.
     */
    private static boolean isDestroyedByDefeat(final AbstractUnit loser,
            final boolean nativeFight) {
        return isDestroyedByDefeat(loser)
                || nativeFight && !CombatStrengths.isArmedLand(loser);
    }

    protected void clearFortificationAfterDefeat() {
        if (unitAction.getType() == UnitActionType.fortify) {
            setAction(UnitActionType.noAction.make());
            model.fireActionWasStarted(this);
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

    @Override
    public boolean isAtPlaceNativeCourse() {
        return place instanceof PlaceNativeCourse;
    }

    @Override
    public boolean isAtPlaceNativeMission() {
        return place instanceof PlaceNativeMission;
    }

    /**
     * Move a unit from the map into the native course of a settlement (R-0148).
     * Movement and any order are spent, so the learner cannot move, work or
     * fight while the course lasts. Its equipment and identity stay unchanged.
     *
     * @param settlementId identity of the settlement that teaches
     */
    void beginNativeCourse(final int settlementId) {
        Preconditions.checkState(isAtPlaceLocation(),
                "Only a unit on the map can begin a native course");
        place.destroy();
        place = new PlaceNativeCourse(this, settlementId);
        movementThirds = 0;
        goTo = null;
        setAction(UnitActionType.noAction.make());
    }

    /**
     * Move a missionary from the map into the mission of a settlement (R-0164).
     * Movement and any order are spent, so the missionary cannot move or act
     * while it is stationed. Its identity, owner and role stay unchanged.
     *
     * @param settlementId identity of the settlement that holds the mission
     */
    void beginNativeMission(final int settlementId) {
        Preconditions.checkState(isAtPlaceLocation(),
                "Only a unit on the map can begin a native mission");
        place.destroy();
        place = new PlaceNativeMission(this, settlementId);
        movementThirds = 0;
        goTo = null;
        setAction(UnitActionType.noAction.make());
    }

    /**
     * This unit will be moved to given place cargo slot.
     *
     * @param placeCargoSlot required place cargo slot
     */
    @Override
    public void placeToCargoSlot(final PlaceCargoSlot placeCargoSlot) {
        Preconditions.checkState(canLeaveColony(),
                "Fortified colony needs three residents");
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
                && !getLocation().equals(slot.getOwnerUnit().getLocation())
                        ? MovementPoints.THIRDS_PER_POINT
                        : 0;
        place.destroy();
        place = placeCargoSlot;
        // Boarding ends the waiting state, so a later return to the pier does
        // not revive the immigrant's old willingness or front choice.
        clearBoardingState();
        movementThirds -= cost;
        slot.store(placeCargoSlot);
        model.fireUnitEmbarked(this, slot);
    }

    void beginShipRepair(final PlaceShipRepair repair) {
        Preconditions.checkState(isAtPlaceLocation(),
                "A ship must be on the map to become damaged");
        place.destroy();
        place = repair;
        // A repair is not a departure: a processed stop stays processed if the
        // ship returns to the same place, and is dropped if it does not.
        movementThirds = 0;
        setAction(UnitActionType.noAction.make());
        if (goTo != null) {
            // A damaged ship cannot sail; the order ends with a reason.
            model.addTurnEvent(TurnEventProvider.getGoToOrder(owner,
                    TurnEventGoTo.Kind.CANCELLED_DAMAGED, id, getType(),
                    goTo.getDestinationName()));
            goTo = null;
        }
    }

    void completeShipRepair(final Location location) {
        Preconditions.checkState(place instanceof PlaceShipRepair);
        place.destroy();
        place = new PlaceLocation(this, location, getDefaultOrintation());
        retainProcessedTradeRouteVisitAt(location);
    }

    void completeShipRepair(final EuropePort port) {
        Preconditions.checkState(place instanceof PlaceShipRepair);
        place.destroy();
        place = new PlaceEuropePort(this, port);
        retainProcessedTradeRouteVisitAt(null);
    }

    @Override
    public void placeToEuropePort(final EuropePort port) {
        Preconditions.checkState(
                !(this instanceof Ship) || !((Ship) this).isDamaged(),
                "Ship is damaged");
        Preconditions.checkState(canLeaveColony(),
                "Fortified colony needs three residents");
        place.destroy();
        place = new PlaceEuropePort(this, Preconditions.checkNotNull(port));
        retainProcessedTradeRouteVisitAt(null);
    }

    @Override
    public Optional<TradeRouteVisit> getProcessedTradeRouteVisit() {
        return Optional.ofNullable(processedTradeRouteVisit);
    }

    /**
     * Records a processed stop. A second mark before the carrier leaves is
     * refused, which keeps one arrival to one processing.
     */
    @Override
    public void markProcessedTradeRouteVisit(final TradeRouteVisit visit) {
        Preconditions.checkNotNull(visit, "Visit is null");
        Preconditions.checkState(processedTradeRouteVisit == null,
                "Trade route stop already processed by %s", this);
        processedTradeRouteVisit = visit;
    }

    /**
     * Restores a saved processed-stop mark. Loading moves nothing and processes
     * nothing; the mark only keeps the saved visit from repeating.
     *
     * @param routeId   saved route ID, validated against routes later
     * @param stopIndex saved stop index
     * @param at        saved map square of the visit, or {@code null} for
     *                  Europe
     */
    public void restoreProcessedTradeRouteVisit(final int routeId,
            final int stopIndex, final Location at) {
        // A ship in repair has no place to compare; its return re-checks.
        Preconditions.checkArgument(
                place instanceof PlaceShipRepair
                        || (at == null ? isAtEuropePort()
                                : isAtPlaceLocation()
                                        && getLocation().equals(at)),
                "Processed trade route stop does not match the place of %s",
                this);
        processedTradeRouteVisit = new TradeRouteVisit(routeId, stopIndex, at);
    }

    /**
     * Only a real change of place ends the processed stop. Placing the unit
     * again where the visit happened, or repairing it in the same port, keeps
     * the stop processed; leaving and arriving again allows a new visit.
     *
     * @param newPlace map square the unit is placed at, or {@code null} for the
     *                 Europe port
     */
    private void retainProcessedTradeRouteVisitAt(final Location newPlace) {
        if (processedTradeRouteVisit != null
                && !processedTradeRouteVisit.isAt(newPlace)) {
            processedTradeRouteVisit = null;
        }
    }

    /** Departure on the high seas always ends the processed stop. */
    private void forgetProcessedTradeRouteVisit() {
        processedTradeRouteVisit = null;
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
        Preconditions.checkState(
                !(this instanceof Ship) || !((Ship) this).isDamaged(),
                "Ship is damaged");
        Preconditions.checkState(unitAction.getType() != UnitActionType.fortify,
                "Cancel fortification before moving");
        Preconditions.checkState(canLeaveColony(),
                "Fortified colony needs three residents");
        retainProcessedTradeRouteVisitAt(target);
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
        Preconditions.checkState(!ship.isDamaged(), "Ship is damaged");
        final Location origin;
        final Direction direction;
        if (isTravelToEurope) {
            Preconditions.checkState(
                    isAtPlaceLocation() && model.getMap().getTerrainTypeAt(
                            getLocation()) == TerrainType.HIGH_SEA,
                    "Departure requires a ship in a sea lane");
            Preconditions.checkState(
                    getActionPoints() > 0 && UnitActionType.noAction
                            .equals(getAction().getType()),
                    "Ship cannot depart without an available movement action");
            origin = getLocation();
            direction = model.getHighSea().getDepartureDirection(origin);
        } else {
            Preconditions.checkState(isAtEuropePort(),
                    "Return departure requires the European port");
            Preconditions.checkState(
                    getAction().getType() == UnitActionType.noAction,
                    "Ship must be activated before departing Europe");
            origin = ship.getDepartureLocation().orElseGet(() -> model
                    .getHighSea().getDefaultArrivalLocation()
                    .orElseThrow(() -> new IllegalStateException(
                            "No eastern sea-lane arrival is available")));
            direction = model.getHighSea().getDepartureDirection(origin);
        }
        final java.util.List<Boarding> boarding = collectDepartureSentries(
                ship);
        final PlaceHighSea voyage = createVoyage(isTravelToEurope, direction);
        boardDepartureSentries(boarding);
        ship.rememberDeparture(origin, direction);
        if (isTravelToEurope) {
            movementThirds -= MovementPoints.THIRDS_PER_POINT;
        }
        place.destroy();
        place = voyage;
        forgetProcessedTradeRouteVisit();
    }

    private void boardDepartureSentries(
            final java.util.List<Boarding> boarding) {
        for (final Boarding assignment : boarding) {
            assignment.passenger.setAction(UnitActionType.noAction.make());
            assignment.passenger.placeToCargoSlot(
                    new PlaceCargoSlot(assignment.passenger, assignment.slot));
        }
    }

    private static final class Boarding {
        private final AbstractUnit passenger;
        private final CargoSlot slot;

        private Boarding(final AbstractUnit passenger, final CargoSlot slot) {
            this.passenger = passenger;
            this.slot = slot;
        }
    }

    private java.util.List<Boarding> collectDepartureSentries(final Ship ship) {
        final java.util.List<Boarding> result = new java.util.ArrayList<>();
        final java.util.List<CargoSlot> free = ship.getCargo().getSlots()
                .stream().filter(CargoSlot::isEmpty)
                .collect(Collectors.toList());
        // The player's front choice goes first; the rest keep stable ID order.
        final java.util.List<Unit> candidates = owner.getAllUnits().stream()
                .filter(unit -> unit != ship)
                .filter(unit -> unit.isAtPlaceLocation()
                        || unit.isAtEuropePier())
                .filter(unit -> ((AbstractUnit) unit)
                        .isDepartureBoardingCandidate())
                .sorted(java.util.Comparator
                        .comparing((Unit unit) -> !unit.isBoardingPriority())
                        .thenComparingInt(Unit::getId))
                .collect(Collectors.toList());
        for (final Unit candidate : candidates) {
            for (final CargoSlot slot : free) {
                if (((AbstractUnit) candidate).canEmbarkAsDeparture(slot)) {
                    result.add(new Boarding((AbstractUnit) candidate, slot));
                    free.remove(slot);
                    break;
                }
            }
        }
        return result;
    }

    PlaceHighSea createVoyage(final boolean toEurope,
            final Direction direction) {
        final int weather = random.nextInt(3) - 1;
        final int duration = AtlanticVoyageDuration.calculate(weather, toEurope,
                direction, getType().getSpeed(), getOwner().getCongressMembers()
                        .contains(Founder.FERDINAND_MAGELLAN));
        return new PlaceHighSea(this, toEurope, weather, duration, duration);
    }

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
        structureSlot.getColony().grantLaSalleStockade();
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
        colonyField.getColony().grantLaSalleStockade();
        model.recordNativeLandUse(getOwner(), colonyField.getLocation());
    }

    protected void validateColonyStructureSlot(
            final ConstructionSlot structureSlot) {
        model.checkTurnPreparationComplete();
        Preconditions.checkState(unitAction.getType() != UnitActionType.sentry,
                "Activate sentry before assigning colony work");
        Preconditions.checkState(!hasStandingOrder(),
                "Cancel go-to before assigning colony work");
        Preconditions.checkState(unitAction.getType() != UnitActionType.fortify,
                "Cancel fortification before assigning colony work");
        Preconditions.checkNotNull(structureSlot);
        Preconditions.checkState(
                canLeaveColony()
                        || isAlreadyInSameColony(structureSlot.getColony()),
                "Fortified colony needs three residents");
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
        Preconditions.checkState(unitAction.getType() != UnitActionType.sentry,
                "Activate sentry before assigning colony work");
        Preconditions.checkState(!hasStandingOrder(),
                "Cancel go-to before assigning colony work");
        Preconditions.checkState(unitAction.getType() != UnitActionType.fortify,
                "Cancel fortification before assigning colony work");
        Preconditions.checkNotNull(colonyField);
        Preconditions.checkState(
                canLeaveColony()
                        || isAlreadyInSameColony(colonyField.getColony()),
                "Fortified colony needs three residents");
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
        return getColony()
                .filter(colony -> colony == colonyWhereWillBeUnitPlaced)
                .isPresent();
    }

    @Override
    public boolean canLeaveColony() {
        return getColony().map(Colony::canVoluntarilyLoseResident).orElse(true);
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
            movementThirds -= MovementPoints.THIRDS_PER_POINT;
            placeToLocation(targetLocation, orientation);
            owner.revealMapForUnit(this);
            model.wakeSentriesForMovement(this, targetLocation);
            model.fireUnitMovedStepFinished(this, startLocation,
                    targetLocation);
            model.fireUnitMovedFinished(this, path);
            model.resolveLostCityRumor(this, targetLocation);
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
                .add("movementThirds", movementThirds)
                .add("unitAction", unitAction).toString();
    }

    public UnitPo save() {
        final UnitPo unitPo = new UnitPo();
        unitPo.setId(id);
        unitPo.setMovementThirds(movementThirds);
        unitPo.setOwnerId(owner.getName());
        unitPo.setType(getType());
        unitPo.setAction(unitAction.save());
        unitPo.setImmigrant(immigrant);
        unitPo.setBoardingOptOut(boardingOptOut);
        unitPo.setBoardingPriority(boardingPriority);
        if (goTo != null) {
            unitPo.setGoToDestination(goTo.getDestination());
            unitPo.setGoToColonyId(goTo.getColonyId());
            unitPo.setGoToDestinationName(goTo.getDestinationName());
            unitPo.setGoToPaused(goTo.isPaused());
        }
        if (tradeRouteAssignment != null) {
            unitPo.setTradeRouteAssignmentRouteId(
                    tradeRouteAssignment.getRouteId());
            unitPo.setTradeRouteAssignmentStopIndex(
                    tradeRouteAssignment.getStopIndex());
            unitPo.setTradeRouteAssignmentPause(
                    tradeRouteAssignment.getPause().orElse(null));
            unitPo.setTradeRouteAssignmentStops(tradeRouteAssignment.getStops()
                    .stream().map(TradeRoutes::savePoStop)
                    .collect(Collectors.toList()));
        }
        if (processedTradeRouteVisit != null) {
            unitPo.setTradeRouteVisitRouteId(
                    processedTradeRouteVisit.getRouteId());
            unitPo.setTradeRouteVisitStopIndex(
                    processedTradeRouteVisit.getStopIndex());
            unitPo.setTradeRouteVisitLocation(
                    processedTradeRouteVisit.getLocation().orElse(null));
        }
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

    void setWaitingOrder(final UnitActionType actionType) {
        Preconditions.checkArgument(actionType == UnitActionType.sentry
                || actionType == UnitActionType.noAction);
        Preconditions.checkState(
                !hasStandingOrder() || actionType != UnitActionType.sentry,
                "Cancel go-to before sentry");
        setAction(actionType.make());
        model.fireActionWasStarted(this);
    }

    void wakeSentry() {
        Preconditions.checkState(unitAction.getType() == UnitActionType.sentry);
        setWaitingOrder(UnitActionType.noAction);
    }

    /** Mark a unit created by paid recruitment or earned immigration. */
    void becomeImmigrant() {
        Preconditions.checkState(isAtEuropePier(),
                "Immigrant must arrive on the European pier");
        immigrant = true;
        boardingOptOut = false;
        boardingPriority = false;
    }

    /**
     * Restore a saved waiting immigrant. The factory validates the saved place
     * and flags; loading neither boards nor moves the unit.
     *
     * @param optOut   whether the immigrant was withdrawn from boarding
     * @param priority whether it was the owner's front choice
     */
    public void restoreImmigrant(final boolean optOut, final boolean priority) {
        immigrant = true;
        boardingOptOut = optOut;
        boardingPriority = priority;
    }

    private void clearBoardingState() {
        immigrant = false;
        boardingOptOut = false;
        boardingPriority = false;
    }

    @Override
    public boolean isImmigrant() {
        return immigrant;
    }

    @Override
    public boolean isWillingToBoard() {
        return immigrant && !boardingOptOut;
    }

    @Override
    public boolean isBoardingOptedOut() {
        return boardingOptOut;
    }

    @Override
    public boolean isBoardingPriority() {
        return boardingPriority;
    }

    /**
     * Set or clear the opt-out. Withdrawing a willing immigrant also drops its
     * front choice.
     *
     * @param optOut required new opt-out state
     */
    void setBoardingOptOut(final boolean optOut) {
        Preconditions.checkState(immigrant,
                "Only an immigrant has boarding willingness");
        boardingOptOut = optOut;
        if (optOut) {
            boardingPriority = false;
        }
    }

    /**
     * Set or clear the front-of-queue choice. The model keeps at most one
     * choice per owner.
     *
     * @param priority required new front-of-queue state
     */
    void setBoardingPriority(final boolean priority) {
        Preconditions.checkState(!priority || isWillingToBoard(),
                "Only a willing immigrant can lead boarding");
        boardingPriority = priority;
    }

    void startFortifying() {
        Preconditions.checkState(unitAction.isNoAction() && !hasStandingOrder(),
                "Cancel go-to before fortifying");
        setAction(new UnitActionFortify(false));
        movementThirds = 0;
        model.fireActionWasStarted(this);
    }

    void cancelFortifying() {
        Preconditions
                .checkState(unitAction.getType() == UnitActionType.fortify);
        setAction(UnitActionType.noAction.make());
        model.fireActionWasStarted(this);
    }

    void cancelPlowing() {
        Preconditions
                .checkState(unitAction.getType() == UnitActionType.plowField);
        setAction(UnitActionType.noAction.make());
        model.fireActionWasStarted(this);
    }

    void startClearingForest() {
        Preconditions.checkState(unitAction.isNoAction() && !hasStandingOrder(),
                "Cancel go-to before clearing forest");
        final UnitActionClearForest next = new UnitActionClearForest();
        if (getType() == UnitType.HARDY_PIONEER) {
            next.setRemainingTurns(
                    UnitActionClearForest.HARDY_TURNS_TO_CLEAR_FOREST);
        }
        setAction(next);
        movementThirds = 0;
        model.fireActionWasStarted(this);
    }

    void cancelClearingForest() {
        Preconditions
                .checkState(unitAction.getType() == UnitActionType.clearForest);
        setAction(UnitActionType.noAction.make());
        model.fireActionWasStarted(this);
    }

    void startBuildingRoad() {
        Preconditions.checkState(unitAction.isNoAction() && !hasStandingOrder(),
                "Cancel go-to before building road");
        final UnitActionBuildRoad next = new UnitActionBuildRoad();
        if (getType() == UnitType.HARDY_PIONEER) {
            next.setRemainingTurns(
                    UnitActionBuildRoad.HARDY_TURNS_TO_BUILD_ROAD);
        }
        setAction(next);
        movementThirds = 0;
        model.fireActionWasStarted(this);
    }

    void cancelBuildingRoad() {
        Preconditions
                .checkState(unitAction.getType() == UnitActionType.buildRoad);
        setAction(UnitActionType.noAction.make());
        model.fireActionWasStarted(this);
    }

    /**
     * Go-to order of this unit, or empty when it has none.
     *
     * @return the order retained for later owner turns
     */
    Optional<GoToOrder> getGoToOrder() {
        return Optional.ofNullable(goTo);
    }

    /**
     * Stores a new go-to order. The model validates the request beforehand.
     *
     * @param order required order; the unit must not have another order
     */
    void startGoTo(final GoToOrder order) {
        Preconditions.checkState(!hasStandingOrder(),
                "Unit %s has a standing order", this);
        goTo = Preconditions.checkNotNull(order);
    }

    /**
     * @return whether a go-to order or a trade route assignment controls this
     *         unit, so no manual order or other standing order may replace it
     */
    boolean hasStandingOrder() {
        return goTo != null || tradeRouteAssignment != null;
    }

    /**
     * @return the persistent trade route assignment, or empty when there is
     *         none
     */
    Optional<TradeRouteAssignment> getTradeRouteAssignment() {
        return Optional.ofNullable(tradeRouteAssignment);
    }

    /**
     * Stores a new trade route assignment. The model has validated it and the
     * unit has no other standing order; nothing moves here.
     *
     * @param assignment required assignment
     */
    void startTradeRouteAssignment(final TradeRouteAssignment assignment) {
        Preconditions.checkState(!hasStandingOrder(),
                "Unit %s has a standing order", this);
        tradeRouteAssignment = Preconditions.checkNotNull(assignment);
    }

    /**
     * Restores a saved trade route assignment. The unit must be idle and placed
     * on the map, in Europe or at sea. Loading moves nothing and processes no
     * stop; the route and stop are checked against the saved routes later.
     *
     * @param routeId   saved stable route ID
     * @param stopIndex saved index of the current stop
     * @param pause     saved reason the carrier waits, or {@code null}
     * @param stops     required saved boundary copy of the route stops
     */
    public void restoreTradeRouteAssignment(final int routeId,
            final int stopIndex, final TradeRoutePause pause,
            final List<TradeRouteStopPo> stops) {
        Preconditions.checkArgument(
                routeId >= 0 && stopIndex >= 0 && stops != null
                        && !hasStandingOrder() && unitAction.isNoAction()
                        && (isAtPlaceLocation() || isAtEuropePort()
                                || isAtHighSea()
                                || place instanceof PlaceShipRepair),
                "Invalid saved trade route assignment for unit %s", this);
        tradeRouteAssignment = new TradeRouteAssignment(routeId, stopIndex,
                pause, stops.stream().map(TradeRoutes::restoreStop)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns the unit to manual orders. Its place, cargo, movement and any
     * voyage stay as they are.
     */
    void clearTradeRouteAssignment() {
        Preconditions.checkState(tradeRouteAssignment != null,
                "Unit %s has no trade route assignment", this);
        tradeRouteAssignment = null;
    }

    /**
     * Forgets a processed stop that names a deleted route. The route cannot be
     * processed again, so no settled transfer can repeat.
     *
     * @param routeId ID of the route being deleted
     */
    void forgetProcessedTradeRouteVisitOf(final int routeId) {
        if (processedTradeRouteVisit != null
                && processedTradeRouteVisit.getRouteId() == routeId) {
            processedTradeRouteVisit = null;
        }
    }

    /**
     * Forgets a processed stop receipt for one stop of a route whose identity
     * was replaced, so a saved receipt never disagrees with its stop.
     *
     * @param routeId   ID of the edited route
     * @param stopIndex index of the replaced stop
     */
    void forgetProcessedTradeRouteVisitOf(final int routeId,
            final int stopIndex) {
        if (processedTradeRouteVisit != null
                && processedTradeRouteVisit.getRouteId() == routeId
                && processedTradeRouteVisit.getStopIndex() == stopIndex) {
            processedTradeRouteVisit = null;
        }
    }

    /**
     * Restores a saved go-to order. The saved unit must be on the map and not
     * busy. A named colony is checked against the loaded colonies by the model
     * and again when the order advances. Nothing moves during loading.
     *
     * @param destination     required saved destination square
     * @param colonyId        identity of the named colony, or {@code null}
     * @param destinationName required saved name for notices
     * @param paused          whether the saved route was paused
     */
    public void restoreGoTo(final Location destination, final Integer colonyId,
            final String destinationName, final boolean paused) {
        Preconditions.checkArgument(
                isAtPlaceLocation() && unitAction.isNoAction()
                        && destination != null
                        && model.getMap().isValid(destination)
                        && !destination.equals(getLocation())
                        && destinationName != null && !destinationName.isBlank()
                        && (colonyId == null || colonyId >= 0),
                "Invalid saved go-to order for unit %s", this);
        startGoTo(
                new GoToOrder(destination, colonyId, destinationName, paused));
    }

    /**
     * Clears the order without moving the unit or refunding its movement.
     */
    void cancelGoTo() {
        Preconditions.checkState(goTo != null, "Unit %s has no go-to order",
                this);
        goTo = null;
    }

    /**
     * Drop the go-to order of a unit the game has just removed. No pause or
     * cancellation notice is due: the removal's own turn notice explains it.
     */
    void discardGoToOrder() {
        goTo = null;
    }

    /**
     * Walks the saved route as far as this turn's movement pays for it. Each
     * step is an ordinary legal move, so costs and passage rules match manual
     * movement. Arrival clears the order and keeps unused movement. A missing
     * route pauses the order once; a named colony that is gone or no longer
     * friendly cancels it. Blocked squares are never entered, so no enemy is
     * attacked.
     */
    void advanceGoTo() {
        if (goTo == null || !isAtPlaceLocation()) {
            return;
        }
        if (goTo.getColonyId() != null && !isNamedDestinationFriendly()) {
            model.fireActionWasStarted(this);
            return;
        }
        final boolean wasPaused = goTo.isPaused();
        final List<Location> walked = new ArrayList<>();
        boolean blocked = false;
        boolean refused = false;
        // Every step replans from the live position, because a step can reveal
        // an obstruction. Each pass either spends movement or ends the walk.
        while (goTo != null && isAtPlaceLocation()) {
            if (getLocation().equals(goTo.getDestination())) {
                goTo = null;
                break;
            }
            final Optional<List<Location>> route = findGoToRoute(
                    goTo.getDestination());
            if (route.isEmpty() || route.get().isEmpty()) {
                blocked = true;
                break;
            }
            final Location step = route.get().get(0);
            if (!canAffordStepTo(step)) {
                break;
            }
            try {
                moveOneStep(step);
            } catch (final IllegalStateException | IllegalArgumentException e) {
                // The shared step rules refused it; nothing was changed.
                blocked = true;
                refused = true;
                break;
            }
            walked.add(step);
        }
        if (goTo != null) {
            goTo.setPaused(blocked);
            if (blocked && !wasPaused) {
                model.addTurnEvent(TurnEventProvider.getGoToOrder(owner,
                        TurnEventGoTo.Kind.PAUSED, id, getType(),
                        goTo.getDestinationName()));
            }
        }
        if (!walked.isEmpty()) {
            model.fireUnitMovedFinished(this, Path.of(walked));
        } else if (refused && isAtPlaceLocation()) {
            // A refused step may already have announced its start; the walk
            // still ends, on the square where the unit stayed.
            model.fireUnitMovedFinished(this, Path.of(List.of(getLocation())));
        }
        if (goTo == null || goTo.isPaused() != wasPaused) {
            model.fireActionWasStarted(this);
        }
    }

    /**
     * Route of a go-to order from the live position. It is the cheapest route
     * of the shared finder, and a ship's route never contains a high-sea
     * departure step, so an order cannot send a ship to Europe. The place is
     * read once: a unit that leaves the map during a user-interface query, for
     * example by departing on the moving thread, has no route.
     *
     * @param destination required destination square
     * @return the route without the start square, or empty when none exists
     */
    Optional<List<Location>> findGoToRoute(final Location destination) {
        Preconditions.checkNotNull(destination);
        final Place current = getPlace();
        if (!(current instanceof PlaceLocation)) {
            return Optional.empty();
        }
        final Location start = ((PlaceLocation) current).getLocation();
        if (destination.equals(start)) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                new PathFinder(this, true).find(start, destination, false));
    }

    /**
     * Route to the nearest square the owner can see that satisfies the goal, by
     * the same shared finder and costs as a go-to order; no high-sea departure
     * step is part of it.
     *
     * @param isGoal required test of the squares to reach
     * @return the route without the start square, or empty when none exists
     */
    Optional<List<Location>> findNearestRoute(
            final Predicate<Location> isGoal) {
        Preconditions.checkNotNull(isGoal);
        final Place current = getPlace();
        if (!(current instanceof PlaceLocation)) {
            return Optional.empty();
        }
        return Optional.ofNullable(new PathFinder(this, true).findNearest(
                ((PlaceLocation) current).getLocation(),
                location -> owner.isVisible(location)
                        && isGoal.test(location)));
    }

    /**
     * Verifies that the colony named by the order still exists at the saved
     * destination and still belongs to the owner. Otherwise the order is
     * canceled with its reason.
     *
     * @return {@code true} when the order may continue
     */
    private boolean isNamedDestinationFriendly() {
        final Optional<Colony> colony = model.getColonyById(goTo.getColonyId())
                .filter(Colony::isValid);
        if (colony.isEmpty()
                || !colony.get().getLocation().equals(goTo.getDestination())) {
            cancelGoToWithNotice(TurnEventGoTo.Kind.CANCELLED_REMOVED);
            return false;
        }
        if (colony.get().getOwner() != owner) {
            cancelGoToWithNotice(TurnEventGoTo.Kind.CANCELLED_NOT_FRIENDLY);
            return false;
        }
        return true;
    }

    private void cancelGoToWithNotice(final TurnEventGoTo.Kind kind) {
        model.addTurnEvent(TurnEventProvider.getGoToOrder(owner, kind, id,
                getType(), goTo.getDestinationName()));
        goTo = null;
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
        Preconditions.checkState(!hasStandingOrder(),
                "Cancel go-to before giving this unit an order");
        Preconditions
                .checkState(
                        unitAction.getType() != UnitActionType.sentry
                                && actionType != UnitActionType.sentry
                                && unitAction
                                        .getType() != UnitActionType.fortify
                                && actionType != UnitActionType.fortify
                                && actionType != UnitActionType.clearForest
                                && actionType != UnitActionType.buildRoad,
                        "Use the dedicated command for this order");
        Preconditions.checkState(getSupportedActions().contains(actionType),
                "Unsupported type '%s' for this unit '%s", unitAction.getType(),
                this);
        if (actionType == UnitActionType.plowField) {
            final Optional<PlowProblem> problem = model.getPlowProblem(this);
            Preconditions.checkState(problem.isEmpty(), "Unit cannot plow: %s",
                    problem.orElse(null));
        }
        final UnitAction next = actionType.make();
        if (actionType == UnitActionType.plowField
                && getType() == UnitType.HARDY_PIONEER) {
            ((UnitActionPlowField) next).setRemainingTurns(
                    UnitActionPlowField.HARDY_TURNS_TO_PLOW_FIELD);
        }
        setAction(next);
        movementThirds = 0;
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
        return getSupportedActions().contains(UnitActionType.plowField)
                && model.canPlowField(this);
    }

    /**
     * @return the model
     */
    protected Model getModel() {
        return model;
    }

}
