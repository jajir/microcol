package org.microcol.model.store;

import org.microcol.model.unit.UnitActionType;

public final class UnitActionNoActionPo implements UnitActionPo {

	@Override
	public UnitActionType getType() {
		return UnitActionType.noAction;
	}
}
