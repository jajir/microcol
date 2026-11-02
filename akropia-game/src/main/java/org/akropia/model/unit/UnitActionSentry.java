package org.akropia.model.unit;

import org.akropia.model.store.UnitActionPo;
import org.akropia.model.store.UnitActionSentryPo;

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
