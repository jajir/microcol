package org.microcol.model;

import com.google.common.base.Preconditions;

public class ColonyBuildingItemUnit extends ColonyBuildingItem {

    private final UnitType unitType;

    ColonyBuildingItemUnit(final UnitType unitType) {
        super(unitType.getRequiredTools(), unitType.getRequiredHammers());
        Preconditions.checkArgument(unitType.canBeBuildInColony(), "Unit can't be build in colony");
        this.unitType = Preconditions.checkNotNull(unitType);
    }

    @Override
    public String getName() {
        return unitType.name();
    }

}
