package org.microcol.ai;

import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

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
        if (unit.isPossibleToAttackAt(location)) {
            final List<Unit> enemies = unit.getOwner()
                    .getEnemyUnitsAt(location);
            if (!enemies.isEmpty()) {
                unit.attack(enemies.get(0).getLocation());
                return true;
            }
        }
        return false;
    }

    void tryToCaptureColony(final Model model, final Unit unit,
            final Location location) {
        Preconditions.checkState(unit.getLocation().isNeighbor(location),
                String.format(
                        "Unit '%s' sould try to conquer city in neighbor and city is at '%s'",
                        unit, location));
        Preconditions.checkState(model.getColonyAt(location).isPresent(),
                "Colony wasn't found at '%s'", location);
        if (unit.isPossibleToCaptureColonyAt(location)) {
            unit.moveOneStep(location);
        }
    }
}
