package org.microcol.model.store;

import org.microcol.model.unit.UnitActionType;

/** Saved fortification order and whether its defense is effective. */
public final class UnitActionFortifyPo implements UnitActionPo {
    private Boolean fortified;

    @Override
    public UnitActionType getType() {
        return UnitActionType.fortify;
    }

    public Boolean getFortified() {
        return fortified;
    }

    public void setFortified(final Boolean value) {
        fortified = value;
    }
}
