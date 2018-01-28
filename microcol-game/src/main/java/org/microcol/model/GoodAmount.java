package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable class holding some number of goods of same type.
 */
public class GoodAmount {

	private final int amount;

	private final GoodType goodType;

	public GoodAmount(final GoodType goodType, final int initialAmount) {
		this.goodType = Preconditions.checkNotNull(goodType);
		Preconditions.checkArgument(initialAmount >= 0, "Amount (%s) can't be less than zero", initialAmount);
		Preconditions.checkArgument(initialAmount <= 100, "Amount (%s) can't be higher than 100", initialAmount);
		amount = initialAmount;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(GoodAmount.class)
				.add("GoodType", goodType.name())
				.add("amount", amount)
				.toString();
	}
	
	public GoodAmount add(final GoodAmount goodAmount) {
		Preconditions.checkNotNull(goodAmount);
		Preconditions.checkArgument(goodType.equals(goodAmount.getGoodType()),
				"Added good amount have differet type (%s) fro current one (%s)", goodAmount, this);
		return new GoodAmount(goodType, amount + goodAmount.getAmount());
	}
	
	public boolean isZero() {
		return amount == 0;
	}

	public GoodAmount substract(final int howMuch) {
		return new GoodAmount(goodType, amount - howMuch);
	}
	
	public int getAmount() {
		return amount;
	}

	public GoodType getGoodType() {
		return goodType;
	}

}
