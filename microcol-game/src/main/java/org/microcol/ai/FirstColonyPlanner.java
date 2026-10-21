package org.microcol.ai;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;

/** Chooses known routes; the caller performs and revalidates each action. */
final class FirstColonyPlanner {
    private static final Comparator<Location> COORDINATES = Comparator
            .comparingInt(Location::getX).thenComparingInt(Location::getY);
    private static final Comparator<Candidate> CANDIDATES = Comparator
            .comparingInt((Candidate candidate) -> candidate.distance)
            .thenComparing(Comparator
                    .comparingInt((Candidate candidate) -> candidate.food)
                    .reversed())
            .thenComparing(candidate -> candidate.site, COORDINATES)
            .thenComparing(candidate -> candidate.approach, COORDINATES);

    /**
     * First colony: the founder walks, or rides its carrier, and exploration
     * reveals unknown ground when no known site exists.
     *
     * @return the next step, or empty when the owner already has a colony
     */
    static Optional<Location> nextStep(final Model model, final Unit founder) {
        final Player owner = founder.getOwner();
        if (model.getColonies().stream()
                .anyMatch(colony -> colony.getOwner() == owner)) {
            return Optional.empty();
        }
        return mover(founder).flatMap(unit -> route(model, founder, unit,
                unit.getLocation(), Optional.empty(), true))
                .flatMap(route -> route.step);
    }

    /**
     * Second colony: the best known legal site for this mover, routed from the
     * given square. A walking founder passes its colony square before release,
     * and a carrier passes its port. Nothing moves and unknown ground is never
     * explored for the choice.
     *
     * @param model   required model
     * @param founder required spare founder
     * @param mover   required unit that would carry or walk the founder
     * @param start   required square the route begins from
     * @return the site, or empty when no known legal site exists
     */
    static Optional<Location> chooseSite(final Model model, final Unit founder,
            final Unit mover, final Location start) {
        return route(model, founder, mover, start, Optional.empty(), false)
                .flatMap(route -> route.site);
    }

    /**
     * Second colony: the next step toward a site chosen earlier. Unknown ground
     * is never explored, so a blocked site yields empty.
     *
     * @param model   required model
     * @param founder required founder
     * @param mover   required unit that would carry or walk the founder
     * @param site    required site chosen earlier
     * @return the next step, or empty when the site is not reachable now
     */
    static Optional<Location> nextStepToSite(final Model model,
            final Unit founder, final Unit mover, final Location site) {
        return route(model, founder, mover, mover.getLocation(),
                Optional.of(site), false).flatMap(route -> route.step);
    }

    /** The unit that moves the founder: the founder itself or its carrier. */
    private static Optional<Unit> mover(final Unit founder) {
        if (founder.isAtPlaceLocation()) {
            return Optional.of(founder);
        }
        if (founder.isAtCargoSlot()) {
            final Unit carrier = founder.getPlaceCargoSlot().getOwnerUnit();
            if (carrier.isShip() && carrier.getOwner() == founder.getOwner()
                    && carrier.isAtPlaceLocation()) {
                return Optional.of(carrier);
            }
        }
        return Optional.empty();
    }

