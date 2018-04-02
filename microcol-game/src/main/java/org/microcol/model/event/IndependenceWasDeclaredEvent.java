package org.microcol.model.event;

import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is send when player declares independence.
 */
public class IndependenceWasDeclaredEvent extends AbstractModelEvent {

    private final Player whoDecalareIt;

    public IndependenceWasDeclaredEvent(final Model model, final Player whoDecalareIt) {
        super(model);
        this.whoDecalareIt = Preconditions.checkNotNull(whoDecalareIt);
    }

    /**
     * @return the whoDecalareIt
     */
    public Player getWhoDecalareIt() {
        return whoDecalareIt;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("whoDecalareIt", whoDecalareIt)
                .toString();
    }

}
