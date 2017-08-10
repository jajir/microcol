package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * It's a place where are units traveling from colonies to Europe and from
 * Europe to colonies.
 */
public class HighSea {

	private final Model model;

	public HighSea(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	private List<PlaceHighSea> getHighSeasAll() {
		return model.getUnits().stream().filter(unit -> unit.getPlace() instanceof PlaceHighSea)
				.map(unit -> (PlaceHighSea) unit.getPlace()).collect(Collectors.toList());
	}

	public List<Unit> getUnitsTravelingTo(final boolean isItToEurope) {
		return getHighSeasAll().stream()
				.filter(hsu -> (isItToEurope && hsu.isTravelToEurope()) || (!isItToEurope && !hsu.isTravelToEurope()))
				.map(PlaceHighSea::getUnit).collect(Collectors.toList());
	}

}
