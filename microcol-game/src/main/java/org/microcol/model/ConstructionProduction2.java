package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Per turn construction production. Object doesn't reflect real availability of
 * consumed goods.
 * 
 * FIXME it's duplicate class
 */
public class ConstructionProduction2 {

	private final ConstructionType constructionType;

	private final int consumptionPerTurn;

	private final int productionPerTurn;

	ConstructionProduction2(final ConstructionType constructionType, final int consumptionPerTurn,
			final int productionPerTurn) {
		this.consumptionPerTurn = consumptionPerTurn;
		this.productionPerTurn = productionPerTurn;
		this.constructionType = Preconditions.checkNotNull(constructionType);
		if (!constructionType.getConsumed().isPresent()) {
			Preconditions.checkArgument(consumptionPerTurn == 0,
					"When consumed goods is null than consumption per turn should be 0, but it's (%s).",
					productionPerTurn);
		}
		if (!constructionType.getProduce().isPresent()) {
			Preconditions.checkArgument(consumptionPerTurn == 0,
					"When produced goods is null than production per turn should be 0, but it's (%s).",
					productionPerTurn);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ConstructionProduction2.class).add("consumptionPerTurn", consumptionPerTurn)
				.add("productionPerTurn", productionPerTurn).toString();
	}

	public int getConsumptionPerTurn() {
		return consumptionPerTurn;
	}

	public int getProductionPerTurn() {
		return productionPerTurn;
	}

	/**
	 * @return the constructionType
	 */
	public ConstructionType getConstructionType() {
		return constructionType;
	}

}
