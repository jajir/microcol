package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

public class UnitShip extends Unit implements CargoHolder {

    public UnitShip(Function<Unit, Cargo> cargoBuilder, Model model, Integer id,
	    Function<Unit, Place> placeBuilder, UnitType unitType, Player owner, int availableMoves,
	    final UnitAction unitAction) {
	super(cargoBuilder, model, id, placeBuilder, unitType, owner, availableMoves, unitAction);
	this.cargo = cargoBuilder.apply(this);
    }

    private final Cargo cargo;

    @Override
    public Cargo getCargo() {
	return cargo;
    }

}
