package org.akropia.gui.screen.colony;

import org.akropia.model.Unit;
import com.google.common.base.Preconditions;

/** Select the same outdoor worker after opening its receiving colony. */
public final class SelectColonyWorkerEvent {
    private final Unit worker;

    public SelectColonyWorkerEvent(final Unit worker) {
        this.worker = Preconditions.checkNotNull(worker);
    }

    public Unit getWorker() {
        return worker;
    }
}
