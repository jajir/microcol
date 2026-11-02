package org.akropia.model.store;

import org.akropia.model.unit.UnitActionType;

public final class UnitActionBuildRoadPo implements UnitActionPo {

    private int remainingTurns;

    @Override
    public UnitActionType getType() {
        return UnitActionType.buildRoad;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }
}
