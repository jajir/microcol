package org.akropia.model.store;

import org.akropia.model.unit.UnitActionType;

public final class UnitActionNoActionPo implements UnitActionPo {

    @Override
    public UnitActionType getType() {
        return UnitActionType.noAction;
    }
}
