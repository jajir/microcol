package org.microcol.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

public enum Terrain {
	CONTINENT,
	OCEAN,
	TUNDRA,
	HIGH_SEA;
	
	public final static List<Terrain> UNIT_CAN_SAIL_AT = ImmutableList.of(OCEAN, HIGH_SEA);

}
