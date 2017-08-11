package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Class represents state when unit is in port.
 */
public class PlaceEuropePort extends AbstractPlace {

	private final EuropePort port;

	PlaceEuropePort(final Unit unit, final EuropePort port) {
		super(unit);
		this.port = Preconditions.checkNotNull(port);
	}

	@Override
	public String getName() {
		return "Port";
	}

	protected EuropePort getPort() {
		return port;
	}

}
