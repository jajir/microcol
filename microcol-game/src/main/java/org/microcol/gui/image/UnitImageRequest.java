package org.microcol.gui.image;

import org.microcol.model.Direction;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Object contains unit image request.
 */
class UnitImageRequest {

    private final Unit unit;

    private final Direction orientation;

    public UnitImageRequest(final Unit unit, final Direction orientation) {
        this.unit = Preconditions.checkNotNull(unit);
        this.orientation = Preconditions.checkNotNull(orientation);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("unitType", getUnitType())
                .add("orientation", orientation).toString();
    }

    /**
     * @return the unitType
     */
    protected UnitType getUnitType() {
        return unit.getType();
    }

    /**
     * @return the orientation
     */
    protected Direction getOrientation() {
        return orientation;
    }

    /**
     * @return the unit
     */
    protected Unit getUnit() {
        return unit;
    }
}