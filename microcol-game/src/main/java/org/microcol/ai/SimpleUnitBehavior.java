package org.microcol.ai;

import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Unit;

public class SimpleUnitBehavior {

    public void tryToFight(final Unit unit) {
        if (unit.getType().canAttack() && unit.isAtPlaceLocation()
                && unit.getAvailableMoves() > 0) {
            /*
             * Following filter stops executing when first location return from
             * tryToAttack true.
             */
            unit.getLocation().getNeighbors().stream()
                    .filter(location -> tryToAttack(unit, location)).findFirst();
        }
    }

    private boolean tryToAttack(final Unit unit, final Location location) {
        if (unit.isPossibleToAttackAt(location)) {
            final List<Unit> enemies = unit.getOwner().getEnemyUnitsAt(location);
            if (!enemies.isEmpty()) {
                unit.attack(enemies.get(0).getLocation());
                return true;
            }
        }
        return false;
    }

}
