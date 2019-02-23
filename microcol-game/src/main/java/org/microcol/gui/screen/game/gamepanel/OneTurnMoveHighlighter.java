package org.microcol.gui.screen.game.gamepanel;

import java.util.List;

import org.microcol.model.Location;

import com.google.inject.Inject;

/**
 * Helps to highlight area where use could move in first turn.
 */
public final class OneTurnMoveHighlighter {

    private List<Location> locations;

    @Inject
    OneTurnMoveHighlighter() {

    }

    public void setLocations(final List<Location> locations) {
        this.locations = locations;
    }

    public boolean isItHighlighted(final Location location) {
        return locations != null && locations.contains(location);
    }

}
