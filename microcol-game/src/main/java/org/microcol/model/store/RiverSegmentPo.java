package org.microcol.model.store;

import org.microcol.model.Location;

/**
 * Saved course segment joining two neighbouring river squares. The segment is
 * undirected, so it is stored once and travels both ways.
 */
public final class RiverSegmentPo {

    private final Location from;

    private final Location to;

    public RiverSegmentPo(final Location from, final Location to) {
        this.from = from;
        this.to = to;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

}