    private static Optional<Route> route(final Model model, final Unit founder,
            final Unit mover, final Location start,
            final Optional<Location> fixedSite, final boolean explore) {
        final Player owner = founder.getOwner();
        if (model.getAllUnits().stream().noneMatch(unit -> unit == founder)
                || !founder.getType().canBuildColony() || owner.isKing()) {
            return Optional.empty();
        }
        if (!known(model, owner, start) || !mover.isPossibleToMoveAt(start)) {
            return Optional.empty();
        }
        final Map<Location, Integer> distance = new HashMap<>();
        final Map<Location, Location> firstStep = new HashMap<>();
        final ArrayDeque<Location> open = new ArrayDeque<>();
        distance.put(start, 0);
        firstStep.put(start, start);
        open.add(start);
        while (!open.isEmpty()) {
            final Location current = open.remove();
            for (final Location neighbor : current.getNeighbors()) {
                if (distance.containsKey(neighbor)
                        || !known(model, owner, neighbor)
                        || (mover.isShip() && model.getMap().getTerrainTypeAt(
                                neighbor) == TerrainType.HIGH_SEA)
                        || model.getColonyAt(neighbor)
                                .filter(colony -> colony.getOwner() != owner)
                                .isPresent()
                        || !mover.isPossibleToMoveAt(neighbor)) {
                    continue;
                }
                distance.put(neighbor, distance.get(current) + 1);
                firstStep.put(neighbor, current.equals(start) ? neighbor
                        : firstStep.get(current));
                open.add(neighbor);
            }
        }
        Candidate best = null;
        for (final Location approach : distance.keySet()) {
            if (mover.isShip()
                    && !model.getMap().getTerrainTypeAt(approach).isSea()) {
                continue;
            }
            final List<Location> sites = mover.isShip()
                    ? approach.getNeighbors()
                    : List.of(approach);
            for (final Location site : sites) {
                if (fixedSite.isPresent() && !site.equals(fixedSite.get())) {
                    continue;
                }
                final int food = foodYield(model, founder, site);
                if (food < 0) {
                    continue;
                }
                final Candidate candidate = new Candidate(site, approach,
                        distance.get(approach) + (mover.isShip() ? 1 : 0),
                        food);
                if (best == null || CANDIDATES.compare(candidate, best) < 0) {
                    best = candidate;
                }
            }
        }
        if (best != null) {
            return Optional.of(new Route(
                    Optional.of(best.approach.equals(start) ? best.site
                            : firstStep.get(best.approach)),
                    Optional.of(best.site)));
        }
        if (!explore) {
            return Optional.empty();
        }
        return distance.keySet().stream().filter(at -> !at.equals(start))
                .filter(at -> at.getNeighbors().stream()
                        .anyMatch(neighbor -> model.getMap().isValid(neighbor)
                                && !owner.isVisible(neighbor)))
                .min(Comparator.comparingInt((Location at) -> distance.get(at))
                        .thenComparing(COORDINATES))
                .map(firstStep::get)
                .map(step -> new Route(Optional.of(step), Optional.empty()));
    }

    private static boolean known(final Model model, final Player owner,
            final Location at) {
        return model.getMap().isValid(at) && owner.isVisible(at);
    }

    private static int foodYield(final Model model, final Unit founder,
            final Location site) {
        final Player owner = founder.getOwner();
        if (!known(model, owner, site)) {
            return -1;
        }
        final List<Location> neighbors = site.getNeighbors();
        if (neighbors.stream().anyMatch(
                at -> model.getMap().isValid(at) && !owner.isVisible(at))
                || !model.canFoundColonyAt(founder, site)
                || neighbors.stream().filter(at -> model.getMap().isValid(at))
                        .noneMatch(at -> model.getMap().getTerrainTypeAt(at)
                                .isSea())) {
            return -1;
        }
        final int yield = model.getAvailableColonyFoodFields(owner, site)
                .stream()
                .filter(at -> model.getMap().getTerrainTypeAt(at).isLand())
                .mapToInt(at -> model.getMap().getTerrainAt(at)
                        .canProduceAmmount(GoodsType.CORN))
                .max().orElse(-1);
        return yield >= founder.getType().getAteFoodPerTurn() ? yield : -1;
    }

    /** A planned next step, and the known site it leads to when one is set. */
    private static final class Route {
        private final Optional<Location> step;
        private final Optional<Location> site;

        private Route(final Optional<Location> step,
                final Optional<Location> site) {
            this.step = step;
            this.site = site;
        }
    }

    private static final class Candidate {
        private final Location site;
        private final Location approach;
        private final int distance;
        private final int food;

        private Candidate(final Location site, final Location approach,
                final int distance, final int food) {
            this.site = site;
            this.approach = approach;
            this.distance = distance;
            this.food = food;
        }
    }
}
