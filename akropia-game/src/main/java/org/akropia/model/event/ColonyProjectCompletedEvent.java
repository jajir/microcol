package org.akropia.model.event;

import org.akropia.model.Colony;
import org.akropia.model.ColonyBuildingItem;
import org.akropia.model.Model;
import com.google.common.base.Preconditions;

/** A colony project has completed, including its resource and queue changes. */
public final class ColonyProjectCompletedEvent extends AbstractModelEvent {
    private final Colony colony;
    private final ColonyBuildingItem item;

    public ColonyProjectCompletedEvent(final Model model, final Colony colony,
            final ColonyBuildingItem item) {
        super(model);
        this.colony = Preconditions.checkNotNull(colony);
        this.item = Preconditions.checkNotNull(item);
    }

    public Colony getColony() {
        return colony;
    }

    public ColonyBuildingItem getItem() {
        return item;
    }
}
