package org.microcol.model.unit;

import org.microcol.model.store.UnitActionPo;
import org.microcol.model.store.UnitActionSentryPo;

/** A persistent waiting order that does not consume movement. */
public final class UnitActionSentry implements UnitAction {

    @Override
    public UnitActionType getType() {
        return UnitActionType.sentry;
    }

    @Override
    public UnitActionPo save() {
        return new UnitActionSentryPo();
    }
}
