package org.akropia.model.store;

import org.akropia.model.unit.UnitActionType;

public final class UnitActionSentryPo implements UnitActionPo {

    @Override
    public UnitActionType getType() {
        return UnitActionType.sentry;
    }
}
