package org.microcol.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class Europe {

	private final List<GoodTrade> goodTrades = ImmutableList.of(new GoodTrade(GoodType.CORN, 0, 8),
			new GoodTrade(GoodType.SUGAR, 3, 5), new GoodTrade(GoodType.TABACCO, 3, 5),
			new GoodTrade(GoodType.COTTON, 3, 5), new GoodTrade(GoodType.FUR, 0, 8),
			new GoodTrade(GoodType.LUMBER, 0, 8), new GoodTrade(GoodType.ORE, 0, 8),
			new GoodTrade(GoodType.SILVER, 0, 8), new GoodTrade(GoodType.HORSE, 0, 8),
			new GoodTrade(GoodType.RUM, 11, 12), new GoodTrade(GoodType.CIGARS, 11, 12),
			new GoodTrade(GoodType.SILK, 11, 12), new GoodTrade(GoodType.COAT, 9, 10),
			new GoodTrade(GoodType.GOODS, 2, 3), new GoodTrade(GoodType.TOOLS, 1, 2),
			new GoodTrade(GoodType.MUSKET, 2, 3));

	Europe() {

	}

	public GoodTrade getGoodTradeForType(final GoodType goodType) {
		return goodTrades.stream().filter(goodTrade -> goodTrade.getGoodType().equals(goodType)).findAny().get();
	}

}
