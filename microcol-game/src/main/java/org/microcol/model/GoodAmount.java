package org.microcol.model;

import com.google.common.base.Preconditions;

public class GoodAmount {

	private int amount;

	private final GoodType goodType;

	public GoodAmount(final GoodType goodType, final int initialAmount) {
		this.goodType = Preconditions.checkNotNull(goodType);
		Preconditions.checkArgument(initialAmount >= 0, "Amount (%s) can't be less than zero", amount);
		Preconditions.checkArgument(initialAmount <= 100, "Amount (%s) can't be higher than 100", amount);
		amount = initialAmount;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public GoodType getGoodType() {
		return goodType;
	}

}
