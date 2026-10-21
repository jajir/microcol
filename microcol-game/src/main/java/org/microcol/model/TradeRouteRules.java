package org.microcol.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

/**
 * Legality of one route leg. The step rule is the shared {@link MovementPolicy}
 * rule applied to a unit type and owner, so no live unit is created. Only
 * squares the owner knows are walked; the check decides whether a leg exists,
 * not how long it takes.
 */
final class TradeRouteRules {

    private final Model model;

    TradeRouteRules(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    /**
     * @param owner required route owner
     * @param kind  required transport kind
     * @param from  required origin stop, already checked for ownership
     * @param to    required destination stop, already checked for ownership
     * @return whether the owner can take this leg now
     */
    boolean isLegalLeg(final Player owner, final TradeRouteKind kind,
            final TradeRouteStop from, final TradeRouteStop to) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        if (from.equals(to) || from.isEurope() && to.isEurope()
                || kind == TradeRouteKind.LAND
                        && (from.isEurope() || to.isEurope())) {
            return false;
        }
        final Optional<Location> start = originOf(owner, kind, from);
        if (start.isEmpty()) {
            return false;
        }
        if (to.isEurope()) {
            return reaches(owner, kind, start.get(), this::isDepartureSquare);
        }
        final Optional<Location> goal = colonySquare(owner, kind, to);
        return goal.isPresent()
                && reaches(owner, kind, start.get(), goal.get()::equals);
    }

    /** Colony square, or the arrival square for a leg leaving Europe. */
    private Optional<Location> originOf(final Player owner,
            final TradeRouteKind kind, final TradeRouteStop from) {
        if (from.isEurope()) {
            return model.getHighSea().getDefaultArrivalLocation();
        }
        return colonySquare(owner, kind, from);
    }

    /**
     * Owned colony square; a sea route also needs the colony to be a coastal
     * port of the owner.
     */
    private Optional<Location> colonySquare(final Player owner,
            final TradeRouteKind kind, final TradeRouteStop stop) {
        return model.getColonyById(stop.getColonyId())
                .filter(colony -> colony.getOwner() == owner)
                .filter(colony -> kind == TradeRouteKind.LAND || model
                        .isOwnedCoastalPort(owner, colony.getLocation()))
                .map(Colony::getLocation);
    }

    /**
     * Whether one route leg can be taken from a known square to the goal
     * square, by the same walk {@link #isLegalLeg} uses. The start square is
     * not tested as a goal, so the caller passes a different goal.
     *
     * @param owner required route owner
     * @param kind  required transport kind
     * @param start required square the leg starts at
     * @param goal  required square the leg must reach
     * @return whether the owner's known map connects the two squares
     */
    boolean isReachable(final Player owner, final TradeRouteKind kind,
            final Location start, final Location goal) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(goal);
        return model.getMap().isValid(start) && !start.equals(goal)
                && reaches(owner, kind, start, goal::equals);
    }

    /**
     * Whether a high-sea square lets a ship leave for Europe. The step itself
     * is the one {@link HighSea} uses for ordinary departures.
     */
    boolean isDepartureSquare(final Location at) {
        return model.getMap().getTerrainTypeAt(at) == TerrainType.HIGH_SEA
                && at.getNeighbors().stream().anyMatch(neighbor -> model
                        .getHighSea().isDepartureStep(at, neighbor));
    }

    /**
     * Breadth-first search over squares the owner knows and the shared policy
     * accepts. The goal is tested when a square is first entered.
     */
    private boolean reaches(final Player owner, final TradeRouteKind kind,
            final Location start, final Predicate<Location> goal) {
        final boolean ship = kind == TradeRouteKind.SEA;
        final UnitType type = ship ? UnitType.GALLEON : UnitType.WAGON;
        final Set<Location> seen = new HashSet<>();
        final Deque<Location> open = new ArrayDeque<>();
        seen.add(start);
        open.add(start);
        while (!open.isEmpty()) {
            final Location at = open.poll();
            for (final Location next : at.getNeighbors()) {
                if (!seen.add(next) || !model.getMap().isValid(next)
                        || !owner.isVisible(next)
                        || !MovementPolicy.isPossibleToEnter(model, type, ship,
                                owner, next, false)) {
                    continue;
                }
                if (goal.test(next)) {
                    return true;
                }
                open.add(next);
            }
        }
        return false;
    }
}
