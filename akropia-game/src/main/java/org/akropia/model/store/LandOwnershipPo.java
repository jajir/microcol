package org.akropia.model.store;

import org.akropia.model.Location;
import org.akropia.model.NativeNation;

/**
 * A colonial land title, retaining its native claim independently of its owner.
 */
public final class LandOwnershipPo {
    private Location location;
    private String owner;
    private NativeNation nativeClaim;
    private boolean purchased;

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location value) {
        location = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String value) {
        owner = value;
    }

    public NativeNation getNativeClaim() {
        return nativeClaim;
    }

    public void setNativeClaim(final NativeNation value) {
        nativeClaim = value;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(final boolean value) {
        purchased = value;
    }
}
