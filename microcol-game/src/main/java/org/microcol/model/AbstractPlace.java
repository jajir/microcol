package org.microcol.model;

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

}
