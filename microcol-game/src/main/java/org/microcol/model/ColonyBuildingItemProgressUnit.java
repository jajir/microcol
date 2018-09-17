package org.microcol.model;

import com.google.common.base.MoreObjects;

/**
 * Object represents work done at some unit.
 */
public class ColonyBuildingItemProgressUnit
        extends ColonyBuildingItemProgress<ColonyBuildingItemUnit> {

    public ColonyBuildingItemProgressUnit(final ColonyBuildingItemUnit item, final int id) {
        super(item, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("id", getId())
                .add("buildHammers", getBuildHammers()).add("visible", isVisible())
                .add("unitType", getUnitType()).toString();
    }

    public UnitType getUnitType() {
        return getItem().getUnitType();
    }

}
