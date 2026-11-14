package org.akropia.gui.screen.turnreport;

import org.akropia.model.Location;

/**
 * Turn event item that offers to open the reported colony while it is still
 * owned by the human player.
 */
public final class TeItemColony extends TeItemSimple {

    private final int colonyId;

    TeItemColony(final String message, final int colonyId) {
        this(message, colonyId, null);
    }

    TeItemColony(final String message, final int colonyId,
            final Location location) {
        super(message, location);
        this.colonyId = colonyId;
    }

    public int getColonyId() {
        return colonyId;
    }

}
