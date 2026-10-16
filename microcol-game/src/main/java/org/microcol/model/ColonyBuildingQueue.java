package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.ColonyBuildingQueueItemPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.unit.UnitActionNoAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Class represents process of building new things in colony. In colony could be
 * build following kinds of items:
 * <ul>
 * <li>Units, for example:
 * <ul>
 * <li>Galleon</li>
 * <li>Cannon</li>
 * <li>Wagon</li>
 * </ul>
 * </li>
 * <li>Buildings, for example:
 * <ul>
 * <li>Lumber mill</li>
 * <li>Church</li>
 * <li>Stable</li>
 * </ul>
 * </li>
 * </ul>
 */
public class ColonyBuildingQueue {

    private final static int INITIAL_ID = -1;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Colony colony;

    private final Model model;

    private final List<ColonyBuildingItemProgress<?>> buildingQueue;

    ColonyBuildingQueue(final Model model, final Colony colony,
            final List<ColonyBuildingItemProgress<?>> buildingQueue) {
        this.model = Preconditions.checkNotNull(model);
        this.colony = Preconditions.checkNotNull(colony);
        this.buildingQueue = new ArrayList<>();
        this.buildingQueue.addAll(buildingQueue);
    }

    private Optional<ColonyBuildingItemProgress<?>> getActualyBuilding() {
        if (buildingQueue.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(buildingQueue.get(0));
        }
    }

