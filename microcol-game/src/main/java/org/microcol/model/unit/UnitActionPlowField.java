package org.microcol.model.unit;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.store.UnitActionPlowFieldPo;
import org.microcol.model.store.UnitActionPo;

import com.google.common.base.MoreObjects;

/**
 * Describe plow field action.
 */
public final class UnitActionPlowField implements UnitAction {

    private static final int REQUIRED_TURNS_TO_PLOW_FIELD = 4;

    private int remainingTurns;

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
     * @param remainingTurns
     *            remaining number of turn
     */
    public void setRemainingTurns(final int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }

    @Override
    public UnitActionPo save() {
        final UnitActionPlowFieldPo out = new UnitActionPlowFieldPo();
        out.setRemainingTurns(remainingTurns);
        return out;
    }

    @Override
    public void startTurn(final Model model, final AbstractUnit unit) {
        unit.setActionPoints(0);
        remainingTurns--;
        if (remainingTurns == 0) {
            model.getMap().plowFiled(unit.getLocation());
            unit.setActionType(UnitActionType.noAction);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("remainingTurns", remainingTurns)
                .toString();
    }

}
