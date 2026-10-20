package org.microcol.model.store;

import org.microcol.model.Location;

/**
 * Saved remaining units of one finite mineral deposit. Both counts are
 * required; a count the deposit does not yield is saved as zero.
 */
public final class MineralReservePo {

    private final Location location;

    private final Integer silver;

    private final Integer ore;

    public MineralReservePo(final Location location, final Integer silver,
            final Integer ore) {
        this.location = location;
        this.silver = silver;
        this.ore = ore;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getSilver() {
        return silver;
    }

    public Integer getOre() {
        return ore;
    }

}
