package org.microcol.gui.screen.turnreport;

/**
 * Turn event item that offers to open the reported colony while it is still
 * owned by the human player.
 */
public final class TeItemColony extends TeItemSimple {

    private final int colonyId;

    TeItemColony(final String message, final int colonyId) {
        super(message);
        this.colonyId = colonyId;
    }

    public int getColonyId() {
        return colonyId;
    }

}
