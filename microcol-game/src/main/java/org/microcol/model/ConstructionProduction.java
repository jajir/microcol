package org.microcol.model;

/**
 * Holds what is produces and consumes in one construction. Class could hold
 * production for one slot and even for all slots.
 */
public class ConstructionProduction {
	
	public final static ConstructionProduction EMPTY = new ConstructionProduction(0, 0, 0, null, null);
	
	private final int consumptionPerTurn;
	
	private final int productionPerTurn;
	
	/**
	 * See {@link ConstructionType#getBaseProductionPerTurn()}.
	 */
	private final int baseProductionPerTurn;
	
	private final GoodType consumedGoods;
	
	private final GoodType producedGoods;
	
	ConstructionProduction(final int consumptionPerTurn, final int baseProductionPerTurn, final int productionPerTurn, final GoodType consumedGoods,
			final GoodType producedGoods) {
		this.consumptionPerTurn = consumptionPerTurn;
		this.baseProductionPerTurn = baseProductionPerTurn;
		this.productionPerTurn = productionPerTurn;
		this.consumedGoods = consumedGoods;
		this.producedGoods = producedGoods;
	}
	
	public ConstructionProduction multiply(final float multiplier){
		return new ConstructionProduction((int) (getConsumptionPerTurn() * multiplier), baseProductionPerTurn,
				(int) (getProductionPerTurn() * multiplier), consumedGoods, producedGoods);
	}
	
	/**
	 * Add production of one type to another one. Base production is not added.
	 * 
	 * @param production
	 *            required production
	 * @return return added productions
	 */
	public ConstructionProduction add(final ConstructionProduction production){
		return new ConstructionProduction(
				getConsumptionPerTurn() + production.getConsumptionPerTurn(),
				production.getBaseProductionPerTurn(),
				getProductionPerTurn() + production.getProductionPerTurn(),
				consumedGoods,
				producedGoods);		
	}
	
	/**
	 * Production limited by real amount of resources in warehouse.
	 *
	 * @param colonyWarehouse
	 *            required colony warehouse
	 * @return construction production which could be really produced.
	 */
	public ConstructionProduction limit(final ColonyWarehouse colonyWarehouse){
		if (consumedGoods == null) {
			// Construction doesn't consume any goods
			return this;
		} else {
			final int maxCanConsumeGoods = consumptionPerTurn;
			final int availableGoods = colonyWarehouse.getGoodAmmount(consumedGoods);
			final int willConsumeGoods = Math.min(maxCanConsumeGoods, availableGoods);
			final int willProduceGoods = (int) (willConsumeGoods * productionPerTurn / (float) consumptionPerTurn);
			return new ConstructionProduction(willConsumeGoods, baseProductionPerTurn, willProduceGoods, consumedGoods,
					producedGoods);
		}
	}
	
	/**
	 * Really add produced good to warehouse and remove consumed.
	 *
	 * @param colonyWarehouse
	 *            required colony warehouse
	 */
	public void consume(final ColonyWarehouse colonyWarehouse) {
		if (consumedGoods != null) {
			colonyWarehouse.removeFromWarehouse(consumedGoods, consumptionPerTurn);
		}
		colonyWarehouse.addToWarehouse(producedGoods, getRealProductionPerTurn());
	}
	
	public int getConsumptionPerTurn() {
		return consumptionPerTurn;
	}

	public int getProductionPerTurn() {
		return productionPerTurn;
	}

	/**
	 * Return real produced number of goods.
	 *
	 * @return real produced good
	 */
	public int getRealProductionPerTurn() {
		return productionPerTurn + baseProductionPerTurn;
	}

	public GoodType getConsumedGoods() {
		return consumedGoods;
	}

	public GoodType getProducedGoods() {
		return producedGoods;
	}

	public int getBaseProductionPerTurn() {
		return baseProductionPerTurn;
	}
	
}
