package org.microcol.model.unit;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.store.UnitActionBuildRoadPo;
import org.microcol.model.store.UnitActionPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Describe build road action. Its completion is the single transaction that
 * records the road and spends the tools.
 */
public final class UnitActionBuildRoad implements UnitAction {

    /** Owner updates needed by an ordinary pioneer. */
    public static final int REQUIRED_TURNS_TO_BUILD_ROAD = 2;

    /** Owner updates needed by a hardy pioneer. */
    public static final int HARDY_TURNS_TO_BUILD_ROAD = 1;

    /** Tools consumed once when the road is completed. */
    public static final int REQUIRED_TOOLS = 20;

    private int remainingTurns;

    /** Set once the work completed or became stale; it never revives. */
    private boolean finished;

    /**
     * Create instance and initialize number of remaining turns.
     */
    public UnitActionBuildRoad() {
        remainingTurns = REQUIRED_TURNS_TO_BUILD_ROAD;
    }

    @Override
    public UnitActionType getType() {
        return UnitActionType.buildRoad;
    }

    /**
     * Get remaining number of turns.
     *
     * @return number of turns
     */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    /**
     * Allows to set how many turns more will be action executed.
     *
     * @param remainingTurns remaining number of turn
     */
    public void setRemainingTurns(final int remainingTurns) {
        Preconditions.checkArgument(
                remainingTurns >= 1
                        && remainingTurns <= REQUIRED_TURNS_TO_BUILD_ROAD,
                "Invalid build road countdown %s", remainingTurns);
        this.remainingTurns = remainingTurns;
    }

    @Override
    public UnitActionPo save() {
        final UnitActionBuildRoadPo out = new UnitActionBuildRoadPo();
        out.setRemainingTurns(remainingTurns);
        return out;
    }

    @Override
    public boolean startTurn(final Model model, final AbstractUnit unit) {
        // A callback from the wrong model or unit, or for a canceled or
        // replaced order, changes nothing, not even this order's lifecycle.
        if (unit.getAction() != this
                || model.tryGetUnitById(unit.getId()).orElse(null) != unit) {
            return true;
        }
        if (finished) {
            return true;
        }
        unit.setActionPoints(0);
        if (!isTargetStillValid(model, unit)) {
            // Stale work ends without a road or tool charge.
            finished = true;
            return true;
        }
        remainingTurns--;
        if (remainingTurns > 0) {
            return false;
        }
        finished = true;
        ((UnitFreeColonist) unit).consumeWorkTools(REQUIRED_TOOLS);
        model.getMap().buildRoad(unit.getLocation());
        return true;
    }

    private boolean isTargetStillValid(final Model model,
            final AbstractUnit unit) {
        if (!(unit instanceof UnitFreeColonist) || !unit.isAtPlaceLocation()
                || ((UnitFreeColonist) unit).getTools() < REQUIRED_TOOLS) {
            return false;
        }
        final Terrain terrain = model.getMap().getTerrainAt(unit.getLocation());
        return terrain.getTerrainType().isLand() && !terrain.isHasRoad();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("remainingTurns", remainingTurns).toString();
    }

}
