package org.microcol.gui.gamepanel;

import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * When mouse is over new tile.
 */
public final class MouseOverTileChangedEvent {

    private final Location mouseOverTileLocaton;

    public MouseOverTileChangedEvent(final Location mouseOverTileLocaton) {
        this.mouseOverTileLocaton = Preconditions.checkNotNull(mouseOverTileLocaton);
    }

    public Location getMouseOverTileLocaton() {
        return mouseOverTileLocaton;
    }

    @Override
    public int hashCode() {
        return mouseOverTileLocaton.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof MouseOverTileChangedEvent)) {
            return false;
        }

        final MouseOverTileChangedEvent motce = (MouseOverTileChangedEvent) obj;

        return mouseOverTileLocaton.equals(motce.getMouseOverTileLocaton());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(MouseOverTileChangedEvent.class)
                .add("mouseOverTileLocaton", mouseOverTileLocaton).toString();
    }

}
