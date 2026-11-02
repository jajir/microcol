package org.akropia.model.event;

import org.akropia.model.Colony;
import org.akropia.model.Model;

import com.google.common.base.Preconditions;

/**
 * Event is send when new colony is founded.
 */
public class ColonyWasFoundEvent extends AbstractModelEvent {

    private final Colony colony;

    public ColonyWasFoundEvent(final Model model, final Colony colony) {
        super(model);
        this.colony = Preconditions.checkNotNull(colony);
    }

    /**
     * @return the colony
     */
    public Colony getColony() {
        return colony;
    }

}
