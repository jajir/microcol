package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Port is a place where units can load and unload cargo.
 */
public class Port {

	private final List<PortUnit> portUnits = Lists.newArrayList();

	Port(final List<Unit> shipsInPort) {
		shipsInPort.forEach(unit -> {
			Preconditions.checkArgument(UnitType.isShip(unit.getType()), "it's not correct unit type");
			portUnits.add(new PortUnit(unit, this));
		});
	}

	public List<Unit> getShipsInPort() {
		return portUnits.stream().map(portUnit -> portUnit.getUnit()).collect(Collectors.toList());
	}

}
