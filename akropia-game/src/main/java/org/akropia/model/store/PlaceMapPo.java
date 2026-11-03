package org.akropia.model.store;

import org.akropia.model.Direction;
import org.akropia.model.Location;

import com.google.common.base.MoreObjects;

public final class PlaceMapPo extends PlacePo {

    private Integer fishingColonyId;

    public Integer getFishingColonyId() {
        return fishingColonyId;
    }

    public void setFishingColonyId(final Integer fishingColonyId) {
        this.fishingColonyId = fishingColonyId;
    }

    private Location location;

    private Direction orientation;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("location", location).add("orientation", orientation)
                .toString();
    }

    public Direction getOrientation() {
        return orientation;
    }

    public void setOrientation(Direction orientation) {
        this.orientation = orientation;
    }

}
