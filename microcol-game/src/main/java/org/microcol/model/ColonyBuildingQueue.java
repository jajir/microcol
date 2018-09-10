package org.microcol.model;

import java.util.List;

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

    //FIXME NYI
    private ColonyBuildingItemProgress actualyIsBuilding;

    private List<ColonyBuildingItemProgress> buildingQueue;

    public List<ColonyBuildingItem> getUnitBuildigItems() {
        return null;
    }

    public List<ColonyBuildingItem> getCostructionBuildigItems() {
        return null;
    }

}
