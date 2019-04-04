package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ColonyBuildingItemUnit extends ColonyBuildingItem {

    private final UnitType unitType;

    public ColonyBuildingItemUnit(final UnitType unitType) {
        Preconditions.checkArgument(unitType.canBeBuildInColony(), "Unit can't be build in colony");
        this.unitType = Preconditions.checkNotNull(unitType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("unitType", unitType)
                .add("requiredTools", getRequiredTools())
                .add("requiredHammers", getRequiredHammers()).toString();
    }

    @Override
    public String getName() {
        return unitType.name();
    }

    /**
     * @return the unitType
     */
    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public int getRequiredHammers() {
        return unitType.getRequiredHammers();
    }

    @Override
    public int getRequiredTools() {
        return unitType.getRequiredTools();
    }

}
