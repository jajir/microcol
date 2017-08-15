package org.microcol.model;

import com.google.common.base.Preconditions;

public class GoodAmmount {

	private int ammount;

	private final GoodType goodType;

	public GoodAmmount(final GoodType goodType, final int initialAmmmount) {
		this.goodType = Preconditions.checkNotNull(goodType);
		ammount = initialAmmmount;
	}

	public int getAmmount() {
		return ammount;
	}

	public void setAmmount(int ammount) {
		this.ammount = ammount;
	}

	public GoodType getGoodType() {
		return goodType;
	}

}
