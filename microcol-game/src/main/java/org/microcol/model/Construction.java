package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Construction {

	private final ConstructionType type;

	private final List<PlaceConstruction> constructions;

	Construction(final ConstructionType type) {
		this.type = Preconditions.checkNotNull(type);
		constructions = Lists.newArrayList();
	}

	ConstructionType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Construction.class).add("name", type.name()).toString();
	}

}
