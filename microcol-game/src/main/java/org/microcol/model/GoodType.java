package org.microcol.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public enum GoodType {

	CORN(), SUGAR(), TABACCO(), COTTON(), FUR(), LUMBER(), ORE(), SILVER(), HORSE(), RUM(), CIGARS(), SILK(), COAT(), GOODS(), TOOLS(), MUSKET();

	GoodType() {
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(GoodType.class).add("name", name()).toString();
	}

	public static List<GoodType> getGoodTypes() {
		return ImmutableList.<GoodType>of(CORN, SUGAR, TABACCO, COTTON, FUR, LUMBER, ORE, SILVER, HORSE, RUM, CIGARS,
				SILK, COAT, GOODS, TOOLS, MUSKET);
	}

}
