package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Contains information if grid should be displayed.
 *
 */
public final class ShowGridEvent {

    private final boolean isGridShown;

    public ShowGridEvent(final boolean isGridShown) {
        this.isGridShown = isGridShown;
    }

    public boolean isGridShown() {
        return isGridShown;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ShowGridEvent.class).add("isGridShown", isGridShown)
                .toString();
    }

}
