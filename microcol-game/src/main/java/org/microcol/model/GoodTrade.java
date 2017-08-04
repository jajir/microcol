package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Holds information about price of some good in Europe.
 */
public class GoodTrade {
	private final GoodType goodType;
	private int sellPrice;
	private int buyPrice;

	GoodTrade(final GoodType goodType, final int sellPrice, final int buyPrice) {
		this.goodType = Preconditions.checkNotNull(goodType);
		this.sellPrice = sellPrice;
		this.buyPrice = buyPrice;
	}

	public int getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(int sellPrice) {
		this.sellPrice = sellPrice;
	}

	public GoodType getGoodType() {
		return goodType;
	}

	public int getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(int buyPrice) {
		this.buyPrice = buyPrice;
	}
}
