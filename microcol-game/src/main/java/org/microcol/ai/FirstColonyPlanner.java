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

    static Optional<Location> nextStep(final Model model, final Unit founder) {
        final Player owner = founder.getOwner();
        if (model.getAllUnits().stream().noneMatch(unit -> unit == founder)
                || !founder.getType().canBuildColony() || owner.isKing()
                || model.getColonies().stream()
                        .anyMatch(colony -> colony.getOwner() == owner)) {
            return Optional.empty();
        }
        final Unit mover;
        if (founder.isAtPlaceLocation()) {
            mover = founder;
        } else if (founder.isAtCargoSlot()) {
            mover = founder.getPlaceCargoSlot().getOwnerUnit();
            if (!mover.isShip() || mover.getOwner() != owner
                    || !mover.isAtPlaceLocation()) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        final Location start = mover.getLocation();
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
            return Optional.of(best.approach.equals(start) ? best.site
                    : firstStep.get(best.approach));
        }
        return distance.keySet().stream().filter(at -> !at.equals(start))
                .filter(at -> at.getNeighbors().stream()
                        .anyMatch(neighbor -> model.getMap().isValid(neighbor)
                                && !owner.isVisible(neighbor)))
                .min(Comparator.comparingInt((Location at) -> distance.get(at))
                        .thenComparing(COORDINATES))
                .map(firstStep::get);
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
