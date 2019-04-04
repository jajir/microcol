package org.microcol.gui.screen.game.gamepanel;

import java.util.Optional;

import org.microcol.gui.util.Listener;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Produce mouse over tile events and holds tile which is mouse over.
 */
@Listener
public final class MouseOverTileManager {

    private final EventBus eventBus;

    private Location mouseOverTile;

    /**
     * Default constructor
     * 
     * @param eventBus
     *            required event bus that allows to fire new event
     */
    @Inject
    public MouseOverTileManager(final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        mouseOverTile = null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("mouseOverTile", mouseOverTile)
                .toString();
    }

    public Optional<Location> getMouseOverTile() {
        return Optional.ofNullable(mouseOverTile);
    }

    public void setMouseOverTile(final Location newMouseOverTile) {
        Preconditions.checkNotNull(newMouseOverTile);
        if (mouseOverTile == null) {
            mouseOverTile = newMouseOverTile;
            eventBus.post(new MouseOverTileChangedEvent(mouseOverTile));
        } else {
            if (!mouseOverTile.equals(newMouseOverTile)) {
                this.mouseOverTile = newMouseOverTile;
                eventBus.post(new MouseOverTileChangedEvent(mouseOverTile));
            }
        }
    }

}
