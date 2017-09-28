package org.microcol.model;

import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Provide reference to placed unit.
 */
public abstract class AbstractPlace implements Place {

	private final Unit unit;

	AbstractPlace(final Unit unit) {
		this.unit = Preconditions.checkNotNull(unit);
	}

	@Override
	public Unit getUnit() {
		return unit;
	}

	@Override
	public void destroy() {
		// default empty implementation.
	}
	
	@Override
	public PlacePo save(final UnitPo unitPo){
		//TODO remove from here
		return null;
	}


}
