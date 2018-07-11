package org.microcol.model.unit;

import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.store.UnitActionPlowFieldPo;
import org.microcol.model.store.UnitActionPo;

import com.google.common.base.MoreObjects;

public final class UnitActionPlowField implements UnitAction {

    private final static int REQUIRED_TURNS_TO_PLOW_FIELD = 4;

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

    public int getRemainingTurns() {
	return remainingTurns;
    }

    public void setRemainingTurns(int remainingTurns) {
	this.remainingTurns = remainingTurns;
    }

    @Override
    public UnitActionPo save() {
	final UnitActionPlowFieldPo out = new UnitActionPlowFieldPo();
	out.setRemainingTurns(remainingTurns);
	return out;
    }

    @Override
    public void startTurn(final Model model, final Unit unit) {
	remainingTurns--;
	if (remainingTurns == 0) {
	    model.getMap().plowFiled(unit.getLocation());
	    unit.setAction(UnitActionType.noAction);
	}
    }

    @Override
    public String toString() {
	return MoreObjects.toStringHelper(getClass()).add("remainingTurns", remainingTurns).toString();
    }

}
