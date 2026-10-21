package org.microcol.model.store;

import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;

/**
 * One European's record on one native-claimed square. The same shape stores a
 * paid right and a one-time unpaid taking; the list it sits in gives its
 * meaning. Both fields are required; the square must be claimed by a native
 * settlement in the same save.
 */
public class NativeLandRecordPo {

    private EuropeanNation european;

    private Location location;

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

}
