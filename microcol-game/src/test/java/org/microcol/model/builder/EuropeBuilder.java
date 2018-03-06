package org.microcol.model.builder;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/**
 * Builder for Europe object.
 */
public class EuropeBuilder {

	private final ModelBuilder modelBuilder;

	private final ModelPo modelPo;

	EuropeBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		this.modelPo = Preconditions.checkNotNull(modelBuilder.getModelPo());
	}

	public ModelBuilder build() {
		return modelBuilder;
	}

	public EuropeBuilder addShipToPort(final UnitPo ship) {
		Preconditions.checkArgument(ship.getType().isShip(), "In Europe port could be just ship.");
		ship.setPlaceEuropePort(new PlaceEuropePortPo());
		modelPo.addUnit(ship);
		return this;
	}

}