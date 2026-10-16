package org.microcol.model.store;

import org.microcol.model.unit.UnitActionType;

public final class UnitActionSentryPo implements UnitActionPo {

    @Override
    public UnitActionType getType() {
        return UnitActionType.sentry;
    }
}
