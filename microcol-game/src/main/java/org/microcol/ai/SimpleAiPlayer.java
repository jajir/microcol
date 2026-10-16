package org.microcol.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.FounderOffer;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;

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
        if (getPlayer().isKing() || !getPlayer().getColonies().isEmpty()) {
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
