package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;

public final class GameStartedEvent extends AbstractModelEvent {
    public GameStartedEvent(final Model model) {
        super(model);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
