package org.microcol.gui.screen.game.gamepanel;

import java.util.List;

import org.microcol.model.Location;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Helps to highlight area where use could move in first turn.
 */
@Singleton
public final class OneTurnMoveHighlighter {

    private List<Location> locations;

    @Inject
    OneTurnMoveHighlighter() {

    }

    public void setLocations(final List<Location> locations) {
        this.locations = locations;
    }

    boolean isItHighlighted(final Location location) {
        return locations != null && locations.contains(location);
    }

}
