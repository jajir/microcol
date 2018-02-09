package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds information about price of some good in Europe.
 */
public class GoodTrade {
	
	private final GoodType goodType;
	
	/**
	 * Player could sell goods for this price. 
	 */
	private int sellPrice;
	
	/**
	 * Player could buy goods for this price. 
	 */
	private int buyPrice;

	GoodTrade(final GoodType goodType, final int sellPrice, final int buyPrice) {
		this.goodType = Preconditions.checkNotNull(goodType);
		this.sellPrice = sellPrice;
		this.buyPrice = buyPrice;
	}
	
	/**
	 * Return how much units of given goods could be bought for given gold.
	 * 
	 * @param availableGold
	 *            required gold
	 * @return Return goods amount which could be bought
	 */
	public GoodAmount getAvailableAmountFor(final int availableGold){
		return new GoodAmount(goodType, Math.min(availableGold / buyPrice, CargoSlot.MAX_CARGO_SLOT_CAPACITY));
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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("buyPrice", buyPrice)
				.add("goodType", goodType)
				.add("sellPrice", sellPrice)
				.toString();
	}	

}
