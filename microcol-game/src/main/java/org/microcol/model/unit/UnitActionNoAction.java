package org.microcol.model.unit;

import com.google.common.base.MoreObjects;

/**
 * Represents not active action. Unit is waiting for command.
 */
public final class UnitActionNoAction implements UnitAction {

    @Override
    public boolean isNoAction() {
        return true;
    }

    @Override
    public UnitActionType getType() {
        return UnitActionType.noAction;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
