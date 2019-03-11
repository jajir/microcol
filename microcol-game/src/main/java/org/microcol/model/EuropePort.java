package org.microcol.model;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * Port is a place where units can load and unload cargo.
 */
public final class EuropePort {

    private final Model model;

    EuropePort(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    public List<UnitWithCargo> getShipsInPort(final Player player) {
        return model.getAllUnits().stream()
                .filter(unit -> unit.isAtEuropePort())
                .filter(unit -> unit.getOwner().equals(player))
                .filter(unit -> ((PlaceEuropePort) unit.getPlace()).getEuropePort().equals(this))
                .map(unit -> (UnitWithCargo) unit)
                .collect(Collectors.toList());
    }

    public void placeShipToPort(final Unit unit) {
        Preconditions.checkArgument(unit.getType().isShip(), "it's not correct unit type");
        unit.placeToEuropePort(this);
    }

}
