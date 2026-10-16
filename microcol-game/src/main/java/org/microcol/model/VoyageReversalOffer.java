package org.microcol.model;

import org.microcol.model.unit.Ship;

/** Snapshot of the ship and route state offered for confirmation. */
public final class VoyageReversalOffer {
    final Model model;
    final Ship ship;
    final Player owner;
    final PlaceHighSea voyage;
    final int position;
    final boolean toEurope;
    final int revision;
    private final int remainingAfterReverse;

    VoyageReversalOffer(final Model model, final Ship ship,
            final PlaceHighSea voyage) {
        this.model = model;
        this.ship = ship;
        this.owner = ship.getOwner();
        this.voyage = voyage;
        this.position = voyage.getPosition();
        this.toEurope = voyage.isTravelToEurope();
        this.revision = voyage.getRevision();
        remainingAfterReverse = voyage.getReverseRemainingTurns();
    }

    public boolean isNewDestinationEurope() {
        return !toEurope;
    }

    public int getRemainingAfterReverse() {
        return remainingAfterReverse;
    }
}
