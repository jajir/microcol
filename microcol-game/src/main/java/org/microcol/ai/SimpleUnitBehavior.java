package org.microcol.ai;

import org.microcol.model.Location;
import org.microcol.model.Unit;

final class SimpleUnitBehavior {

    void tryToFight(final Unit unit) {
        if (unit.canAttack() && unit.isAtPlaceLocation()
                && unit.getActionPoints() > 0) {
            /*
             * Following filter stops executing when first location return from
             * tryToAttack true.
             */
            unit.getLocation().getNeighbors().stream()
                    .filter(location -> tryToAttack(unit, location))
                    .findFirst();
        }
    }

    private boolean tryToAttack(final Unit unit, final Location location) {
        // The model selects the defender, also among colony residents that
        // stand on no map tile of their own.
        if (unit.isPossibleToAttackAt(location)) {
            unit.attack(location);
            return true;
        }
        return false;
    }
}
