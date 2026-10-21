package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.TradeRoutePo;
import org.microcol.model.store.TradeRouteStopPo;

import com.google.common.base.Preconditions;

/**
 * Persisted collection of trade routes with stable IDs. It keeps names unique
 * per owner, validates saved data without guessing defaults and performs the
 * atomic insertion after {@link Model} has checked every rule.
 */
final class TradeRoutes {

    static final int MAX_STOPS = 4;

    private final List<TradeRoute> routes = new ArrayList<>();
    private int nextId;
    /** First colony ID not yet allocated; saved stops must stay below it. */
    private final int nextColonyId;

    /**
     * Restores saved routes. Required identity data must be present; the colony
     * IDs must be allocated, though the colonies themselves may have been lost
     * since.
     *
     * @param model   required model with players already restored
     * @param modelPo required saved model
     */
    TradeRoutes(final Model model, final ModelPo modelPo) {
        Preconditions.checkArgument(
                modelPo.getNextTradeRouteId() != null
                        && modelPo.getNextTradeRouteId() >= 0,
                "Unsupported save: missing or invalid nextTradeRouteId");
        Preconditions.checkArgument(modelPo.getTradeRoutes() != null,
                "Unsupported save: missing trade routes");
        nextId = modelPo.getNextTradeRouteId();
        nextColonyId = modelPo.getNextColonyId();
        final Set<Integer> ids = new HashSet<>();
        for (final TradeRoutePo po : modelPo.getTradeRoutes()) {
            final TradeRoute route = restore(model, modelPo.getNextColonyId(),
                    po);
            Preconditions.checkArgument(
                    ids.add(route.getId()) && route.getId() < nextId,
                    "Invalid or duplicate trade route id");
            Preconditions.checkArgument(
                    !isNameTaken(route.getOwner(), route.getName()),
                    "Duplicate trade route name");
            routes.add(route);
        }
    }

    private static TradeRoute restore(final Model model,
            final Integer nextColonyId, final TradeRoutePo po) {
        Preconditions.checkArgument(
                po != null && po.getId() != null && po.getId() >= 0
                        && po.getOwnerName() != null && po.getName() != null
                        && po.getKind() != null && po.getStartColonyId() != null
                        && po.getStops() != null && po.getStops().size() >= 1
                        && po.getStops().size() <= MAX_STOPS,
                "Invalid trade route");
        Preconditions.checkArgument(
                po.getStartColonyId() >= 0
                        && po.getStartColonyId() < nextColonyId,
                "Trade route refers to an unallocated colony id");
        final Player owner = model.getPlayerStore().getPlayers().stream()
                .filter(player -> player.getName().equals(po.getOwnerName())
                        && player.isColonialNation())
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Trade route owner is not a European player"));
        Preconditions.checkArgument(
                ColonyNames.getTextProblem(po.getName()).isEmpty()
                        && ColonyNames.normalize(po.getName())
                                .equals(po.getName()),
                "Invalid trade route name");
        final List<TradeRouteStop> stops = new ArrayList<>();
        for (final TradeRouteStopPo stopPo : po.getStops()) {
            Preconditions.checkArgument(stopPo != null
                    && stopPo.getEurope() != null
                    && (stopPo.getEurope() ? stopPo.getColonyId() == null
                            : stopPo.getColonyId() != null
                                    && stopPo.getColonyId() >= 0
                                    && stopPo.getColonyId() < nextColonyId),
                    "Invalid trade route stop");
            Preconditions.checkArgument(
                    !(stopPo.getEurope()
                            && po.getKind() == TradeRouteKind.LAND),
                    "Land trade route cannot visit Europe");
            // Missing lists are rejected here; empty lists are saved
            // explicitly.
            stops.add((stopPo.getEurope() ? TradeRouteStop.europe()
                    : TradeRouteStop.ofColony(stopPo.getColonyId()))
                    .withCargo(stopPo.getUnloadGoods(), stopPo.getLoadGoods()));
        }
        return new TradeRoute(po.getId(), owner, po.getName(), po.getKind(),
                po.getStartColonyId(), stops);
    }

