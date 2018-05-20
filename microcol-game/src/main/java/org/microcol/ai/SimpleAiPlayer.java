package org.microcol.ai;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;

public class SimpleAiPlayer extends AbstractRobotPlayer {

    private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

    private final Directions unitDirections;

    public SimpleAiPlayer(final Model model, final Player player,
            final AnimationManager animationManager) {
        super(model, player, animationManager);
        unitDirections = new Directions();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).toString();
    }

    @Override
    void moveUnit(Unit unit) {
        final List<Location> locations = computeMoveLocation(unit);

        if (!locations.isEmpty()) {
            getModel().moveUnit(unit, Path.of(locations));
        }

        simpleUnitBehavior.tryToFight(unit);
        tryToEmbark(unit);
    }

    private List<Location> computeMoveLocation(final Unit unit) {
        final List<Location> locations = new ArrayList<>();
        Location lastLocation = unit.getLocation();
        while (locations.size() < unit.getAvailableMoves()) {
            if (isPossibleToAttack(unit, lastLocation)) {
                return locations;
            }
            final Location newLocation = lastLocation.add(unitDirections.getLastDirection(unit));
            if (canMoveAt(unit, newLocation)) {
                locations.add(newLocation);
                lastLocation = newLocation;
            } else {
                unitDirections.resetDirection(unit);
            }
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
     * @param unit
     *            required unit
     * @param location
     *            required location
     * @return return <code>true</code> when unit can move to given location
     */
    private boolean canMoveAt(final Unit unit, final Location location) {
        return unit.isPossibleToMoveAt(location)
                && !getModel().getMap().getTerrainTypeAt(location).equals(TerrainType.HIGH_SEA);
    }

    private boolean isPossibleToAttack(final Unit unit, final Location lastLocation) {
        if (unit.getType().canAttack()) {
            for (final Location location : lastLocation.getNeighbors()) {
                if (unit.isPossibleToAttackAt(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void tryToEmbark(final Unit unit) {
        if (unit.isStorable() && unit.isAtPlaceLocation() && unit.getAvailableMoves() > 0) {
            unit.getStorageUnits().stream().flatMap(u -> u.getCargo().getSlots().stream())
                    .filter(slot -> slot.isEmpty()).findAny().get().store(unit);
        }
    }

    @Override
    void turnStarted() {
        // do nothing
    }
}
