package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Colony;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.FounderOffer;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.MoreObjects;

public final class SimpleAiPlayer extends AbstractRobotPlayer {

    private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

    private final UnitMoveNavigator unitDirections;

    private Unit founder;

    private Unit carrier;

    public SimpleAiPlayer(final Model model, final Player player,
            final AnimationLock animationManager) {
        super(model, player, animationManager);
        unitDirections = new UnitMoveNavigator();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).toString();
    }

    @Override
    void moveUnit(final Unit unit) {
        if (!unit.getAction().isNoAction()) {
            return;
        }
        if (unit == founder || unit == carrier) {
            pursueFirstColony(unit);
            return;
        }
        while (isActiveTurn()
                && getModel().getCurrentPlayer() == unit.getOwner()
                && unit.isAtPlaceLocation() && unit.getActionPoints() > 0) {
            final List<Location> locations = computeMoveLocation(unit);
            if (!locations.isEmpty()) {
                getModel().moveUnit(unit, Path.of(locations));
            }
            if (!isActiveTurn()) {
                return;
            }
            simpleUnitBehavior.tryToFight(unit);
            if (locations.isEmpty()) {
                break;
            }
        }
        if (isActiveTurn()) {
            tryToEmbark(unit);
        }
    }

    private List<Location> computeMoveLocation(final Unit unit) {
        final List<Location> locations = new ArrayList<>();
        Location lastLocation = unit.getLocation();
        while (locations.size() < 1
                && locations.size() < unit.getActionPoints()) {
            if (isPossibleToAttack(unit, lastLocation)) {
                return locations;
            }
            final Location preferred = lastLocation
                    .add(unitDirections.getLastVector(unit));
            if (canMoveAt(unit, preferred)) {
                locations.add(preferred);
                lastLocation = preferred;
                continue;
            }
            boolean moved = false;
            for (Location neighbor : lastLocation.getNeighbors()) {
                if (canMoveAt(unit, neighbor)) {
                    locations.add(neighbor);
                    lastLocation = neighbor;
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                return locations;
            }
            unitDirections.resetDirection(unit);
        }
        return locations;
    }

    /**
     * Prevent move unit to wrong place.
     * <p>
     * Ships are blocked to go to high-seas
     * </p>
     * .
     *
     * @param unit     required unit
     * @param location required location
     * @return return <code>true</code> when unit can move to given location
     */
    private boolean canMoveAt(final Unit unit, final Location location) {
        return unit.isPossibleToMoveAt(location) && !getModel().getMap()
                .getTerrainTypeAt(location).equals(TerrainType.HIGH_SEA);
    }

    private boolean isPossibleToAttack(final Unit unit,
            final Location lastLocation) {
        if (unit.canAttack()) {
            for (final Location location : lastLocation.getNeighbors()) {
                if (unit.isPossibleToAttackAt(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void tryToEmbark(final Unit unit) {
        if (unit.isStorable() && unit.isAtPlaceLocation()
                && unit.getActionPoints() > 0) {
            unit.getNeighborUnitsWithFreeSlot().stream()
                    .flatMap(u -> u.getCargo().getSlots().stream())
                    .filter(unit::canEmbark).findFirst()
                    .ifPresent(unit::embarkFromLocation);
        }
    }

    @Override
    void turnStarted() {
        founder = null;
        carrier = null;
        if (!getPlayer().getFounderOffers().isEmpty()) {
            final FounderOffer offer = getPlayer().getFounderOffers().get(0);
            if (getPlayer().canChooseFounder(offer)) {
                getPlayer().chooseFounder(offer);
            }
        }
        if (getPlayer().isKing()) {
            return;
        }
        if (!getPlayer().getColonies().isEmpty()) {
            getPlayer().getColonies().stream()
                    .sorted(Comparator.comparingInt(Colony::getId))
                    .forEach(this::assignColonyWorkforce);
            return;
        }
        founder = getPlayer().getAllUnits().stream()
                .filter(unit -> unit.getType().canBuildColony()
                        && unit.getAction().isNoAction())
                .filter(unit -> unit.isAtPlaceLocation() || (unit
                        .isAtCargoSlot()
                        && unit.getPlaceCargoSlot().getOwnerUnit().isShip()
                        && unit.getPlaceCargoSlot().getOwnerUnit()
                                .getOwner() == getPlayer()
                        && unit.getPlaceCargoSlot().getOwnerUnit()
                                .isAtPlaceLocation()
                        && unit.getPlaceCargoSlot().getOwnerUnit().getAction()
                                .isNoAction()))
                .min(Comparator.comparingInt(Unit::getId)).orElse(null);
        if (founder != null && founder.isAtCargoSlot()) {
            carrier = founder.getPlaceCargoSlot().getOwnerUnit();
        }
    }

    /**
     * One bounded pass over the colony's available workers: food first, then
     * spare workers on lumber. Offers and execution stay model-validated.
     */
    private void assignColonyWorkforce(final Colony colony) {
        final Set<Integer> teaching = new HashSet<>();
        colony.getEducationCourses().forEach(course -> {
            teaching.add(course.getStudentId());
            if (course.getTeacherId() != null) {
                teaching.add(course.getTeacherId());
            }
        });
        final List<Unit> workers = new ArrayList<>();
        workers.addAll(colony.getUnitsInsideColony());
        getModel().getUnitsAt(colony.getLocation()).stream().filter(
                unit -> unit.isAtPlaceLocation() && !workers.contains(unit))
                .forEach(workers::add);
        workers.sort(Comparator.comparingInt(Unit::getId));
        for (final Unit worker : workers) {
            if (!isActiveTurn() || !colony.isValid()) {
                return;
            }
            if (!isOrdinaryWorker(worker)
                    || teaching.contains(worker.getId())) {
                continue;
            }
            try {
                assignWorker(colony, worker);
            } catch (IllegalArgumentException | IllegalStateException stale) {
                // The model rejected a stale offer; keep the worker as is.
                org.slf4j.LoggerFactory.getLogger(SimpleAiPlayer.class)
                        .debug("Colony job was rejected", stale);
            }
        }
    }

    /** Own, unordered civilian colonists; armed or mounted units keep duty. */
    private boolean isOrdinaryWorker(final Unit unit) {
        return unit instanceof UnitFreeColonist
                && unit.getOwner() == getPlayer()
                && unit.getAction().isNoAction()
                && !((UnitFreeColonist) unit).isHoldingGuns()
                && !((UnitFreeColonist) unit).isMounted();
    }

    private void assignWorker(final Colony colony, final Unit worker) {
        final boolean resident = colony.getUnitsInsideColony().contains(worker);
        final GoodsType current = worker.isAtPlaceColonyField()
                ? worker.getPlaceColonyField().getProducedGoodsType()
                : null;
        final List<ColonyJobChoice> choices = colony.getJobChoices(worker)
                .stream()
                .filter(choice -> choice.getUnavailableReason().isEmpty()
                        && choice.getPotential().isPresent()
                        && choice.getPotential().get().getAmount() > 0
                        && choice.getKind() != ColonyJobChoice.Kind.INDOOR)
                .collect(Collectors.toList());
        final Optional<ColonyJobChoice> food = bestChoice(choices,
                GoodsType.CORN);
        final Optional<ColonyJobChoice> lumber = bestChoice(choices,
                GoodsType.LUMBER);
        final int net = colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
        final int eaten = resident ? 0 : worker.getType().getAteFoodPerTurn();
        final int produced = current == GoodsType.CORN
                ? choices.stream()
                        .filter(choice -> choice
                                .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD
                                && choice.getGoodsType() == GoodsType.CORN)
                        .mapToInt(choice -> choice.getPotential().get()
                                .getAmount())
                        .findFirst().orElse(0)
                : 0;
        if (current == GoodsType.CORN) {
            if (net < 0) {
                // Short of food: move only to a strictly better legal field.
                if (food.isPresent() && food.get().getPotential().get()
                        .getAmount() > produced) {
                    food.get().execute();
                }
            } else if (lumber.isPresent() && net - produced >= 0) {
                // Clear surplus: this food worker is spare.
                lumber.get().execute();
            }
        } else if (resident) {
            if (net < 0 && food.isPresent()) {
                food.get().execute();
            } else if (net >= 0 && current != GoodsType.LUMBER
                    && lumber.isPresent()) {
                lumber.get().execute();
            }
        } else if (lumber.isPresent() && net - eaten >= 0) {
            lumber.get().execute();
        } else if (food.isPresent()) {
            food.get().execute();
        }
    }

    /** Highest yield wins; the current field, then offer order, break ties. */
    private Optional<ColonyJobChoice> bestChoice(
            final List<ColonyJobChoice> choices, final GoodsType goods) {
        ColonyJobChoice best = null;
        for (final ColonyJobChoice choice : choices) {
            if (choice.getGoodsType() == goods && (best == null
                    || choice.getPotential().get().getAmount() > best
                            .getPotential().get().getAmount())) {
                best = choice;
            }
        }
        return Optional.ofNullable(best);
    }

    private void pursueFirstColony(final Unit mover) {
        // One extra iteration permits landing after the carrier spends its last
        // point.
        final int limit = mover.getActionPoints() + 1;
        for (int step = 0; step < limit && isActiveTurn()
                && mover.isAtPlaceLocation(); step++) {
            final Optional<Location> next = FirstColonyPlanner
                    .nextStep(getModel(), founder);
            if (next.isEmpty()) {
                return;
            }
            final Location target = next.get();
            try {
                if (founder.isAtPlaceLocation()
                        && target.equals(founder.getLocation())) {
                    if (getModel().canUnitBuildColony(founder)) {
                        getModel().buildColony(getPlayer(), founder,
                                getModel().suggestColonyName(Set.of()));
                    }
                    return;
                }
                if (founder.isAtCargoSlot() && getModel().getMap()
                        .getTerrainTypeAt(target).isLand()) {
                    if (founder.canUnitDisembarkAt(target)) {
                        founder.disembarkToLocation(target);
                        waitForAnimations();
                    }
                    return;
                }
                if (mover.getActionPoints() <= 0) {
                    return;
                }
                final Location before = mover.getLocation();
                getModel().moveUnit(mover, Path.of(List.of(target)));
                waitForAnimations();
                if (!isActiveTurn() || !mover.isAtPlaceLocation()
                        || mover.getLocation().equals(before)) {
                    return;
                }
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                // A live model rejection ends this attempt; the next turn
                // replans.
                org.slf4j.LoggerFactory.getLogger(SimpleAiPlayer.class)
                        .debug("First-colony action was rejected", rejected);
                return;
            }
        }
    }
}