    List<TradeRoute> getRoutes(final Player owner) {
        return routes.stream().filter(route -> route.getOwner() == owner)
                .collect(Collectors.toList());
    }

    /**
     * @param owner          required owner
     * @param normalizedName required name already normalized
     * @return whether this owner already has a route with the same name under
     *         case-insensitive comparison
     */
    boolean isNameTaken(final Player owner, final String normalizedName) {
        return routes.stream().anyMatch(route -> route.getOwner() == owner
                && route.getName().equalsIgnoreCase(normalizedName));
    }

    boolean hasIdCapacity() {
        return nextId < Integer.MAX_VALUE;
    }

    /** @return the stored route with this stable ID, if one exists */
    Optional<TradeRoute> find(final int id) {
        return routes.stream().filter(route -> route.getId() == id).findFirst();
    }

    /**
     * Replaces one stop of a stored route. The ID, name, owner, kind, start and
     * stop order stay the same; the caller has already validated the stop.
     *
     * @param route     required stored route, by identity
     * @param stopIndex index of the stop in {@code route}
     * @param stop      required replacement stop
     * @return the stored replacement route
     */
    TradeRoute replaceStop(final TradeRoute route, final int stopIndex,
            final TradeRouteStop stop) {
        final int at = routes.indexOf(route);
        Preconditions.checkState(at >= 0, "Trade route %s is not stored",
                route.getId());
        final List<TradeRouteStop> stops = new ArrayList<>(route.getStops());
        stops.set(stopIndex, stop);
        final TradeRoute updated = new TradeRoute(route.getId(),
                route.getOwner(), route.getName(), route.getKind(),
                route.getStartColonyId(), stops);
        routes.set(at, updated);
        return updated;
    }

