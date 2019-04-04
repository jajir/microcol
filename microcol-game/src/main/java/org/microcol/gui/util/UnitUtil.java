package org.microcol.gui.util;

import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

/**
 * Utility methods that simplify work with units.
 */
public class UnitUtil {

    public boolean isPossibleToDisembarkAt(final Unit unit, final Location targetLocation) {
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            return unitWithCargo.isPossibleToDisembarkAt(targetLocation, true);
        } else {
            return false;
        }
    }

}
