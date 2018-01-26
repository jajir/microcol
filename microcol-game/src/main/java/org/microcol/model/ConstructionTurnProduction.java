package org.microcol.model;

public class ConstructionTurnProduction {

	private final int consumedGoods;

	private final int producedGoods;
	
	private final int blockedGoods;
	
	ConstructionTurnProduction(final int consumedGoods, final int producedGoods, final int blockedGoods){
		this.consumedGoods = consumedGoods;
		this.producedGoods = producedGoods;
		this.blockedGoods = blockedGoods;
	}

	/**
	 * @return the consumedGoods
	 */
	public int getConsumedGoods() {
		return consumedGoods;
	}

	/**
	 * @return the producedGoods
	 */
	public int getProducedGoods() {
		return producedGoods;
	}

	/**
	 * @return the blockedGoods
	 */
	public int getBlockedGoods() {
		return blockedGoods;
	}
	
}
