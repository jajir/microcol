package org.microcol.model;

import java.util.function.Predicate;

import com.google.common.base.Preconditions;

/**
 * Step legality and entry cost of one way of moving, the inputs of
 * {@link PathFinder}. A unit's profile asks the live unit, so its own state,
 * such as a damaged ship, still applies. A type profile applies the same shared
 * {@link MovementPolicy} and entry costs to a unit type and owner without any
 * unit, so a route preview never creates or moves one.
 */
final class MovementProfile {

    private final Model model;
    private final boolean ship;
    private final Predicate<Location> enterable;

    private MovementProfile(final Model model, final boolean ship,
            final Predicate<Location> enterable) {
        this.model = Preconditions.checkNotNull(model);
        this.ship = ship;
        this.enterable = Preconditions.checkNotNull(enterable);
    }

    /**
     * @param unit required moving unit
     * @return the profile of that unit as it is now
     */
    static MovementProfile of(final AbstractUnit unit) {
        Preconditions.checkNotNull(unit);
        return new MovementProfile(unit.model, unit.isShip(),
                unit::isPossibleToMoveAt);
    }

    /**
     * @param model required model supplying the map, colonies and diplomacy
     * @param type  required unit type
     * @param owner required moving player
     * @return the profile of an undamaged unit of that type and owner
     */
    static MovementProfile of(final Model model, final UnitType type,
            final Player owner) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(owner);
        return new MovementProfile(model, type.isShip(),
                location -> MovementPolicy.isPossibleToEnter(model, type,
                        type.isShip(), owner, location, false));
    }

    Model getModel() {
        return model;
    }

    boolean isShip() {
        return ship;
    }

    boolean canEnter(final Location location) {
        return enterable.test(location);
    }

    int getEntryCostThirds(final Location from, final Location to) {
        return entryCostThirds(model, ship, from, to);
    }

    /**
     * Entry cost of one step, shared by moving units and route previews.
     *
     * @param model required model supplying the map
     * @param ship  whether a ship makes the step
     * @param from  required square the step leaves
     * @param to    required square the step enters
     * @return cost in thirds of a movement point
     */
    static int entryCostThirds(final Model model, final boolean ship,
            final Location from, final Location to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        // Ships keep one whole point for every sea-lane, lake or port step.
        if (ship) {
            return MovementPoints.THIRDS_PER_POINT;
        }
        // A connected road needs a road on both squares of the step; a river
        // course needs its own segment. Either one gives the same third.
        final boolean connectedRoad = model.getMap().hasRoadAt(from)
                && model.getMap().hasRoadAt(to);
        final boolean connectedRiver = model.getMap().hasRiverSegment(from, to);
        return model.getMap().getTerrainTypeAt(to).getMoveCostThirds(
                model.getMap().hasTreesAt(to), connectedRoad || connectedRiver);
    }
}
