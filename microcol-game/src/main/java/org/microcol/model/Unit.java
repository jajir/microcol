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

    int getActionPoints();

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

    boolean isAtPlaceConstructionSlot();

    boolean isAtPlaceColonyField();

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

    boolean isPossibleToCaptureColonyAt(Location targetLocation);

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

    double getMilitaryStrenght();

    List<Location> getVisibleLocations();

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
