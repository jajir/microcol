package org.akropia.gui.util;

import org.akropia.model.Location;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitWithCargo;

/**
 * Utility methods that simplify work with units.
 */
public class UnitUtil {

    public boolean isPossibleToDisembarkAt(final Unit unit,
            final Location targetLocation) {
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            return !unitWithCargo.isPossibleToGoToPort(targetLocation)
                    && unitWithCargo.isPossibleToDisembarkAt(targetLocation,
                            true);
        } else {
            return false;
        }
    }

}
