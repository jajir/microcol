package org.microcol.model.unit;

import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.store.UnitActionPlowFieldPo;
import org.microcol.model.store.UnitActionPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Utility class converting unit action persistent object to model unit action
 * object.
 */
public class UnitActionConverter {

    private static ChainOfCommandStrategy<UnitActionPo, UnitAction> convertor = new ChainOfCommandStrategy<>(
	    ImmutableList.of(po -> {
		if (UnitActionType.noAction.equals(po.getType())) {
		    return new UnitActionNoAction();
		}
		return null;
	    }, po -> {
		if (UnitActionType.plowField.equals(po.getType())) {
		    final UnitActionPlowField out = new UnitActionPlowField();
		    out.setRemainingTurns(((UnitActionPlowFieldPo) po).getRemainingTurns());
		    return out;
		}
		return null;
	    }));

    public static UnitAction convert(final UnitActionPo unitActionPo) {
	Preconditions.checkNotNull(unitActionPo, "unitActionPo is null");
	return convertor.apply(unitActionPo);
    }

}
