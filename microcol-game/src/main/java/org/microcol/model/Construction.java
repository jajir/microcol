package org.microcol.model;

import com.google.common.base.Preconditions;

public class Construction {

	private final ConstructionType type;

	Construction(final ConstructionType type) {
		this.type = Preconditions.checkNotNull(type);
	}

}
