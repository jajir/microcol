package org.microcol.model.unit;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.Terrain;
import org.microcol.model.store.UnitActionPlowFieldPo;
import org.microcol.model.store.UnitActionPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Describe plow field action.
 */
public final class UnitActionPlowField implements UnitAction {

    /** Owner updates needed by an ordinary pioneer. */
    public static final int REQUIRED_TURNS_TO_PLOW_FIELD = 4;

    /** Owner updates needed by a hardy pioneer. */
    public static final int HARDY_TURNS_TO_PLOW_FIELD = 2;

    /** Tools consumed once when the field is completed. */
    public static final int REQUIRED_TOOLS = 20;

    private int remainingTurns;

    /** Set once the work completed or became stale; it never revives. */
    private boolean finished;

    /**
     * Create instance and initialize number of remaining turns.
     */
    public UnitActionPlowField() {
        remainingTurns = REQUIRED_TURNS_TO_PLOW_FIELD;
    }

    @Override
    public UnitActionType getType() {
        return UnitActionType.plowField;
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
                        && remainingTurns <= REQUIRED_TURNS_TO_PLOW_FIELD,
                "Invalid plow countdown %s", remainingTurns);
        this.remainingTurns = remainingTurns;
    }

    @Override
    public UnitActionPo save() {
        final UnitActionPlowFieldPo out = new UnitActionPlowFieldPo();
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
            // Stale work ends without a field effect or tool charge.
            finished = true;
            return true;
        }
        remainingTurns--;
        if (remainingTurns > 0) {
            return false;
        }
        finished = true;
        ((UnitFreeColonist) unit).consumeWorkTools(REQUIRED_TOOLS);
        model.getMap().plowFiled(unit.getLocation());
        model.recordNativeLandUse(unit.getOwner(), unit.getLocation());
        return true;
    }

    private boolean isTargetStillValid(final Model model,
            final AbstractUnit unit) {
        if (!(unit instanceof UnitFreeColonist) || !unit.isAtPlaceLocation()
                || ((UnitFreeColonist) unit).getTools() < REQUIRED_TOOLS
                || model.getColonyAt(unit.getLocation()).isPresent()) {
            return false;
        }
        final Terrain terrain = model.getMap().getTerrainAt(unit.getLocation());
        return terrain.getTerrainType().isCanHaveField()
                && !terrain.isHasTrees() && !terrain.isHasField()
                && terrain.getRiverSize() == org.microcol.model.RiverSize.NONE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("remainingTurns", remainingTurns).toString();
    }

}