    public Optional<BuildingStatus<ColonyBuildingItem>> getActuallyBuildingStat() {
        if (getActualyBuilding().isPresent()) {
            final ColonyBuildingItem pok = getActualyBuilding().get().getItem();
            final int alreadyHaveHammers = colony.getWarehouse()
                    .getGoods(GoodsType.HAMMERS).getAmount();
            final int productionHammers = colony.getGoodsStats()
                    .getStatsByType(GoodsType.HAMMERS).getNetProduction();
            final int alreadyHaveTools = colony.getWarehouse()
                    .getGoods(GoodsType.TOOLS).getAmount();
            final int productionTools = colony.getGoodsStats()
                    .getStatsByType(GoodsType.TOOLS).getNetProduction();
            return Optional.of(new BuildingStatus<ColonyBuildingItem>(pok,
                    alreadyHaveHammers, productionHammers, alreadyHaveTools,
                    productionTools));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ColonyBuildingItemProgress<?>> getItemById(
            final int itemId) {
        return buildingQueue.stream().filter(item -> item.getId() == itemId)
                .findAny();
    }

    public List<ColonyBuildingItemProgress<?>> getBuildingQueue() {
        return buildingQueue.stream().filter(item -> item.isVisible())
                .collect(ImmutableList.toImmutableList());
    }

    public void moveItemBefore(final ColonyBuildingItemProgress<?> movingItem,
            final ColonyBuildingItemProgress<?> beforeItem) {
        validateEditable();
        Preconditions.checkArgument(
                buildingQueue.contains(movingItem)
                        && buildingQueue.contains(beforeItem),
                "Queue item is no longer present");
        if (movingItem != beforeItem) {
            buildingQueue.remove(movingItem);
            buildingQueue.add(buildingQueue.indexOf(beforeItem), movingItem);
        }
    }

    public void moveItemAtTheEnd(
            final ColonyBuildingItemProgress<?> movingItem) {
        validateEditable();
        Preconditions.checkArgument(buildingQueue.contains(movingItem),
                "Queue item is no longer present");
        logger.debug("Moving item '{}' at the end of the queue.", movingItem);
        buildingQueue.remove(movingItem);
        buildingQueue.add(movingItem);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("ColonyName", colony.getName())
                .add("actualyIsBuilding", getActualyBuilding()).toString();
    }

    /**
     * Get list of units that could be build in this colony.
     *
     * @return Return list building items.
     */
    public List<ColonyBuildingItemUnit> getBuildigItemsUnit() {
        return UnitType.UNIT_TYPES.stream().filter(type -> canUnitBeBuild(type))
                .map(type -> new ColonyBuildingItemUnit(type))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Get list of constructions that could be build in this colony.
     *
     * @return Return list building items.
     */
    public List<ColonyBuildingItemConstruction> getBuildigItemsConstruction() {
        return ConstructionType.ALL.stream()
                .filter(type -> canConstructionBeBuild(type))
                .map(type -> new ColonyBuildingItemConstruction(type))
                .collect(ImmutableList.toImmutableList());
    }

    private boolean canUnitBeBuild(final UnitType unitType) {
        return getUnitUnavailableReason(unitType).isEmpty();
    }

    public enum UnavailableReason {
        UNKNOWN_TYPE, COLONY_UNAVAILABLE, NOT_A_PROJECT, ALREADY_BUILT,
        ALREADY_QUEUED, MISSING_PREDECESSOR, POPULATION, MISSING_SHIPYARD,
        COASTAL_REQUIRED
    }

    public Optional<UnavailableReason> getUnitUnavailableReason(
            final UnitType type) {
        if (type == null || !UnitType.UNIT_TYPES.contains(type)) {
            return Optional.of(UnavailableReason.UNKNOWN_TYPE);
        }
        if (!colony.isValid() || !model.getPlayers().contains(colony.getOwner())
                || model.getCurrentPlayer() != colony.getOwner()) {
            return Optional.of(UnavailableReason.COLONY_UNAVAILABLE);
        }
        if (!type.canBeBuildInColony()) {
            return Optional.of(UnavailableReason.NOT_A_PROJECT);
        }
        if (type == UnitType.GALLEON || type == UnitType.FRIGATE) {
            if (!colony.wasConstructionBuilded(ConstructionType.SHIPYARD)) {
                return Optional.of(UnavailableReason.MISSING_SHIPYARD);
            }
            if (!model.isOwnedCoastalPort(colony.getOwner(),
                    colony.getLocation())) {
                return Optional.of(UnavailableReason.COASTAL_REQUIRED);
            }
        }
        return type.getCanByBuildInColony().apply(model, colony)
                ? Optional.empty()
                : Optional.of(UnavailableReason.MISSING_PREDECESSOR);
    }

    public Optional<UnavailableReason> getUnavailableReason(
            final ConstructionType type) {
        if (type == null || !ConstructionType.ALL.contains(type)) {
            return Optional.of(UnavailableReason.UNKNOWN_TYPE);
        }
        if (!colony.isValid() || !model.getPlayers().contains(colony.getOwner())
                || model.getCurrentPlayer() != colony.getOwner()) {
            return Optional.of(UnavailableReason.COLONY_UNAVAILABLE);
        }
        if ((type == ConstructionType.DOCK || type == ConstructionType.DRYDOCK
                || type == ConstructionType.SHIPYARD)
                && !model.isOwnedCoastalPort(colony.getOwner(),
                        colony.getLocation())) {
            return Optional.of(UnavailableReason.COASTAL_REQUIRED);
        }
        if (!type.canBeConstructed()) {
            return Optional.of(UnavailableReason.NOT_A_PROJECT);
        }
        if (colony.isContainsConstructionByType(type)) {
            return Optional.of(UnavailableReason.ALREADY_BUILT);
        }
        final boolean alreadyQueued = buildingQueue.stream().filter(
                item -> item instanceof ColonyBuildingItemProgressConstruction)
                .map(item -> ((ColonyBuildingItemProgressConstruction) item)
                        .getConstructionType())
                .anyMatch(qType -> type.getUpgradeChain().contains(qType)
                        || qType.getUpgradeChain().contains(type));
        if (alreadyQueued) {
            return Optional.of(UnavailableReason.ALREADY_QUEUED);
        }
        if (type.getUpgradeFrom().isPresent()
                && colony.getConstructions().stream().noneMatch(
                        c -> c.getType() == type.getUpgradeFrom().get())) {
            return Optional.of(UnavailableReason.MISSING_PREDECESSOR);
        }
        if (colony.getUnitsInsideColony().size() < type
                .getRequiredColonyPopulation()) {
            return Optional.of(UnavailableReason.POPULATION);
        }
        return Optional.empty();
    }

    private boolean canConstructionBeBuild(final ConstructionType type) {
        return getUnavailableReason(type).isEmpty();
    }

    /** Add a project only while its current admission conditions hold. */
    public ColonyBuildingItemProgressConstruction addAtEnd(
            final ConstructionType constructionType) {
        model.checkTurnPreparationComplete();
        final Optional<UnavailableReason> reason = getUnavailableReason(
                constructionType);
        Preconditions.checkArgument(reason.isEmpty(),
                "Construction unavailable: %s", reason);
        final ColonyBuildingItemProgressConstruction item = new ColonyBuildingItemProgressConstruction(
                new ColonyBuildingItemConstruction(constructionType),
                getNextId());
        buildingQueue.add(item);
        return item;
    }

    public ColonyBuildingItemProgressUnit addAtEnd(final UnitType unitType) {
        model.checkTurnPreparationComplete();
        Preconditions.checkArgument(canUnitBeBuild(unitType),
                "Unit project is unavailable");
        final ColonyBuildingItemProgressUnit item = new ColonyBuildingItemProgressUnit(
                new ColonyBuildingItemUnit(unitType), getNextId());
        buildingQueue.add(item);
        return item;
    }

    public ColonyBuildingItemProgressConstruction addBeforeItem(
            final ConstructionType constructionType, final int beforeId) {
        final ColonyBuildingItemProgress<?> before = getItemById(beforeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Insertion anchor is no longer present"));
        final ColonyBuildingItemProgressConstruction item = addAtEnd(
                constructionType);
        moveItemBefore(item, before);
        return item;
    }

    public ColonyBuildingItemProgressUnit addBeforeItem(final UnitType unitType,
            final int beforeId) {
        final ColonyBuildingItemProgress<?> before = getItemById(beforeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Insertion anchor is no longer present"));
        final ColonyBuildingItemProgressUnit item = addAtEnd(unitType);
        moveItemBefore(item, before);
        return item;
    }

    public void removeItem(final ColonyBuildingItemProgress<?> item) {
        validateEditable();
        Preconditions.checkArgument(buildingQueue.contains(item),
                "Queue item is no longer present");
        buildingQueue.remove(item);
    }

    private void validateEditable() {
        model.checkTurnPreparationComplete();
        Preconditions.checkArgument(
                colony.isValid()
                        && model.getPlayers().contains(colony.getOwner())
                        && colony.getOwner().equals(model.getCurrentPlayer()),
                "Colony is unavailable");
    }

    private int getNextId() {
        final int greatest = buildingQueue.stream()
                .mapToInt(item -> item.getId()).max().orElse(INITIAL_ID);
        Preconditions.checkArgument(greatest < Integer.MAX_VALUE,
                "Queue identity space is exhausted");
        return greatest + 1;
    }

    List<ColonyBuildingQueueItemPo> save() {
        final List<ColonyBuildingQueueItemPo> out = new ArrayList<>();
        for (final ColonyBuildingItemProgress<?> item : getBuildingQueue()) {
            final ColonyBuildingQueueItemPo stored = new ColonyBuildingQueueItemPo();
            stored.setId(item.getId());
            if (item instanceof ColonyBuildingItemProgressConstruction) {
                stored.setType(QueueItemType.CONSTRUCTION);
                stored.setConstructionType(
                        ((ColonyBuildingItemProgressConstruction) item)
                                .getConstructionType());
            } else if (item instanceof ColonyBuildingItemProgressUnit) {
                stored.setType(QueueItemType.UNIT);
                stored.setUnitType(
                        ((ColonyBuildingItemProgressUnit) item).getUnitType());
            } else {
                throw new IllegalArgumentException("Unknown queue item kind");
            }
            out.add(stored);
        }
        return out;
    }

    public Optional<UnavailableReason> getCompletionUnavailableReason() {
        if (buildingQueue.isEmpty()) {
            return Optional.empty();
        }
        if (!colony.isValid() || !model.getPlayers().contains(colony.getOwner())
                || model.getCurrentPlayer() != colony.getOwner()) {
            return Optional.of(UnavailableReason.COLONY_UNAVAILABLE);
        }
        ColonyBuildingItemProgress<?> first = buildingQueue.get(0);
        if (first instanceof ColonyBuildingItemProgressConstruction) {
            ColonyBuildingItemProgressConstruction construction = (ColonyBuildingItemProgressConstruction) first;
            ConstructionType type = construction.getConstructionType();
            if ((type == ConstructionType.DOCK
                    || type == ConstructionType.DRYDOCK
                    || type == ConstructionType.SHIPYARD)
                    && !model.isOwnedCoastalPort(colony.getOwner(),
                            colony.getLocation())) {
                return Optional.of(UnavailableReason.COASTAL_REQUIRED);
            }
            if (colony.isContainsConstructionByType(type)) {
                return Optional.of(UnavailableReason.ALREADY_BUILT);
            }
            Optional<ConstructionType> upgradeFrom = type.getUpgradeFrom();
            if (upgradeFrom.isPresent()) {
                ConstructionType predecessor = upgradeFrom.get();
                if (colony.getConstructions().stream()
                        .noneMatch(c -> c.getType() == predecessor)) {
                    return Optional.of(UnavailableReason.MISSING_PREDECESSOR);
                }
            }
            return Optional.empty();
        } else if (first instanceof ColonyBuildingItemProgressUnit) {
            ColonyBuildingItemProgressUnit unitProgress = (ColonyBuildingItemProgressUnit) first;
            UnitType unitType = unitProgress.getUnitType();
            return getUnitUnavailableReason(unitType);
        } else {
            return Optional.of(UnavailableReason.UNKNOWN_TYPE);
        }
    }

    void startTurn() {
        if (getActualyBuilding().isPresent()
                && getCompletionUnavailableReason().isEmpty()) {
            final ColonyBuildingItemProgress<?> item = getActualyBuilding()
                    .get();
            if (colony.getWarehouse().getGoods(GoodsType.HAMMERS)
                    .getAmount() < item.getRequiredHammers()
                    || colony.getWarehouse().getGoods(GoodsType.TOOLS)
                            .getAmount() < item.getRequiredTools()) {
                return;
            }
            if (item instanceof ColonyBuildingItemProgressUnit) {
                Preconditions.checkState(
                        model.getUnitStorage().canAllocateUnit(),
                        "Unit identity space is exhausted");
            }
            colony.getWarehouse().removeGoods(
                    Goods.of(GoodsType.HAMMERS, item.getRequiredHammers()));
            colony.getWarehouse().removeGoods(
                    Goods.of(GoodsType.TOOLS, item.getRequiredTools()));
            if (item.getItem() instanceof ColonyBuildingItemUnit) {
                final ColonyBuildingItemUnit unit = (ColonyBuildingItemUnit) item
                        .getItem();
                createUnit(unit.getUnitType());
            } else if (item
                    .getItem() instanceof ColonyBuildingItemConstruction) {
                final ColonyBuildingItemConstruction construction = (ColonyBuildingItemConstruction) item
                        .getItem();
                colony.createConstruction(construction.getConstructionType());
            } else {
                throw new IllegalArgumentException(
                        String.format("Unknown class '%s'.", item.getItem()));
            }
            buildingQueue.remove(0);
        }
    }

    private void createUnit(final UnitType unitType) {
        final Function<Unit, Cargo> cargoBuilder = unit -> new Cargo(unit,
                unitType.getCargoCapacity());
        final Function<Unit, Place> placeBuilder = unit -> new PlaceLocation(
                unit, colony.getLocation(), unit.getDefaultOrintation());

        model.getUnitStorage().createUnit(cargoBuilder, model, placeBuilder,
                unitType, colony.getOwner(), unitType.getSpeed(),
                new UnitActionNoAction());
    }

}
