package org.akropia.model.event;

import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.base.MoreObjects;

public final class GoldWasChangedEvent extends AbstractModelEvent {

    private final Player player;
    private final int oldValue;
    private final int newValue;

    public GoldWasChangedEvent(final Model model, final Player player,
            final int oldValue, final int newValue) {
        super(model);
        this.player = player;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("player", player)
                .add("oldValue", oldValue).add("newValue", newValue).toString();
    }

}