    /**
     * Checks a restored processed-visit mark against the saved route it names
     * and, while the stop's colony still exists, against its square. A bad
     * reference rejects the save instead of being dropped silently.
     *
     * @param model required model with every unit and colony restored
     * @param unit  required restored unit
     */
    void checkProcessedVisit(final Model model, final Unit unit) {
        unit.getProcessedTradeRouteVisit().ifPresent(visit -> {
            final TradeRoute route = find(visit.getRouteId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Processed trade route visit names a missing route"));
            Preconditions.checkArgument(
                    route.getOwner() == unit.getOwner()
                            && visit.getStopIndex() < route.getStops().size(),
                    "Invalid processed trade route visit");
            // A carrier that still follows the route may have taken a later
            // plan after processing; its visit is a receipt of the place, not
            // a promise about the current stop, so only the range is checked.
            if (unit instanceof AbstractUnit
                    && ((AbstractUnit) unit).getTradeRouteAssignment()
                            .filter(assignment -> assignment
                                    .getRouteId() == visit.getRouteId())
                            .isPresent()) {
                return;
            }
            final TradeRouteStop stop = route.getStops()
                    .get(visit.getStopIndex());
            Preconditions.checkArgument(
                    stop.isEurope() == visit.getLocation().isEmpty(),
                    "Processed trade route visit does not match its stop");
            if (!stop.isEurope()) {
                model.getColonyById(stop.getColonyId())
                        .ifPresent(colony -> Preconditions.checkArgument(
                                visit.isAt(colony.getLocation()),
                                "Processed trade route visit is not at its colony"));
            }
        });
    }

    /**
     * Checks a restored trade route assignment against the saved route it
     * names: the route must exist, belong to the carrier's owner, match the
     * carrier's transport kind and name one of its stops. A bad reference
     * rejects the save instead of being repaired silently.
     *
     * @param unit required restored unit; units without an assignment pass
     */
    void checkAssignment(final Unit unit) {
        if (!(unit instanceof AbstractUnit)) {
            return;
        }
        ((AbstractUnit) unit).getTradeRouteAssignment()
                .ifPresent(assignment -> {
                    final TradeRoute route = find(assignment.getRouteId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Trade route assignment names a missing route"));
                    Preconditions.checkArgument(
                            route.getOwner() == unit.getOwner()
                                    && Model.isTradeRouteCarrierKind(unit,
                                            route.getKind())
                                    && assignment.getStopIndex() < route
                                            .getStops().size()
                                    && assignment.getStops().size() == route
                                            .getStops().size(),
                            "Invalid trade route assignment");
                    // The accepted stops may differ from the saved route only
                    // by a pending edit, but each must still be a legal stop
                    // of this kind whose colony ID was allocated.
                    for (final TradeRouteStop accepted : assignment
                            .getStops()) {
                        Preconditions.checkArgument(
                                accepted.isEurope()
                                        ? route.getKind() == TradeRouteKind.SEA
                                        : accepted.getColonyId() < nextColonyId,
                                "Invalid accepted trade route stop");
                    }
                });
    }

    /**
     * Restores one stop of a carrier's saved boundary copy. The colony is not
     * required to exist now; a lost stop is reported while the carrier waits.
     *
     * @param stopPo required saved stop
     * @return the stop with its cargo policy
     */
    static TradeRouteStop restoreStop(final TradeRouteStopPo stopPo) {
        Preconditions.checkArgument(
                stopPo != null && stopPo.getEurope() != null
                        && stopPo.getUnloadGoods() != null
                        && stopPo.getLoadGoods() != null
                        && (stopPo.getEurope() ? stopPo.getColonyId() == null
                                : stopPo.getColonyId() != null
                                        && stopPo.getColonyId() >= 0),
                "Invalid trade route stop");
        return (stopPo.getEurope() ? TradeRouteStop.europe()
                : TradeRouteStop.ofColony(stopPo.getColonyId()))
                .withCargo(stopPo.getUnloadGoods(), stopPo.getLoadGoods());
    }

    /**
     * Removes a stored route after {@link Model} has cleared every assignment
     * and processed visit that names it.
     *
     * @param route required stored route, by identity
     */
    void remove(final TradeRoute route) {
        Preconditions.checkState(routes.remove(route),
                "Trade route %s is not stored", route.getId());
    }

    /**
     * Adds a route that has already passed every model check.
     *
     * @return the stored route with its new stable ID
     */
    TradeRoute add(final Player owner, final String name,
            final TradeRouteKind kind, final int startColonyId,
            final List<TradeRouteStop> stops) {
        Preconditions.checkState(hasIdCapacity(), "No trade route id left");
        final TradeRoute route = new TradeRoute(nextId, owner,
                ColonyNames.normalize(name), kind, startColonyId, stops);
        routes.add(route);
        nextId++;
        return route;
    }

    void save(final ModelPo out) {
        out.setNextTradeRouteId(nextId);
        out.setTradeRoutes(routes.stream().map(TradeRoutes::savePo)
                .collect(Collectors.toList()));
    }

    private static TradeRoutePo savePo(final TradeRoute route) {
        final TradeRoutePo po = new TradeRoutePo();
        po.setId(route.getId());
        po.setOwnerName(route.getOwner().getName());
        po.setName(route.getName());
        po.setKind(route.getKind());
        po.setStartColonyId(route.getStartColonyId());
        po.setStops(route.getStops().stream().map(TradeRoutes::savePoStop)
                .collect(Collectors.toList()));
        return po;
    }

    static TradeRouteStopPo savePoStop(final TradeRouteStop stop) {
        final TradeRouteStopPo stopPo = new TradeRouteStopPo();
        stopPo.setEurope(stop.isEurope());
        stopPo.setColonyId(stop.isEurope() ? null : stop.getColonyId());
        stopPo.setUnloadGoods(new ArrayList<>(stop.getUnloadGoods()));
        stopPo.setLoadGoods(new ArrayList<>(stop.getLoadGoods()));
        return stopPo;
    }
}
