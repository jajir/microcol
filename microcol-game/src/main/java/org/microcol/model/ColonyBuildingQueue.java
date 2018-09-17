package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.model.store.ColonyBuildingQueueItemPo;
import org.microcol.model.store.QueueItemType;
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

    private final List<ColonyBuildingItemProgress<?>> buildingQueue;

    ColonyBuildingQueue(final Colony colony,
            final List<ColonyBuildingItemProgress<?>> buildingQueue) {
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

    public Optional<ColonyBuildingItemProgress<?>> getItemById(final int itemId) {
        return buildingQueue.stream().filter(item -> item.getId() == itemId).findAny();
    }

    public List<ColonyBuildingItemProgress<?>> getBuildingQueue() {
        return buildingQueue.stream().filter(item -> item.isVisible())
                .collect(ImmutableList.toImmutableList());
    }

    public void moveItemBefore(final ColonyBuildingItemProgress<?> movingItem,
            final ColonyBuildingItemProgress<?> beforeItem) {
        buildingQueue.remove(movingItem);
        final int index = buildingQueue.indexOf(beforeItem);
        buildingQueue.add(index, movingItem);
    }

    public void moveItemAtTheEnd(final ColonyBuildingItemProgress<?> movingItem) {
        logger.debug("Moving item '%s' at the end of the queue.", movingItem);
        buildingQueue.remove(movingItem);
        buildingQueue.add(movingItem);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("ColonyName", colony.getName())
                .add("actualyIsBuilding", getActualyBuilding()).toString();
    }

    /**
     * Get list of units that could be build in this colony.
     *
     * @return Return list building items.
     */
    public List<ColonyBuildingItemUnit> getBuildigItemsUnit() {
        return UnitType.UNIT_TYPES.stream().filter(type -> type.canBeBuildInColony())
                .map(type -> new ColonyBuildingItemUnit(type))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Get list of constructions that could be build in this colony.
     *
     * @return Return list building items.
     */
    public List<ColonyBuildingItemConstruction> getBuildigItemsConstruction() {
        return ConstructionType.ALL.stream().filter(type -> canBeBuild(type))
                .map(type -> new ColonyBuildingItemConstruction(type))
                .collect(ImmutableList.toImmutableList());
    }

    private boolean canBeBuild(final ConstructionType type) {
        final boolean isAlreadyBuilded = type.getUpgradeChain().stream()
                .filter(t -> colony.isContainsConstructionByType(t)).findAny().isPresent();
        if (isAlreadyBuilded) {
            return false;
        } else {
            final Optional<ColonyBuildingItemProgressConstruction> oItem = getItemByType(type);
            if (oItem.isPresent()) {
                return false;
            } else {
                if (type.getUpgradeFrom().isPresent()) {
                    return colony.isContainsConstructionByType(type.getUpgradeFrom().get());
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Add new construction to building queue.
     *
     * @param constructionType
     *            required construction type
     * @return Return <code>null</code> when it wasn't possible to add
     *         construction to queue otherwise return newly created item.
     */
    public ColonyBuildingItemProgressConstruction addAtEnd(
            final ConstructionType constructionType) {
        final Optional<ColonyBuildingItemProgressConstruction> oItem = getItemByType(
                constructionType);
        if (oItem.isPresent()) {
            return null;
        } else {
            final ColonyBuildingItemProgressConstruction item = new ColonyBuildingItemProgressConstruction(
                    new ColonyBuildingItemConstruction(constructionType), getNextId());
            buildingQueue.add(item);
            return item;
        }
    }

    public ColonyBuildingItemProgressUnit addAtEnd(final UnitType unitType) {
        final ColonyBuildingItemProgressUnit item = new ColonyBuildingItemProgressUnit(
                new ColonyBuildingItemUnit(unitType), getNextId());
        buildingQueue.add(item);
        return item;
    }

    public ColonyBuildingItemProgressConstruction addBeforeItem(
            final ConstructionType constructionType, final int beforeId) {
        final ColonyBuildingItemProgressConstruction item = addAtEnd(constructionType);
        if (item == null) {
            return null;
        } else {
            final Optional<ColonyBuildingItemProgress<?>> oBeforeItem = getItemById(beforeId);
            moveItemBefore(item, oBeforeItem.get());
            return item;
        }
    }

    public ColonyBuildingItemProgressUnit addBeforeItem(final UnitType unitType,
            final int beforeId) {
        final ColonyBuildingItemProgressUnit item = addAtEnd(unitType);
        final Optional<ColonyBuildingItemProgress<?>> oBeforeItem = getItemById(beforeId);
        moveItemBefore(item, oBeforeItem.get());
        return item;
    }

    public void removeItem(final ColonyBuildingItemProgress<?> item) {
        buildingQueue.remove(item);
    }

    private int getNextId() {
        return buildingQueue.stream().mapToInt(item -> item.getId()).max().orElse(INITIAL_ID) + 1;
    }

    private Optional<ColonyBuildingItemProgressConstruction> getItemByType(
            final ConstructionType constructionType) {
        return buildingQueue.stream()
                .filter(item -> item instanceof ColonyBuildingItemProgressConstruction)
                .map(item -> (ColonyBuildingItemProgressConstruction) item)
                .filter(item -> item.getItem().getConstructionType().equals(constructionType))
                .findFirst();
    }

    List<ColonyBuildingQueueItemPo> save() {
        final List<ColonyBuildingQueueItemPo> out = new ArrayList<>();
        buildingQueue.stream().filter(item -> item.isVisible())
                .filter(item -> item instanceof ColonyBuildingItemProgressConstruction)
                .map(item -> (ColonyBuildingItemProgressConstruction) item).forEach(item -> {
                    final ColonyBuildingQueueItemPo itemPo = new ColonyBuildingQueueItemPo();
                    itemPo.setType(QueueItemType.CONSTRUCTION);
                    itemPo.setConstructionType(item.getConstructionType());
                    itemPo.setId(item.getId());
                    itemPo.setBuildHammers(item.getBuildHammers());
                    out.add(itemPo);
                });
        buildingQueue.stream().filter(item -> item.isVisible())
                .filter(item -> item instanceof ColonyBuildingItemProgressUnit)
                .map(item -> (ColonyBuildingItemProgressUnit) item).forEach(item -> {
                    final ColonyBuildingQueueItemPo itemPo = new ColonyBuildingQueueItemPo();
                    itemPo.setType(QueueItemType.UNIT);
                    itemPo.setUnitType(item.getUnitType());
                    itemPo.setId(item.getId());
                    itemPo.setBuildHammers(item.getBuildHammers());
                    out.add(itemPo);
                });
        return out;
    }

}
