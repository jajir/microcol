package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Builder for Europe object.
 */
public class EuropeBuilder {
	//TODO class could be removed
	private final ModelBuilder modelBuilder;

	private final List<Unit> unitsInEuropePort = Lists.newArrayList();

	EuropeBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
	}

	public ModelBuilder build() {
		return modelBuilder;
	}

	//TODO use same patern like for unitBuilder.
	public EuropeBuilder addShipToPort(final Unit ship) {
		Preconditions.checkArgument(UnitType.isShip(ship.getType()),"In Europe port could be just ship.");
		modelBuilder.addUnit(ship);
		unitsInEuropePort.add(ship);

		return this;
	}

	List<Unit> getUnitsInEuropePort() {
		return unitsInEuropePort;
	}

}
