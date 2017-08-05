package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * It's a place where are units traveling from colonies to Europe and from
 * Europe to colonies.
 */
public class HighSea {

	private final List<HighSeaUnit> highSeaUnits = Lists.newArrayList();

	public HighSea(final List<HighSeaUnit> initializedHighSeaUnits) {
		Preconditions.checkNotNull(initializedHighSeaUnits);
		this.highSeaUnits.addAll(initializedHighSeaUnits);
	}

	public List<Unit> getUnitsTravelingTo(final boolean isItToEurope) {
		return highSeaUnits.stream()
				.filter(hsu -> (isItToEurope && hsu.isTravelToEurope()) || (!isItToEurope && !hsu.isTravelToEurope()))
				.map(HighSeaUnit::getUnit).collect(Collectors.toList());
	}

}
