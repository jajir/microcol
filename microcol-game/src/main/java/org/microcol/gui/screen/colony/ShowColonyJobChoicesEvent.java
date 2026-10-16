package org.microcol.gui.screen.colony;

import org.microcol.model.Unit;
import com.google.common.base.Preconditions;

public final class ShowColonyJobChoicesEvent {
    private final Unit worker;
    private final boolean rejectedDrop;

    public ShowColonyJobChoicesEvent(final Unit worker) {
        this(worker, false);
    }

    public ShowColonyJobChoicesEvent(final Unit worker,
            final boolean rejectedDrop) {
        this.worker = Preconditions.checkNotNull(worker);
        this.rejectedDrop = rejectedDrop;
    }

    public boolean isRejectedDrop() {
        return rejectedDrop;
    }

    public Unit getWorker() {
        return worker;
    }
}
