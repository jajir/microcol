package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Connect unit and unit between each other.
 */
public class PortUnit {

	private final Unit unit;

	private final Port port;

	public PortUnit(final Unit unit, final Port port) {
		this.unit = Preconditions.checkNotNull(unit);
		this.port = Preconditions.checkNotNull(port);
	}

	public Unit getUnit() {
		return unit;
	}

	public Port getPort() {
		return port;
	}

}
