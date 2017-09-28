package org.microcol.model;

import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

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

	@Override
	public PlacePo save(final UnitPo unitPo){
		PlaceEuropePortPo out = new PlaceEuropePortPo();
		out.setOnPier(false);
		unitPo.setPlaceEuropePort(out);
		return out;
	}

}
