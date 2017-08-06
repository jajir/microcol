package org.microcol.model;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class EuropeBuilder {

	private final ModelBuilder modelBuilder;

	private final List<Unit> shipsInPort = Lists.newArrayList();

	EuropeBuilder(final ModelBuilder modelBuilder) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
	}

	public ModelBuilder build() {
		Europe europe = new Europe(shipsInPort);
		// FIXME set ships to places
		modelBuilder.setEurope(europe);
		return modelBuilder;
	}

	public EuropeBuilder addShipToPort(final Unit ship) {
		Preconditions.checkArgument(UnitType.isShip(ship.getType()));
		shipsInPort.add(ship);
		modelBuilder.addUnit(ship);

		return this;
	}

}
