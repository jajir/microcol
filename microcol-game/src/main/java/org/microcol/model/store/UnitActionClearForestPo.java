package org.microcol.model.store;

import org.microcol.model.unit.UnitActionType;

public final class UnitActionClearForestPo implements UnitActionPo {

    private int remainingTurns;

    @Override
    public UnitActionType getType() {
        return UnitActionType.clearForest;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }
}
