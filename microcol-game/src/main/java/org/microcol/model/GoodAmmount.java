package org.microcol.model;

import com.google.common.base.Preconditions;

public class GoodAmmount {

	private int ammount;

	private final GoodType goodType;

	GoodAmmount(final GoodType goodType) {
		this.goodType = Preconditions.checkNotNull(goodType);
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
