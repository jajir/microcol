package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Class represents state when unit is in port.
 */
public class PlaceEuropePort extends AbstractPlace {

	private final EuropePort europePort;

	PlaceEuropePort(final Unit unit, final EuropePort port) {
		super(unit);
		this.europePort = Preconditions.checkNotNull(port);
	}

	@Override
	public String getName() {
		return "Port";
	}

	protected EuropePort getEuropePort() {
		return europePort;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PlaceEuropePort.class)
				.add("unit id", getUnit().getId())
				.add("europePort", europePort)
				.toString();
	}

}
