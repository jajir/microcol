package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class BeforeDeclaringIndependenceEvent extends AbstractStoppableEvent {

    private final Player whoDecalareIt;

    public BeforeDeclaringIndependenceEvent(final Model model,
            final Player whoDecalareIt) {
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
        return MoreObjects.toStringHelper(getClass())
                .add("whoDecalareIt", whoDecalareIt).toString();
    }
}
