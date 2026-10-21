package org.microcol.model;

import java.util.List;
import java.util.Optional;

import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitWithCargo;

/**
 * MicroCol unit.
 */
public interface Unit {

    boolean isShip();

    /**
     * Get owner of unit.
     *
     * @return owner player object
     */
    Player getOwner();

    /**
     * Get unit type.
     *
     * @return unit type
     */
    UnitType getType();

    /**
     * Whole action points left. A fraction of a point is not counted, so this
     * is right for whole-point actions only.
     *
     * @return whole points of remaining movement
     */
    int getActionPoints();

    /**
     * Exact remaining movement.
     *
     * @return remaining movement in thirds of a point
     */
    int getMovementThirds();

    /**
     * Movement that stepping from one adjacent location into another costs this
     * unit. It is the same value that moving, path planning and reach use. A
     * road discount needs a road on both squares.
     *
     * @param from required square the step starts on
     * @param to   required adjacent square the step enters
     * @return entry cost in movement thirds
     */
    int getEntryCostThirds(Location from, Location to);

    /**
     * Whether this unit may enter an adjacent location from its current place
     * now. It does not check legality of the square itself.
     *
     * @param location required adjacent location
     * @return {@code true} when the entry cost is affordable
     */
    boolean canAffordStepTo(Location location);

    /**
     * Verifies, without changing anything, that every step of the path is legal
     * for this unit when walked in order from its current place. It does not
     * check movement points.
     *
     * @param path required consecutive locations
     * @throws IllegalArgumentException when a step is not adjacent, not on the
     *                                  map or not enterable by this unit
     * @throws IllegalStateException    when the game or the unit cannot act now
     */
    void verifyPathIsLegal(List<Location> path);

    /**
     * Whether the path can be walked from the current place in this turn, step
     * by step with the same costs as moving.
     *
     * @param path required consecutive locations
     * @return {@code true} when every step is legal and affordable
     */
    boolean canAffordPath(List<Location> path);

    Location getLocation();

    boolean canUnitDisembarkAt(Location targetLocation);

    boolean canUnitDisembarkAt(Location targetLocation, boolean inCurrentTurn);

    boolean canEmbark(CargoSlot cargoSlot);

    boolean canDisembarkToEuropePier();

    void setActionType(UnitActionType actionType);

    UnitAction getAction();

    boolean isStorable();

    boolean isAtCargoSlot();

    boolean isAtHighSea();

    boolean isAtPlaceLocation();

    boolean isAtEuropePort();

    boolean isAtEuropePier();

    /**
     * Whether this unit is a waiting immigrant. Only a new paid or earned
     * arrival on the European pier is one, until it boards.
     *
     * @return true for a waiting immigrant
     */
    boolean isImmigrant();

    /**
     * Whether a waiting immigrant is willing to board a departing ship.
     *
     * @return true for an immigrant that the player has not opted out
     */
    boolean isWillingToBoard();

    boolean isBoardingOptedOut();

    /**
     * Whether this immigrant is the owner's single front-of-queue choice.
     *
     * @return true for the current front choice
     */
    boolean isBoardingPriority();

    boolean isAtPlaceConstructionSlot();

    /**
     * @return whether this unit is learning at a native settlement (R-0148)
     */
    boolean isAtPlaceNativeCourse();

    boolean isAtPlaceColonyField();

    /**
     * Whether a voluntary order may remove this resident from its colony.
     *
     * @return true for nonresidents or when the colony permits departure
     */
    boolean canLeaveColony();

    List<TerrainType> getMoveableTerrainTypes();

    boolean canMount();

    boolean canHoldGuns();

    boolean canHoldTools();

    boolean canHoldCargo();

    boolean canPlowFiled();

    void disembarkToLocation(Location targetLocation);

    /**
     * Embark from location to given cargo slot.
     *
     * @param cargoSlot required cargo slot where will be this unit placed
     */
    void embarkFromLocation(CargoSlot cargoSlot);

    int getId();

    void placeToCargoSlot(PlaceCargoSlot placeCargoSlot);

    void placeToEuropePort(EuropePort port);

    Direction getDefaultOrintation();

    void placeToLocation(Location target);

    void placeToLocation(Location target, Direction orientation);

    void placeToHighSeas(boolean isTravelToEurope);

    void placeToEuropePortPier();

    void startTurn();

    /**
     * Allows to change unit owner.
     *
     * @param player required player will be new unit's owner
     */
    public void takeOver(Player player);

    PlaceCargoSlot getPlaceCargoSlot();

    PlaceConstructionSlot getPlaceConstruction();

    PlaceColonyField getPlaceColonyField();

    PlaceLocation getPlaceLocation();

    boolean isSameOwner(List<Unit> units);

    /**
     * @return whether this unit can attack
     */
    boolean canAttack();

    boolean isPossibleToAttackAt(Location targetLocation);

    /**
     * Prepares a read-only proposal for a live adjacent target.
     *
     * @param targetLocation   proposed battlefield
     * @param allowDeclaration whether a peaceful European target may be
     *                         proposed
     * @return a proposal bound to the selected participants, or no legal attack
     */
    Optional<AttackOrder> prepareAttack(Location targetLocation,
            boolean allowDeclaration);

    boolean isPossibleToEmbarkAt(Location targetLocation);

    boolean isPossibleToGoToPort(Location moveToLocation);

    boolean isPossibleToMoveAt(Location location);

    boolean isPossibleToMoveAt(Location location, boolean ignoreEnemies);

    void moveOneStep(Location moveTo);

    void placeToColonyField(ColonyField colonyField,
            GoodsType producedGoodsType);

    void placeToColonyStructureSlot(ConstructionSlot structureSlot);

    void placeToMap(Location location);

    UnitPo save();

    /**
     * @return the route stop this carrier has processed and not yet left, if
     *         any
     */
    Optional<TradeRouteVisit> getProcessedTradeRouteVisit();

    /**
     * Records that this carrier processed a route stop. The model calls this
     * only after the stop's transfers are complete.
     *
     * @param visit required visit just processed
     * @throws IllegalStateException when a processed visit is already recorded
     */
    void markProcessedTradeRouteVisit(TradeRouteVisit visit);

    double getMilitaryStrenght();

    List<Location> getVisibleLocations();

    /** Effective map sight in tiles, independent of remaining movement. */
    int getSightRadius();

    List<UnitWithCargo> getNeighborUnitsWithFreeSlot();

    Optional<List<Location>> getPath(Location destination);

    Optional<List<Location>> getPath(Location destination,
            boolean excludeDestination);

    void attack(Location attackAt);

    List<Location> getAvailableLocations();

    Optional<UnitWithCargo> getFirstUnitToEmbarkAt(Location targetLocation);

    Place getPlace();

    int getSpeed();
}
