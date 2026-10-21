package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Shared step rule for a unit type and owner: terrain, colony ownership and
 * enemy occupancy. Units and trade route legality both call it. It reads the
 * model only through its getters, so no live unit is needed to decide a square.
 */
final class MovementPolicy {

    private MovementPolicy() {
    }

    /**
     * @param model         required model supplying the map, colonies and
     *                      diplomacy
     * @param type          required unit type
     * @param ship          whether the unit is a ship
     * @param owner         required moving player
     * @param location      required target square
     * @param ignoreEnemies whether enemy units on the square are ignored
     * @return whether the owner may enter the square
     */
    static boolean isPossibleToEnter(final Model model, final UnitType type,
            final boolean ship, final Player owner, final Location location,
            final boolean ignoreEnemies) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(location);
        if (!model.getMap().isValid(location)) {
            return false;
        }
        final Optional<Colony> optColony = model.getColonyAt(location);
        boolean hostileColony = false;
        if (optColony.isPresent()) {
            final Colony colony = optColony.get();
            hostileColony = !owner.equals(colony.getOwner())
                    && model.areHostile(owner, colony.getOwner());
            // An ally's colony grants passage only; it is never captured.
            if (!owner.equals(colony.getOwner()) && !hostileColony
                    && !model.areAllied(owner, colony.getOwner())) {
                return false;
            }
        }

        if (!type.canMoveAtTerrain(model.getMap().getTerrainTypeAt(location))) {
            final boolean port = ship
                    ? model.isOwnedCoastalPort(owner, location)
                    : model.getColoniesAt(location, owner).isPresent();
            if (port) {
                return ignoreEnemies
                        || !model.isPassageBlockedAt(owner, location);
            }
            return false;
        }

        if (hostileColony) {
            // Ordinary movement, even ignoring enemies and even from a square
            // kept after a former alliance, never enters a hostile colony: its
            // defense battle is the only way to take it.
            return false;
        }
        return ignoreEnemies || !model.isPassageBlockedAt(owner, location);
    }
}
